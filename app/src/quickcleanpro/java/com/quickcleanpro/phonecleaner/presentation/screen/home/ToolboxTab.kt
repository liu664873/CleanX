package com.quickcleanpro.phonecleaner.presentation.screen.home

import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quickcleanpro.phonecleaner.R
import com.quickcleanpro.phonecleaner.domain.model.device.BatteryInfo
import com.quickcleanpro.phonecleaner.presentation.ads.AdPlacement
import com.quickcleanpro.phonecleaner.presentation.ads.NativeAdSlot
import com.quickcleanpro.phonecleaner.presentation.common.CleanXCardColor
import com.quickcleanpro.phonecleaner.presentation.common.CleanXCardShape
import com.quickcleanpro.phonecleaner.presentation.common.CleanXCompactButtonHeight
import com.quickcleanpro.phonecleaner.presentation.common.CleanXGridSpacing
import com.quickcleanpro.phonecleaner.presentation.common.CleanXIconTile
import com.quickcleanpro.phonecleaner.presentation.common.CleanXInfoPanel
import com.quickcleanpro.phonecleaner.presentation.common.CleanXPillShape
import com.quickcleanpro.phonecleaner.presentation.common.CleanXSectionTitle
import com.quickcleanpro.phonecleaner.presentation.common.CleanXText
import com.quickcleanpro.phonecleaner.presentation.common.isWhatsAppInstalled
import com.quickcleanpro.phonecleaner.presentation.common.ToolboxDestination
import com.quickcleanpro.phonecleaner.presentation.common.route.AppNavigationEvent

private data class ToolboxItem(
    @param:StringRes val titleRes: Int,
    val iconRes: Int,
    val route: String
)

@Composable
internal fun ToolBoxTabContent(
    batteryInfo: BatteryInfo,
    deviceModel: String,
    androidVersion: String,
    onFeatureClick: () -> Unit = {},
    onNavigate: (AppNavigationEvent) -> Unit = {}
) {
    val context = LocalContext.current
    var showWhatsAppMissingDialog by remember { mutableStateOf(false) }
    val tools = remember {
        listOf(
            ToolboxItem(R.string.app_usage, R.drawable.ic_app_usage, ToolboxDestination.AppUsage.route),
            ToolboxItem(R.string.notification_bar, R.drawable.ic_notification_bar, ToolboxDestination.NotificationBar.route),
            ToolboxItem(R.string.whatsapp_cleaner, R.drawable.ic_whats_app_cleaner, ToolboxDestination.WhatsAppCleaner.route),
            ToolboxItem(R.string.network_usage, R.drawable.ic_network_usage, ToolboxDestination.NetworkUsage.route),
            ToolboxItem(R.string.network_scan, R.drawable.ic_network_scan, ToolboxDestination.NetworkScan.route),
            ToolboxItem(R.string.network_speed, R.drawable.ic_network_speed, ToolboxDestination.NetworkSpeed.route)
        )
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding(),
        contentPadding = PaddingValues(bottom = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(CleanXGridSpacing),
        verticalArrangement = Arrangement.spacedBy(CleanXGridSpacing)
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(CleanXGridSpacing)
            ) {
                DeviceInfoCard(
                    modifier = Modifier.weight(1f),
                    icon = R.drawable.ic_device,
                    labels = listOf(
                        R.string.home_device_model to deviceModel,
                        R.string.home_system_version to androidVersion
                    ),
                    buttonText = stringResource(R.string.check_now),
                    background = Color(0xFF5866E8),
                    onClick = {
                        onFeatureClick()
                        onNavigate(AppNavigationEvent.AdDestination(ToolboxDestination.DeviceInfo.route))
                    }
                )
                DeviceInfoCard(
                    modifier = Modifier.weight(1f),
                    icon = R.drawable.ic_battery,
                    labels = listOf(
                        R.string.home_battery_status to "${batteryInfo.levelPercent}% ${batteryInfo.technology}",
                        R.string.home_health to batteryInfo.health
                    ),
                    buttonText = stringResource(R.string.check_now),
                    background = Color(0xFF35C979),
                    onClick = {
                        onFeatureClick()
                        onNavigate(AppNavigationEvent.AdDestination(ToolboxDestination.BatteryInfo.route))
                    }
                )
            }
        }

        item(span = { GridItemSpan(maxLineSpan) }) {
            CleanXSectionTitle(
                text = stringResource(R.string.home_toolbox_title),
                modifier = Modifier.padding(top = 12.dp, bottom = 2.dp)
            )
        }

        item(span = { GridItemSpan(maxLineSpan) }) {
            NativeAdSlot(placement = AdPlacement.ToolboxNative)
        }

        items(tools) { tool ->
            val title = stringResource(tool.titleRes)
            CleanXIconTile(
                title = title,
                icon = painterResource(id = tool.iconRes),
                onClick = {
                    onFeatureClick()
                    if (tool.route == ToolboxDestination.WhatsAppCleaner.route && !isWhatsAppInstalled(context)) {
                        showWhatsAppMissingDialog = true
                    } else {
                        onNavigate(AppNavigationEvent.AdDestination(tool.route))
                    }
                }
            )
        }
    }

    if (showWhatsAppMissingDialog) {
        AlertDialog(
            onDismissRequest = { showWhatsAppMissingDialog = false },
            containerColor = CleanXCardColor,
            shape = CleanXCardShape,
            title = { Text(stringResource(R.string.whatsapp_not_found_title), color = CleanXText) },
            text = { Text(stringResource(R.string.whatsapp_not_found_message)) },
            confirmButton = {
                TextButton(onClick = { showWhatsAppMissingDialog = false }) {
                    Text(stringResource(R.string.ok))
                }
            }
        )
    }
}

@Composable
private fun DeviceInfoCard(
    modifier: Modifier,
    icon: Int,
    labels: List<Pair<Int, String>>,
    buttonText: String,
    background: Color,
    onClick: () -> Unit
) {
    CleanXInfoPanel(
        modifier = modifier.heightIn(min = 184.dp),
        background = background,
        contentPadding = PaddingValues(14.dp)
    ) {
        Image(
            painter = painterResource(id = icon),
            contentDescription = null,
            modifier = Modifier.size(40.dp),
            colorFilter = ColorFilter.tint(Color.White)
        )

        Spacer(modifier = Modifier.height(12.dp))

        labels.forEachIndexed { index, pair ->
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(pair.first),
                    color = Color.White.copy(alpha = 0.68f),
                    fontSize = 10.sp,
                    lineHeight = 13.sp,
                    fontWeight = FontWeight.Normal
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = pair.second,
                    color = Color.White,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (index != labels.lastIndex) {
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(CleanXCompactButtonHeight),
            shape = CleanXPillShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White,
                contentColor = CleanXText
            ),
            contentPadding = PaddingValues(horizontal = 8.dp)
        ) {
            Text(
                text = buttonText,
                fontSize = 14.sp,
                lineHeight = 17.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
