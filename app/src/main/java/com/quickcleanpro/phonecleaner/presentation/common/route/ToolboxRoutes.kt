package com.quickcleanpro.phonecleaner.presentation.common.route

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.quickcleanpro.phonecleaner.presentation.screen.appusage.AppUsageScreen
import com.quickcleanpro.phonecleaner.presentation.screen.batteryinfo.BatteryInfoScreen
import com.quickcleanpro.phonecleaner.presentation.screen.deviceinfo.DeviceInfoScreen
import com.quickcleanpro.phonecleaner.presentation.screen.networkscan.NetworkScanDevicesScreen
import com.quickcleanpro.phonecleaner.presentation.screen.networkscan.NetworkScanScreen
import com.quickcleanpro.phonecleaner.presentation.screen.networkspeed.NetworkSpeedScreen
import com.quickcleanpro.phonecleaner.presentation.screen.notificationcleaner.NotificationCleanerScreen
import com.quickcleanpro.phonecleaner.presentation.screen.networkusage.NetworkUsageScreen
import com.quickcleanpro.phonecleaner.presentation.screen.notificationbar.NotificationBarScreen
import com.quickcleanpro.phonecleaner.presentation.screen.whatsappcleaner.WhatsAppCleanerScreen

internal fun NavGraphBuilder.registerToolboxRoutes() {
    composable(Screen.DeviceInfo.route) {
        DeviceInfoScreen()
    }
    composable(Screen.BatteryInfo.route) {
        BatteryInfoScreen()
    }
    composable(Screen.AppUsage.route) {
        AppUsageScreen()
    }
    composable(Screen.NetworkUsage.route) {
        NetworkUsageScreen()
    }
    composable(Screen.NetworkScan.route) {
        NetworkScanScreen()
    }
    composable(Screen.NetworkScanDevices.route) {
        NetworkScanDevicesScreen()
    }
    composable(Screen.NetworkSpeed.route) {
        NetworkSpeedScreen()
    }
    composable(Screen.WhatsAppCleaner.route) {
        WhatsAppCleanerScreen()
    }
    composable(Screen.NotificationBar.route) {
        NotificationBarScreen()
    }
    composable(Screen.NotificationCleaner.route) {
        val router = LocalRouter.current
        NotificationCleanerScreen(
            onBack = { router.goBack() },
            onDeviceInfo = { router.navigate(Screen.DeviceInfo) },
            onJunkRemoval = { router.navigate(Screen.Scan) },
            onBatteryInfo = { router.navigate(Screen.BatteryInfo) },
            onNetworkScan = { router.navigate(Screen.NetworkScan) },
            onNetworkUsage = { router.navigate(Screen.NetworkUsage) },
        )
    }
}
