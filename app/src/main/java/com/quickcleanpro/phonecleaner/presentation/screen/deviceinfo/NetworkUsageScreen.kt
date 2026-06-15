package com.quickcleanpro.phonecleaner.presentation.screen.deviceinfo

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
private val Divider15 = Color(0x261D2959)
private val Blue = Color(0xFF4179FC)
private val CardRadius = 12.dp

private data class NetworkAppEntry(
    val appName: String,
    val traffic: String,
)

@Composable
fun NetworkUsageScreen(onBack: () -> Unit = {}) {
    // 0 = Cellular, 1 = Wi-Fi
    var selectedTab by remember { mutableIntStateOf(0) }
    // Toggle: true = show data, false = show empty
    val hasData = remember { selectedTab == 1 }

    val tabs = listOf(
        stringResource(R.string.cellular_today),
        stringResource(R.string.wifi_today),
    )

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            CleanXTopAppBar(
                title = stringResource(R.string.network_usage),
                onBack = onBack,
                modifier = Modifier.systemBarsPadding(),
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
                    .padding(horizontal = 16.dp),
            ) {
                // Tab bar
                Spacer(modifier = Modifier.height(16.dp))
                NetworkTabBar(
                    tabs = tabs,
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it },
                )

                if (!hasData) {
                    // Empty state
                    EmptyNetworkState()
                } else {
                    // Data state
                    Spacer(modifier = Modifier.height(16.dp))
                    NetworkSummaryCard()

                    Spacer(modifier = Modifier.height(16.dp))
                    NetworkAppList()
                }

                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}

@Composable
private fun NetworkTabBar(
    tabs: List<String>,
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        tabs.forEachIndexed { index, tab ->
            val isSelected = selectedTab == index
            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(CardRadius))
                    .clickable { onTabSelected(index) },
                color = if (isSelected) Blue else Color.Transparent,
                shape = RoundedCornerShape(CardRadius),
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = tab,
                        color = if (isSelected) Color.White else NavyMuted,
                        fontSize = 20.sp,
                        fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                        textAlign = TextAlign.Start,
                    )
                    Text(
                        text = if (index == 0) "0 MB" else "84.7 MB",
                        color = if (isSelected) Color.White else NavyMuted,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                        textAlign = TextAlign.Start,
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyNetworkState() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 136.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Empty illustration placeholder
            Box(
                modifier = Modifier.size(256.dp),
                contentAlignment = Alignment.Center,
            ) {
                // Use robot image as fallback placeholder for empty network state
                androidx.compose.foundation.Image(
                    painter = painterResource(id = R.drawable.robot),
                    contentDescription = null,
                    modifier = Modifier.size(200.dp),
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.no_data_available),
                color = NavyMuted,
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
                textAlign = TextAlign.Center,
            )
        }
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
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Total Usage
            SummaryColumn(
                value = "404.0 MB",
                label = stringResource(R.string.total_usage_today),
            )
            // Vertical divider
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(38.dp)
                    .background(Divider15),
            )
            // Downloads
            SummaryColumn(
                value = "354.9 MB",
                label = stringResource(R.string.downloads_today),
            )
            // Vertical divider
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(38.dp)
                    .background(Divider15),
            )
            // Uploads
            SummaryColumn(
                value = "49.1 MB",
                label = stringResource(R.string.uploads_today),
            )
        }
    }
}

@Composable
private fun SummaryColumn(value: String, label: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = value,
            color = Navy,
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            color = Navy,
            fontSize = 10.sp,
            fontWeight = FontWeight.Normal,
            lineHeight = 16.sp,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun NetworkAppList() {
    val entries = listOf(
        NetworkAppEntry("Tencent Meeting", "48.5MB"),
        NetworkAppEntry("Facebook", "15.5MB"),
    )

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = CardBg,
        shape = RoundedCornerShape(CardRadius),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 20.dp),
        ) {
            entries.forEachIndexed { index, entry ->
                if (index > 0) {
                    HorizontalDivider(color = Divider15, thickness = 1.dp)
                }
                NetworkAppRow(entry = entry)
            }
        }
    }
}

@Composable
private fun NetworkAppRow(entry: NetworkAppEntry) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // App icon + name
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // App icon placeholder (colored circle with first letter)
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF0F3F7)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = entry.appName.take(1),
                    color = Navy,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                )
            }
            Text(
                text = entry.appName,
                color = Navy,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
            )
        }

        // Traffic
        Text(
            text = entry.traffic,
            color = NavyMuted,
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
        )
    }
}
