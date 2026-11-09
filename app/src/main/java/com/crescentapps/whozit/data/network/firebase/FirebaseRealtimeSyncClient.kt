package com.crescentapps.whozit.data.network.firebase

import android.util.Log
import com.crescentapps.whozit.core.model.MemberPresence
import com.crescentapps.whozit.core.model.SyncEvent
import com.crescentapps.whozit.data.network.RealtimeSyncClient
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await

class FirebaseRealtimeSyncClient(
    private val authManager: FirebaseAuthManager
) : RealtimeSyncClient {

    private val firestore: FirebaseFirestore
        get() = FirebaseFirestore.getInstance()

    private val _connectionState = MutableStateFlow(false)
    override val connectionState: Flow<Boolean> = _connectionState.asStateFlow()

    private val _incomingEvents = MutableSharedFlow<SyncEvent>(extraBufferCapacity = 64)
    override val incomingEvents: Flow<SyncEvent> = _incomingEvents.asSharedFlow()

    private var activeRoomId: String? = null
    private var currentMemberId: String? = null
    private var eventsListener: ListenerRegistration? = null
    private var membersListener: ListenerRegistration? = null
    private var schedulesListener: ListenerRegistration? = null
    private var lastEventTimestamp: Long = 0L

    override suspend fun connect(roomId: String, memberId: String, displayName: String) {
        val uidResult = authManager.authenticateAnonymously()
        if (uidResult.isFailure) {
            Log.e("FirebaseRealtimeClient", "Cannot connect, Firebase Auth failed", uidResult.exceptionOrNull())
            return
        }
        
        disconnect() // Cleanup previous connection if any
        activeRoomId = roomId
        currentMemberId = memberId
        lastEventTimestamp = System.currentTimeMillis()

        setupListeners(roomId)
        _connectionState.value = true
    }

    private fun setupListeners(roomId: String) {
        // Listen to explicit operations in the events collection (for Occurrence completion, etc.)
        eventsListener = firestore.collection("rooms").document(roomId).collection("events")
            .whereGreaterThan("timestamp", lastEventTimestamp)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("FirebaseRealtimeClient", "Events listener error", error)
                    return@addSnapshotListener
                }

                snapshot?.documentChanges?.forEach { docChange ->
                    if (docChange.type == com.google.firebase.firestore.DocumentChange.Type.ADDED) {
                        val event = docChange.document.toObject(SyncEvent::class.java)
                        // Ignore events sent by ourselves since they are already persisted locally
                        if (event.senderMemberId != currentMemberId) {
                            _incomingEvents.tryEmit(event)
                        }
                    }
                }
            }

        // Snapshot listener on Members to handle changes triggered outside explicitly pushed SyncEvents
        membersListener = firestore.collection("rooms").document(roomId).collection("members")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("FirebaseRealtimeClient", "Members listener error", error)
                    return@addSnapshotListener
                }
                
                snapshot?.documentChanges?.forEach { change ->
                    val memberId = change.document.getString("remoteMemberId") ?: return@forEach
                    val displayName = change.document.getString("displayName") ?: "Unknown"

                    if (memberId != currentMemberId) {
                        when (change.type) {
                            com.google.firebase.firestore.DocumentChange.Type.ADDED -> {
                                _incomingEvents.tryEmit(
                                    SyncEvent(
                                        eventId = "mem_add_${memberId}_${System.currentTimeMillis()}",
                                        roomId = roomId,
                                        type = com.crescentapps.whozit.core.model.SyncEventType.MEMBER_JOINED,
                                        senderDeviceId = "device_$memberId",
                                        senderMemberId = memberId,
                                        senderDisplayName = displayName,
                                        entityId = memberId
                                    )
                                )
                            }
                            com.google.firebase.firestore.DocumentChange.Type.REMOVED -> {
                                _incomingEvents.tryEmit(
                                    SyncEvent(
                                        eventId = "mem_rm_${memberId}_${System.currentTimeMillis()}",
                                        roomId = roomId,
                                        type = com.crescentapps.whozit.core.model.SyncEventType.MEMBER_LEFT,
                                        senderDeviceId = "device_$memberId",
                                        senderMemberId = memberId,
                                        senderDisplayName = displayName,
                                        entityId = memberId
                                    )
                                )
                            }
                            else -> {}
                        }
                    }
                }
            }

        // Snapshot listener on Schedules to broadcast when a schedule is shared or modified
        schedulesListener = firestore.collection("rooms").document(roomId).collection("schedules")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("FirebaseRealtimeClient", "Schedules listener error", error)
                    return@addSnapshotListener
                }

                snapshot?.documentChanges?.forEach { change ->
                    // Emit a simple SCHEDULE_UPDATED event to trigger refetch in RoomRepository
                    if (change.type == com.google.firebase.firestore.DocumentChange.Type.ADDED || 
                        change.type == com.google.firebase.firestore.DocumentChange.Type.MODIFIED) {
                        
                        val remoteScheduleId = change.document.id
                        _incomingEvents.tryEmit(
                            SyncEvent(
                                eventId = "sched_upd_${remoteScheduleId}_${System.currentTimeMillis()}",
                                roomId = roomId,
                                type = com.crescentapps.whozit.core.model.SyncEventType.SCHEDULE_UPDATED,
                                senderDeviceId = "firebase",
                                senderMemberId = "firebase",
                                senderDisplayName = "Firebase",
                                entityId = remoteScheduleId
                            )
                        )
                    }
                }
            }
    }

    override suspend fun disconnect() {
        eventsListener?.remove()
        eventsListener = null
        membersListener?.remove()
        membersListener = null
        schedulesListener?.remove()
        schedulesListener = null
        activeRoomId = null
        currentMemberId = null
        _connectionState.value = false
    }

    override suspend fun sendEvent(event: SyncEvent): Boolean {
        val roomId = activeRoomId ?: return false
        return try {
            firestore.collection("rooms").document(roomId).collection("events")
                .document(event.eventId).set(event).await()
            true
        } catch (e: Exception) {
            Log.e("FirebaseRealtimeClient", "Failed to send event", e)
            false
        }
    }

    override suspend fun updatePresence(presence: MemberPresence) {
        val roomId = activeRoomId ?: return
        val uid = currentMemberId ?: return
        try {
            firestore.collection("rooms").document(roomId).collection("members")
                .document(uid).update("presence", presence.name).await()
        } catch (e: Exception) {
            Log.e("FirebaseRealtimeClient", "Failed to update presence", e)
        }
    }
}
