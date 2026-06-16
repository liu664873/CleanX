package com.quickcleanpro.phonecleaner.presentation.screen.networkusage

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quickcleanpro.phonecleaner.R
import com.quickcleanpro.phonecleaner.presentation.common.CleanXSegmentedTabs
import com.quickcleanpro.phonecleaner.presentation.common.CleanXTabItem
import com.quickcleanpro.phonecleaner.presentation.common.components.CleanXScaffoldPage
import com.quickcleanpro.phonecleaner.presentation.common.components.PackageAppIcon
import com.quickcleanpro.phonecleaner.presentation.common.components.RoundedProgressBar
import com.quickcleanpro.phonecleaner.presentation.common.components.styles.CleanXBlue
import com.quickcleanpro.phonecleaner.presentation.common.permission.CleanXPermissionFeature
import com.quickcleanpro.phonecleaner.presentation.common.permission.PermissionGatePresets

private val CardBg = Color(0xFFF6F7FB)
private val Navy = Color(0xFF1D2959)
private val TitleNavy = Color(0xFF2D3748)
private val NavyMuted = Color(0xA61D2959)
private val ValueMuted = Color(0xFF8190A5)
private val Divider15 = Color(0x332D3748)
private val CardRadius = 12.dp

private data class NetworkAppEntry(
    val appName: String,
    val packageName: String?,
    val traffic: String,
    val progress: Float,
    val fallbackText: String,
)

@Composable
fun NetworkUsageScreen() {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs =
        listOf(
            CleanXTabItem(
                title = stringResource(R.string.cellular_today),
                value = "0 MB",
            ),
            CleanXTabItem(
                title = stringResource(R.string.wifi_today),
                value = "84.7 MB",
            ),
        )

    CleanXScaffoldPage(
        title = stringResource(R.string.network_usage),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        permissionGateConfig = PermissionGatePresets.usageAccess(CleanXPermissionFeature.NetworkUsage),
    ) {
        CleanXSegmentedTabs(
            items = tabs,
            selectedIndex = selectedTab,
            onSelected = { selectedTab = it },
            cornerRadius = 12.dp,
            horizontalSpacing = 12.dp,
            horizontalPadding = 10.dp,
            verticalPadding = 8.dp,
            fontSize = 20.sp,
            lineHeight = 24.sp,
            valueFontSize = 14.sp,
            valueLineHeight = 18.sp,
            unselectedContainerColor = Color.Transparent,
        )

        Spacer(modifier = Modifier.height(20.dp))

        if (selectedTab == 0) {
            EmptyNetworkState()
        } else {
            NetworkSummaryCard()
            Spacer(modifier = Modifier.height(16.dp))
            NetworkAppList()
        }

        Spacer(modifier = Modifier.height(100.dp))
    }
}

@Composable
private fun EmptyNetworkState() {
    Box(
        modifier = Modifier.fillMaxWidth().padding(top = 74.dp),
        contentAlignment = Alignment.TopCenter,
    ) {
        Image(
            painter = painterResource(id = R.drawable.network_usage_empty),
            contentDescription = null,
            modifier = Modifier.size(256.dp),
            contentScale = ContentScale.Fit,
        )
    }
}

@Composable
private fun NetworkSummaryCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = CardBg,
        shape = RoundedCornerShape(CardRadius),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().height(98.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SummaryColumn(
                value = "404.0 MB",
                label = stringResource(R.string.total_usage_today),
            )
            SummaryDivider()
            SummaryColumn(
                value = "354.9 MB",
                label = stringResource(R.string.downloads_today),
            )
            SummaryDivider()
            SummaryColumn(
                value = "49.1 MB",
                label = stringResource(R.string.uploads_today),
            )
        }
    }
}

@Composable
private fun SummaryDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(18.dp)
            .background(Divider15),
    )
}

@Composable
private fun SummaryColumn(value: String, label: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = value,
            color = Navy,
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
        )
        Text(
            text = label,
            color = Navy,
            fontSize = 10.sp,
            lineHeight = 16.sp,
            fontWeight = FontWeight.Normal,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun NetworkAppList() {
    val entries =
        listOf(
            NetworkAppEntry(
                appName = "Tencent Meeting",
                packageName = "com.tencent.wemeet.app",
                traffic = "48.5MB",
                progress = 0.60f,
                fallbackText = "T",
            ),
            NetworkAppEntry(
                appName = "Facebook",
                packageName = "com.facebook.katana",
                traffic = "15.5MB",
                progress = 0.27f,
                fallbackText = "F",
            ),
        )

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = CardBg,
        shape = RoundedCornerShape(CardRadius),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            entries.forEachIndexed { index, entry ->
                NetworkAppRow(entry = entry)
                if (index < entries.lastIndex) {
                    HorizontalDivider(color = Divider15, thickness = 1.dp)
                }
            }
        }
    }
}

@Composable
private fun NetworkAppRow(entry: NetworkAppEntry) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PackageAppIcon(
            packageName = entry.packageName,
            fallbackText = entry.fallbackText,
            color = CleanXBlue,
            modifier = Modifier.size(44.dp),
        )

        Spacer(modifier = Modifier.width(8.dp))

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = entry.appName,
                    color = TitleNavy,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = entry.traffic,
                    color = ValueMuted,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    maxLines = 1,
                )
            }
            RoundedProgressBar(
                progress = entry.progress,
                width = 209.dp,
                height = 4.dp,
                trackColor = Navy.copy(alpha = 0.15f),
                fillColor = CleanXBlue,
            )
        }
    }
}
