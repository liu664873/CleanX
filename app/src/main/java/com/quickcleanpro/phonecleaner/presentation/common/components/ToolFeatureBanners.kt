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
import com.quickcleanpro.phonecleaner.presentation.navigation.AppNavigationEvent
import com.quickcleanpro.phonecleaner.presentation.navigation.Screen
import kotlin.random.Random

// --- Color Constants ---
private val CardBg = Color(0xFFF6F7FB)
private val Navy = Color(0xFF1D2959)
private val NavyMuted = Color(0xA61D2959)
private val Blue = Color(0xFF4179FC)
private val CardRadius = 12.dp

/**
 * Data class representing a toolbox feature for the result page banner.
 */
data class ToolFeature(
    val title: String,
    val subtitle: String,
    val route: String,
    val iconRes: Int,
    val gradient: Brush,
    val actionLabel: String,
)

/** All 8 toolbox module features available for random selection. */
val AllToolFeatures
    get() = listOf(
        ToolFeature(
            title = "Device Info",
            subtitle = "View detailed specs of your phone",
            route = Screen.DeviceInfo.route,
            iconRes = R.drawable.ic_ok,
            gradient = Brush.linearGradient(
                colors = listOf(Color(0xFF7F6CFF), Color(0xFF462BF9)),
            ),
            actionLabel = "View Now",
        ),
        ToolFeature(
            title = "Battery Info",
            subtitle = "View battery health and status",
            route = Screen.BatteryInfo.route,
            iconRes = R.drawable.ic_ok,
            gradient = Brush.linearGradient(
                colors = listOf(Color(0xFF90FB9C), Color(0xFF8AFB88)),
            ),
            actionLabel = "View Now",
        ),
        ToolFeature(
            title = "App Usage",
            subtitle = "See your app usage statistics",
            route = Screen.AppUsage.route,
            iconRes = R.drawable.ic_ok,
            gradient = Brush.linearGradient(
                colors = listOf(Color(0xFF6EC6FF), Color(0xFF2196F3)),
            ),
            actionLabel = "View Now",
        ),
        ToolFeature(
            title = "Notification Bar",
            subtitle = "Manage notification settings",
            route = Screen.NotificationBar.route,
            iconRes = R.drawable.ic_ok,
            gradient = Brush.linearGradient(
                colors = listOf(Color(0xFFFF9A80), Color(0xFFFF6E40)),
            ),
            actionLabel = "View Now",
        ),
        ToolFeature(
            title = "WhatsApp Cleaner",
            subtitle = "Free up space from WhatsApp",
            route = Screen.WhatsAppCleaner.route,
            iconRes = R.drawable.ic_ok,
            gradient = Brush.linearGradient(
                colors = listOf(Color(0xFF81C784), Color(0xFF4CAF50)),
            ),
            actionLabel = "View Now",
        ),
        ToolFeature(
            title = "Network Usage",
            subtitle = "Monitor your data usage",
            route = Screen.NetworkUsage.route,
            iconRes = R.drawable.ic_ok,
            gradient = Brush.linearGradient(
                colors = listOf(Color(0xFF64B5F6), Color(0xFF1976D2)),
            ),
            actionLabel = "View Now",
        ),
        ToolFeature(
            title = "Network Scan",
            subtitle = "Network Scan",
            route = Screen.NetworkScan.route,
            iconRes = R.drawable.ic_ok,
            gradient = Brush.linearGradient(
                colors = listOf(Color(0xFFFFC745), Color(0xFFFE9915)),
            ),
            actionLabel = "Scan Now",
        ),
        ToolFeature(
            title = "Network Speed",
            subtitle = "Test your internet speed",
            route = Screen.NetworkSpeed.route,
            iconRes = R.drawable.ic_ok,
            gradient = Brush.linearGradient(
                colors = listOf(Color(0xFFCE93D8), Color(0xFF9C27B0)),
            ),
            actionLabel = "Test Now",
        ),
    )

/**
 * Displays 2 randomly-picked feature banners from the 8 toolbox modules.
 * Used in result pages (JunkClean, NetworkSpeed, etc.) to promote other features.
 */
@Composable
fun ToolFeatureBanners(
    onNavigate: (AppNavigationEvent) -> Unit = {},
    modifier: Modifier = Modifier,
    excludeRoutes: Set<String> = emptySet(),
    seed: Long = System.currentTimeMillis(),
) {
    val features = remember(seed) {
        val pool = AllToolFeatures.filter { it.route !in excludeRoutes }
        val shuffled = pool.shuffled(Random(seed))
        shuffled.take(2)
    }

    Column(modifier = modifier.fillMaxWidth()) {
        features.forEach { feature ->
            ToolFeatureBanner(
                feature = feature,
                onClick = { onNavigate(AppNavigationEvent.ToolFromResult(feature.route)) },
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ToolFeatureBanner(
    feature: ToolFeature,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = CardBg,
        shape = RoundedCornerShape(CardRadius),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 8.dp, top = 16.dp, bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .background(feature.gradient),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter = painterResource(id = feature.iconRes),
                        contentDescription = null,
                        modifier = Modifier.size(28.dp),
                        tint = Color.White,
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = feature.title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = Navy,
                    )
                    Text(
                        text = feature.subtitle,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Normal,
                        color = NavyMuted,
                    )
                }
            }

            TextButton(onClick = onClick) {
                Text(
                    text = feature.actionLabel,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    color = Blue,
                )
            }
        }
    }
}
