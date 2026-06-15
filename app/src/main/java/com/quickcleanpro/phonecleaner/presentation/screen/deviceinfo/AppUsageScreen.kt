package com.quickcleanpro.phonecleaner.presentation.screen.deviceinfo

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
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
private val NavyLight = Color(0x591D2959)
private val Divider = Color(0x261D2959)
private val Blue = Color(0xFF4179FC)
private val CardRadius = 12.dp

private data class AppUsageEntry(
    val appName: String,
    val duration: String,
    val color: Color,
)

private data class TimeRange(
    @androidx.annotation.StringRes val labelRes: Int,
)

@Composable
fun AppUsageScreen(onBack: () -> Unit = {}) {
    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            CleanXTopAppBar(
                title = stringResource(R.string.app_usage),
                onBack = onBack,
                modifier = Modifier.systemBarsPadding(),
                titleFontSize = 22.sp,
                fontWeight = FontWeight.SemiBold,
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
                // Card 1: Pie Chart + Legend
                PieChartCard()

                Spacer(modifier = Modifier.height(16.dp))

                // Card 2: Usage Data with tabs
                UsageDataCard()
            }
        }
    }
}

@Composable
private fun PieChartCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = CardBg,
        shape = RoundedCornerShape(CardRadius),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp, 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Donut Chart
            Box(
                modifier = Modifier.size(144.dp),
                contentAlignment = Alignment.Center,
            ) {
                DonutChart(
                    modifier = Modifier.size(144.dp),
                    items = listOf(
                        DonutItem(0.35f, Color(0xFFFF565D)),
                        DonutItem(0.25f, Color(0xFFA03CFE)),
                        DonutItem(0.22f, Color(0xFF80F17D)),
                        DonutItem(0.18f, Color(0xFF4F75FE)),
                    ),
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "2hr 35",
                        color = Navy,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        text = "min",
                        color = Navy,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                ) {
                    LegendItem(
                        color = Color(0xFFFF565D),
                        label = stringResource(R.string.app_usage_legend_storage_clean),
                    )
                    LegendItem(
                        color = Color(0xFFA03CFE),
                        label = "Other",
                    )
                    LegendItem(
                        color = Color(0xFF80F17D),
                        label = stringResource(R.string.app_usage_legend_feishu),
                    )
                    LegendItem(
                        color = Color(0xFF4F75FE),
                        label = stringResource(R.string.app_usage_legend_one_ui_home),
                    )
                }
            }
        }
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(
            modifier = Modifier
                .size(5.dp)
                .clip(CircleShape)
                .background(color),
        )
        Text(
            text = label,
            color = Navy,
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
        )
    }
}

@Composable
private fun UsageDataCard() {
    // 0 = Hours spent, 1 = Times opened
    var selectedTab by remember { mutableIntStateOf(0) }
    // Time range: 0=Today, 1=Yesterday, 2=Last 7 days, 3=Last 30 days
    var selectedTimeRange by remember { mutableIntStateOf(0) }

    val timeRanges = listOf(
        TimeRange(R.string.today),
        TimeRange(R.string.yesterday),
        TimeRange(R.string.last_7_days),
        TimeRange(R.string.last_30_days),
    )

    val hoursEntries = listOf(
        AppUsageEntry("Tencent Meeting", "2hour", Color(0xFF4F75FE)),
        AppUsageEntry("Facebook", "48minutes", Color(0xFF4F75FE)),
    )

    val timesEntries = listOf(
        AppUsageEntry("Tencent Meeting", "12 times", Color(0xFF4F75FE)),
        AppUsageEntry("Facebook", "8 times", Color(0xFF4F75FE)),
    )

    val currentEntries = if (selectedTab == 0) hoursEntries else timesEntries

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = CardBg,
        shape = RoundedCornerShape(CardRadius),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp, 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Hours spent / Times opened tabs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(42.dp),
                ) {
                    Text(
                        text = stringResource(R.string.hours_spent),
                        color = if (selectedTab == 0) Navy else NavyMuted,
                        fontSize = 20.sp,
                        fontWeight = if (selectedTab == 0) FontWeight.Medium else FontWeight.Normal,
                        modifier = Modifier.clickable { selectedTab = 0 },
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        text = stringResource(R.string.times_opened),
                        color = if (selectedTab == 1) Navy else NavyMuted,
                        fontSize = 20.sp,
                        fontWeight = if (selectedTab == 1) FontWeight.Medium else FontWeight.Normal,
                        modifier = Modifier.clickable { selectedTab = 1 },
                        textAlign = TextAlign.Center,
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Divider, thickness = 1.dp)
            Spacer(modifier = Modifier.height(16.dp))

            // App usage list
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                currentEntries.forEach { entry ->
                    AppUsageRow(entry = entry)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Time range selector (horizontally scrollable)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                timeRanges.forEachIndexed { index, range ->
                    val isSelected = selectedTimeRange == index
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .clickable { selectedTimeRange = index },
                        color = if (isSelected) Blue else CardBg,
                        shape = RoundedCornerShape(4.dp),
                    ) {
                        Text(
                            text = stringResource(range.labelRes),
                            color = if (isSelected) Color.White else NavyMuted,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AppUsageRow(entry: AppUsageEntry) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            // App icon placeholder
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(entry.color.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = entry.appName.take(1),
                    color = entry.color,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                )
            }
            Text(
                text = entry.appName,
                color = Navy,
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = entry.duration,
                color = NavyMuted,
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
            )
            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { },
                color = Blue.copy(alpha = 0.65f),
                shape = RoundedCornerShape(8.dp),
            ) {
                Text(
                    text = stringResource(R.string.stop),
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                )
            }
        }
    }
}

private data class DonutItem(
    val fraction: Float,
    val color: Color,
)

@Composable
private fun DonutChart(
    modifier: Modifier,
    items: List<DonutItem>,
) {
    Canvas(modifier = modifier) {
        val strokeWidth = 21.dp.toPx()
        val radius = (size.minDimension - strokeWidth) / 2
        val topLeft = Offset(
            (size.width - radius * 2) / 2,
            (size.height - radius * 2) / 2,
        )
        val arcSize = Size(radius * 2, radius * 2)
        var startAngle = -90f

        items.forEach { item ->
            val sweepAngle = item.fraction * 360f
            drawArc(
                color = item.color,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Butt),
            )
            startAngle += sweepAngle
        }
    }
}
