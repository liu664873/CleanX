package com.quickcleanpro.phonecleaner.presentation.screen.tools.batteryinfo

import androidx.compose.runtime.Composable
import com.quickcleanpro.phonecleaner.presentation.screen.tools.batteryinfo.views.BatteryInfoScreenState
import com.quickcleanpro.phonecleaner.presentation.screen.tools.common.device.BatteryInfoViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun BatteryInfoScreen(viewModel: BatteryInfoViewModel = koinViewModel()) {
    BatteryInfoScreenState(viewModel = viewModel)
}
