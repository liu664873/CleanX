package com.quickcleanpro.phonecleaner.presentation.screen.tools.networkspeed.views

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.quickcleanpro.phonecleaner.presentation.common.components.AllToolFeatures
import com.quickcleanpro.phonecleaner.presentation.common.components.ToolFeatureBanners
import com.quickcleanpro.phonecleaner.presentation.common.route.Screen
import com.quickcleanpro.phonecleaner.presentation.screen.tools.networkspeed.NetworkSpeedUiState

@Composable
internal fun NetworkSpeedResultView(uiState: NetworkSpeedUiState) {
    NetworkSpeedMetricCard(
        uiState = uiState,
        showRobot = false,
    )
    Spacer(modifier = Modifier.height(40.dp))
    ToolFeatureBanners(
        features =
            AllToolFeatures.filter {
                it.screen == Screen.DeviceInfo || it.screen == Screen.NetworkScan
            },
    )
    Spacer(modifier = Modifier.height(32.dp))
}
