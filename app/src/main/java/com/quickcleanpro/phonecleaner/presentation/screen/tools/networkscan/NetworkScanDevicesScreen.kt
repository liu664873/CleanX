package com.quickcleanpro.phonecleaner.presentation.screen.tools.networkscan

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.quickcleanpro.phonecleaner.R
import com.quickcleanpro.phonecleaner.presentation.common.components.CleanXScaffoldPage
import com.quickcleanpro.phonecleaner.presentation.screen.tools.networkscan.views.NetworkScanDevicesContentView
import org.koin.androidx.compose.koinViewModel

@Composable
fun NetworkScanDevicesScreen(viewModel: NetworkScanDevicesViewModel = koinViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    CleanXScaffoldPage(
        title = stringResource(R.string.network_scan),
        titleFontSize = 20.sp,
    ) {
        NetworkScanDevicesContentView(
            uiState = uiState,
            onRetry = viewModel::loadDevices,
        )
    }
}
