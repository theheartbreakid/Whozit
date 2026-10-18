package com.crescentapps.whozit.presentation.navigation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Loop
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Loop
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Today
import androidx.compose.foundation.Image
import androidx.compose.foundation.text.BasicText
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.crescentapps.whozit.presentation.components.AppWindowWidthSizeClass
import com.crescentapps.whozit.presentation.components.AppWindowHeightSizeClass
import com.crescentapps.whozit.presentation.components.LocalAppWindowSizeDetails
import com.crescentapps.whozit.presentation.components.LocalPrismalAdaptiveColor
import com.crescentapps.whozit.presentation.components.liquid.LiquidBottomTab
import com.crescentapps.whozit.presentation.components.liquid.LiquidBottomTabs
import com.crescentapps.whozit.presentation.theme.LocalWhozitColors
import com.kyant.backdrop.Backdrop

sealed class Screen(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    object Home : Screen("home", "Today", Icons.Filled.Today, Icons.Outlined.Today)
    object Calendar : Screen("calendar", "Calendar", Icons.Filled.CalendarMonth, Icons.Outlined.CalendarMonth)
    object Schedules : Screen("schedules", "Schedules", Icons.Filled.Loop, Icons.Outlined.Loop)
    object Rooms : Screen("rooms", "Shared", Icons.Filled.Share, Icons.Outlined.Share)
    object History : Screen("history", "History", Icons.Filled.History, Icons.Outlined.History)
    object Settings : Screen("settings", "Settings", Icons.Filled.Settings, Icons.Outlined.Settings)

    // Sub-screens
    object CreateSchedule : Screen("create_schedule", "New Schedule", Icons.Filled.Add, Icons.Outlined.Add)
    object ScheduleDetail : Screen("schedule_detail/{scheduleId}", "Schedule Details", Icons.Filled.Info, Icons.Outlined.Info) {
        fun createRoute(scheduleId: Long) = "schedule_detail/$scheduleId"
    }

    object CreateRoom : Screen("create_room", "New Room", Icons.Filled.Add, Icons.Outlined.Add)
    object JoinRoom : Screen("join_room?code={code}", "Join Room", Icons.Filled.QrCodeScanner, Icons.Outlined.QrCodeScanner) {
        fun createRoute(code: String? = null) = if (code.isNullOrBlank()) "join_room" else "join_room?code=$code"
    }
    object RoomDetail : Screen("room_detail/{roomId}", "Room Details", Icons.Filled.Info, Icons.Outlined.Info) {
        fun createRoute(roomId: Long) = "room_detail/$roomId"
    }
    object RoomSettings : Screen("room_settings/{roomId}", "Room Settings", Icons.Filled.Settings, Icons.Outlined.Settings) {
        fun createRoute(roomId: Long) = "room_settings/$roomId"
    }
}

val mainScreens = listOf(
    Screen.Home,
    Screen.Calendar,
    Screen.Schedules,
    Screen.Rooms,
    Screen.History,
    Screen.Settings
)

/**
 * DhikrCounter Liquid Glass Dock / Bottom Tabs Navigation
 */
@Composable
fun WhozitLiquidDock(
    currentRoute: String?,
    onNavigate: (Screen) -> Unit,
    backdrop: Backdrop,
    modifier: Modifier = Modifier
) {
    val selectedIndex = mainScreens.indexOfFirst { it.route == currentRoute }.coerceAtLeast(0)
    val sizeDetails = LocalAppWindowSizeDetails.current
    val isCompact = sizeDetails.widthClass == AppWindowWidthSizeClass.COMPACT ||
                    sizeDetails.heightClass == AppWindowHeightSizeClass.COMPACT ||
                    sizeDetails.isLandscape ||
                    sizeDetails.heightDp < 500

    LiquidBottomTabs(
        selectedTabIndex = { selectedIndex },
        onTabSelected = { index ->
            if (index in mainScreens.indices) {
                onNavigate(mainScreens[index])
            }
        },
        backdrop = backdrop,
        tabsCount = mainScreens.size,
        accentColor = LocalWhozitColors.current.accent,
        modifier = modifier
    ) {
        val whozitColors = LocalWhozitColors.current
        mainScreens.forEach { screen ->
            val isSelected = currentRoute == screen.route
            val adaptiveColor = LocalPrismalAdaptiveColor.current
            
            val isDarkDockSurface = adaptiveColor.luminance() > 0.5f
            val selectedColor = if (isDarkDockSurface) {
                if (whozitColors.accent.luminance() < 0.4f) Color.White else whozitColors.accent
            } else {
                if (whozitColors.accent.luminance() > 0.65f) whozitColors.textPrimary else whozitColors.accent
            }

            LiquidBottomTab(
                onClick = { onNavigate(screen) }
            ) {
                Image(
                    imageVector = if (isSelected) screen.selectedIcon else screen.unselectedIcon,
                    contentDescription = screen.title,
                    modifier = Modifier.size(if (isCompact) 20.dp else 22.dp),
                    colorFilter = ColorFilter.tint(if (isSelected) selectedColor else adaptiveColor)
                )
                if (!isCompact) {
                    BasicText(
                        text = screen.title,
                        style = TextStyle(
                            fontSize = 9.sp,
                            color = if (isSelected) selectedColor else adaptiveColor.copy(alpha = 0.7f)
                        ),
                        maxLines = 1
                    )
                }
            }
        }
    }
}

/**
 * DhikrCounter Liquid Glass Navigation Rail for Expanded / Tablet Layouts
 */
@Composable
fun WhozitLiquidNavigationRail(
    currentRoute: String?,
    onNavigate: (Screen) -> Unit,
    backdrop: Backdrop,
    modifier: Modifier = Modifier
) {
    val whozitColors = LocalWhozitColors.current
    val adaptiveColor = LocalPrismalAdaptiveColor.current

    com.crescentapps.whozit.presentation.components.liquid.LiquidSurface(
        modifier = modifier
            .fillMaxHeight()
            .width(80.dp)
            .padding(vertical = 16.dp, horizontal = 8.dp),
        backdrop = backdrop,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(32.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
        ) {
            mainScreens.forEach { screen ->
                val isSelected = currentRoute == screen.route
                val selectedColor = if (adaptiveColor.luminance() > 0.5f) {
                    if (whozitColors.accent.luminance() < 0.4f) Color.White else whozitColors.accent
                } else {
                    if (whozitColors.accent.luminance() > 0.65f) whozitColors.textPrimary else whozitColors.accent
                }

                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clickable { onNavigate(screen) },
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Image(
                            imageVector = if (isSelected) screen.selectedIcon else screen.unselectedIcon,
                            contentDescription = screen.title,
                            modifier = Modifier.size(22.dp),
                            colorFilter = ColorFilter.tint(if (isSelected) selectedColor else adaptiveColor)
                        )
                        Spacer(Modifier.height(4.dp))
                        BasicText(
                            text = screen.title,
                            style = TextStyle(
                                fontSize = 10.sp,
                                color = if (isSelected) selectedColor else adaptiveColor.copy(alpha = 0.7f)
                            ),
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

