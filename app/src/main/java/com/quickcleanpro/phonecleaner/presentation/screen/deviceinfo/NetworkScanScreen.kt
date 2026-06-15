package com.quickcleanpro.phonecleaner.presentation.screen.deviceinfo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quickcleanpro.phonecleaner.R
import com.quickcleanpro.phonecleaner.presentation.common.components.CleanXCheckBadge
import com.quickcleanpro.phonecleaner.presentation.common.components.buttons.CleanXPrimaryButton
import com.quickcleanpro.phonecleaner.presentation.common.components.styles.CleanXBlue
import com.quickcleanpro.phonecleaner.presentation.common.components.styles.CleanXButtonShape
import com.quickcleanpro.phonecleaner.presentation.common.components.styles.CleanXButtonHeight
import com.quickcleanpro.phonecleaner.presentation.common.components.CleanXTopAppBar
import com.quickcleanpro.phonecleaner.presentation.navigation.AppNavigationEvent
import com.quickcleanpro.phonecleaner.presentation.navigation.Screen

private val PageBgGradient = Brush.linearGradient(
    colors = listOf(Color(0xFFE3ECFD), Color(0xFFDFEBF5)),
)
private val CardBg = Color(0xFFF6F7FB)
private val Navy = Color(0xFF1D2959)
private val NavyMuted = Color(0xA61D2959)
private val Divider15 = Color(0x261D2959)
private val CardRadius = 12.dp

@Composable
fun NetworkScanScreen(
    onBack: () -> Unit = {},
    onNavigate: (AppNavigationEvent) -> Unit = {},
) {
    var hasScanned by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            CleanXTopAppBar(
                title = stringResource(R.string.network_scan),
                onBack = onBack,
                modifier = Modifier.systemBarsPadding(),
            )
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(PageBgGradient),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // Wi-Fi Scan Result card
                WifiScanResultCard(hasScanned = hasScanned)

                if (hasScanned) {
                    // Devices card
                    Spacer(modifier = Modifier.height(16.dp))
                    DevicesSummaryCard(
                        onDevicesClick = {
                            onNavigate(AppNavigationEvent.Destination(Screen.NetworkScanDevices.route))
                        },
                    )
                }

                // Details card
                Spacer(modifier = Modifier.height(16.dp))
                ScanDetailsCard(hasScanned = hasScanned)

                Spacer(modifier = Modifier.height(16.dp))

                if (!hasScanned) {
                    CleanXPrimaryButton(
                        text = stringResource(R.string.scan_wifi),
                        onClick = { hasScanned = true },
                    )
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        OutlinedButton(
                            onClick = { },
                            modifier = Modifier
                                .weight(1f)
                                .height(CleanXButtonHeight),
                            shape = CleanXButtonShape,
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = Color.White,
                                contentColor = CleanXBlue,
                            ),
                            border = ButtonDefaults.outlinedButtonBorder.copy(
                                width = 1.56.dp,
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
                            onClick = { },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }

                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}

@Composable
private fun WifiScanResultCard(hasScanned: Boolean) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = CardBg,
        shape = RoundedCornerShape(CardRadius),
    ) {
        Column(
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 20.dp),
        ) {
            Text(
                text = "Wi-Fi Scan Result",
                color = Navy,
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
            )
            Spacer(modifier = Modifier.height(16.dp))
            ScanInfoRow(
                label = "SSID",
                value = if (hasScanned) "1002" else String.format("<%s>", stringResource(R.string.unknown_ssid)),
            )
            ScanDivider()
            ScanInfoRow(
                label = stringResource(R.string.last_scan),
                value = if (hasScanned) "05/26/2026 16:25:58" else "--",
            )
        }
    }
}

@Composable
private fun ScanInfoRow(label: String, value: String) {
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
private fun ScanDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(vertical = 16.dp),
        color = Divider15,
        thickness = 1.dp,
    )
}

@Composable
private fun DevicesSummaryCard(onDevicesClick: () -> Unit) {
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
                Text(
                    text = "Devices (8)",
                    color = Navy,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                )
                Icon(
                    painter = painterResource(id = R.drawable.ic_ok),
                    contentDescription = "Go to devices",
                    modifier = Modifier.size(24.dp),
                    tint = NavyMuted,
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            // IP list
            DeviceIpRow(ip = "192.168.111.1", label = "unknown")
            ScanDivider()
            DeviceIpRow(ip = "192.168.111.2", label = "unknown")
        }
    }
}

@Composable
private fun DeviceIpRow(ip: String, label: String) {
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
                text = ip,
                color = Navy,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
            )
        }
        Text(
            text = label,
            color = Navy,
            fontSize = 16.sp,
            fontWeight = FontWeight.Normal,
        )
    }
}

@Composable
private fun ScanDetailsCard(hasScanned: Boolean) {
    val detailItems = if (hasScanned) {
        listOf(
            stringResource(R.string.wifi_auth),
            stringResource(R.string.arp_spoofing),
            stringResource(R.string.ssl_stripping),
            stringResource(R.string.ssl_splitting),
            stringResource(R.string.dns_spoofing),
            "Devices",
        )
    } else {
        listOf(
            stringResource(R.string.wifi_auth),
            stringResource(R.string.arp_spoofing),
            stringResource(R.string.ssl_stripping),
            stringResource(R.string.ssl_splitting),
            "Alert Resolution",
        )
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
                text = "Details",
                color = Navy,
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
            )
            Spacer(modifier = Modifier.height(16.dp))
            detailItems.forEachIndexed { index, label ->
                if (index > 0) {
                    ScanDivider()
                }
                DetailRow(label = label, checked = hasScanned)
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, checked: Boolean) {
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
