package com.quickcleanpro.phonecleaner.presentation.screen.toolbox

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quickcleanpro.phonecleaner.R
import com.quickcleanpro.phonecleaner.presentation.common.CleanXBackground
import com.quickcleanpro.phonecleaner.presentation.common.CleanXBlue
import com.quickcleanpro.phonecleaner.presentation.common.CleanXMutedText
import com.quickcleanpro.phonecleaner.presentation.common.CleanXPrimaryTabs
import com.quickcleanpro.phonecleaner.presentation.common.CleanXTabItem
import com.quickcleanpro.phonecleaner.presentation.common.CleanXText
import com.quickcleanpro.phonecleaner.presentation.common.RotatingRingAnimation
import com.quickcleanpro.phonecleaner.domain.model.toolbox.AppUsageInfo
import com.quickcleanpro.phonecleaner.domain.model.toolbox.NetworkDeviceInfo
import com.quickcleanpro.phonecleaner.domain.model.toolbox.NetworkUsageInfo
import com.quickcleanpro.phonecleaner.domain.model.toolbox.NetworkUsageApp
import com.quickcleanpro.phonecleaner.domain.model.toolbox.UNKNOWN_NETWORK_TRAFFIC_PACKAGE
import java.util.concurrent.ConcurrentHashMap
import java.util.Locale

internal data class NetworkUsageDisplayItem(
    val appName: String,
    val packageName: String,
    val rxBytes: Long,
    val txBytes: Long,
    val totalBytes: Long,
    val isAggregate: Boolean = false
) {
    companion object {
        fun fromApp(app: NetworkUsageApp): NetworkUsageDisplayItem =
            NetworkUsageDisplayItem(
                appName = app.appName,
                packageName = app.packageName,
                rxBytes = app.rxBytes,
                txBytes = app.txBytes,
                totalBytes = app.totalBytes,
                isAggregate = app.packageName == UNKNOWN_NETWORK_TRAFFIC_PACKAGE
            )

        fun systemTraffic(totalBytes: Long): NetworkUsageDisplayItem =
            NetworkUsageDisplayItem(
                appName = "System & unknown traffic",
                packageName = UNKNOWN_NETWORK_TRAFFIC_PACKAGE,
                rxBytes = totalBytes,
                txBytes = 0L,
                totalBytes = totalBytes,
                isAggregate = true
            )
    }
}

internal fun buildNetworkUsageDisplayItems(
    apps: List<NetworkUsageApp>,
    selectedTotalBytes: Long
): List<NetworkUsageDisplayItem> {
    if (selectedTotalBytes <= 0L) return emptyList()

    val items = apps
        .filter { it.totalBytes > 0L }
        .map { NetworkUsageDisplayItem.fromApp(it) }
        .sortedByDescending { it.totalBytes }

    val normalizedItems = mutableListOf<NetworkUsageDisplayItem>()
    var remainingBytes = selectedTotalBytes
    for (item in items) {
        if (remainingBytes <= 0L) break
        val displayBytes = item.totalBytes.coerceAtMost(remainingBytes)
        normalizedItems += item.copy(
            rxBytes = displayBytes,
            txBytes = 0L,
            totalBytes = displayBytes
        )
        remainingBytes -= displayBytes
    }

    if (remainingBytes > 0L) {
        normalizedItems += NetworkUsageDisplayItem.systemTraffic(remainingBytes)
    }

    val aggregateBytes = normalizedItems
        .filter { it.isAggregate }
        .sumOf { it.totalBytes }
    val nonAggregateItems = normalizedItems.filterNot { it.isAggregate }

    return (nonAggregateItems + listOfNotNull(
        aggregateBytes
            .takeIf { it > 0L }
            ?.let { NetworkUsageDisplayItem.systemTraffic(it) }
    )).sortedByDescending { it.totalBytes }
}

internal enum class AppStopButtonState(
    val enabled: Boolean,
    val backgroundAlpha: Float
) {
    Running(enabled = true, backgroundAlpha = 1f),
    Closed(enabled = false, backgroundAlpha = 0.65f)
}

internal fun appStopButtonStateForPackage(
    packageName: String,
    runningPackages: Set<String>
): AppStopButtonState =
    if (packageName in runningPackages) {
        AppStopButtonState.Running
    } else {
        AppStopButtonState.Closed
    }

