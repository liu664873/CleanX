package com.quickcleanpro.phonecleaner.presentation.navigation

import com.quickcleanpro.phonecleaner.presentation.navigation.Screen.AntiVirus.route

sealed interface AppNavigationEvent {
    data object Back : AppNavigationEvent
    data object Home : AppNavigationEvent
    data class Destination(val route: String) : AppNavigationEvent
//    插屏广告页，暂时未实现
//    data class AdDestination(
//        val route: String,
//        val placement: com.quickcleanpro.phonecleaner.presentation.ads.AdPlacement =
//            com.quickcleanpro.phonecleaner.presentation.ads.AdPlacement.InterstitialToolLaunch
//    ) : AppNavigationEvent
    data class ToolFromResult(val route: String) : AppNavigationEvent
}

fun AppDestination.toNavigationEvent(): AppNavigationEvent =
    AppNavigationEvent.Destination(route)
