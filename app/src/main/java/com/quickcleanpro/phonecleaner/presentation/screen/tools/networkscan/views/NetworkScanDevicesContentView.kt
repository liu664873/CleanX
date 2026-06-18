package com.quickcleanpro.phonecleaner.presentation.screen.tools.networkscan.views

import androidx.compose.runtime.Composable
import com.quickcleanpro.phonecleaner.presentation.screen.tools.networkscan.NetworkScanDevicesUiState

@Composable
internal fun NetworkScanDevicesContentView(
    uiState: NetworkScanDevicesUiState,
    onRetry: () -> Unit,
) {
    when {
        uiState.isLoading -> NetworkScanDevicesLoadingView(uiState = uiState)
        uiState.errorMessage != null -> NetworkScanDevicesErrorView(
            uiState = uiState,
            onRetry = onRetry,
        )
        else -> NetworkScanDevicesResultView(uiState = uiState)
    }
}
