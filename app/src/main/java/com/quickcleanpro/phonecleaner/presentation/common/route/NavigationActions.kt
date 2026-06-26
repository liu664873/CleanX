package com.quickcleanpro.phonecleaner.presentation.common.route

import androidx.navigation.NavHostController
import com.quickcleanpro.phonecleaner.data.source.notification.ToolNotificationIntentFactory

internal fun NavHostController.safePopBackStack(): Boolean =
    runCatching {
        val currentRoute = currentDestination?.route
        if (currentRoute != null && currentRoute in rootRoutes) {
            return@runCatching false
        }
        if (popBackStack()) {
            return@runCatching true
        }
        navigate(Screen.Home.route) {
            currentRoute?.let { route ->
                popUpTo(route) { inclusive = true }
            }
            launchSingleTop = true
        }
        true
    }.getOrDefault(false)

internal fun NavHostController.navigateToHomeClearingStack() {
    runCatching {
        val currentRoute = currentDestination?.route
        if (currentRoute != null && currentRoute in homeRoutes) {
            return@runCatching
        }
        val existingHomeRoute =
            homeRoutes.firstOrNull { route ->
                runCatching { getBackStackEntry(route) }.isSuccess
            }
        if (existingHomeRoute != null && popBackStack(existingHomeRoute, inclusive = false)) {
            return@runCatching
        }
        navigate(Screen.Home.route) {
            launchSingleTop = true
        }
    }
}

internal fun NavHostController.navigateReplaceStack(route: String) {
    runCatching {
        navigateToHomeClearingStack()
        if (route != Screen.Home.route) {
            navigate(route) { launchSingleTop = true }
        }
    }
}

internal fun NavHostController.navigateReplacingCurrent(route: String) {
    runCatching {
        val currentRoute = currentDestination?.route
        navigate(route) {
            currentRoute?.let { popUpTo(it) { inclusive = true } }
            launchSingleTop = true
        }
    }
}

private val homeRoutes =
    setOf(Screen.Home.route) + ToolNotificationIntentFactory.homeTabRoutes

private val rootRoutes =
    setOf(
        Screen.Splash.route,
        Screen.OnboardingScan.route,
    ) + homeRoutes
