package com.quickcleanpro.phonecleaner.presentation.screen.tools.networkspeed.views

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quickcleanpro.phonecleaner.presentation.screen.tools.networkspeed.NetworkSpeedUiState

@Composable
internal fun NetworkSpeedTestingView(uiState: NetworkSpeedUiState) {
    NetworkSpeedInfoCard(uiState = uiState)
    Spacer(modifier = Modifier.height(20.dp))
    NetworkSpeedMetricCard(
        uiState = uiState,
        showActiveBadges = true,
        showGauge = true,
        gaugeAnimating = true,
    )
    Spacer(modifier = Modifier.height(8.dp))
    Text(
        modifier = Modifier.fillMaxWidth(),
        text = networkSpeedPhaseLabel(uiState.progress.phase),
        color = NetworkSpeedNavyMuted,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        fontWeight = FontWeight.Normal,
        textAlign = TextAlign.Center
    )
    Spacer(modifier = Modifier.height(96.dp))
}

private fun networkSpeedPhaseLabel(phase: String): String =
    when (phase) {
        "latency" -> "Testing latency..."
        "download" -> "Testing download..."
        "upload" -> "Testing upload..."
        else -> "Testing..."
    }
