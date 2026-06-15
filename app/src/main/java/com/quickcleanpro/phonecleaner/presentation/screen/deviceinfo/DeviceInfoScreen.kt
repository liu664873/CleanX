package com.quickcleanpro.phonecleaner.presentation.screen.deviceinfo

import android.os.Build
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quickcleanpro.phonecleaner.R
import com.quickcleanpro.phonecleaner.presentation.common.components.CleanXTopAppBar
import com.quickcleanpro.phonecleaner.presentation.common.components.RoundedProgressBar
import com.quickcleanpro.phonecleaner.presentation.navigation.LocalNavController

private val PageBgGradient = Brush.linearGradient(
    colors = listOf(Color(0xFFE3ECFD), Color(0xFFDFEBF5)),
)
private val CardBg = Color(0xFFF6F7FB)
private val Navy = Color(0xFF1D2959)
private val NavyMuted = Color(0xA61D2959) // rgba(29,41,89,0.65)
private val Divider = Color(0x261D2959) // rgba(29,41,89,0.15)
private val CardRadius = 12.dp

@Composable
fun DeviceInfoScreen(onBack: () -> Unit = {}) {
    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            CleanXTopAppBar(
                title = stringResource(R.string.device_model),
                onBack = onBack,
                modifier = Modifier.systemBarsPadding(),
                titleFontSize = 22.sp,
                fontWeight = FontWeight.SemiBold,
                actions = {
                    Image(
                        painter = painterResource(id = R.drawable.ic_device_phone),
                        contentDescription = null,
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .size(44.dp),
                        contentScale = ContentScale.Fit,
                    )
                },
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
                // Card 1: Device Model
                DeviceModelCard()

                Spacer(modifier = Modifier.height(16.dp))

                // Card 2: State Info (CPU / RAM / Storage)
                StateInfoCard()

                Spacer(modifier = Modifier.height(16.dp))

                // Card 3: Screen
                ScreenCard()

                Spacer(modifier = Modifier.height(16.dp))

                // Card 4: Battery
                BatteryCard()

                Spacer(modifier = Modifier.height(16.dp))

                // Card 5: Sensors
                SensorsCard()

                Spacer(modifier = Modifier.height(16.dp))

                // Card 6: CPU
                CpuCard()

                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}

@Composable
private fun DeviceModelCard() {
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
                LabelValueRow(
                    label = stringResource(R.string.device_model),
                    value = "${Build.MANUFACTURER}-${Build.MODEL}",
                    labelColor = NavyMuted,
                    valueColor = Navy,
                    labelSize = 14.sp,
                    valueSize = 18.sp,
                    valueWeight = FontWeight.Bold,
                )
                LabelValueRow(
                    label = stringResource(R.string.home_system_version),
                    value = "Android ${Build.VERSION.RELEASE}",
                    labelColor = NavyMuted,
                    valueColor = Navy,
                    labelSize = 14.sp,
                    valueSize = 18.sp,
                    valueWeight = FontWeight.Bold,
                )
            }

            Image(
                painter = painterResource(id = R.drawable.ic_device_phone),
                contentDescription = null,
                modifier = Modifier.size(width = 79.dp, height = 81.dp),
                contentScale = ContentScale.Fit,
            )
        }
    }
}

@Composable
private fun StateInfoCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = CardBg,
        shape = RoundedCornerShape(CardRadius),
    ) {
        Column(
            modifier = Modifier.padding(20.dp, 20.dp, 20.dp, 20.dp)
                .then(Modifier.padding(top = 0.dp, bottom = 0.dp)),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            SectionTitle(stringResource(R.string.device_state_info))
            SectionDivider()
            Spacer(modifier = Modifier.height(14.dp))

            StateProgressRow(
                label = "CPU:",
                value = "34%",
                suffix = "/ 1.0\u00A0GHz",
                progress = 0.34f,
            )
            StateProgressRow(
                label = "RAM:",
                value = "4.5GB",
                suffix = "/ 5.6GB",
                progress = 0.80f,
            )
            StateProgressRow(
                label = "Storage:",
                value = "46.8GB",
                suffix = "/ 104.6GB",
                progress = 0.45f,
            )
        }
    }
}

