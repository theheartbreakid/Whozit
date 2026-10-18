package com.crescentapps.whozit.presentation.gaussianblur

import androidx.compose.animation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.crescentapps.whozit.presentation.components.Icon
import com.crescentapps.whozit.presentation.components.Text
import com.crescentapps.whozit.presentation.gaussianblur.backdrop.gaussianGlass
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.crescentapps.whozit.WhozitApplication
import com.crescentapps.whozit.core.model.Schedule
import com.crescentapps.whozit.data.preferences.UserPreferences
import com.crescentapps.whozit.presentation.gaussianblur.backdrop.GaussianBlurBackdropHost
import com.crescentapps.whozit.presentation.gaussianblur.navigation.GaussianDock
import com.crescentapps.whozit.presentation.gaussianblur.navigation.GaussianNavDestination
import com.crescentapps.whozit.presentation.gaussianblur.screens.*
import com.crescentapps.whozit.presentation.gaussianblur.theme.GaussianBlurTheme
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

@Composable
fun GaussianBlurApp(
    app: WhozitApplication,
    prefs: UserPreferences,
    deepLinkCode: String? = null
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val homeViewModel: HomeViewModel = viewModel { HomeViewModel(app.repository, app.roomRepository) }
    val calendarViewModel: CalendarViewModel = viewModel { CalendarViewModel(app.repository) }
    val historyViewModel: HistoryViewModel = viewModel { HistoryViewModel(app.repository) }
    val roomViewModel: RoomViewModel = viewModel { RoomViewModel(app.roomRepository, app.repository) }
    val settingsViewModel: SettingsViewModel = viewModel { SettingsViewModel(app.userPreferencesRepository, app.repository, app.updateManager) }
    val scheduleFormViewModel: ScheduleFormViewModel = viewModel { ScheduleFormViewModel(app.repository, app.roomRepository) }

    val schedulesState = app.repository.activeSchedules.collectAsState(initial = emptyList())
    val schedules = schedulesState.value

    // Map current route to dock destination
    val currentDockDestination = when {
        currentRoute == "today" -> GaussianNavDestination.TODAY
        currentRoute == "calendar" -> GaussianNavDestination.CALENDAR
        currentRoute == "schedules" || currentRoute?.startsWith("schedule") == true -> GaussianNavDestination.SCHEDULES
        currentRoute == "history" -> GaussianNavDestination.HISTORY
        currentRoute == "rooms" || currentRoute?.startsWith("room") == true || currentRoute?.startsWith("create_room") == true || currentRoute?.startsWith("join_room") == true -> GaussianNavDestination.ROOMS
        currentRoute == "settings" -> GaussianNavDestination.SETTINGS
        else -> GaussianNavDestination.TODAY
    }

    LaunchedEffect(deepLinkCode) {
        if (!deepLinkCode.isNullOrBlank()) {
            navController.navigate("join_room?code=$deepLinkCode")
        }
    }

    val config = androidx.compose.ui.platform.LocalConfiguration.current
    val isExpanded = config.screenWidthDp >= 600
    val showDock = !isExpanded && currentRoute in listOf("today", "calendar", "schedules", "history", "rooms", "settings")

    GaussianBlurTheme(prefs = prefs) {
        GaussianBlurBackdropHost(prefs = prefs) {
            val dockState = remember(showDock) {
                com.crescentapps.whozit.presentation.theme.DockSafeAreaState().apply {
                    dockHeight = if (showDock) 100.dp else 0.dp
                }
            }
            CompositionLocalProvider(
                com.crescentapps.whozit.presentation.theme.LocalDockSafeArea provides dockState
            ) {
                androidx.compose.foundation.layout.Row(modifier = Modifier.fillMaxSize()) {
                    if (isExpanded && currentRoute in listOf("today", "calendar", "schedules", "history", "rooms", "settings")) {
                        GaussianNavigationRail(
                            currentDestination = currentDockDestination,
                            onNavigate = { dest ->
                                val route = when (dest) {
                                    GaussianNavDestination.TODAY -> "today"
                                    GaussianNavDestination.CALENDAR -> "calendar"
                                    GaussianNavDestination.SCHEDULES -> "schedules"
                                    GaussianNavDestination.HISTORY -> "history"
                                    GaussianNavDestination.ROOMS -> "rooms"
                                    GaussianNavDestination.SETTINGS -> "settings"
                                }
                                navController.navigate(route) {
                                    popUpTo("today") { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }

                    Box(modifier = Modifier.weight(1f).fillMaxSize()) {
                        NavHost(
                            navController = navController,
                            startDestination = "today",
                            modifier = Modifier.fillMaxSize(),
                            enterTransition = { fadeIn() },
                            exitTransition = { fadeOut() }
                        ) {
                            composable("today") {
                                GaussianHomeScreen(
                                    viewModel = homeViewModel,
                                    onCreateSchedule = { navController.navigate("create_schedule") },
                                    onScheduleClick = { id -> navController.navigate("schedule_detail/$id") }
                                )
                            }

                            composable("calendar") {
                                GaussianCalendarScreen(
                                    viewModel = calendarViewModel,
                                    onScheduleClick = { id -> navController.navigate("schedule_detail/$id") }
                                )
                            }

                            composable("schedules") {
                                GaussianScheduleListScreen(
                                    schedules = schedules,
                                    onCreateSchedule = { navController.navigate("create_schedule") },
                                    onScheduleClick = { id -> navController.navigate("schedule_detail/$id") }
                                )
                            }

                            composable("create_schedule") {
                                GaussianCreateScheduleScreen(
                                    viewModel = scheduleFormViewModel,
                                    onNavigateBack = { navController.popBackStack() },
                                    onCreatedSuccessfully = { navController.popBackStack() }
                                )
                            }

                            composable(
                                route = "schedule_detail/{scheduleId}",
                                arguments = listOf(navArgument("scheduleId") { type = NavType.LongType })
                            ) { backStackEntry ->
                                val scheduleId = backStackEntry.arguments?.getLong("scheduleId") ?: 0L
                                val detailViewModel: ScheduleDetailViewModel = viewModel(key = "schedule_detail_$scheduleId") {
                                    ScheduleDetailViewModel(app.repository, scheduleId)
                                }
                                GaussianScheduleDetailScreen(
                                    viewModel = detailViewModel,
                                    onNavigateBack = { navController.popBackStack() }
                                )
                            }

                            composable("history") {
                                GaussianHistoryScreen(viewModel = historyViewModel)
                            }

                            composable("rooms") {
                                GaussianRoomListScreen(
                                    viewModel = roomViewModel,
                                    onCreateRoom = { navController.navigate("create_room") },
                                    onJoinRoom = { navController.navigate("join_room") },
                                    onRoomClick = { id -> navController.navigate("room_detail/$id") }
                                )
                            }

                            composable("create_room") {
                                GaussianCreateRoomScreen(
                                    viewModel = roomViewModel,
                                    onNavigateBack = { navController.popBackStack() },
                                    onRoomCreated = { id ->
                                        navController.popBackStack()
                                        navController.navigate("room_detail/$id")
                                    }
                                )
                            }

                            composable(
                                route = "join_room?code={code}",
                                arguments = listOf(navArgument("code") {
                                    type = NavType.StringType
                                    nullable = true
                                    defaultValue = null
                                })
                            ) { backStackEntry ->
                                val code = backStackEntry.arguments?.getString("code")
                                GaussianJoinRoomScreen(
                                    viewModel = roomViewModel,
                                    initialRoomCode = code,
                                    onNavigateBack = { navController.popBackStack() },
                                    onJoinedSuccessfully = { id ->
                                        navController.popBackStack()
                                        navController.navigate("room_detail/$id")
                                    }
                                )
                            }

                            composable(
                                route = "room_detail/{roomId}",
                                arguments = listOf(navArgument("roomId") { type = NavType.LongType })
                            ) { backStackEntry ->
                                val roomId = backStackEntry.arguments?.getLong("roomId") ?: 0L
                                val roomDetailViewModel: RoomDetailViewModel = viewModel(key = "room_detail_$roomId") {
                                    RoomDetailViewModel(app.roomRepository, app.repository, roomId)
                                }
                                GaussianRoomDetailScreen(
                                    viewModel = roomDetailViewModel,
                                    onNavigateBack = { navController.popBackStack() },
                                    onNavigateToSettings = { id -> navController.navigate("room_settings/$id") },
                                    onScheduleClick = { id -> navController.navigate("schedule_detail/$id") }
                                )
                            }

                            composable(
                                route = "room_settings/{roomId}",
                                arguments = listOf(navArgument("roomId") { type = NavType.LongType })
                            ) { backStackEntry ->
                                val roomId = backStackEntry.arguments?.getLong("roomId") ?: 0L
                                val roomDetailViewModel: RoomDetailViewModel = viewModel(key = "room_settings_$roomId") {
                                    RoomDetailViewModel(app.roomRepository, app.repository, roomId)
                                }
                                RoomSettingsScreen(
                                    viewModel = roomDetailViewModel,
                                    onNavigateBack = { navController.popBackStack() },
                                    onRoomExited = {
                                        navController.popBackStack("rooms", false)
                                    }
                                )
                            }

                            composable("settings") {
                                GaussianSettingsScreen(viewModel = settingsViewModel)
                            }
                        }

                        if (showDock) {
                            GaussianDock(
                                currentDestination = currentDockDestination,
                                onNavigate = { dest ->
                                    val route = when (dest) {
                                        GaussianNavDestination.TODAY -> "today"
                                        GaussianNavDestination.CALENDAR -> "calendar"
                                        GaussianNavDestination.SCHEDULES -> "schedules"
                                        GaussianNavDestination.HISTORY -> "history"
                                        GaussianNavDestination.ROOMS -> "rooms"
                                        GaussianNavDestination.SETTINGS -> "settings"
                                    }
                                    navController.navigate(route) {
                                        popUpTo("today") { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                modifier = Modifier.align(Alignment.BottomCenter)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GaussianNavigationRail(
    currentDestination: GaussianNavDestination,
    onNavigate: (GaussianNavDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    val backdrop = com.crescentapps.whozit.presentation.gaussianblur.backdrop.LocalAppBackdrop.current
    val colors = com.crescentapps.whozit.presentation.gaussianblur.theme.LocalGaussianBlurColors.current
    val pillShape = remember { RoundedCornerShape(28.dp) }

    Column(
        modifier = modifier
            .fillMaxHeight()
            .width(80.dp)
            .statusBarsPadding()
            .padding(vertical = 16.dp, horizontal = 8.dp)
            .gaussianGlass(
                shape = pillShape,
                backdrop = backdrop,
                blurRadiusDp = 28f,
                surfaceOpacity = 0.45f,
                isDark = colors.isDark
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
    ) {
        GaussianNavDestination.entries.forEach { dest ->
            val isSelected = dest == currentDestination
            val tint = if (isSelected) colors.accent else colors.textMuted

            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .clickable { onNavigate(dest) },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = dest.icon,
                        contentDescription = dest.label,
                        tint = tint,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = dest.label,
                        fontSize = 10.sp,
                        color = tint,
                        maxLines = 1
                    )
                }
            }
        }
    }
}
