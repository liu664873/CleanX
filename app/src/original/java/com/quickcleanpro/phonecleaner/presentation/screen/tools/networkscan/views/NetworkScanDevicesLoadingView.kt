package com.quickcleanpro.phonecleaner.presentation.screen.tools.networkscan.views

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.quickcleanpro.phonecleaner.R
import com.quickcleanpro.phonecleaner.presentation.screen.tools.networkscan.NetworkScanDevicesUiState

@Composable
internal fun NetworkScanDevicesLoadingView(uiState: NetworkScanDevicesUiState) {
    NetworkScanDevicesHeaderCard(count = uiState.devices.size)
    Spacer(modifier = Modifier.height(20.dp))
    NetworkScanMessageCard(
        title = stringResource(R.string.scanning_devices),
        message = stringResource(R.string.scan_loading_fallback),
    )
    Spacer(modifier = Modifier.height(96.dp))
}
