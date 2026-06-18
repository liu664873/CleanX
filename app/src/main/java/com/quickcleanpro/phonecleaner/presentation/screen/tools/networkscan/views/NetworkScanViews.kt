package com.quickcleanpro.phonecleaner.presentation.screen.tools.networkscan.views

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import com.quickcleanpro.phonecleaner.domain.model.toolbox.NetworkDeviceInfo
import com.quickcleanpro.phonecleaner.domain.model.toolbox.NetworkScanResult
import com.quickcleanpro.phonecleaner.presentation.common.components.CleanXCheckBadge
import com.quickcleanpro.phonecleaner.presentation.common.components.CleanXScaffoldPage
import com.quickcleanpro.phonecleaner.presentation.common.components.buttons.CleanXPrimaryButton
import com.quickcleanpro.phonecleaner.presentation.common.components.styles.CleanXBlue
import com.quickcleanpro.phonecleaner.presentation.common.components.styles.CleanXButtonHeight
import com.quickcleanpro.phonecleaner.presentation.common.components.styles.CleanXButtonShape
import com.quickcleanpro.phonecleaner.presentation.common.permission.PermissionGatePresets
import com.quickcleanpro.phonecleaner.presentation.screen.tools.networkscan.NetworkScanState
import com.quickcleanpro.phonecleaner.presentation.screen.tools.networkscan.NetworkScanUiState
import com.quickcleanpro.phonecleaner.presentation.screen.tools.networkscan.NetworkScanViewModel

private val CardBg = Color(0xFFF6F7FB)
private val Navy = Color(0xFF1D2959)
private val NavyMuted = Color(0xA61D2959)
private val Divider15 = Color(0x261D2959)
private val CardRadius = 12.dp

@Composable
internal fun NetworkScanScreenState(
    viewModel: NetworkScanViewModel,
    onDevicesClick: () -> Unit,
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    DisposableEffect(lifecycleOwner, viewModel) {
        val observer =
            LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME) {
                    viewModel.refreshNetworkStateUntilWifiConnected()
                }
            }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    CleanXScaffoldPage(
        title = stringResource(R.string.network_scan),
        permissionGateConfig = PermissionGatePresets.location(),
    ) {
        val scan = uiState.scan
        WifiScanResultCard(uiState = uiState)

        if (uiState.scanState == NetworkScanState.Done && scan != null) {
            Spacer(modifier = Modifier.height(16.dp))
            DevicesSummaryCard(
                scan = scan,
                onDevicesClick = onDevicesClick,
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        ScanDetailsCard(uiState = uiState)

        if (uiState.errorMessage != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = uiState.errorMessage.orEmpty(),
                color = Color(0xFFFF6B3D),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        when {
            !uiState.hasWifi -> {
                EmptyStateCard(
                    title = stringResource(R.string.wifi_not_connected),
                    message = stringResource(R.string.network_scan_no_wifi_desc),
                )
                Spacer(modifier = Modifier.height(16.dp))
                CleanXPrimaryButton(
                    text = stringResource(R.string.scan_again),
                    onClick = viewModel::refreshNetworkStateUntilWifiConnected,
                )
            }
            uiState.scanState == NetworkScanState.Running -> {
                CleanXPrimaryButton(
                    text = stringResource(R.string.scanning_devices),
                    onClick = {},
                    enabled = false,
                )
            }
            uiState.scanState == NetworkScanState.Done -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    OutlinedButton(
                        onClick = viewModel::refreshNetworkStateUntilWifiConnected,
                        modifier =
                            Modifier
                                .weight(1f)
                                .height(CleanXButtonHeight),
                        shape = CleanXButtonShape,
                        colors =
                            ButtonDefaults.outlinedButtonColors(
                                containerColor = Color.White,
                                contentColor = CleanXBlue,
                            ),
                    ) {
                        Text(
                            text = stringResource(R.string.switch_wifi),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                    CleanXPrimaryButton(
                        text = stringResource(R.string.scan_again),
                        onClick = viewModel::startScan,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
            else -> {
                CleanXPrimaryButton(
                    text = stringResource(R.string.scan_wifi),
                    onClick = viewModel::startScan,
                )
            }
        }

        Spacer(modifier = Modifier.height(100.dp))
    }
}

@Composable
private fun WifiScanResultCard(uiState: NetworkScanUiState) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = CardBg,
        shape = RoundedCornerShape(CardRadius),
    ) {
        Column(
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 20.dp),
        ) {
            Text(
                text = stringResource(R.string.wifi_scan_result),
                color = Navy,
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
            )
            Spacer(modifier = Modifier.height(16.dp))
            ScanInfoRow(
                label = "SSID",
                value = uiState.scan?.ssid?.displayUnknownSsid() ?: uiState.networkInfo.ssid,
            )
            ScanDivider()
            ScanInfoRow(
                label = stringResource(R.string.last_scan),
                value = uiState.scanTime,
            )
            if (uiState.scan != null) {
                ScanDivider()
                ScanInfoRow(label = stringResource(R.string.ip), value = uiState.scan.deviceIp)
                ScanDivider()
                ScanInfoRow(label = "Gateway", value = uiState.scan.gatewayIp)
                ScanDivider()
                ScanInfoRow(label = "DNS", value = uiState.scan.dnsIp)
            }
        }
    }
}

@Composable
private fun ScanInfoRow(
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
private fun ScanDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(vertical = 16.dp),
        color = Divider15,
        thickness = 1.dp,
    )
}

@Composable
private fun DevicesSummaryCard(
    scan: NetworkScanResult,
    onDevicesClick: () -> Unit,
) {
    val devices = scan.devices
    Surface(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(enabled = devices.isNotEmpty(), onClick = onDevicesClick),
        color = CardBg,
        shape = RoundedCornerShape(CardRadius),
    ) {
        Column(
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 20.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.devices_count, devices.size),
                    color = Navy,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                )
                Icon(
                    painter = painterResource(id = R.drawable.ic_ok),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = if (devices.isNotEmpty()) NavyMuted else Color.Transparent,
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            if (devices.isEmpty()) {
                Text(
                    text = stringResource(R.string.no_devices_on_wifi),
                    color = NavyMuted,
                    fontSize = 15.sp,
                )
            } else {
                devices.take(2).forEachIndexed { index, device ->
                    if (index > 0) ScanDivider()
                    DeviceIpRow(device = device)
                }
            }
        }
    }
}

