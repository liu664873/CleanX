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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quickcleanpro.phonecleaner.R
import com.quickcleanpro.phonecleaner.presentation.common.components.CleanXStatusBadge
import com.quickcleanpro.phonecleaner.presentation.common.components.CleanXStatusBadgeState
import com.quickcleanpro.phonecleaner.presentation.common.components.animations.SemiCircularGauge
import com.quickcleanpro.phonecleaner.presentation.screen.tools.networkspeed.NetworkSpeedUiState

@Composable
internal fun NetworkSpeedInfoCard(uiState: NetworkSpeedUiState) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = NetworkSpeedCardBg,
        shape = RoundedCornerShape(NetworkSpeedCardRadius),
    ) {
        Column(
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 20.dp),
        ) {
            NetworkSpeedInfoLine(
                label = stringResource(R.string.network_type),
                value = if (uiState.hasNetwork) uiState.networkInfo.type else "--",
            )
            NetworkSpeedInfoDivider()
            NetworkSpeedInfoLine(
                label = stringResource(R.string.wifi_name),
                value = if (uiState.hasNetwork) uiState.networkInfo.ssid else "--",
            )
            NetworkSpeedInfoDivider()
            NetworkSpeedInfoLine(
                label = stringResource(R.string.ip),
                value = if (uiState.hasNetwork) uiState.networkInfo.ip else "--",
            )
        }
    }
}


@Composable
internal fun NetworkSpeedMetricCard(
    uiState: NetworkSpeedUiState,
    showActiveBadges: Boolean = false,
    showGauge: Boolean = false,
    gaugeAnimating: Boolean = false,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = NetworkSpeedCardBg,
        shape = RoundedCornerShape(NetworkSpeedCardRadius),
    ) {
        Column(
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (showGauge) {
                SemiCircularGauge(
                    modifier = Modifier.size(width = 166.dp, height = 126.dp),
                    isAnimating = gaugeAnimating,
                    arcStartColor = Color(0xFF4179FC),   // ② 蓝色渐变起点
                    arcEndColor = Color(0xFF6B9BFF),     // ③ 蓝色渐变终点（更亮）
                    needleColor = Color(0xFF4179FC),
                    tickColor = Color(0xFFB0C4DE)        // ⑤ 浅灰蓝刻度
                )
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = NetworkSpeedDivider, thickness = 1.dp)
            }
            Spacer(modifier = Modifier.height(8.dp))
            NetworkSpeedMetricsRow(
                downloadValue = uiState.downloadLabel,
                uploadValue = uiState.uploadLabel,
                showDownloadBadge = showActiveBadges && uiState.progress.downloadMbps == null,
                showUploadBadge = showActiveBadges && uiState.progress.uploadMbps == null,
                valueFontSize = 30,
            )
            // 保留底部 fallback 提示，与动画条件一致
            if (uiState.speed?.measured == false) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Using link speed fallback",
                    color = NetworkSpeedNavyMuted,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
internal fun NetworkSpeedEmptyCard(
    title: String,
    message: String,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = NetworkSpeedCardBg,
        shape = RoundedCornerShape(NetworkSpeedCardRadius),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = title,
                color = NetworkSpeedNavy,
                fontSize = 18.sp,
                lineHeight = 24.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = message,
                color = NetworkSpeedNavyMuted,
                fontSize = 15.sp,
                lineHeight = 20.sp,
            )
        }
    }
}

@Composable
internal fun NetworkSpeedErrorText(message: String?) {
    if (message.isNullOrBlank()) return

    Text(
        text = message,
        color = NetworkSpeedDanger,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        fontWeight = FontWeight.Medium,
        modifier = Modifier.padding(horizontal = 4.dp),
    )
}

@Composable
private fun NetworkSpeedInfoLine(
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
            color = NetworkSpeedNavy,
            fontSize = 18.sp,
            lineHeight = 24.sp,
            fontWeight = FontWeight.Normal,
        )
        Text(
            text = value.ifBlank { "--" },
            color = NetworkSpeedNavy,
            fontSize = 16.sp,
            lineHeight = 24.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.End,
            modifier = Modifier.padding(start = 16.dp),
        )
    }
}

@Composable
private fun NetworkSpeedInfoDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(vertical = 16.dp),
        color = NetworkSpeedDivider,
        thickness = 1.dp,
    )
}

@Composable
private fun NetworkSpeedMetricsRow(
    downloadValue: String,
    uploadValue: String,
    showDownloadBadge: Boolean,
    showUploadBadge: Boolean,
    valueFontSize: Int,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        NetworkSpeedMetricColumn(
            label = stringResource(R.string.download),
            value = downloadValue,
            showActiveBadge = showDownloadBadge,
            valueFontSize = valueFontSize,
        )
        NetworkSpeedVerticalDivider()
        NetworkSpeedMetricColumn(
            label = stringResource(R.string.upload),
            value = uploadValue,
            showActiveBadge = showUploadBadge,
            valueFontSize = valueFontSize,
        )
    }
}

@Composable
private fun NetworkSpeedMetricColumn(
    label: String,
    value: String,
    showActiveBadge: Boolean,
    valueFontSize: Int,
) {
    Column(
        modifier = Modifier.width(120.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            modifier = Modifier.height(42.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            if (showActiveBadge) {
                CleanXStatusBadge(
                    state = CleanXStatusBadgeState.Active,
                    size = 26.dp,
                    ringWidth = 2.dp,
                )
            } else {
                Text(
                    text = value.speedPlaceholder(),
                    color = NetworkSpeedNavy,
                    fontSize = valueFontSize.sp,
                    lineHeight = 34.sp,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = stringResource(R.string.mbps),
                    color = NetworkSpeedNavy,
                    fontSize = 18.sp,
                    lineHeight = 22.sp,
                    fontWeight = FontWeight.Normal,
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "$label ${stringResource(R.string.mbps)}",
            color = NetworkSpeedNavyMuted,
            fontSize = 14.sp,
            lineHeight = 18.sp,
            fontWeight = FontWeight.Normal,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun NetworkSpeedVerticalDivider() {
    Box(
        modifier =
            Modifier
                .width(1.dp)
                .height(24.dp)
                .background(NetworkSpeedDivider),
    )
}

private fun String.speedPlaceholder(): String = if (this == "--") "-" else this
