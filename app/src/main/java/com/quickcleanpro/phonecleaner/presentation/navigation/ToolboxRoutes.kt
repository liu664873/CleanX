package com.quickcleanpro.phonecleaner.presentation.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController

internal fun NavGraphBuilder.registerToolboxRoutes(navController: NavHostController) {
    registerPlaceholderRoutes(
        navController = navController,
        screens =
            listOf(
                Screen.DeviceInfo,
                Screen.BatteryInfo,
                Screen.AppUsage,
                Screen.NetworkUsage,
                Screen.NetworkScan,
                Screen.NetworkScanDevices,
                Screen.NetworkSpeed,
                Screen.WhatsAppCleaner,
                Screen.NotificationCleaner,
                Screen.NotificationBar,
            ),
    )
}