@Composable
private fun DeviceIpRow(device: NetworkDeviceInfo) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_ok),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = Navy,
            )
            Text(
                text = device.ip,
                color = Navy,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
            )
        }
        Text(
            text = device.hostName.displayUnknown(),
            color = Navy,
            fontSize = 16.sp,
            fontWeight = FontWeight.Normal,
        )
    }
}

@Composable
private fun ScanDetailsCard(uiState: NetworkScanUiState) {
    val detailItems =
        listOf(
            stringResource(R.string.wifi_auth),
            stringResource(R.string.arp_spoofing),
            stringResource(R.string.ssl_stripping),
            stringResource(R.string.ssl_splitting),
            stringResource(R.string.dns_spoofing),
            stringResource(R.string.devices),
        )
    val completed =
        when (uiState.scanState) {
            NetworkScanState.Done -> detailItems.size
            NetworkScanState.Running -> uiState.completedDetailCount
            else -> 0
        }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = CardBg,
        shape = RoundedCornerShape(CardRadius),
    ) {
        Column(
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 20.dp),
        ) {
            Text(
                text = stringResource(R.string.details),
                color = Navy,
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
            )
            Spacer(modifier = Modifier.height(16.dp))
            detailItems.forEachIndexed { index, label ->
                if (index > 0) {
                    ScanDivider()
                }
                DetailRow(label = label, checked = index < completed)
            }
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    checked: Boolean,
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
        CleanXCheckBadge(checked = checked)
    }
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

private fun String.displayUnknownSsid(): String =
    takeUnless { it == "<unknown ssid>" || it.isBlank() } ?: "--"

private fun String.displayUnknown(): String =
    takeUnless { it.equals("unknown", ignoreCase = true) || it.isBlank() } ?: "--"
