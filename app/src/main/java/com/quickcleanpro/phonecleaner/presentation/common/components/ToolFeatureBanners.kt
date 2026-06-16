package com.quickcleanpro.phonecleaner.presentation.common.components

import androidx.compose.foundation.background
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
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quickcleanpro.phonecleaner.R
import com.quickcleanpro.phonecleaner.presentation.common.route.LocalRouter
import com.quickcleanpro.phonecleaner.presentation.common.route.Screen
import kotlin.random.Random

private val CardBg = Color(0xFFF6F7FB)
private val Navy = Color(0xFF1D2959)
private val NavyMuted = Color(0xA61D2959)
private val Blue = Color(0xFF4179FC)
private val CardRadius = 12.dp

data class ToolFeature(
    val title: String,
    val subtitle: String,
    val screen: Screen,
    val iconRes: Int,
    val gradient: Brush,
    val actionLabel: String,
)

val AllToolFeatures
    get() = listOf(
        ToolFeature("Device Info", "View detailed specs of your phone", Screen.DeviceInfo, R.drawable.ic_ok,
            Brush.linearGradient(listOf(Color(0xFF7F6CFF), Color(0xFF462BF9))), "View Now"),
        ToolFeature("Battery Info", "View battery health and status", Screen.BatteryInfo, R.drawable.ic_ok,
            Brush.linearGradient(listOf(Color(0xFF90FB9C), Color(0xFF8AFB88))), "View Now"),
        ToolFeature("App Usage", "See your app usage statistics", Screen.AppUsage, R.drawable.ic_ok,
            Brush.linearGradient(listOf(Color(0xFF6EC6FF), Color(0xFF2196F3))), "View Now"),
        ToolFeature("Notification Bar", "Manage notification settings", Screen.NotificationBar, R.drawable.ic_ok,
            Brush.linearGradient(listOf(Color(0xFFFF9A80), Color(0xFFFF6E40))), "View Now"),
        ToolFeature("WhatsApp Cleaner", "Free up space from WhatsApp", Screen.WhatsAppCleaner, R.drawable.ic_ok,
            Brush.linearGradient(listOf(Color(0xFF81C784), Color(0xFF4CAF50))), "View Now"),
        ToolFeature("Network Usage", "Monitor your data usage", Screen.NetworkUsage, R.drawable.ic_ok,
            Brush.linearGradient(listOf(Color(0xFF64B5F6), Color(0xFF1976D2))), "View Now"),
        ToolFeature("Network Scan", "Network Scan", Screen.NetworkScan, R.drawable.ic_ok,
            Brush.linearGradient(listOf(Color(0xFFFFC745), Color(0xFFFE9915))), "Scan Now"),
        ToolFeature("Network Speed", "Test your internet speed", Screen.NetworkSpeed, R.drawable.ic_ok,
            Brush.linearGradient(listOf(Color(0xFFCE93D8), Color(0xFF9C27B0))), "Test Now"),
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
private fun ToolFeatureBanner(feature: ToolFeature, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = CardBg,
        shape = RoundedCornerShape(CardRadius),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 8.dp, top = 16.dp, bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(44.dp).clip(RoundedCornerShape(9.dp)).background(feature.gradient),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(painterResource(feature.iconRes), null, Modifier.size(28.dp), tint = Color.White)
                }
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(feature.title, fontSize = 18.sp, fontWeight = FontWeight.Medium, color = Navy)
                    Text(feature.subtitle, fontSize = 16.sp, fontWeight = FontWeight.Normal, color = NavyMuted)
                }
            }
            TextButton(onClick = onClick) {
                Text(feature.actionLabel, fontSize = 20.sp, fontWeight = FontWeight.Medium, color = Blue)
            }
        }
    }
}
