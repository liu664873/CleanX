package com.quickcleanpro.phonecleaner.presentation.common.route

import androidx.navigation.NavHostController
import com.quickcleanpro.phonecleaner.data.source.notification.ToolNotificationIntentFactory
import com.quickcleanpro.phonecleaner.navigation.AppRoute

internal fun NavHostController.safePopBackStack(): Boolean =
    runCatching {
        val currentRoute = currentDestination?.route
        if (currentRoute != null && currentRoute in rootRoutes) {
            return@runCatching false
        }
        if (popBackStack()) {
            return@runCatching true
        }
        navigate(AppRoute.Home.value) {
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
        navigate(AppRoute.Home.value) {
            launchSingleTop = true
        }
    }
}

internal fun NavHostController.navigateReplaceStack(route: String) {
    runCatching {
        navigateToHomeClearingStack()
        if (route != AppRoute.Home.value) {
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
    setOf(AppRoute.Home.value) + ToolNotificationIntentFactory.homeTabRoutes

private val rootRoutes =
    setOf(
        AppRoute.Splash.value,
        AppRoute.OnboardingScan.value,
    ) + homeRoutes
