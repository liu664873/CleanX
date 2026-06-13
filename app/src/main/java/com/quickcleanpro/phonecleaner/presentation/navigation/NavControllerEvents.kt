package com.quickcleanpro.phonecleaner.presentation.navigation

import androidx.navigation.NavHostController

internal fun NavHostController.handleNavigationEvent(
    event: AppNavigationEvent,
//    adController: AdController
) {
    when (event) {
        AppNavigationEvent.Back -> safePopBackStack()
        AppNavigationEvent.Home -> navigateHomeClearingStack()
        is AppNavigationEvent.Destination -> {
            navigate(event.route) {
                launchSingleTop = true
            }
        }
//        is AppNavigationEvent.AdDestination -> {
//            adController.runWithInterstitial(event.placement) {
//                navigate(event.route) { launchSingleTop = true }
//            }
//        }
        is AppNavigationEvent.ToolFromResult -> navigateToolFromResult(event.route)
    }
}
