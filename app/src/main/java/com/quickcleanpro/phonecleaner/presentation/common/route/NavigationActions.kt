package com.quickcleanpro.phonecleaner.presentation.common.route

import androidx.navigation.NavHostController

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
        val homeInBackStack = runCatching { getBackStackEntry(Screen.Home.route) }.isSuccess
        navigate(Screen.Home.route) {
            if (homeInBackStack) {
                popUpTo(Screen.Home.route) { inclusive = true }
            }
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

private val rootRoutes =
    setOf(
        Screen.Splash.route,
        Screen.OnboardingScan.route,
        Screen.Home.route,
    )
