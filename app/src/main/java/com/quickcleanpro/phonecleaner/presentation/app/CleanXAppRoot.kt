package com.quickcleanpro.phonecleaner.presentation.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.quickcleanpro.phonecleaner.domain.repository.SettingsRepository
import com.quickcleanpro.phonecleaner.presentation.common.route.AppNavGraph
import com.quickcleanpro.phonecleaner.presentation.common.route.Screen

@Composable
fun CleanXAppRoot(
    launchCoordinator: AppLaunchCoordinator,
    settingsRepository: SettingsRepository,
    hasNotificationPermission: () -> Boolean,
    openAppSettings: () -> Unit,
    onStartPersistentNotification: () -> Unit,
) {
    val navController = rememberNavController()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route
    val pendingRequest = launchCoordinator.pendingRequest

    LaunchedEffect(currentRoute) {
        launchCoordinator.onRouteChanged(currentRoute)
    }

    AppNavGraph(
        navController = navController,
        launchCoordinator = launchCoordinator,
        startDestination = Screen.Splash.route,
    )

    LaunchedEffect(navController, pendingRequest) {
        if (pendingRequest !is AppLaunchRequest.NotificationTarget &&
            pendingRequest !is AppLaunchRequest.ForegroundReturn
        ) {
            return@LaunchedEffect
        }
        if (currentRoute == Screen.Splash.route) return@LaunchedEffect
        navController.navigate(Screen.Splash.route) {
            launchSingleTop = true
        }
    }

    NotificationPermissionPrompt(
        isHomeVisible = currentRoute == Screen.Home.route,
        hasNotificationPermission = hasNotificationPermission,
        hasDeniedNotificationPermission = {
            runCatching { settingsRepository.hasDeniedNotificationRuntimePermission() }.getOrDefault(false)
        },
        rememberNotificationDenied = {
            runCatching { settingsRepository.saveNotificationRuntimePermissionDenied() }
        },
        openAppSettings = openAppSettings,
        onPermissionGranted = onStartPersistentNotification,
    )
}

internal fun NavHostController.navigateToNotificationTarget(route: String) {
    while (popBackStack()) {
        // Clear the existing stack before rebuilding Home -> target.
    }
    val currentRoute = currentDestination?.route
    navigate(Screen.Home.route) {
        currentRoute?.let { popUpTo(it) { inclusive = true } }
        launchSingleTop = true
    }
    if (route != Screen.Home.route) {
        navigate(route) { launchSingleTop = true }
    }
}

internal fun NavHostController.returnFromForegroundSplash(previousRoute: String) {
    if (popBackStack()) return
    navigate(previousRoute) {
        currentDestination?.route?.let { popUpTo(it) { inclusive = true } }
        launchSingleTop = true
    }
}
