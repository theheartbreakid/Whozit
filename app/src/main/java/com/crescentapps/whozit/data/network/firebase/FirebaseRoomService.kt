package com.crescentapps.whozit.data.network.firebase

import android.util.Log
import com.crescentapps.whozit.core.model.RoomMember
import com.crescentapps.whozit.core.model.RoomRole
import com.crescentapps.whozit.core.model.ScheduleWithParticipants
import com.crescentapps.whozit.core.model.SyncEvent
import com.crescentapps.whozit.core.util.RoomCodeGenerator
import com.crescentapps.whozit.data.network.CreateRoomRequest
import com.crescentapps.whozit.data.network.CreateRoomResponse
import com.crescentapps.whozit.data.network.JoinRoomRequest
import com.crescentapps.whozit.data.network.RoomMetadataResponse
import com.crescentapps.whozit.data.network.RoomService
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import java.util.UUID

class FirebaseRoomService(
    private val authManager: FirebaseAuthManager
) : RoomService {

    private val firestore: FirebaseFirestore
        get() = FirebaseFirestore.getInstance()

    override suspend fun createRoom(request: CreateRoomRequest): Result<CreateRoomResponse> {
        val uidResult = authManager.authenticateAnonymously()
        if (uidResult.isFailure) return Result.failure(uidResult.exceptionOrNull()!!)

        val uid = uidResult.getOrThrow()
        val roomId = UUID.randomUUID().toString()
        val roomCode = RoomCodeGenerator.generateCode()
        val inviteToken = UUID.randomUUID().toString().replace("-", "").take(16)
        val createdAt = System.currentTimeMillis()

        val roomData = mapOf(
            "roomId" to roomId,
            "roomCode" to roomCode,
            "name" to request.name,
            "description" to request.description,
            "ownerMemberId" to uid, // enforce UID
            "ownerDisplayName" to request.ownerDisplayName,
            "createdAt" to createdAt
        )

        val ownerMemberData = mapOf(
            "id" to 1L,
            "roomId" to 0L, // local id handled by repo
            "remoteMemberId" to uid,
            "displayName" to request.ownerDisplayName,
            "role" to RoomRole.OWNER.name,
            "presence" to "ONLINE",
            "joinedAt" to createdAt
        )

        val inviteData = mapOf(
            "roomId" to roomId,
            "roomName" to request.name,
            "ownerUid" to uid,
            "ownerDisplayName" to request.ownerDisplayName,
            "status" to "ACTIVE"
        )

        return try {
            val batch = firestore.batch()

            val roomRef = firestore.collection("rooms").document(roomId)
            batch.set(roomRef, roomData)

            val memberRef = roomRef.collection("members").document(uid)
            batch.set(memberRef, ownerMemberData)

            // Optional schedules
            request.initialSchedules.forEach { s ->
                val remoteScheduleId = "rem_sched_${s.schedule.id}"
                val scheduleRef = roomRef.collection("schedules").document(remoteScheduleId)
                // For simplicity, serialize objects. In real prod, custom encoding or Gson conversion.
                // We'll trust the native field mappings for now, but a dedicated Mapper is better.
                batch.set(scheduleRef, mapOf(
                    "schedule" to s.schedule,
                    "participants" to s.participants,
                    "pattern" to s.pattern
                ))
            }

            val inviteRef = firestore.collection("roomInvites").document(roomCode)
            batch.set(inviteRef, inviteData)

            batch.commit().await()

            Result.success(
                CreateRoomResponse(
                    roomId = roomId,
                    roomCode = roomCode,
                    inviteToken = inviteToken,
                    createdAt = createdAt
                )
            )
        } catch (e: Exception) {
            Log.e("FirebaseRoomService", "Failed to create room", e)
            Result.failure(e)
        }
    }

    override suspend fun previewRoom(roomCode: String): Result<RoomMetadataResponse> {
        val uidResult = authManager.authenticateAnonymously()
        if (uidResult.isFailure) return Result.failure(uidResult.exceptionOrNull()!!)
        val normalizedCode = RoomCodeGenerator.normalizeCode(roomCode)
        val uid = uidResult.getOrThrow()

        return try {
            val inviteDoc = firestore.collection("roomInvites").document(normalizedCode).get().await()
            if (!inviteDoc.exists()) {
                return Result.failure(IllegalArgumentException("Room code '$normalizedCode' not found."))
            }

            val roomId = inviteDoc.getString("roomId") ?: return Result.failure(IllegalStateException("Invalid invite data"))

            // Check if user is already a member
            val memberDoc = firestore.collection("rooms").document(roomId).collection("members").document(uid).get().await()
            val isMember = memberDoc.exists()
            val isOwner = inviteDoc.getString("ownerUid") == uid

            if (isMember || isOwner) {
                // Fetch full private data
                val roomDoc = firestore.collection("rooms").document(roomId).get().await()
                if (!roomDoc.exists()) return Result.failure(IllegalArgumentException("Room no longer exists."))
                
                val membersSnapshot = firestore.collection("rooms").document(roomId).collection("members").get().await()
                val members = membersSnapshot.documents.mapNotNull { it.toObject(RoomMember::class.java) }

                val schedulesSnapshot = firestore.collection("rooms").document(roomId).collection("schedules").get().await()
                val sharedSchedules = schedulesSnapshot.documents.mapNotNull { doc ->
                    try {
                        doc.toObject(ScheduleWithParticipants::class.java)
                    } catch (e: Exception) {
                        null
                    }
                }

                val meta = RoomMetadataResponse(
                    roomId = roomId,
                    roomCode = normalizedCode,
                    name = roomDoc.getString("name") ?: "",
                    description = roomDoc.getString("description") ?: "",
                    ownerMemberId = roomDoc.getString("ownerMemberId") ?: "",
                    ownerDisplayName = roomDoc.getString("ownerDisplayName") ?: "",
                    members = members,
                    sharedSchedules = sharedSchedules,
                    recentOccurrences = emptyList()
                )
                Result.success(meta)
            } else {
                // Return safe preview data only
                val meta = RoomMetadataResponse(
                    roomId = roomId,
                    roomCode = normalizedCode,
                    name = inviteDoc.getString("roomName") ?: "Unknown Room",
                    description = "",
                    ownerMemberId = inviteDoc.getString("ownerUid") ?: "",
                    ownerDisplayName = inviteDoc.getString("ownerDisplayName") ?: "Owner",
                    members = emptyList(), // Hidden in preview
                    sharedSchedules = emptyList(), // Hidden in preview
                    recentOccurrences = emptyList()
                )
                Result.success(meta)
            }
        } catch (e: Exception) {
            Log.e("FirebaseRoomService", "Failed to preview room", e)
            Result.failure(e)
        }
    }

    override suspend fun joinRoom(request: JoinRoomRequest): Result<RoomMetadataResponse> {
        val uidResult = authManager.authenticateAnonymously()
        if (uidResult.isFailure) return Result.failure(uidResult.exceptionOrNull()!!)
        val uid = uidResult.getOrThrow()
        
        val previewResult = previewRoom(request.roomCode)
        if (previewResult.isFailure) return previewResult

        val meta = previewResult.getOrThrow()
        
        val newMemberData = mapOf(
            "id" to System.currentTimeMillis(),
            "roomId" to 0L,
            "remoteMemberId" to uid,
            "displayName" to request.memberDisplayName,
            "role" to RoomRole.MEMBER.name,
            "presence" to "ONLINE",
            "joinedAt" to System.currentTimeMillis()
        )

        return try {
            firestore.collection("rooms").document(meta.roomId)
                .collection("members").document(uid)
                .set(newMemberData, SetOptions.merge())
                .await()

            // Fetch updated meta
            previewRoom(request.roomCode)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun leaveRoom(roomId: String, memberId: String): Result<Unit> {
        if (authManager.currentUid == null) return Result.success(Unit)
        return try {
            firestore.collection("rooms").document(roomId)
                .collection("members").document(authManager.currentUid!!)
                .delete()
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteRoom(roomId: String, ownerMemberId: String): Result<Unit> {
        if (authManager.currentUid != ownerMemberId) return Result.failure(IllegalStateException("Only owner can delete."))
        return try {
            firestore.collection("rooms").document(roomId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun uploadSchedule(
        roomId: String,
        schedule: ScheduleWithParticipants,
        shareHistory: Boolean
    ): Result<String> {
        if (authManager.currentUid == null) return Result.failure(IllegalStateException("Not authenticated"))
        
        val remoteScheduleId = "rem_sched_${schedule.schedule.id}"
        val scheduleRef = firestore.collection("rooms").document(roomId).collection("schedules").document(remoteScheduleId)
        
        return try {
            scheduleRef.set(mapOf(
                "schedule" to schedule.schedule,
                "participants" to schedule.participants,
                "pattern" to schedule.pattern
            )).await()
            Result.success(remoteScheduleId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun stopSharingSchedule(roomId: String, remoteScheduleId: String): Result<Unit> {
        return try {
            firestore.collection("rooms").document(roomId).collection("schedules").document(remoteScheduleId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun pushSyncOperations(roomId: String, operations: List<SyncEvent>): Result<List<String>> {
        val uids = mutableListOf<String>()
        return try {
            val batch = firestore.batch()
            operations.forEach { op ->
                val ref = firestore.collection("rooms").document(roomId).collection("events").document(op.eventId)
                batch.set(ref, op)
                uids.add(op.eventId)
            }
            // Use timeout to prevent indefinite suspension when offline, so the local queue can properly mark as PENDING/OFFLINE
            kotlinx.coroutines.withTimeout(5000) {
                batch.commit().await()
            }
            Result.success(uids)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun fetchLatestEvents(roomId: String, sinceTimestamp: Long): Result<List<SyncEvent>> {
        return try {
            val snap = firestore.collection("rooms").document(roomId).collection("events")
                .whereGreaterThan("timestamp", sinceTimestamp)
                .get()
                .await()
            
            val events = snap.documents.mapNotNull { it.toObject(SyncEvent::class.java) }
            Result.success(events)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
