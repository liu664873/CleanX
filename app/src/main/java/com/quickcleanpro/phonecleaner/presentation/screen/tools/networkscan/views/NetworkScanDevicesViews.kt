package com.quickcleanpro.phonecleaner.presentation.screen.tools.networkscan.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.quickcleanpro.phonecleaner.R
import com.quickcleanpro.phonecleaner.domain.model.toolbox.NetworkDeviceInfo
import com.quickcleanpro.phonecleaner.presentation.common.components.CleanXScaffoldPage
import com.quickcleanpro.phonecleaner.presentation.common.components.buttons.CleanXPrimaryButton
import com.quickcleanpro.phonecleaner.presentation.screen.tools.networkscan.NetworkScanDevicesViewModel

private val CardBg = Color(0xFFF6F7FB)
private val Navy = Color(0xFF1D2959)
private val NavyMuted = Color(0xA61D2959)
private val Divider15 = Color(0x261D2959)
private val CardRadius = 12.dp

@Composable
internal fun NetworkScanDevicesScreenState(viewModel: NetworkScanDevicesViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    CleanXScaffoldPage(
        title = stringResource(R.string.network_scan),
    ) {
        SummaryCard(count = uiState.devices.size)

        when {
            uiState.isLoading -> {
                Spacer(modifier = Modifier.height(16.dp))
                MessageCard(
                    title = stringResource(R.string.scanning_devices),
                    message = stringResource(R.string.scan_loading_fallback),
                )
            }
            uiState.errorMessage != null -> {
                Spacer(modifier = Modifier.height(16.dp))
                MessageCard(
                    title = stringResource(R.string.scan_failed),
                    message = uiState.errorMessage.orEmpty(),
                )
                Spacer(modifier = Modifier.height(16.dp))
                CleanXPrimaryButton(
                    text = stringResource(R.string.retry),
                    onClick = viewModel::loadDevices,
                )
            }
            uiState.devices.isEmpty() -> {
                Spacer(modifier = Modifier.height(16.dp))
                MessageCard(
                    title = stringResource(R.string.no_device_found),
                    message = stringResource(R.string.no_devices_on_wifi),
                )
            }
            else -> {
                uiState.devices.forEach { device ->
                    Spacer(modifier = Modifier.height(16.dp))
                    DeviceDetailCard(device = device)
                }
            }
        }

        Spacer(modifier = Modifier.height(100.dp))
    }
}

@Composable
private fun SummaryCard(count: Int) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = CardBg,
        shape = RoundedCornerShape(CardRadius),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.devices_count, count),
                color = Navy,
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
            )
            Icon(
                painter = painterResource(id = R.drawable.ic_ok),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = Color.Transparent,
            )
        }
    }
}

@Composable
private fun DeviceDetailCard(device: NetworkDeviceInfo) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
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

            DevicesDivider()

            DeviceInfoRow(
                label = stringResource(R.string.vendor),
                value = stringResource(R.string.unknown),
            )
            DevicesDivider()
            DeviceInfoRow(
                label = stringResource(R.string.name),
                value = device.hostName.displayUnknown(),
            )
            DevicesDivider()
            DeviceInfoRow(
                label = stringResource(R.string.mac_address),
                value = device.macAddress.displayUnknown(),
            )
        }
    }
}

@Composable
private fun DeviceInfoRow(
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
            text = value,
            color = Navy,
            fontSize = 16.sp,
            fontWeight = FontWeight.Normal,
        )
    }
}

@Composable
private fun DevicesDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(vertical = 16.dp),
        color = Divider15,
        thickness = 1.dp,
    )
}

@Composable
private fun MessageCard(
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
            )
        }
    }
}

private fun String.displayUnknown(): String =
    takeUnless { it.equals("unknown", ignoreCase = true) || it.isBlank() } ?: "--"
