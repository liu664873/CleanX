package com.quickcleanpro.phonecleaner.presentation.screen.tools.networkscan

import androidx.compose.runtime.Composable
import com.quickcleanpro.phonecleaner.presentation.screen.tools.networkscan.views.NetworkScanDevicesScreenState
import org.koin.androidx.compose.koinViewModel

@Composable
fun NetworkScanDevicesScreen(viewModel: NetworkScanDevicesViewModel = koinViewModel()) {
    NetworkScanDevicesScreenState(viewModel = viewModel)
}