@Composable
private fun StateProgressRow(
    label: String,
    value: String,
    suffix: String,
    progress: Float,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom,
        ) {
            Row(
                modifier = Modifier.width(134.dp),
                verticalAlignment = Alignment.Bottom,
            ) {
                Text(
                    text = label,
                    color = NavyMuted,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal,
                    lineHeight = 24.sp,
                )
                Text(
                    text = value,
                    color = NavyMuted,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal,
                    lineHeight = 24.sp,
                )
            }
            Text(
                text = suffix,
                color = NavyMuted,
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
                lineHeight = 24.sp,
            )
        }
        RoundedProgressBar(
            progress = progress,
            width = 311.dp,
            height = 12.dp,
            trackColor = Color(0xFFE0E6F0),
            fillColor = Navy,
        )
    }
}

@Composable
private fun ScreenCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = CardBg,
        shape = RoundedCornerShape(CardRadius),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
        ) {
            SectionTitle(stringResource(R.string.device_screen))
            SectionDivider()
            Spacer(modifier = Modifier.height(6.dp))
            InfoRow(stringResource(R.string.device_screen_size), "1080x2109")
            InfoRow(stringResource(R.string.device_screen_density), "450 DPI")
            InfoRow(stringResource(R.string.device_screen_multi_touch), stringResource(R.string.device_supported))
        }
    }
}

@Composable
private fun BatteryCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = CardBg,
        shape = RoundedCornerShape(CardRadius),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
        ) {
            SectionTitle(stringResource(R.string.battery_info))
            SectionDivider()
            Spacer(modifier = Modifier.height(6.dp))
            InfoRow(stringResource(R.string.battery_health_status), stringResource(R.string.battery_health_good))
            InfoRow(stringResource(R.string.battery_current_capacity), "14 mAh")
            InfoRow(stringResource(R.string.battery_total_capacity), "100 mAh")
            InfoRow(stringResource(R.string.battery_voltage), "3686 V")
            InfoRow(stringResource(R.string.battery_temperature), "19.5\u00A0\u2103")
            InfoRow(stringResource(R.string.battery_status), stringResource(R.string.battery_discharging))
            InfoRow(stringResource(R.string.battery_charging_status), stringResource(R.string.battery_not_charging))
            InfoRow(stringResource(R.string.battery_technology), "Li-ion")
        }
    }
}

@Composable
private fun SensorsCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = CardBg,
        shape = RoundedCornerShape(CardRadius),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
        ) {
            SectionTitle(stringResource(R.string.device_sensors))
            SectionDivider()
            Spacer(modifier = Modifier.height(6.dp))
            InfoRow(stringResource(R.string.device_accelerometer_sensor), stringResource(R.string.device_supported))
            InfoRow(stringResource(R.string.device_magnetic_field_sensor), stringResource(R.string.device_supported))
            InfoRow(stringResource(R.string.device_orientation_sensor), stringResource(R.string.device_supported))
            InfoRow(stringResource(R.string.device_gyroscope_sensor), stringResource(R.string.device_not_supported))
            InfoRow(stringResource(R.string.device_light_sensor), stringResource(R.string.device_not_supported))
            InfoRow(stringResource(R.string.device_distance_sensor), stringResource(R.string.device_supported))
            InfoRow(stringResource(R.string.device_temperature_sensor), stringResource(R.string.device_not_supported))
        }
    }
}

@Composable
private fun CpuCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = CardBg,
        shape = RoundedCornerShape(CardRadius),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
        ) {
            SectionTitle("CPU")
            SectionDivider()
            Spacer(modifier = Modifier.height(6.dp))
            InfoRow(stringResource(R.string.device_cpu_hardware), "mt6789")
            InfoRow(stringResource(R.string.device_cpu_model), stringResource(R.string.device_unknown))
            InfoRow(stringResource(R.string.device_cpu_cores), "8")
            InfoRow(stringResource(R.string.device_cpu_frequency), "2000 MHz")
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
private fun LabelValueRow(
    label: String,
    value: String,
    labelColor: Color,
    valueColor: Color,
    labelSize: androidx.compose.ui.unit.TextUnit,
    valueSize: androidx.compose.ui.unit.TextUnit,
    valueWeight: FontWeight = FontWeight.Normal,
) {
    Column {
        Text(
            text = label,
            color = labelColor,
            fontSize = labelSize,
            fontWeight = FontWeight.Normal,
        )
        Text(
            text = value,
            color = valueColor,
            fontSize = valueSize,
            fontWeight = valueWeight,
        )
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
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
            modifier = Modifier.weight(1f),
        )
        Text(
            text = value,
            color = Navy,
            fontSize = 16.sp,
            fontWeight = FontWeight.Normal,
        )
    }
}
