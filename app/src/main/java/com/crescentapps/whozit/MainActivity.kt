package com.crescentapps.whozit

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
import com.crescentapps.whozit.presentation.navigation.WhozitLiquidNavigationRail
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

class MainActivity : ComponentActivity() {

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* Permission result handled */ }

    private val incomingDeepLinkCode = kotlinx.coroutines.flow.MutableStateFlow<String?>(null)

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
            val prefs by app.userPreferencesRepository.userPreferencesFlow.collectAsState(
                initial = com.crescentapps.whozit.data.preferences.UserPreferences()
            )
            val deepLinkCode by incomingDeepLinkCode.collectAsState()

            com.crescentapps.whozit.presentation.WhozitApp(
                app = app,
                prefs = prefs,
                deepLinkCode = deepLinkCode
            )
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIncomingIntent(intent)
    }

    private fun handleIncomingIntent(intent: android.content.Intent?) {
        val uri = intent?.data ?: return
        val rawUriString = uri.toString()
        val extractedCode = com.crescentapps.whozit.core.util.QRCodeUtils.extractRoomCode(rawUriString)
        if (!extractedCode.isNullOrBlank()) {
            incomingDeepLinkCode.value = extractedCode
        }
    }
}
