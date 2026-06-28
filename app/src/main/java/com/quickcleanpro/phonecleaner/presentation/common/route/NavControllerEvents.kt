package com.quickcleanpro.phonecleaner.presentation.common.route

import androidx.navigation.NavHostController
import com.quickcleanpro.phonecleaner.data.source.notification.ToolNotificationIntentFactory
import com.quickcleanpro.phonecleaner.navigation.AppRoute

internal fun NavHostController.handleNavigationEvent(
    event: AppNavigationEvent,
    adManager: AdManager = NoOpAdManager,
) {
    when (event) {
        AppNavigationEvent.Back -> safePopBackStack()
        AppNavigationEvent.Home -> {
            if (currentDestination?.route in adHomeRoutes) {
                navigateToHomeClearingStack()
            } else {
                adManager.showAd(AdPlacements.RETURN_HOME) {
                    navigateToHomeClearingStack()
                }
            }
        }
        is AppNavigationEvent.Destination -> {
            navigate(event.finalRoute()) { launchSingleTop = true }
        }
        is AppNavigationEvent.AdDestination -> {
            adManager.showAd(event.placement) {
                if (event.route in adHomeRoutes) {
                    navigateToHomeClearingStack()
                } else {
                    navigate(event.route) { launchSingleTop = true }
                }
            }
        }
        is AppNavigationEvent.ReplaceStack -> navigateReplaceStack(event.route)
        is AppNavigationEvent.ReplaceCurrent -> navigateReplacingCurrent(event.route)
    }
}

private val adHomeRoutes =
    setOf(AppRoute.Home.value) + ToolNotificationIntentFactory.homeTabRoutes
