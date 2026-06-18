package com.quickcleanpro.phonecleaner.presentation.screen.tools.deviceinfo

import androidx.compose.runtime.Composable
import com.quickcleanpro.phonecleaner.presentation.screen.tools.common.device.DeviceInfoViewModel
import com.quickcleanpro.phonecleaner.presentation.screen.tools.deviceinfo.views.DeviceInfoScreenState
import org.koin.androidx.compose.koinViewModel

@Composable
fun DeviceInfoScreen(viewModel: DeviceInfoViewModel = koinViewModel()) {
    DeviceInfoScreenState(viewModel = viewModel)
}