@Composable
internal fun NetworkInfoCard(network: NetworkInfo) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(154.dp),
        color = Color.White,
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
            InfoLine(stringResource(R.string.network_type), network.type)
            InfoDivider()
            InfoLine(stringResource(R.string.wifi_name), network.ssid)
            InfoDivider()
            InfoLine(stringResource(R.string.ip), network.ip)
        }
    }
}

@Composable
internal fun SpeedMetricCard(
    download: String,
    upload: String,
    isDownloadTesting: Boolean = false,
    isUploadTesting: Boolean = false
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(122.dp),
        color = Color.White,
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SpeedMetric(
                direction = "download",
                value = download,
                label = stringResource(R.string.download),
                isTesting = isDownloadTesting,
                modifier = Modifier.weight(1f)
            )
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(58.dp)
                    .background(Color(0xFFD9DEE6))
            )
            SpeedMetric(
                direction = "upload",
                value = upload,
                label = stringResource(R.string.upload),
                isTesting = isUploadTesting,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
internal fun SpeedMetric(
    direction: String,
    value: String,
    label: String,
    isTesting: Boolean,
    modifier: Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.height(44.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            if (isTesting) {
                RotatingRingAnimation(
                    modifier = Modifier
                        .size(34.dp)
                        .padding(bottom = 4.dp),
                    ringWidth = 4.dp,
                    ringColor = CleanXBlue,
                    backgroundColor = CleanXBlue.copy(alpha = 0.16f)
                )
            } else {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        value,
                        color = CleanXText,
                        fontSize = if (value == "--") 26.sp else 36.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        stringResource(R.string.mbps),
                        color = CleanXText,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            TransferBubble(direction)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "$label\n${stringResource(R.string.mbps)}",
                color = CleanXMutedText,
                fontSize = 20.sp,
                lineHeight = 24.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
internal fun TransferBubble(direction: String) {
    Box(
        modifier = Modifier
            .size(22.dp)
            .clip(RoundedCornerShape(50)),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = if (direction == "download") painterResource(R.drawable.ic_download) else painterResource(R.drawable.ic_upload),
            contentDescription = null,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
internal fun ActionCard(
    iconColor: Color,
    title: String,
    description: String,
    action: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(98.dp)
            .clickable { onClick() },
        color = Color.White,
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SquareToolIcon(color = iconColor)
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = CleanXText, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(description, color = CleanXMutedText, fontSize = 14.sp)
            }
            Text(
                action,
                color = CleanXBlue,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Bottom).padding(bottom = 18.dp)
            )
        }
    }
}

@Composable
internal fun SquareToolIcon(color: Color) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(color),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(28.dp)) {
            drawRoundRect(Color.White, Offset(size.width * 0.18f, size.height * 0.18f), Size(size.width * 0.64f, size.height * 0.58f), CornerRadius(4.dp.toPx(), 4.dp.toPx()))
            drawLine(Color.White, Offset(size.width * 0.28f, size.height * 0.08f), Offset(size.width * 0.72f, size.height * 0.08f), strokeWidth = 4.dp.toPx(), cap = StrokeCap.Round)
            drawLine(Color.White.copy(alpha = 0.8f), Offset(size.width * 0.38f, size.height * 0.40f), Offset(size.width * 0.62f, size.height * 0.62f), strokeWidth = 3.dp.toPx(), cap = StrokeCap.Round)
            drawLine(Color.White.copy(alpha = 0.8f), Offset(size.width * 0.62f, size.height * 0.40f), Offset(size.width * 0.38f, size.height * 0.62f), strokeWidth = 3.dp.toPx(), cap = StrokeCap.Round)
        }
    }
}

@Composable
internal fun ScanResultCard(ssid: String, lastScan: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)) {
            Text(stringResource(R.string.wifi_scan_result), color = CleanXText, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            InfoLine("SSID", ssid)
            InfoDivider()
            InfoLine(stringResource(R.string.last_scan), lastScan)
        }
    }
}

