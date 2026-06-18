package com.quickcleanpro.phonecleaner.presentation.screen.tools.notificationcleaner

import androidx.compose.runtime.Composable
import com.quickcleanpro.phonecleaner.presentation.screen.tools.common.notification.NotificationCleanerViewModel
import com.quickcleanpro.phonecleaner.presentation.screen.tools.notificationcleaner.views.NotificationCleanerScreenState
import org.koin.androidx.compose.koinViewModel

@Composable
fun NotificationCleanerScreen(
    onBack: () -> Unit,
    onDeviceInfo: () -> Unit,
    onJunkRemoval: () -> Unit,
    onBatteryInfo: () -> Unit,
    onNetworkScan: () -> Unit,
    onNetworkUsage: () -> Unit,
    viewModel: NotificationCleanerViewModel = koinViewModel(),
) {
    NotificationCleanerScreenState(
        onBack = onBack,
        onDeviceInfo = onDeviceInfo,
        onJunkRemoval = onJunkRemoval,
        onBatteryInfo = onBatteryInfo,
        onNetworkScan = onNetworkScan,
        onNetworkUsage = onNetworkUsage,
        viewModel = viewModel,
    )
}
