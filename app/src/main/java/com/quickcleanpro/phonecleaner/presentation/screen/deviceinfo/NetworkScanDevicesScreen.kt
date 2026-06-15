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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.quickcleanpro.phonecleaner.presentation.common.components.CleanXTopAppBar

private val PageBgGradient = Brush.linearGradient(
    colors = listOf(Color(0xFFE3ECFD), Color(0xFFDFEBF5)),
)
private val CardBg = Color(0xFFF6F7FB)
private val Navy = Color(0xFF1D2959)
private val Divider15 = Color(0x261D2959)
private val CardRadius = 12.dp

@Composable
fun NetworkScanDevicesScreen(onBack: () -> Unit = {}) {

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

                // Devices header card
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = CardBg,
                    shape = RoundedCornerShape(CardRadius),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 20.dp),
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
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = Color.Transparent,
                        )
                    }
                }

                // Device cards
                val devices = listOf(
                    "192.168.111.1",
                    "192.168.111.2",
                    "192.168.111.3",
                )

                devices.forEach { ip ->
                    Spacer(modifier = Modifier.height(16.dp))
                    DeviceDetailCard(ip = ip)
                }

                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}

@Composable
private fun DeviceDetailCard(ip: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = CardBg,
        shape = RoundedCornerShape(CardRadius),
    ) {
        Column(
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 20.dp),
        ) {
            // IP + hostname row
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
                    text = stringResource(R.string.unknown),
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
                value = stringResource(R.string.unknown),
            )
            DevicesDivider()
            DeviceInfoRow(
                label = stringResource(R.string.mac_address),
                value = stringResource(R.string.unknown),
            )
        }
    }
}

@Composable
private fun DeviceInfoRow(label: String, value: String) {
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
