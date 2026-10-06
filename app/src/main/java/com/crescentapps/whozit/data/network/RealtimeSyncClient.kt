package com.crescentapps.whozit.data.network

import com.crescentapps.whozit.core.model.MemberPresence
import com.crescentapps.whozit.core.model.SyncEvent
import kotlinx.coroutines.flow.Flow

interface RealtimeSyncClient {
    val connectionState: Flow<Boolean>
    val incomingEvents: Flow<SyncEvent>

    suspend fun connect(roomId: String, memberId: String, displayName: String)
    suspend fun disconnect()
    suspend fun sendEvent(event: SyncEvent): Boolean
    suspend fun updatePresence(presence: MemberPresence)
}
