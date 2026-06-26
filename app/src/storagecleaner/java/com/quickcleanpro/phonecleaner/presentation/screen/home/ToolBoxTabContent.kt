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
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quickcleanpro.phonecleaner.R
import com.quickcleanpro.phonecleaner.presentation.common.components.popups.CleanXSingleActionDialog
import com.quickcleanpro.phonecleaner.presentation.common.components.styles.CleanXBlue
import com.quickcleanpro.phonecleaner.presentation.common.route.LocalRouter
import com.quickcleanpro.phonecleaner.presentation.common.route.Screen
import com.quickcleanpro.phonecleaner.presentation.theme.LocalVariantTheme
import androidx.compose.runtime.ReadOnlyComposable

private val OnboardingNavy: Color @Composable @ReadOnlyComposable get() = LocalVariantTheme.current.colors.navy
private val DividerColor: Color @Composable @ReadOnlyComposable get() = LocalVariantTheme.current.colors.dividerSubtle
private val LabelMuted: Color @Composable @ReadOnlyComposable get() = LocalVariantTheme.current.colors.navyMuted

private val DeviceInfoGradient =
    Brush.linearGradient(
        colors = listOf(Color(0xFF90C5FB), Color(0xFF88C9FB)),
    )
private val DeviceInfoDarkGradient =
    Brush.linearGradient(
        colors = listOf(Color(0xFF90FB9C), Color(0xFF8AFB88)),
    )

@Composable
fun ToolBoxTabContent(
    summaryState: HomeSummaryUiState,
    onFeatureClick: () -> Unit = {},
) {
    val router = LocalRouter.current
    val context = LocalContext.current
    var showWhatsAppNotFoundDialog by remember { mutableStateOf(false) }
    val batteryCapacity =
        summaryState.batteryInfo.levelPercent
            .takeIf { it >= 0 }
            ?.let { "$it%" }
            ?: "--"
    val batteryStatus = localizedBatteryStatus(summaryState.batteryStatusText)

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(top = 32.dp),
    ) {
        DeviceInfoCard(
            model = summaryState.deviceModel,
            androidVersion = summaryState.androidVersion,
            onClick = {
                onFeatureClick()
                router.navigate(Screen.DeviceInfo)
            },
        )

        Spacer(modifier = Modifier.height(16.dp))

        BatteryInfoCard(
            status = batteryStatus,
            capacity = batteryCapacity,
            onClick = {
                onFeatureClick()
                router.navigate(Screen.BatteryInfo)
            },
        )

        Spacer(modifier = Modifier.height(16.dp))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            shape = RoundedCornerShape(12.dp),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
            ) {
                Text(
                    text = stringResource(R.string.home_toolbox_title),
                    color = OnboardingNavy,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 12.dp),
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    ToolItem(R.drawable.ic_app_usage, stringResource(R.string.app_usage), Modifier.weight(1f)) {
                        onFeatureClick()
                        router.navigate(Screen.AppUsage)
                    }
                    ToolItem(R.drawable.ic_notification_bar, stringResource(R.string.notification_bar), Modifier.weight(1f)) {
                        onFeatureClick()
                        router.navigate(Screen.NotificationBar)
                    }
                    ToolItem(R.drawable.ic_whatsapp_cleaner, stringResource(R.string.whatsapp_cleaner), Modifier.weight(1f)) {
                        onFeatureClick()
                        if (context.hasWhatsAppInstalled()) {
                            router.navigate(Screen.WhatsAppCleaner)
                        } else {
                            showWhatsAppNotFoundDialog = true
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = DividerColor, thickness = 1.dp)
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    ToolItem(R.drawable.ic_network_usage, stringResource(R.string.network_usage), Modifier.weight(1f)) {
                        onFeatureClick()
                        router.navigate(Screen.NetworkUsage)
                    }
                    ToolItem(R.drawable.ic_network_scan, stringResource(R.string.network_scan), Modifier.weight(1f)) {
                        onFeatureClick()
                        router.navigate(Screen.NetworkScan)
                    }
                    ToolItem(R.drawable.ic_network_speed, stringResource(R.string.network_speed), Modifier.weight(1f)) {
                        onFeatureClick()
                        router.navigate(Screen.NetworkSpeed)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(100.dp))
    }

    if (showWhatsAppNotFoundDialog) {
        CleanXSingleActionDialog(
            title = stringResource(R.string.whatsapp_not_found_title),
            message = stringResource(R.string.whatsapp_not_found_message),
            actionText = stringResource(R.string.close),
            onAction = { showWhatsAppNotFoundDialog = false },
            onDismissRequest = { showWhatsAppNotFoundDialog = false },
        )
    }
}

private fun android.content.Context.hasWhatsAppInstalled(): Boolean =
    WhatsAppPackageNames.any { packageName ->
        packageManager.getLaunchIntentForPackage(packageName) != null
    }

private val WhatsAppPackageNames = listOf("com.whatsapp", "com.whatsapp.w4b")

@Composable
private fun DeviceInfoCard(
    model: String,
    androidVersion: String,
    onClick: () -> Unit,
) {
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(brush = DeviceInfoGradient),
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                DeviceInfoRow(
                    label = stringResource(R.string.device_model),
                    value = model,
                )
                Spacer(modifier = Modifier.height(6.dp))
                DeviceInfoRow(
                    label = stringResource(R.string.home_system_version),
                    value = androidVersion,
                )
                Spacer(modifier = Modifier.height(10.dp))
                CheckNowButton(onClick = onClick)
            }
            Spacer(modifier = Modifier.height(10.dp))
            Image(
                painter = painterResource(id = R.drawable.ic_device_phone),
                contentDescription = null,
                modifier = Modifier.size(98.dp),
                contentScale = ContentScale.Fit,
            )
        }
    }
}

@Composable
private fun BatteryInfoCard(
    status: String,
    capacity: String,
    onClick: () -> Unit,
) {
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(brush = DeviceInfoDarkGradient),
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                DeviceInfoRow(
                    label = stringResource(R.string.battery_status),
                    value = status,
                )
                Spacer(modifier = Modifier.height(6.dp))
                DeviceInfoRow(
                    label = stringResource(R.string.battery_capacity),
                    value = capacity,
                )
                Spacer(modifier = Modifier.height(10.dp))
                CheckNowButton(onClick = onClick)
            }
            Spacer(modifier = Modifier.height(10.dp))
            Image(
                painter = painterResource(id = R.drawable.battery),
                contentDescription = null,
                modifier = Modifier.size(98.dp),
                contentScale = ContentScale.Fit,
            )
        }
    }
}

@Composable
private fun CheckNowButton(onClick: () -> Unit) {
    Button(
        modifier = Modifier
            .wrapContentWidth()
            .height(40.dp),
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        colors =
            ButtonDefaults.buttonColors(
                containerColor = Color.White,
                contentColor = CleanXBlue,
            )
    ) {
        Text(
            text = stringResource(R.string.check_now),
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
private fun ToolItem(
    iconRes: Int,
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    Column(
        modifier =
            modifier
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
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            lineHeight = 17.sp,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun DeviceInfoRow(
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier
            .width(203.dp)
            .height(21.dp)
    ) {
        Text(
            text = label,
            color = LabelMuted,
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = value,
            color = OnboardingNavy,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun localizedBatteryStatus(statusText: String): String =
    when (statusText) {
        "Charging" -> stringResource(R.string.battery_charging)
        "Discharging" -> stringResource(R.string.battery_discharging)
        "Full" -> stringResource(R.string.battery_full)
        "Not Charging" -> stringResource(R.string.battery_not_charging)
        else -> stringResource(R.string.unknown)
    }