@Composable
internal fun ScanDetailsCard(
    scanState: NetworkScanState,
    completedDetailCount: Int
) {
    val rows = listOf(
        R.string.wifi_auth,
        R.string.arp_spoofing,
        R.string.ssl_stripping,
        R.string.ssl_splitting,
        R.string.dns_spoofing,
        R.string.devices
    )
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)) {
            Text(stringResource(R.string.details), color = CleanXText, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            rows.forEachIndexed { index, row ->
                val status = when {
                    scanState == NetworkScanState.Idle -> ScanDetailStatus.Unknown
                    scanState == NetworkScanState.Running && index == completedDetailCount -> ScanDetailStatus.Scanning
                    scanState == NetworkScanState.Running && index < completedDetailCount -> ScanDetailStatus.Done
                    scanState == NetworkScanState.Done -> ScanDetailStatus.Done
                    else -> ScanDetailStatus.Unknown
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(stringResource(row), color = CleanXMutedText, fontSize = 16.sp, modifier = Modifier.weight(1f))
                    ScanDetailStatusIcon(status = status)
                }
                if (index != rows.lastIndex) InfoDivider()
            }
        }
    }
}

@Composable
internal fun ScanDetailStatusIcon(status: ScanDetailStatus) {
    val tag = when (status) {
        ScanDetailStatus.Unknown -> "ScanDetailStatusUnknown"
        ScanDetailStatus.Scanning -> "ScanDetailStatusScanning"
        ScanDetailStatus.Done -> "ScanDetailStatusDone"
        ScanDetailStatus.Empty -> "ScanDetailStatusEmpty"
    }
    when (status) {
        ScanDetailStatus.Unknown -> Icon(
            Icons.AutoMirrored.Filled.Help,
            contentDescription = null,
            tint = Color(0xFFFF3F42),
            modifier = Modifier.size(21.dp).testTag(tag)
        )
        ScanDetailStatus.Scanning -> RotatingRingAnimation(
            modifier = Modifier.size(21.dp).testTag(tag),
            ringWidth = 2.2.dp,
            ringColor = CleanXBlue,
            backgroundColor = CleanXBlue.copy(alpha = 0.18f),
            animationDurationMillis = 800,
            arcLength = 180f
        )
        ScanDetailStatus.Done -> CheckBubble(checked = true, modifier = Modifier.testTag(tag))
        ScanDetailStatus.Empty -> CheckBubble(checked = false, modifier = Modifier.testTag(tag))
    }
}

@Composable
internal fun CheckBubble(checked: Boolean, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(20.dp)
            .clip(RoundedCornerShape(50))
            .background(if (checked) CleanXBlue else Color.Transparent),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            if (checked) {
                drawLine(Color.White, Offset(size.width * 0.28f, size.height * 0.52f), Offset(size.width * 0.43f, size.height * 0.68f), strokeWidth = 2.4.dp.toPx(), cap = StrokeCap.Round)
                drawLine(Color.White, Offset(size.width * 0.43f, size.height * 0.68f), Offset(size.width * 0.72f, size.height * 0.34f), strokeWidth = 2.4.dp.toPx(), cap = StrokeCap.Round)
            } else {
                drawCircle(Color(0xFF8EDFFF), radius = size.minDimension * 0.42f, style = Stroke(width = 2.dp.toPx()))
            }
        }
    }
}

@Composable
internal fun DevicesSummaryCard(devices: List<NetworkDeviceInfo>, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        color = Color.White,
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    stringResource(R.string.devices_count, devices.size),
                    color = CleanXText,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = CleanXText, modifier = Modifier.size(28.dp))
            }
            Spacer(modifier = Modifier.height(12.dp))
            val preview = devices.take(2)
            if (preview.isEmpty()) {
                DeviceSummaryRow(stringResource(R.string.no_device_found), "--")
            } else {
                preview.forEachIndexed { index, device ->
                    DeviceSummaryRow(device.ip, device.hostName)
                    if (index != preview.lastIndex) InfoDivider()
                }
            }
        }
    }
}

@Composable
internal fun DeviceSummaryRow(left: String, right: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(42.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ServerIcon()
        Spacer(modifier = Modifier.width(8.dp))
        Text(left, color = CleanXMutedText, fontSize = 16.sp)
        Spacer(modifier = Modifier.width(8.dp))
        Text(right, color = CleanXText,
            fontSize = 16.sp,
            modifier = Modifier.weight(1f),
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Right
            )
    }
}

@Composable
internal fun DeviceCard(device: NetworkDeviceInfo) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
            DeviceSummaryRow(device.ip, device.hostName)
            InfoDivider()
            InfoLine(stringResource(R.string.vendor), stringResource(R.string.unknown).lowercase())
            InfoDivider()
            InfoLine(stringResource(R.string.name), device.hostName)
            InfoDivider()
            InfoLine(stringResource(R.string.mac_address), device.macAddress)
        }
    }
}

