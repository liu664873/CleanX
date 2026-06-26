package com.quickcleanpro.phonecleaner.presentation.common.components

import androidx.annotation.StringRes
import androidx.compose.foundation.Image
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quickcleanpro.phonecleaner.R
import com.quickcleanpro.phonecleaner.presentation.common.route.LocalRouter
import com.quickcleanpro.phonecleaner.presentation.common.route.Screen
import com.quickcleanpro.phonecleaner.presentation.theme.LocalVariantTheme
import androidx.compose.runtime.ReadOnlyComposable
import kotlin.random.Random

private val CardBg: Color @Composable @ReadOnlyComposable get() = LocalVariantTheme.current.colors.virusBackgroundCard
private val Navy: Color @Composable @ReadOnlyComposable get() = LocalVariantTheme.current.colors.navy
private val NavyMuted: Color @Composable @ReadOnlyComposable get() = LocalVariantTheme.current.colors.navyMuted
private val Blue: Color @Composable @ReadOnlyComposable get() = LocalVariantTheme.current.colors.primary
private val CardRadius = 12.dp

data class ToolFeature(
    @param:StringRes val titleRes: Int,
    @param:StringRes val subtitleRes: Int,
    val screen: Screen,
    val iconRes: Int,
    val gradient: Brush,
    @param:StringRes val actionLabelRes: Int,
)

val AllToolFeatures
    get() = listOf(
        ToolFeature(R.string.nav_device_info, R.string.common_tool_device_desc, Screen.DeviceInfo, R.drawable.ic_n_device_info,
            Brush.linearGradient(listOf(Color(0xFF7F6CFF), Color(0xFF462BF9))), R.string.view_now),
        ToolFeature(R.string.nav_battery_info, R.string.common_tool_battery_desc, Screen.BatteryInfo, R.drawable.ic_n_battery_info,
            Brush.linearGradient(listOf(Color(0xFF90FB9C), Color(0xFF8AFB88))), R.string.view_now),
        ToolFeature(R.string.nav_app_usage, R.string.common_tool_app_usage_desc, Screen.AppUsage, R.drawable.ic_app_usage,
            Brush.linearGradient(listOf(Color(0xFF6EC6FF), Color(0xFF2196F3))), R.string.view_now),
        ToolFeature(R.string.nav_notification_bar, R.string.common_tool_notification_bar_desc, Screen.NotificationBar, R.drawable.ic_n_notification_bar,
            Brush.linearGradient(listOf(Color(0xFFFF9A80), Color(0xFFFF6E40))), R.string.view_now),
        ToolFeature(R.string.nav_whatsapp_cleaner, R.string.common_tool_whatsapp_desc, Screen.WhatsAppCleaner, R.drawable.ic_n_whatsapp,
            Brush.linearGradient(listOf(Color(0xFF81C784), Color(0xFF4CAF50))), R.string.view_now),
        ToolFeature(R.string.nav_network_usage, R.string.common_tool_network_usage_desc, Screen.NetworkUsage, R.drawable.ic_n_network_usage,
            Brush.linearGradient(listOf(Color(0xFF64B5F6), Color(0xFF1976D2))), R.string.view_now),
        ToolFeature(R.string.nav_network_scan, R.string.common_tool_network_scan_desc, Screen.NetworkScan, R.drawable.ic_n_network_scan,
            Brush.linearGradient(listOf(Color(0xFFFFC745), Color(0xFFFE9915))), R.string.scan_now),
        ToolFeature(R.string.nav_network_speed, R.string.common_tool_network_speed_desc, Screen.NetworkSpeed, R.drawable.ic_network_speed,
            Brush.linearGradient(listOf(Color(0xFFCE93D8), Color(0xFF9C27B0))), R.string.test_now),
    )

@Composable
fun ToolFeatureBanners(
    modifier: Modifier = Modifier,
    excludeRoutes: Set<String> = emptySet(),
    seed: Long = System.currentTimeMillis(),
) {
    val router = LocalRouter.current

    val features = remember(seed) {
        val pool = AllToolFeatures.filter { it.screen.route !in excludeRoutes }
        pool.shuffled(Random(seed)).take(2)
    }

    Column(modifier = modifier.fillMaxWidth()) {
        features.forEach { feature ->
            ToolFeatureBanner(
                feature = feature,
                onClick = { router.navigateAndClearStack(feature.screen) },
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun ToolFeatureBanners(
    features: List<ToolFeature>,
    modifier: Modifier = Modifier,
) {
    val router = LocalRouter.current

    Column(modifier = modifier.fillMaxWidth()) {
        features.forEach { feature ->
            ToolFeatureBanner(
                feature = feature,
                onClick = { router.navigateAndClearStack(feature.screen) },
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ToolFeatureBanner(feature: ToolFeature, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = CardBg,
        shape = RoundedCornerShape(CardRadius),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 8.dp, top = 16.dp, bottom = 16.dp),
        ) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                Box(
                    modifier = Modifier.size(44.dp).clip(RoundedCornerShape(9.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Image(painterResource(feature.iconRes), null, Modifier.size(44.dp))
                }
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(stringResource(feature.titleRes), fontSize = 18.sp, fontWeight = FontWeight.Medium, color = Navy)
                    Text(stringResource(feature.subtitleRes), fontSize = 16.sp, fontWeight = FontWeight.Normal, color = NavyMuted)
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onClick) {
                    Text(stringResource(feature.actionLabelRes), fontSize = 20.sp, fontWeight = FontWeight.Medium, color = Blue)
                }
            }
        }
    }
}
