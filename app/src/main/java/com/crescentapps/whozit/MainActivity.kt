package com.crescentapps.whozit

import androidx.lifecycle.compose.collectAsStateWithLifecycle

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import com.crescentapps.whozit.presentation.components.Text
import com.crescentapps.whozit.presentation.components.Icon
import com.crescentapps.whozit.presentation.theme.LocalWhozitColors
import androidx.compose.ui.text.TextStyle

import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.crescentapps.whozit.core.model.Schedule
import com.crescentapps.whozit.core.model.ThemeMode
import com.crescentapps.whozit.core.notification.DailyNotificationScheduler
import com.crescentapps.whozit.presentation.catalog.utils.LocalBackdrop
import com.crescentapps.whozit.presentation.components.*
import com.crescentapps.whozit.presentation.navigation.Screen
import com.crescentapps.whozit.presentation.navigation.WhozitLiquidDock
import com.crescentapps.whozit.presentation.screens.calendar.CalendarScreen
import com.crescentapps.whozit.presentation.screens.calendar.CalendarViewModel
import com.crescentapps.whozit.presentation.screens.history.HistoryScreen
import com.crescentapps.whozit.presentation.screens.history.HistoryViewModel
import com.crescentapps.whozit.presentation.screens.home.HomeScreen
import com.crescentapps.whozit.presentation.screens.home.HomeViewModel
import com.crescentapps.whozit.presentation.screens.room.*
import com.crescentapps.whozit.presentation.screens.schedule.*
import com.crescentapps.whozit.presentation.screens.settings.SettingsScreen
import com.crescentapps.whozit.presentation.screens.settings.SettingsViewModel
import com.crescentapps.whozit.presentation.theme.WhozitAppTheme
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull

