package com.quickcleanpro.phonecleaner.presentation.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.annotation.StringRes
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quickcleanpro.phonecleaner.R
import com.quickcleanpro.phonecleaner.presentation.ads.AdPlacement
import com.quickcleanpro.phonecleaner.presentation.ads.NativeAdSlot
import com.quickcleanpro.phonecleaner.presentation.common.route.Screen

data class CommonToolboxEntry(
    @param:StringRes val titleRes: Int,
    @param:StringRes val descriptionRes: Int,
    @param:StringRes val actionRes: Int,
    val iconRes: Int,
    val route: String,
    val iconBackground: Brush
)
private val CommonResultTitle = Color(0xFF2D3748)
private val CommonResultSecondary = Color(0xFF8190A5)
private val CommonResultBlue = Color(0xFF1AA7EC)

@Composable
fun CommonResultScreen(
    title: String,
    onBack: () -> Unit,
    onNavigateTool: (String) -> Unit,
    modifier: Modifier = Modifier,
    excludedToolRoutes: Set<String> = emptySet(),
    completionContent: @Composable () -> Unit
) {
    CleanXScaffold(
        title = title,
        onBack = onBack,
        modifier = modifier,
        containerColor = Color.White,
        horizontalPadding = 20.dp
    ) { paddingValues ->
        CommonResultContent(
            onNavigateTool = onNavigateTool,
            modifier = Modifier.padding(paddingValues),
            excludedToolRoutes = excludedToolRoutes,
            completionContent = completionContent
        )
    }
}

@Composable
fun CommonResultScreen(
    @StringRes titleRes: Int,
    onBack: () -> Unit,
    onNavigateTool: (String) -> Unit,
    modifier: Modifier = Modifier,
    excludedToolRoutes: Set<String> = emptySet(),
    completionContent: @Composable () -> Unit
) {
    CommonResultScreen(
        title = stringResource(titleRes),
        onBack = onBack,
        onNavigateTool = onNavigateTool,
        modifier = modifier,
        excludedToolRoutes = excludedToolRoutes,
        completionContent = completionContent
    )
}

@Composable
fun CommonResultContent(
    onNavigateTool: (String) -> Unit,
    modifier: Modifier = Modifier,
    excludedToolRoutes: Set<String> = emptySet(),
    completionContent: @Composable () -> Unit
) {
    val context = LocalContext.current
    val isWhatsAppAvailable = remember(context) { isWhatsAppInstalled(context) }
    val excludedKey = excludedToolRoutes.sorted().joinToString("|")
    val toolEntries = remember(excludedKey, isWhatsAppAvailable) {
        defaultCommonToolboxEntries()
            .availableForDevice(isWhatsAppAvailable)
            .filterNot { it.route in excludedToolRoutes }
            .shuffled()
            .take(2)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding()
            .padding(horizontal = 16.dp)
            .padding(bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(40.dp))
        completionContent()
        NativeAdSlot(placement = AdPlacement.ResultNative)
        Spacer(modifier = Modifier.height(36.dp))
        toolEntries.forEachIndexed { index, entry ->
            CommonToolboxEntryCard(
                entry = entry,
                onClick = { onNavigateTool(entry.route) }
            )
            if (index != toolEntries.lastIndex) {
                Spacer(modifier = Modifier.height(14.dp))
            }
        }
    }
}

@Composable
fun CommonResultCheckIcon(
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = 45.dp
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(Color(0xFFCFEFFF)),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(size * 0.74f)
                .clip(CircleShape)
                .background(CommonResultBlue),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(size * 0.52f)
            )
        }
    }
}

@Composable
private fun CommonToolboxEntryCard(
    entry: CommonToolboxEntry,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        color = Color(0xFFF7FAFD),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .padding(top = 1.dp)
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(entry.iconBackground),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(entry.iconRes),
                    contentDescription = null,
                    modifier = Modifier.size(27.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Top
            ) {
                Text(
                    text = stringResource(entry.titleRes),
                    color = CommonResultTitle,
                    fontSize = 16.sp,
                    lineHeight = 20.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(entry.descriptionRes),
                    color = CommonResultSecondary,
                    fontSize = 13.sp,
                    lineHeight = 16.sp,
//                    maxLines = 1,
//                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(entry.actionRes),
                color = CommonResultBlue,
                fontSize = 15.sp,
                lineHeight = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Bottom)
            )
        }
    }
}