@Composable
internal fun ServerIcon() {
    Canvas(modifier = Modifier.size(18.dp)) {
        repeat(2) { index ->
            val top = size.height * (0.12f + index * 0.42f)
            drawRect(Color(0xFF293344), Offset(size.width * 0.12f, top), Size(size.width * 0.76f, size.height * 0.30f))
            drawCircle(Color.White, radius = 1.2.dp.toPx(), center = Offset(size.width * 0.30f, top + size.height * 0.15f))
            drawLine(Color.White.copy(alpha = 0.8f), Offset(size.width * 0.48f, top + size.height * 0.15f), Offset(size.width * 0.74f, top + size.height * 0.15f), strokeWidth = 1.dp.toPx())
        }
    }
}

@Composable
internal fun TwoTabHeader(
    leftTitle: String,
    leftValue: String,
    rightTitle: String,
    rightValue: String,
    selected: Int,
    onSelected: (Int) -> Unit
) {
    CleanXPrimaryTabs(
        items = listOf(
            CleanXTabItem(title = leftTitle, value = leftValue),
            CleanXTabItem(title = rightTitle, value = rightValue)
        ),
        selectedIndex = selected,
        onSelected = onSelected
    )
}

@Composable
internal fun UsageSummaryCard(usage: NetworkUsageInfo, selected: Int) {
    val rx = if (selected == 0) usage.cellularRxBytes else usage.wifiRxBytes
    val tx = if (selected == 0) usage.cellularTxBytes else usage.wifiTxBytes
    val total = rx + tx
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(86.dp),
        color = Color.White,
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SummaryMetric(formatNetworkBytes(total), stringResource(if (usage.isToday) R.string.total_usage_today else R.string.total_usage_since_boot), Modifier.weight(1f))
            DividerVertical()
            SummaryMetric(formatNetworkBytes(rx), stringResource(if (usage.isToday) R.string.downloads_today else R.string.downloads_since_boot), Modifier.weight(1f))
            DividerVertical()
            SummaryMetric(formatNetworkBytes(tx), stringResource(if (usage.isToday) R.string.uploads_today else R.string.uploads_since_boot), Modifier.weight(1f))
        }
    }
}

@Composable
internal fun SummaryMetric(value: String, label: String, modifier: Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = CleanXText, fontSize = 15.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(5.dp))
        Text(label, color = CleanXMutedText, fontSize = 12.sp, lineHeight = 14.sp, textAlign = TextAlign.Center)
    }
}

@Composable
internal fun UsageAppsCard(
    items: List<NetworkUsageDisplayItem>
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)) {
            val maxUsage = items.maxOfOrNull { it.totalBytes }?.coerceAtLeast(1L) ?: 1L
            items.forEachIndexed { index, item ->
                NetworkUsageAppRow(
                    item = item,
                    progress = (item.totalBytes.toFloat() / maxUsage).coerceIn(0.08f, 1f),
                    color = usageColor(index),
                )
                if (index != items.lastIndex) {
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }
}

@Composable
private fun NetworkUsageAppRow(
    item: NetworkUsageDisplayItem,
    progress: Float,
    color: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        PackageAppIcon(
            packageName = item.packageName,
            fallbackText = if (item.isAggregate) "S" else item.appName.take(1).ifBlank { "A" },
            color = color,
            isAggregate = item.isAggregate
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = if (item.isAggregate) stringResource(R.string.system_unknown_traffic) else item.appName,
                color = CleanXText,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(7.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color(0xFFE9E9E9))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .height(4.dp)
                        .background(CleanXBlue)
                )
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = formatNetworkBytes(item.totalBytes),
                color = CleanXText,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
        }
    }
}

