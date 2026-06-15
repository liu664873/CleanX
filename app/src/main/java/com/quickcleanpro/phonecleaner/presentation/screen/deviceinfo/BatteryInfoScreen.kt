package com.quickcleanpro.phonecleaner.presentation.screen.deviceinfo

import androidx.compose.foundation.Image
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quickcleanpro.phonecleaner.R
import com.quickcleanpro.phonecleaner.presentation.common.components.CleanXTopAppBar

private val PageBgGradient = Brush.linearGradient(
    colors = listOf(Color(0xFFE3ECFD), Color(0xFFDFEBF5)),
)
private val CardBg = Color(0xFFF6F7FB)
private val Navy = Color(0xFF1D2959)
private val NavyMuted = Color(0xA61D2959)
private val NavyLight = Color(0x591D2959)
private val Divider = Color(0x261D2959)
private val CardRadius = 12.dp
private val SmallCardSize = 166.5.dp

@Composable
fun BatteryInfoScreen(onBack: () -> Unit = {}) {
    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            CleanXTopAppBar(
                title = stringResource(R.string.battery_info),
                onBack = onBack,
                modifier = Modifier.systemBarsPadding(),
                titleFontSize = 22.sp,
                fontWeight = FontWeight.SemiBold,
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
                    .padding(horizontal = 16.dp)
                    .padding(top = 16.dp, bottom = 32.dp),
            ) {
                // Card 1: Battery Status + Capacity
                BatteryStatusCard()

                Spacer(modifier = Modifier.height(16.dp))

                // Row: Battery Health | Life
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    MetricBoxCard(
                        modifier = Modifier.weight(1f),
                        value = stringResource(R.string.battery_health_good),
                        label = stringResource(R.string.battery_health),
                        iconRes = R.drawable.ic_battery,
                        iconHeight = 56.dp,
                    )
                    MetricBoxCard(
                        modifier = Modifier.weight(1f),
                        value = "2h 52min",
                        label = stringResource(R.string.battery_life),
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Card 4: Battery Temperature
                BatteryTemperatureCard()

                Spacer(modifier = Modifier.height(16.dp))

                // Card 5: Electric Current
                ElectricCurrentCard()

                Spacer(modifier = Modifier.height(16.dp))

                // Row: Temperature | Voltage
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    MetricBoxCard(
                        modifier = Modifier.weight(1f),
                        value = "19.5\u00A0\u2103",
                        label = stringResource(R.string.battery_temperature),
                        iconRes = R.drawable.ic_battery,
                        iconHeight = 40.dp,
                    )
                    MetricBoxCard(
                        modifier = Modifier.weight(1f),
                        value = "3942.0 V",
                        label = stringResource(R.string.battery_voltage),
                    )
                }

                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}

// Card 1
@Composable
private fun BatteryStatusCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = CardBg,
        shape = RoundedCornerShape(CardRadius),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.width(203.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                LabelValuePair(
                    label = stringResource(R.string.battery_status),
                    value = stringResource(R.string.battery_discharging),
                )
                LabelValuePair(
                    label = stringResource(R.string.battery_capacity),
                    value = "59%",
                )
            }

            Image(
                painter = painterResource(id = R.drawable.ic_battery),
                contentDescription = null,
                modifier = Modifier.size(81.dp),
                contentScale = ContentScale.Fit,
            )
        }
    }
}

@Composable
private fun LabelValuePair(label: String, value: String) {
    Column {
        Text(
            text = label,
            color = NavyMuted,
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
        )
        Text(
            text = value,
            color = Navy,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

// Small metric box (Life, Health, Temperature, Voltage)
@Composable
private fun MetricBoxCard(
    modifier: Modifier = Modifier,
    value: String,
    label: String,
    iconRes: Int? = null,
    iconHeight: androidx.compose.ui.unit.Dp = 60.dp,
) {
    Surface(
        modifier = modifier.height(148.dp),
        color = CardBg,
        shape = RoundedCornerShape(CardRadius),
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            if (iconRes != null) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Image(
                        painter = painterResource(id = iconRes),
                        contentDescription = null,
                        modifier = Modifier.height(iconHeight),
                        contentScale = ContentScale.Fit,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = value,
                        color = Navy,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = label,
                        color = NavyLight,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Normal,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxSize(),
                ) {
                    Text(
                        text = value,
                        color = Navy,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = label,
                        color = NavyLight,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Normal,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}

// Card 4: Battery Temperature
@Composable
private fun BatteryTemperatureCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = CardBg,
        shape = RoundedCornerShape(CardRadius),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
        ) {
            SectionTitle(stringResource(R.string.battery_temperature_title))
            SectionDivider()
            Spacer(modifier = Modifier.height(6.dp))
            InfoValueRow(
                label = stringResource(R.string.battery_realtime_temperature),
                value = "19.5\u00A0\u2103",
            )
            InfoValueRow(
                label = stringResource(R.string.battery_average_temperature),
                value = "19.5\u00A0\u2103",
            )
            Spacer(modifier = Modifier.height(10.dp))
            // Chart placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFE0E6F0)),
            )
        }
    }
}

// Card 5: Electric Current
@Composable
private fun ElectricCurrentCard() {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf(
        stringResource(R.string.battery_range_1_min),
        stringResource(R.string.battery_range_1_hour),
        stringResource(R.string.battery_range_24_hours),
    )

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = CardBg,
        shape = RoundedCornerShape(CardRadius),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
        ) {
            SectionTitle(stringResource(R.string.battery_electric_current))
            SectionDivider()
            Spacer(modifier = Modifier.height(6.dp))
            InfoValueRow(
                label = stringResource(R.string.battery_realtime_current),
                value = "-337.00 mA",
            )
            InfoValueRow(
                label = stringResource(R.string.battery_average_current),
                value = "-21.40 mA",
            )
            Spacer(modifier = Modifier.height(10.dp))
            // Chart placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFE0E6F0)),
            )
            Spacer(modifier = Modifier.height(12.dp))
            // 1Min / 1Hour / 24Hour tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(15.dp))
                    .background(Divider),
            ) {
                tabs.forEachIndexed { index, tab ->
                    val isSelected = selectedTab == index
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(15.dp))
                            .background(if (isSelected) Navy else Color.Transparent)
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = tab,
                            color = if (isSelected) Color.White else NavyLight,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        color = Navy,
        fontSize = 20.sp,
        fontWeight = FontWeight.Medium,
    )
}

@Composable
private fun SectionDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(vertical = 4.dp),
        color = Divider,
        thickness = 1.dp,
    )
}

@Composable
private fun InfoValueRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            color = Navy,
            fontSize = 16.sp,
            fontWeight = FontWeight.Normal,
        )
        Text(
            text = value,
            color = Navy,
            fontSize = 16.sp,
            fontWeight = FontWeight.Normal,
        )
    }
}