private fun defaultCommonToolboxEntries(): List<CommonToolboxEntry> = listOf(
    CommonToolboxEntry(
        titleRes = R.string.nav_device_info,
        descriptionRes = R.string.common_tool_device_desc,
        actionRes = R.string.view_now,
        iconRes = R.drawable.ic_device,
        route = Screen.DeviceInfo.route,
        iconBackground = Brush.verticalGradient(listOf(Color(0xFFFFB16D), Color(0xFFFF842D)))
    ),
    CommonToolboxEntry(
        titleRes = R.string.nav_battery_info,
        descriptionRes = R.string.common_tool_battery_desc,
        actionRes = R.string.view_now,
        iconRes = R.drawable.ic_battery,
        route = Screen.BatteryInfo.route,
        iconBackground = Brush.verticalGradient(listOf(Color(0xFF7CEB9C), Color(0xFF34C86F)))
    ),
    CommonToolboxEntry(
        titleRes = R.string.nav_app_usage,
        descriptionRes = R.string.common_tool_app_usage_desc,
        actionRes = R.string.check_now,
        iconRes = R.drawable.ic_app_usage,
        route = Screen.AppUsage.route,
        iconBackground = Brush.verticalGradient(listOf(Color(0xFFFF6B7A), Color(0xFFFF3F55)))
    ),
    CommonToolboxEntry(
        titleRes = R.string.nav_notification_bar,
        descriptionRes = R.string.common_tool_notification_bar_desc,
        actionRes = R.string.open,
        iconRes = R.drawable.ic_notification_bar,
        route = Screen.NotificationBar.route,
        iconBackground = Brush.verticalGradient(listOf(Color(0xFF91A8FF), Color(0xFF526DFF)))
    ),
    CommonToolboxEntry(
        titleRes = R.string.nav_whatsapp_cleaner,
        descriptionRes = R.string.common_tool_whatsapp_desc,
        actionRes = R.string.clean_now,
        iconRes = R.drawable.ic_whats_app_cleaner,
        route = Screen.WhatsAppCleaner.route,
        iconBackground = Brush.verticalGradient(listOf(Color(0xFF6DE78D), Color(0xFF24C565)))
    ),
    CommonToolboxEntry(
        titleRes = R.string.nav_network_usage,
        descriptionRes = R.string.common_tool_network_usage_desc,
        actionRes = R.string.check_now,
        iconRes = R.drawable.ic_network_usage,
        route = Screen.NetworkUsage.route,
        iconBackground = Brush.verticalGradient(listOf(Color(0xFF68D8FF), Color(0xFF1AA7EC)))
    ),
    CommonToolboxEntry(
        titleRes = R.string.nav_network_scan,
        descriptionRes = R.string.common_tool_network_scan_desc,
        actionRes = R.string.scan_now,
        iconRes = R.drawable.ic_network_scan,
        route = Screen.NetworkScan.route,
        iconBackground = Brush.verticalGradient(listOf(Color(0xFFFFDF3B), Color(0xFFFFCA12)))
    ),
    CommonToolboxEntry(
        titleRes = R.string.nav_network_speed,
        descriptionRes = R.string.common_tool_network_speed_desc,
        actionRes = R.string.test_now,
        iconRes = R.drawable.ic_network_speed,
        route = Screen.NetworkSpeed.route,
        iconBackground = Brush.verticalGradient(listOf(Color(0xFFFF9359), Color(0xFFFF623C)))
    ),
    CommonToolboxEntry(
        titleRes = R.string.nav_notification_cleaner,
        descriptionRes = R.string.common_tool_notification_cleaner_desc,
        actionRes = R.string.check_now,
        iconRes = R.drawable.ic_n_notification_cleaner,
        route = Screen.NotificationCleaner.route,
        iconBackground = Brush.verticalGradient(listOf(Color(0xFFC082FF), Color(0xFF8E40F7)))
    )
)

internal fun List<CommonToolboxEntry>.availableForDevice(
    isWhatsAppAvailable: Boolean
): List<CommonToolboxEntry> =
    filterNot { entry ->
        entry.route == Screen.WhatsAppCleaner.route && !isWhatsAppAvailable
    }