@Composable
internal fun PermissionPromptCard(
    title: String,
    description: String,
    action: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = CleanXText, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(5.dp))
                Text(description, color = CleanXMutedText, fontSize = 14.sp, lineHeight = 17.sp)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Button(
                onClick = onClick,
                modifier = Modifier.height(36.dp),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = CleanXBlue),
                contentPadding = PaddingValues(horizontal = 18.dp)
            ) {
                Text(action, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
internal fun AppUsageTabsHeader(
    selectedTab: AppUsageMetricTab,
    onSelected: (AppUsageMetricTab) -> Unit
) {
    CleanXPrimaryTabs(
        items = listOf(
            CleanXTabItem(title = stringResource(AppUsageMetricTab.Duration.titleRes)),
            CleanXTabItem(title = stringResource(AppUsageMetricTab.LaunchCount.titleRes))
        ),
        selectedIndex = AppUsageMetricTab.values().indexOf(selectedTab).coerceAtLeast(0),
        onSelected = { index ->
            AppUsageMetricTab.values().getOrNull(index)?.let(onSelected)
        }
    )
}

@Composable
internal fun AppUsageRowsContent(
    items: List<AppUsageDisplayItem>,
    onStopApp: (String) -> Unit
) {
    if (items.isEmpty()) {
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Text(
                text = stringResource(R.string.no_usage_data_available),
                color = CleanXMutedText,
                fontSize = 16.sp
            )
        }
    } else {
        items.forEachIndexed { index, item ->
            AppUsageRow(
                iconText = item.iconText,
                packageName = item.packageName,
                name = item.appName,
                value = item.value,
                progress = item.progress,
                color = usageColor(item.colorIndex),
                showStop = true,
                stopState = item.stopButtonState,
                onStop = { onStopApp(item.packageName) }
            )
            if (index != items.lastIndex) {
                Spacer(modifier = Modifier.height(18.dp))
            }
        }
    }
}

@Composable
internal fun AppUsageRow(
    iconText: String,
    packageName: String? = null,
    name: String,
    value: String,
    progress: Float,
    color: Color,
    showStop: Boolean = false,
    stopLabel: String? = null,
    stopState: AppStopButtonState = AppStopButtonState.Running,
    onStop: () -> Unit = {}
) {
    val resolvedStopLabel = stopLabel ?: stringResource(R.string.stop)
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        PackageAppIcon(packageName = packageName, fallbackText = iconText, color = color)
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(name, color = CleanXText, fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                if (!showStop) Text(value, color = CleanXMutedText, fontSize = 12.sp)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(4.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Color(0xFFE9E9E9))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progress)
                            .height(4.dp)
                            .background(CleanXBlue)
                    )
                }
                if (showStop) {
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(value, color = CleanXMutedText, fontSize = 12.sp)
                }
            }
        }
        if (showStop) {
            Spacer(modifier = Modifier.width(12.dp))
            val stopButtonColor = Color(0xFF1AA7EC)
            Button(
                onClick = onStop,
                enabled = stopState.enabled,
                modifier = Modifier
                    .size(width = 64.dp, height = 36.dp)
                    .testTag("AppStopButton${stopState.name}"),
                contentPadding = PaddingValues(0.dp),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = stopButtonColor.copy(alpha = stopState.backgroundAlpha),
                    disabledContainerColor = stopButtonColor.copy(alpha = stopState.backgroundAlpha),
                    disabledContentColor = Color.White
                )
            ) {
                Text(resolvedStopLabel, fontSize = 14.sp)
            }
        }
    }
}

@Composable
internal fun AppIcon(text: String, color: Color) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(RoundedCornerShape(50))
            .background(color),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
internal fun PackageAppIcon(
    packageName: String?,
    fallbackText: String,
    color: Color,
    isAggregate: Boolean = false
) {
    if (isAggregate) {
        AppIcon(text = fallbackText, color = color)
        return
    }

    val context = LocalContext.current
    val appIcon = remember(packageName) {
        packageName?.let { loadPackageIconBitmap(context, it) }
    }

    Box(
        modifier = Modifier
            .size(44.dp)
            .background(Color(0xFFF0F3FA)),
        contentAlignment = Alignment.Center
    ) {
        if (appIcon != null) {
            Image(
                bitmap = appIcon,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                contentScale = ContentScale.Fit
            )
        } else {
            Image(
                painter = painterResource(id = R.drawable.app_logo),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(9.dp),
                contentScale = ContentScale.Fit
            )
        }
    }
}


private val packageIconBitmapCache = ConcurrentHashMap<String, ImageBitmap>()


