package com.quickcleanpro.phonecleaner.presentation.screen.tools.networkspeed.views

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.quickcleanpro.phonecleaner.R
import com.quickcleanpro.phonecleaner.presentation.common.components.CleanXScaffoldPage
import com.quickcleanpro.phonecleaner.presentation.common.components.ToolFeatureBanners
import com.quickcleanpro.phonecleaner.presentation.common.components.buttons.CleanXPrimaryButton
import com.quickcleanpro.phonecleaner.presentation.common.route.Screen
import com.quickcleanpro.phonecleaner.presentation.screen.tools.networkspeed.NetworkSpeedUiState
import com.quickcleanpro.phonecleaner.presentation.screen.tools.networkspeed.NetworkSpeedViewModel
import com.quickcleanpro.phonecleaner.presentation.screen.tools.networkspeed.SpeedTestState

private val CardBg = Color(0xFFF6F7FB)
private val Navy = Color(0xFF1D2959)
private val NavyMuted = Color(0xA61D2959)
private val Divider15 = Color(0x261D2959)
private val CardRadius = 12.dp

@Composable
internal fun NetworkSpeedScreenState(viewModel: NetworkSpeedViewModel) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    DisposableEffect(lifecycleOwner, viewModel) {
        val observer =
            LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME) {
                    viewModel.refreshNetworkStateUntilNetworkAvailable()
                }
            }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    CleanXScaffoldPage(
        title = stringResource(R.string.network_speed),
    ) {
        NetworkInfoCard(uiState = uiState)

        Spacer(modifier = Modifier.height(16.dp))

        if (!uiState.hasNetwork) {
            EmptyStateCard(
                title = stringResource(R.string.no_network_connection),
                message = stringResource(R.string.network_speed_no_connection_desc),
            )
        } else {
            SpeedCard(uiState = uiState)
        }

        if (uiState.errorMessage != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = uiState.errorMessage.orEmpty(),
                color = Color(0xFFFF6B3D),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
            )
        }

        if (uiState.speedState == SpeedTestState.Done) {
            Spacer(modifier = Modifier.height(16.dp))
            ToolFeatureBanners(
                excludeRoutes = setOf(Screen.NetworkSpeed.route),
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (uiState.speedState) {
            SpeedTestState.Running -> {
                CleanXPrimaryButton(
                    text = stringResource(R.string.stop),
                    onClick = viewModel::stopSpeedTest,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = phaseLabel(uiState.progress.phase),
                    color = NavyMuted,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    modifier = Modifier.padding(start = 4.dp),
                )
            }
            else -> {
                CleanXPrimaryButton(
                    text = stringResource(R.string.run_speed_test),
                    onClick = viewModel::runSpeedTest,
                    enabled = uiState.hasNetwork,
                )
            }
        }

        Spacer(modifier = Modifier.height(100.dp))
    }
}

@Composable
private fun NetworkInfoCard(uiState: NetworkSpeedUiState) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = CardBg,
        shape = RoundedCornerShape(CardRadius),
    ) {
        Column(
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 20.dp),
        ) {
            InfoLine(
                label = stringResource(R.string.network_type),
                value = if (uiState.hasNetwork) uiState.networkInfo.type else "--",
            )
            InfoDivider()
            InfoLine(
                label = stringResource(R.string.wifi_name),
                value = if (uiState.hasNetwork) uiState.networkInfo.ssid else "--",
            )
            InfoDivider()
            InfoLine(
                label = stringResource(R.string.ip),
                value = if (uiState.hasNetwork) uiState.networkInfo.ip else "--",
            )
        }
    }
}

@Composable
private fun InfoLine(
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            color = Navy,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
        )
        Text(
            text = value.ifBlank { "--" },
            color = Navy,
            fontSize = 16.sp,
            fontWeight = FontWeight.Normal,
        )
    }
}

@Composable
private fun InfoDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(vertical = 16.dp),
        color = Divider15,
        thickness = 1.dp,
    )
}

@Composable
private fun SpeedCard(uiState: NetworkSpeedUiState) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = CardBg,
        shape = RoundedCornerShape(CardRadius),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (uiState.speedState != SpeedTestState.Done) {
                Image(
                    painter = painterResource(id = R.drawable.robot),
                    contentDescription = null,
                    modifier = Modifier.size(120.dp),
                    contentScale = ContentScale.Fit,
                )
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = Divider15, thickness = 1.dp)
                Spacer(modifier = Modifier.height(16.dp))
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                SpeedColumn(
                    label = stringResource(R.string.download),
                    value = uiState.downloadLabel,
                )
                VerticalSpeedDivider()
                SpeedColumn(
                    label = stringResource(R.string.upload),
                    value = uiState.uploadLabel,
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = Divider15, thickness = 1.dp)
            Spacer(modifier = Modifier.height(16.dp))
            InfoLine(
                label = "Latency",
                value = "${uiState.latencyLabel} ms",
            )
            if (uiState.speed?.measured == false) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Using link speed fallback",
                    color = NavyMuted,
                    fontSize = 13.sp,
                )
            }
        }
    }
}

@Composable
private fun SpeedColumn(
    label: String,
    value: String,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = value,
                color = Navy,
                fontSize = 22.sp,
                fontWeight = FontWeight.Normal,
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = stringResource(R.string.mbps),
                color = Navy,
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                modifier = Modifier.padding(bottom = 3.dp),
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            color = NavyMuted,
            fontSize = 16.sp,
            fontWeight = FontWeight.Normal,
        )
    }
}

@Composable
private fun VerticalSpeedDivider() {
    Box(
        modifier =
            Modifier
                .width(1.dp)
                .height(64.dp)
                .background(Divider15),
    )
}

@Composable
private fun EmptyStateCard(
    title: String,
    message: String,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = CardBg,
        shape = RoundedCornerShape(CardRadius),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = title,
                color = Navy,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = message,
                color = NavyMuted,
                fontSize = 15.sp,
                lineHeight = 20.sp,
            )
        }
    }
}

private fun phaseLabel(phase: String): String =
    when (phase) {
        "latency" -> "Testing latency..."
        "download" -> "Testing download..."
        "upload" -> "Testing upload..."
        else -> "Testing..."
    }
