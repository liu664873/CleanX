package com.quickcleanpro.phonecleaner.presentation.common.route

import androidx.navigation.NavHostController

internal fun NavHostController.handleNavigationEvent(
    event: AppNavigationEvent,
    adManager: AdManager = NoOpAdManager,
) {
    when (event) {
        AppNavigationEvent.Back -> safePopBackStack()
        AppNavigationEvent.Home -> navigateToHomeClearingStack()
        is AppNavigationEvent.Destination -> {
            val finalRoute =
                if (event.args.isEmpty()) {
                    event.route
                } else {
                    val query = event.args.entries.joinToString("&") { (k, v) -> "$k=$v" }
                    "${event.route}?$query"
                }
            navigate(finalRoute) { launchSingleTop = true }
        }
        is AppNavigationEvent.AdDestination -> {
            adManager.showAd(event.placement) {
                navigate(event.route) { launchSingleTop = true }
            }
        }
        is AppNavigationEvent.ReplaceStack -> navigateReplaceStack(event.route)
        is AppNavigationEvent.ReplaceCurrent -> navigateReplacingCurrent(event.route)
    }
}
