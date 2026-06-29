package com.quickcleanpro.phonecleaner.presentation.screen.notification

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.quickcleanpro.phonecleaner.presentation.theme.QuickCleanTheme

@Preview(showBackground = true, backgroundColor = 0xFFF4F8FF)
@Composable
private fun PreviewNotificationCleanerScreen() {
    QuickCleanTheme {
        NotificationCleanerScreen(
            onBack = {},
            onDeviceInfo = {},
            onJunkRemoval = {},
            onBatteryInfo = {},
            onNetworkScan = {},
            onNetworkUsage = {}
        )
    }
}
