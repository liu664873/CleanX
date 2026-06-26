package com.quickcleanpro.phonecleaner.presentation.screen.tools.networkscan.views

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.quickcleanpro.phonecleaner.presentation.screen.tools.networkscan.NetworkScanUiState

@Composable
internal fun NetworkScanResultView(
    uiState: NetworkScanUiState,
    onDevicesClick: () -> Unit,
) {
    NetworkScanResultCard(uiState = uiState)
    Spacer(modifier = Modifier.height(20.dp))
    NetworkScanDetailsCard(uiState = uiState)
    uiState.scan?.let { scan ->
        Spacer(modifier = Modifier.height(20.dp))
        NetworkScanDevicesSummaryCard(
            scan = scan,
            onDevicesClick = onDevicesClick,
        )
    }
    Spacer(modifier = Modifier.height(152.dp))
}
