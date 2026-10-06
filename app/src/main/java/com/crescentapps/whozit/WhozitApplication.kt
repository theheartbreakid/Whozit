package com.crescentapps.whozit

import android.app.Application
import com.crescentapps.whozit.core.notification.NotificationHelper
import com.crescentapps.whozit.data.local.WhozitDatabase
import com.crescentapps.whozit.data.preferences.UserPreferencesRepository
import com.crescentapps.whozit.data.repository.WhozitRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

import com.crescentapps.whozit.data.network.RealtimeSyncClient
import com.crescentapps.whozit.data.network.RoomService
import com.crescentapps.whozit.data.network.impl.MockRealtimeSyncClient
import com.crescentapps.whozit.data.network.impl.MockRoomService
import com.crescentapps.whozit.data.repository.RoomRepository
import com.crescentapps.whozit.data.sync.SyncWorker
import kotlinx.coroutines.flow.first

class WhozitApplication : Application() {
    val database: WhozitDatabase by lazy { WhozitDatabase.getInstance(this) }
    val repository: WhozitRepository by lazy { WhozitRepository(database) }
    val userPreferencesRepository: UserPreferencesRepository by lazy { UserPreferencesRepository(this) }
    val hapticManager: com.crescentapps.whozit.core.util.HapticManager by lazy { com.crescentapps.whozit.core.util.HapticManager(this) }
    val soundManager: com.crescentapps.whozit.core.util.SoundManager by lazy { com.crescentapps.whozit.core.util.SoundManager(this) }
    val updateManager: com.crescentapps.whozit.core.update.UpdateManager by lazy { com.crescentapps.whozit.core.update.UpdateManager.getInstance(this) }

    val roomService: RoomService by lazy { MockRoomService() }
    val realtimeSyncClient: RealtimeSyncClient by lazy { MockRealtimeSyncClient() }
    val roomRepository: RoomRepository by lazy {
        RoomRepository(
            db = database,
            whozitRepository = repository,
            roomService = roomService,
            realtimeClient = realtimeSyncClient,
            preferencesRepository = userPreferencesRepository
        )
    }

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createNotificationChannels(this)

        // Run automatic history reconciliation on launch
        applicationScope.launch {
            repository.reconcileMissedHistory()
            val prefs = userPreferencesRepository.userPreferencesFlow.first()
            if (prefs.onlineSyncEnabled) {
                SyncWorker.schedulePeriodicSync(this@WhozitApplication, prefs.syncOverMobileData)
            }
            com.crescentapps.whozit.core.update.UpdateCheckWorker.schedule(
                this@WhozitApplication,
                prefs.updateCheckFrequency
            )
        }
    }
}
