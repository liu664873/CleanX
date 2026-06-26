package com.quickcleanpro.phonecleaner.presentation.screen.tools.networkspeed.views

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.quickcleanpro.phonecleaner.R
import com.quickcleanpro.phonecleaner.presentation.screen.tools.networkspeed.NetworkSpeedUiState

@Composable
internal fun NetworkSpeedErrorView(uiState: NetworkSpeedUiState) {
    NetworkSpeedInfoCard(uiState = uiState)
    Spacer(modifier = Modifier.height(20.dp))
    if (uiState.hasNetwork) {
        NetworkSpeedMetricCard(
            uiState = uiState,
            showGauge = false,
        )
    } else {
        NetworkSpeedEmptyCard(
            title = stringResource(R.string.no_network_connection),
            message = stringResource(R.string.network_speed_no_connection_desc),
        )
    }
    Spacer(modifier = Modifier.height(12.dp))
    NetworkSpeedErrorText(message = uiState.errorMessage)
    Spacer(modifier = Modifier.height(96.dp))
}
