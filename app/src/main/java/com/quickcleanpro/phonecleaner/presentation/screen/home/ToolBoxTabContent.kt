package com.quickcleanpro.phonecleaner.presentation.screen.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
import com.quickcleanpro.phonecleaner.presentation.common.components.styles.CleanXBlue
import com.quickcleanpro.phonecleaner.presentation.navigation.AppNavigationEvent
import com.quickcleanpro.phonecleaner.presentation.navigation.Screen

private val OnboardingNavy = Color(0xFF1D2959)
private val DividerColor = Color(0x0D1D2959)
private val LabelMuted = Color(0xA61D2959)

private val DeviceInfoGradient = Brush.linearGradient(
    colors = listOf(Color(0xFF90C5FB), Color(0xFF88C9FB)),
)
private val DeviceInfoDarkGradient = Brush.linearGradient(
    colors = listOf(Color(0xFF90FB9C), Color(0xFF8AFB88)),
)

@Composable
fun ToolBoxTabContent(onNavigate: (AppNavigationEvent) -> Unit = {}) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .padding(top = 32.dp),
    ) {
        // Device Info Card（圆角渐变背景，使用 Box 修正）
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(brush = DeviceInfoGradient)
        ) {
            Row(
                modifier = Modifier.padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    DeviceInfoRow(
                        label = stringResource(R.string.device_model),
                        value = "SM-A165F",
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    DeviceInfoRow(
                        label = stringResource(R.string.home_system_version),
                        value = "Android 14",
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = { onNavigate(AppNavigationEvent.Destination(Screen.DeviceInfo.route)) },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = CleanXBlue,
                        ),
                        modifier = Modifier
                            .width(107.dp)
                            .height(40.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.check_now),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                }
                Image(
                    painter = painterResource(id = R.drawable.ic_device_phone),
                    contentDescription = null,
                    modifier = Modifier.size(98.dp),
                    contentScale = ContentScale.Fit,
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Battery Info Card（圆角渐变背景，使用 Box 修正）
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(brush = DeviceInfoDarkGradient)
        ) {
            Row(
                modifier = Modifier.padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    DeviceInfoRow(
                        label = stringResource(R.string.battery_status),
                        value = stringResource(R.string.battery_discharging),
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    DeviceInfoRow(
                        label = stringResource(R.string.battery_capacity),
                        value = "59%",
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = { onNavigate(AppNavigationEvent.Destination(Screen.BatteryInfo.route)) },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = CleanXBlue,
                        ),
                        modifier = Modifier
                            .width(107.dp)
                            .height(40.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.check_now),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                }
                Image(
                    painter = painterResource(id = R.drawable.ic_battery),
                    contentDescription = null,
                    modifier = Modifier.size(98.dp),
                    contentScale = ContentScale.Fit,
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ToolBox Card（白色，无渐变）
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            shape = RoundedCornerShape(12.dp),
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
            ) {
                Text(
                    text = stringResource(R.string.home_toolbox_title),
                    color = OnboardingNavy,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 12.dp),
                )
                // Row 1
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    ToolItem(R.drawable.ic_app_usage, stringResource(R.string.app_usage)) {
                        onNavigate(AppNavigationEvent.Destination(Screen.AppUsage.route))
                    }
                    ToolItem(R.drawable.ic_notification_bar, stringResource(R.string.notification_bar)) {
                        onNavigate(AppNavigationEvent.Destination(Screen.NotificationBar.route))
                    }
                    ToolItem(R.drawable.ic_whatsapp_cleaner, stringResource(R.string.whatsapp_cleaner)) {
                        onNavigate(AppNavigationEvent.Destination(Screen.WhatsAppCleaner.route))
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = DividerColor, thickness = 1.dp)
                Spacer(modifier = Modifier.height(16.dp))
                // Row 2
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    ToolItem(R.drawable.ic_network_usage, stringResource(R.string.network_usage)) {
                        onNavigate(AppNavigationEvent.Destination(Screen.NetworkUsage.route))
                    }
                    ToolItem(R.drawable.ic_network_scan, stringResource(R.string.network_scan)) {
                        onNavigate(AppNavigationEvent.Destination(Screen.NetworkScan.route))
                    }
                    ToolItem(R.drawable.ic_network_speed, stringResource(R.string.network_speed)) {
                        onNavigate(AppNavigationEvent.Destination(Screen.NetworkSpeed.route))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(100.dp))
    }
}

@Composable
private fun ToolItem(iconRes: Int, label: String, onClick: () -> Unit = {}) {
    Column(
        modifier = Modifier
            .width(80.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            painter = painterResource(id = iconRes),
            contentDescription = label,
            modifier = Modifier.size(44.dp),
            contentScale = ContentScale.Fit,
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            color = OnboardingNavy,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            lineHeight = 20.sp,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun DeviceInfoRow(label: String, value: String) {
    Column {
        Text(
            text = label,
            color = LabelMuted,
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            color = OnboardingNavy,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}