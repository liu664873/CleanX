package com.quickcleanpro.phonecleaner.presentation.common.route

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.quickcleanpro.phonecleaner.presentation.screen.deviceinfo.BatteryInfoRoute
import com.quickcleanpro.phonecleaner.presentation.screen.deviceinfo.DeviceInfoRoute
import com.quickcleanpro.phonecleaner.presentation.screen.notification.NotificationCleanerRoute
import com.quickcleanpro.phonecleaner.presentation.screen.toolbox.AppUsageRoute
import com.quickcleanpro.phonecleaner.presentation.screen.toolbox.NetworkScanDevicesRoute
import com.quickcleanpro.phonecleaner.presentation.screen.toolbox.NetworkScanRoute
import com.quickcleanpro.phonecleaner.presentation.screen.toolbox.NetworkSpeedRoute
import com.quickcleanpro.phonecleaner.presentation.screen.toolbox.NetworkUsageRoute
import com.quickcleanpro.phonecleaner.presentation.screen.toolbox.NotificationBarRoute
import com.quickcleanpro.phonecleaner.presentation.screen.toolbox.WhatsAppCleanerRoute

internal fun NavGraphBuilder.registerToolboxRoutes() {
    composable(Screen.DeviceInfo.route) {
        val router = LocalRouter.current
        DeviceInfoRoute(onBack = { router.goBack() })
    }
    composable(Screen.BatteryInfo.route) {
        val router = LocalRouter.current
        BatteryInfoRoute(onBack = { router.goBack() })
    }
    composable(Screen.AppUsage.route) {
        val router = LocalRouter.current
        AppUsageRoute(onBack = { router.goBack() })
    }
    composable(Screen.NetworkUsage.route) {
        val router = LocalRouter.current
        NetworkUsageRoute(onBack = { router.goBack() })
    }
    composable(Screen.NetworkScan.route) {
        val router = LocalRouter.current
        NetworkScanRoute(
            onBack = { router.goBack() },
            onDevices = { router.navigate(Screen.NetworkScanDevices) },
        )
    }
    composable(Screen.NetworkScanDevices.route) {
        val router = LocalRouter.current
        NetworkScanDevicesRoute(onBack = { router.goBack() })
    }
    composable(Screen.NetworkSpeed.route) {
        val router = LocalRouter.current
        NetworkSpeedRoute(
            onBack = { router.goBack() },
            onResultBack = { router.goBack() },
            onNavigateTool = { route -> router.navigate(route) },
        )
    }
    composable(Screen.WhatsAppCleaner.route) {
        val router = LocalRouter.current
        WhatsAppCleanerRoute(
            onBack = { router.goBack() },
            onNavigateTool = { route -> router.navigate(route) },
        )
    }
    composable(Screen.NotificationBar.route) {
        val router = LocalRouter.current
        NotificationBarRoute(onBack = { router.goBack() })
    }
    composable(Screen.NotificationCleaner.route) {
        val router = LocalRouter.current
        NotificationCleanerRoute(
            onBack = { router.goBack() },
            onDeviceInfo = { router.navigate(Screen.DeviceInfo) },
            onJunkRemoval = { router.navigate(Screen.Scan) },
            onBatteryInfo = { router.navigate(Screen.BatteryInfo) },
            onNetworkScan = { router.navigate(Screen.NetworkScan) },
            onNetworkUsage = { router.navigate(Screen.NetworkUsage) },
        )
    }
}