class MainActivity : ComponentActivity() {

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* Permission result handled */ }

    private val _incomingDeepLinkCode = kotlinx.coroutines.flow.MutableStateFlow<String?>(null)
    private val incomingDeepLinkCode = _incomingDeepLinkCode.asStateFlow()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as WhozitApplication

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        DailyNotificationScheduler.scheduleDailyAlarm(this)

        handleIncomingIntent(intent)

        setContent {
            val basePrefs by app.userPreferencesRepository.userPreferencesFlow.collectAsStateWithLifecycle(
                initialValue = com.crescentapps.whozit.data.preferences.UserPreferences()
            )
            val deepLinkCode by incomingDeepLinkCode.collectAsStateWithLifecycle()

            val benchmarkOverrideMode = remember(intent) {
                getValidatedBenchmarkUiSystemMode(intent)
            }
            val effectivePrefs = remember(basePrefs, benchmarkOverrideMode) {
                if (benchmarkOverrideMode != null) {
                    basePrefs.copy(uiSystemMode = benchmarkOverrideMode)
                } else {
                    basePrefs
                }
            }

            val shouldSeedDemoData = remember(intent) {
                intent?.getBooleanExtra("seed_demo_data", false) == true || benchmarkOverrideMode != null
            }

            LaunchedEffect(shouldSeedDemoData) {
                if (shouldSeedDemoData) {
                    val currentSchedules = app.repository.allSchedules.firstOrNull()
                    if (currentSchedules.isNullOrEmpty()) {
                        seedBenchmarkDemoData(app.repository)
                    }
                }
            }

            com.crescentapps.whozit.presentation.WhozitApp(
                app = app,
                prefs = effectivePrefs,
                deepLinkCode = deepLinkCode
            )
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIncomingIntent(intent)
    }

    private fun getValidatedBenchmarkUiSystemMode(intent: android.content.Intent?): com.crescentapps.whozit.core.model.UiSystemMode? {
        val rawMode = intent?.getStringExtra("benchmark_ui_system_mode")
            ?: intent?.getStringExtra("ui_system_mode")
            ?: return null
        return when (rawMode.uppercase()) {
            "LIQUID", "LIQUID_UI" -> com.crescentapps.whozit.core.model.UiSystemMode.LIQUID
            "GAUSSIAN_BLUR", "GAUSSIAN", "GAUSSIAN_BLUR_UI" -> com.crescentapps.whozit.core.model.UiSystemMode.GAUSSIAN_BLUR
            "MATERIAL_3_EXPRESSIVE", "MATERIAL_3", "EXPRESSIVE" -> com.crescentapps.whozit.core.model.UiSystemMode.MATERIAL_3_EXPRESSIVE
            else -> null
        }
    }

    private suspend fun seedBenchmarkDemoData(repository: com.crescentapps.whozit.data.repository.WhozitRepository) {
        val p1Id = repository.saveParticipant(com.crescentapps.whozit.core.model.Participant(name = "Mohsin", colorHex = "#3B82F6"))
        val p2Id = repository.saveParticipant(com.crescentapps.whozit.core.model.Participant(name = "Mom", colorHex = "#EC4899"))
        val p3Id = repository.saveParticipant(com.crescentapps.whozit.core.model.Participant(name = "Dad", colorHex = "#10B981"))

        val s1 = com.crescentapps.whozit.core.model.Schedule(
            name = "Family Savings",
            description = "Daily contribution rotation",
            type = com.crescentapps.whozit.core.model.ScheduleType.MONEY,
            currencyCode = "INR",
            defaultAmount = 500.0,
            frequencyType = com.crescentapps.whozit.core.model.FrequencyType.DAILY,
            frequencyInterval = 1,
            startDate = com.crescentapps.whozit.core.util.DateUtils.today().minusDays(3).let { com.crescentapps.whozit.core.util.DateUtils.format(it) }
        )
        repository.saveScheduleWithDetails(s1, listOf(p1Id, p2Id, p3Id), listOf(p1Id, p2Id, p3Id))

        val s2 = com.crescentapps.whozit.core.model.Schedule(
            name = "Kitchen Cleaning",
            description = "Daily evening dishwashing & counter cleanup",
            type = com.crescentapps.whozit.core.model.ScheduleType.TASK,
            frequencyType = com.crescentapps.whozit.core.model.FrequencyType.DAILY,
            frequencyInterval = 1,
            startDate = com.crescentapps.whozit.core.util.DateUtils.today().minusDays(1).let { com.crescentapps.whozit.core.util.DateUtils.format(it) }
        )
        repository.saveScheduleWithDetails(s2, listOf(p1Id, p2Id, p3Id), listOf(p2Id, p3Id, p1Id))

        val s3 = com.crescentapps.whozit.core.model.Schedule(
            name = "Pet Duty & Feeding",
            description = "Morning and evening pet care",
            type = com.crescentapps.whozit.core.model.ScheduleType.RESPONSIBILITY,
            frequencyType = com.crescentapps.whozit.core.model.FrequencyType.DAILY,
            frequencyInterval = 1,
            startDate = com.crescentapps.whozit.core.util.DateUtils.today().minusDays(2).let { com.crescentapps.whozit.core.util.DateUtils.format(it) }
        )
        repository.saveScheduleWithDetails(s3, listOf(p1Id, p2Id, p3Id), listOf(p3Id, p1Id, p2Id))

        val s4 = com.crescentapps.whozit.core.model.Schedule(
            name = "Grocery & Restock",
            description = "Weekly household supplies",
            type = com.crescentapps.whozit.core.model.ScheduleType.GENERAL,
            frequencyType = com.crescentapps.whozit.core.model.FrequencyType.DAILY,
            frequencyInterval = 1,
            startDate = com.crescentapps.whozit.core.util.DateUtils.today().minusDays(4).let { com.crescentapps.whozit.core.util.DateUtils.format(it) }
        )
        repository.saveScheduleWithDetails(s4, listOf(p1Id, p2Id, p3Id), listOf(p1Id, p3Id, p2Id))
    }

    private fun handleIncomingIntent(intent: android.content.Intent?) {
        val uri = intent?.data ?: return
        val rawUriString = uri.toString()
        val extractedCode = com.crescentapps.whozit.core.util.QRCodeUtils.extractRoomCode(rawUriString)
        if (!extractedCode.isNullOrBlank()) {
            _incomingDeepLinkCode.value = extractedCode
        }
    }
}
