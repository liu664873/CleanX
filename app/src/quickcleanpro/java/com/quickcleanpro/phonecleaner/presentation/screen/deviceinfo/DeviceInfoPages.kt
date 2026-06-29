package com.quickcleanpro.phonecleaner.presentation.screen.deviceinfo

import androidx.compose.runtime.Composable

@Composable
internal fun DeviceInfoRoute(
    onBack: () -> Unit
) {
    DeviceInfoRoute(
        mode = DeviceInfoMode.Device,
        onBack = onBack
    )
}

@Composable
internal fun BatteryInfoRoute(
    onBack: () -> Unit
) {
    DeviceInfoRoute(
        mode = DeviceInfoMode.Battery,
        onBack = onBack
    )
}
