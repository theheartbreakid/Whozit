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

            val inviteRef = firestore.collection("roomInvites").document(roomCode)
            batch.set(inviteRef, inviteData)

            withTimeout(10000) {
                batch.commit().await()
            }

            // Upload initial schedules after room and owner member records exist in Firestore
            request.initialSchedules.forEach { s ->
                val remoteScheduleId = "rem_sched_${s.schedule.id}"
                val scheduleRef = roomRef.collection("schedules").document(remoteScheduleId)
                try {
                    scheduleRef.set(s.toFirestoreMap()).await()
                } catch (e: Exception) {
                    Log.e("FirebaseRoomService", "Failed to upload initial schedule ${s.schedule.id}", e)
                }
            }

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
                    doc.toScheduleWithParticipants()
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
        val uidResult = authManager.authenticateAnonymously()
        if (uidResult.isFailure) return Result.failure(uidResult.exceptionOrNull()!!)
        val uid = uidResult.getOrThrow()

        return try {
            val roomRef = firestore.collection("rooms").document(roomId)
            val roomDoc = roomRef.get().await()
            val roomCode = roomDoc.getString("roomCode")
            
            val storedOwnerUid = roomDoc.getString("ownerMemberId")
            if (uid != storedOwnerUid) {
                return Result.failure(IllegalStateException("Only owner can delete."))
            }
            
            // Note: Batch writes are limited to 500 operations.
            // We must delete subcollections first so that isRoomOwner() in firestore.rules continues to pass
            val subcollections = listOf("members", "schedules", "events")
            for (coll in subcollections) {
                var deletedSize: Int
                do {
                    val snapshot = roomRef.collection(coll).limit(400).get().await()
                    deletedSize = snapshot.size()
                    if (deletedSize > 0) {
                        val batch = firestore.batch()
                        snapshot.documents.forEach { batch.delete(it.reference) }
                        batch.commit().await()
                    }
                } while (deletedSize >= 400)
            }
            
            // Delete invite and main room document
            val batch = firestore.batch()
            if (roomCode != null) {
                batch.delete(firestore.collection("roomInvites").document(roomCode))
            }
            batch.delete(roomRef)
            batch.commit().await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("FirebaseRoomService", "Failed to delete room", e)
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
            scheduleRef.set(schedule.toFirestoreMap()).await()
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

private fun ScheduleWithParticipants.toFirestoreMap(): Map<String, Any?> {
    return mapOf(
        "schedule" to mapOf(
            "id" to schedule.id,
            "name" to schedule.name,
            "description" to schedule.description,
            "type" to schedule.type.name,
            "currencyCode" to schedule.currencyCode,
            "defaultAmount" to schedule.defaultAmount,
            "frequencyType" to schedule.frequencyType.name,
            "frequencyInterval" to schedule.frequencyInterval,
            "selectedWeekdays" to schedule.selectedWeekdays,
            "dayOfMonth" to schedule.dayOfMonth,
            "startDate" to schedule.startDate,
            "timezone" to schedule.timezone,
            "isPaused" to schedule.isPaused,
            "pauseUntil" to schedule.pauseUntil,
            "overrideStrategy" to schedule.overrideStrategy.name,
            "notifyTimeHour" to schedule.notifyTimeHour,
            "notifyTimeMinute" to schedule.notifyTimeMinute,
            "notificationsEnabled" to schedule.notificationsEnabled,
            "isArchived" to schedule.isArchived,
            "createdAt" to schedule.createdAt,
            "updatedAt" to schedule.updatedAt
        ),
        "participants" to participants.map { p ->
            mapOf(
                "id" to p.id,
                "name" to p.name,
                "nickname" to p.nickname,
                "colorHex" to p.colorHex,
                "avatarEmoji" to p.avatarEmoji,
                "isActive" to p.isActive,
                "createdAt" to p.createdAt,
                "updatedAt" to p.updatedAt
            )
        },
        "pattern" to pattern.map { r ->
            mapOf(
                "id" to r.id,
                "scheduleId" to r.scheduleId,
                "participantId" to r.participantId,
                "patternPosition" to r.patternPosition
            )
        }
    )
}

private fun com.google.firebase.firestore.DocumentSnapshot.toScheduleWithParticipants(): ScheduleWithParticipants? {
    return try {
        val schedMap = get("schedule") as? Map<*, *> ?: return null
        val id = (schedMap["id"] as? Number)?.toLong() ?: 0L
        val name = schedMap["name"] as? String ?: ""
        val description = schedMap["description"] as? String ?: ""
        val typeStr = schedMap["type"] as? String ?: "GENERAL"
        val type = runCatching { com.crescentapps.whozit.core.model.ScheduleType.valueOf(typeStr) }.getOrDefault(com.crescentapps.whozit.core.model.ScheduleType.GENERAL)
        val currencyCode = schedMap["currencyCode"] as? String ?: "INR"
        val defaultAmount = (schedMap["defaultAmount"] as? Number)?.toDouble()
        val freqTypeStr = schedMap["frequencyType"] as? String ?: "DAILY"
        val frequencyType = runCatching { com.crescentapps.whozit.core.model.FrequencyType.valueOf(freqTypeStr) }.getOrDefault(com.crescentapps.whozit.core.model.FrequencyType.DAILY)
        val frequencyInterval = (schedMap["frequencyInterval"] as? Number)?.toInt() ?: 1
        val selectedWeekdays = (schedMap["selectedWeekdays"] as? List<*>)?.mapNotNull { (it as? Number)?.toInt() } ?: emptyList()
        val dayOfMonth = (schedMap["dayOfMonth"] as? Number)?.toInt()
        val startDate = schedMap["startDate"] as? String ?: ""
        val timezone = schedMap["timezone"] as? String ?: "UTC"
        val isPaused = schedMap["isPaused"] as? Boolean ?: false
        val pauseUntil = schedMap["pauseUntil"] as? String
        val overrideStratStr = schedMap["overrideStrategy"] as? String ?: "SINGLE_DATE_ONLY"
        val overrideStrategy = runCatching { com.crescentapps.whozit.core.model.OverrideStrategy.valueOf(overrideStratStr) }.getOrDefault(com.crescentapps.whozit.core.model.OverrideStrategy.SINGLE_DATE_ONLY)
        val notifyHour = (schedMap["notifyTimeHour"] as? Number)?.toInt() ?: 9
        val notifyMinute = (schedMap["notifyTimeMinute"] as? Number)?.toInt() ?: 0
        val notificationsEnabled = schedMap["notificationsEnabled"] as? Boolean ?: true
        val isArchived = schedMap["isArchived"] as? Boolean ?: false
        val createdAt = (schedMap["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
        val updatedAt = (schedMap["updatedAt"] as? Number)?.toLong() ?: System.currentTimeMillis()

        val sched = com.crescentapps.whozit.core.model.Schedule(
            id = id,
            name = name,
            description = description,
            type = type,
            currencyCode = currencyCode,
            defaultAmount = defaultAmount,
            frequencyType = frequencyType,
            frequencyInterval = frequencyInterval,
            selectedWeekdays = selectedWeekdays,
            dayOfMonth = dayOfMonth,
            startDate = startDate,
            timezone = timezone,
            isPaused = isPaused,
            pauseUntil = pauseUntil,
            overrideStrategy = overrideStrategy,
            notifyTimeHour = notifyHour,
            notifyTimeMinute = notifyMinute,
            notificationsEnabled = notificationsEnabled,
            isArchived = isArchived,
            createdAt = createdAt,
            updatedAt = updatedAt
        )

        val partsList = get("participants") as? List<Map<*, *>> ?: emptyList()
        val participants = partsList.mapNotNull { pMap ->
            val pId = (pMap["id"] as? Number)?.toLong() ?: 0L
            val pName = pMap["name"] as? String ?: ""
            val nickname = pMap["nickname"] as? String
            val colorHex = pMap["colorHex"] as? String ?: "#3B82F6"
            val avatarEmoji = pMap["avatarEmoji"] as? String
            val isActive = pMap["isActive"] as? Boolean ?: true
            val pCreatedAt = (pMap["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
            val pUpdatedAt = (pMap["updatedAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
            com.crescentapps.whozit.core.model.Participant(
                id = pId,
                name = pName,
                nickname = nickname,
                colorHex = colorHex,
                avatarEmoji = avatarEmoji,
                isActive = isActive,
                createdAt = pCreatedAt,
                updatedAt = pUpdatedAt
            )
        }

        val patternList = get("pattern") as? List<Map<*, *>> ?: emptyList()
        val pattern = patternList.mapNotNull { rMap ->
            val rId = (rMap["id"] as? Number)?.toLong() ?: 0L
            val sId = (rMap["scheduleId"] as? Number)?.toLong() ?: 0L
            val pId = (rMap["participantId"] as? Number)?.toLong() ?: 0L
            val patternPos = (rMap["patternPosition"] as? Number)?.toInt() ?: 0
            com.crescentapps.whozit.core.model.RotationPatternItem(
                id = rId,
                scheduleId = sId,
                participantId = pId,
                patternPosition = patternPos
            )
        }

        ScheduleWithParticipants(sched, participants, pattern)
    } catch (e: Exception) {
        Log.e("FirebaseRoomService", "Failed to deserialize ScheduleWithParticipants", e)
        null
    }
}
