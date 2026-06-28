package com.quickcleanpro.phonecleaner.presentation.common.route

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.quickcleanpro.phonecleaner.config.FeatureKey
import com.quickcleanpro.phonecleaner.presentation.screen.tools.appusage.AppUsageScreen
import com.quickcleanpro.phonecleaner.presentation.screen.tools.batteryinfo.BatteryInfoScreen
import com.quickcleanpro.phonecleaner.presentation.screen.tools.deviceinfo.DeviceInfoScreen
import com.quickcleanpro.phonecleaner.presentation.screen.tools.networkscan.NetworkScanDevicesScreen
import com.quickcleanpro.phonecleaner.presentation.screen.tools.networkscan.NetworkScanScreen
import com.quickcleanpro.phonecleaner.presentation.screen.tools.networkspeed.NetworkSpeedScreen
import com.quickcleanpro.phonecleaner.presentation.screen.tools.networkusage.NetworkUsageScreen
import com.quickcleanpro.phonecleaner.presentation.screen.tools.notificationbar.NotificationBarScreen
import com.quickcleanpro.phonecleaner.presentation.screen.tools.notificationcleaner.NotificationCleanerScreen
import com.quickcleanpro.phonecleaner.presentation.screen.tools.whatsappcleaner.WhatsAppCleanerScreen

internal fun NavGraphBuilder.registerToolboxRoutes(registry: FeatureRegistry) {
    if (registry.isEnabled(FeatureKey.DEVICE_INFO)) {
        composable(Screen.DeviceInfo.route) {
            DeviceInfoScreen()
        }
    }
    if (registry.isEnabled(FeatureKey.BATTERY_INFO)) {
        composable(Screen.BatteryInfo.route) {
            BatteryInfoScreen()
        }
    }
    if (registry.isEnabled(FeatureKey.APP_USAGE)) {
        composable(Screen.AppUsage.route) {
            AppUsageScreen()
        }
    }
    if (registry.isEnabled(FeatureKey.NETWORK_USAGE)) {
        composable(Screen.NetworkUsage.route) {
            NetworkUsageScreen()
        }
    }
    if (registry.isEnabled(FeatureKey.NETWORK_SCAN)) {
        composable(Screen.NetworkScan.route) {
            NetworkScanScreen()
        }
        composable(Screen.NetworkScanDevices.route) {
            NetworkScanDevicesScreen()
        }
    }
    if (registry.isEnabled(FeatureKey.NETWORK_SPEED)) {
        composable(Screen.NetworkSpeed.route) {
            NetworkSpeedScreen()
        }
    }
    if (registry.isEnabled(FeatureKey.WHATSAPP_CLEANER)) {
        composable(Screen.WhatsAppCleaner.route) {
            WhatsAppCleanerScreen()
        }
    }
    if (registry.isEnabled(FeatureKey.NOTIFICATION_BAR)) {
        composable(Screen.NotificationBar.route) {
            NotificationBarScreen()
        }
    }
    if (registry.isEnabled(FeatureKey.NOTIFICATION_CLEANER)) {
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
}