private fun loadPackageIconBitmap(context: android.content.Context, packageName: String): ImageBitmap? {
    packageIconBitmapCache[packageName]?.let { return it }

    val packageManager = context.packageManager
    val selfIcon = runCatching { packageManager.getApplicationIcon(packageName) }.getOrNull()
    if (selfIcon != null) {
        return cachePackageIcon(packageName, selfIcon.toBitmap().asImageBitmap())
    }

    val launchIcon = runCatching {
        packageManager.getLaunchIntentForPackage(packageName)
            ?.resolveActivity(packageManager)
            ?.let { componentName ->
                packageManager.getActivityInfo(componentName, 0).loadIcon(packageManager)
            }
    }.getOrNull()
    if (launchIcon != null) {
        return cachePackageIcon(packageName, launchIcon.toBitmap().asImageBitmap())
    }

    val desktopIcon = runCatching {
        packageManager.getLaunchIntentForPackage(packageName)?.let { intent ->
            intent.resolveActivity(packageManager)?.let { componentName ->
                packageManager.getActivityIcon(componentName)
            }
        }
    }.getOrNull()
    if (desktopIcon != null) {
        return cachePackageIcon(packageName, desktopIcon.toBitmap().asImageBitmap())
    }

    val fallbackIcon = runCatching {
        packageManager.getApplicationInfo(packageName, 0).loadIcon(packageManager)
    }.getOrNull()
    return fallbackIcon?.toBitmap()?.asImageBitmap()?.let { cachePackageIcon(packageName, it) }
}


private fun cachePackageIcon(packageName: String, bitmap: ImageBitmap): ImageBitmap {
    packageIconBitmapCache[packageName] = bitmap
    return bitmap
}

private fun Drawable.toBitmap(): Bitmap {
    if (this is BitmapDrawable && bitmap != null) return bitmap
    val width = intrinsicWidth.takeIf { it > 0 } ?: 96
    val height = intrinsicHeight.takeIf { it > 0 } ?: 96
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(bitmap)
    setBounds(0, 0, canvas.width, canvas.height)
    draw(canvas)
    return bitmap
}



@Composable
internal fun AppUsageChartCard(label: String) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(172.dp),
        color = Color.White,
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            DonutChart(label = label, modifier = Modifier.size(112.dp))
            Spacer(modifier = Modifier.width(34.dp))
            Column(verticalArrangement = Arrangement.spacedBy(17.dp)) {
                LegendDot(Color(0xFFFF4D5D), stringResource(R.string.app_usage_legend_storage_clean))
                LegendDot(Color(0xFF9B3DF4), stringResource(R.string.app_usage_legend_one_ui_home))
                LegendDot(Color(0xFF70E56E), stringResource(R.string.app_usage_legend_feishu))
                LegendDot(Color(0xFF4B6DFF), stringResource(R.string.other))
            }
        }
    }
}

@Composable
internal fun DonutChart(label: String, modifier: Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val stroke = 25.dp.toPx()
            val arcSize = Size(size.width - stroke, size.height - stroke)
            val topLeft = Offset(stroke / 2f, stroke / 2f)
            drawArc(Color(0xFFFF4D5D), -4f, 95f, false, topLeft, arcSize, style = Stroke(stroke, cap = StrokeCap.Butt))
            drawArc(Color(0xFF9B3DF4), 91f, 58f, false, topLeft, arcSize, style = Stroke(stroke, cap = StrokeCap.Butt))
            drawArc(Color(0xFF70E56E), 149f, 118f, false, topLeft, arcSize, style = Stroke(stroke, cap = StrokeCap.Butt))
            drawArc(Color(0xFF4B6DFF), 267f, 89f, false, topLeft, arcSize, style = Stroke(stroke, cap = StrokeCap.Butt))
        }
        Text(label, color = CleanXText, fontSize = 18.sp, lineHeight = 21.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
    }
}

internal fun usageColor(index: Int): Color =
    listOf(
        Color(0xFFFF4D5D),
        Color(0xFF9B3DF4),
        Color(0xFF70E56E),
        Color(0xFF4B6DFF),
        Color(0xFFFF8522),
        Color(0xFF22A9E8)
    )[index % 6]

@Composable
internal fun LegendDot(color: Color, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(5.dp).clip(RoundedCornerShape(50)).background(color))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text, color = CleanXText, fontSize = 14.sp)
    }
}

internal enum class SpeedState {
    Idle,
    Running,
    Done
}

internal enum class NetworkScanState {
    Idle,
    Running,
    Done
}

internal enum class ScanDetailStatus {
    Unknown,
    Scanning,
    Done,
    Empty
}
