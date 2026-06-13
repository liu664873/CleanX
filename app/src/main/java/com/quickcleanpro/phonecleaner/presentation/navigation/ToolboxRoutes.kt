package com.quickcleanpro.phonecleaner.presentation.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.quickcleanpro.phonecleaner.presentation.navigation.AppNavigationEvent
import com.quickcleanpro.phonecleaner.presentation.screen.deviceinfo.AppUsageScreen
import com.quickcleanpro.phonecleaner.presentation.screen.deviceinfo.BatteryInfoScreen
import com.quickcleanpro.phonecleaner.presentation.screen.deviceinfo.DeviceInfoScreen
import com.quickcleanpro.phonecleaner.presentation.screen.deviceinfo.NetworkScanDevicesScreen
import com.quickcleanpro.phonecleaner.presentation.screen.deviceinfo.NetworkScanScreen
import com.quickcleanpro.phonecleaner.presentation.screen.deviceinfo.NetworkSpeedScreen
import com.quickcleanpro.phonecleaner.presentation.screen.deviceinfo.NetworkUsageScreen

internal fun NavGraphBuilder.registerToolboxRoutes(navController: NavHostController) {
    val onBack: () -> Unit = { navController.handleNavigationEvent(AppNavigationEvent.Back) }
    val onNavigate: (AppNavigationEvent) -> Unit = navController::handleNavigationEvent

    composable(Screen.DeviceInfo.route) {
        DeviceInfoScreen(onBack = onBack)
    }
    composable(Screen.BatteryInfo.route) {
        BatteryInfoScreen(onBack = onBack)
    }
    composable(Screen.AppUsage.route) {
        AppUsageScreen(onBack = onBack)
    }
    composable(Screen.NetworkUsage.route) {
        NetworkUsageScreen(onBack = onBack)
    }
    composable(Screen.NetworkScan.route) {
        NetworkScanScreen(onBack = onBack, onNavigate = onNavigate)
    }
    composable(Screen.NetworkScanDevices.route) {
        NetworkScanDevicesScreen(onBack = onBack)
    }
    composable(Screen.NetworkSpeed.route) {
        NetworkSpeedScreen(onBack = onBack, onNavigate = onNavigate)
    }
    registerPlaceholderRoutes(
        navController = navController,
        screens =
            listOf(
                Screen.WhatsAppCleaner,
                Screen.NotificationCleaner,
                Screen.NotificationBar,
            ),
    )
}
