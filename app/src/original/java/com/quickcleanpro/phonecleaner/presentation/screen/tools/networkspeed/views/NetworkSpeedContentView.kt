package com.quickcleanpro.phonecleaner.presentation.screen.tools.networkspeed.views

import androidx.compose.runtime.Composable
import com.quickcleanpro.phonecleaner.presentation.screen.tools.networkspeed.NetworkSpeedPhase
import com.quickcleanpro.phonecleaner.presentation.screen.tools.networkspeed.NetworkSpeedUiState

@Composable
internal fun NetworkSpeedContentView(uiState: NetworkSpeedUiState) {
    when (uiState.phase) {
        NetworkSpeedPhase.Idle -> NetworkSpeedIdleView(uiState = uiState)
        NetworkSpeedPhase.Testing -> NetworkSpeedTestingView(uiState = uiState)
        NetworkSpeedPhase.Result -> NetworkSpeedResultView(uiState = uiState)
        NetworkSpeedPhase.Error -> NetworkSpeedErrorView(uiState = uiState)
    }
}
