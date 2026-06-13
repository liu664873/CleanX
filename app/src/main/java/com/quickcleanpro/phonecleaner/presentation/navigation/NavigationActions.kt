package com.quickcleanpro.phonecleaner.presentation.navigation

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
                popUpTo(route) {
                    inclusive = true
                }
            }
            launchSingleTop = true
        }
        true
    }.getOrDefault(false)

internal fun NavHostController.navigateHomeClearingStack() {
    runCatching {
        val homeInBackStack = runCatching { getBackStackEntry(Screen.Home.route) }.isSuccess
        navigate(Screen.Home.route) {
            if (homeInBackStack) {
                popUpTo(Screen.Home.route) {
                    inclusive = true
                }
            }
            launchSingleTop = true
        }
    }
}

internal fun NavHostController.navigateToolFromResult(route: String) {
    runCatching {
        navigateHomeClearingStack()
        if (route != Screen.Home.route) {
            navigate(route) {
                launchSingleTop = true
            }
        }
    }
}

private val rootRoutes =
    setOf(
        Screen.Splash.route,
        Screen.OnboardingScan.route,
        Screen.Home.route,
    )
