package com.quickcleanpro.phonecleaner.presentation.screen.appusage

import android.content.ActivityNotFoundException
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.quickcleanpro.phonecleaner.R
import com.quickcleanpro.phonecleaner.domain.model.toolbox.AppUsageInfo
import com.quickcleanpro.phonecleaner.presentation.common.CleanXSegmentedTabs
import com.quickcleanpro.phonecleaner.presentation.common.CleanXTabItem
import com.quickcleanpro.phonecleaner.presentation.common.components.CleanXScaffoldPage
import com.quickcleanpro.phonecleaner.presentation.common.components.PackageAppIcon
import com.quickcleanpro.phonecleaner.presentation.common.components.RoundedProgressBar
import com.quickcleanpro.phonecleaner.presentation.common.components.styles.CleanXBlue
import com.quickcleanpro.phonecleaner.presentation.common.permission.CleanXPermissionFeature
import com.quickcleanpro.phonecleaner.presentation.common.permission.PermissionGatePresets
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import java.util.Locale

// ==================== 颜色与样式常量 ====================
private val CardBg = Color(0xFFF6F7FB)
private val Navy = Color(0xFF1D2959)
private val NavyMuted = Color(0xA61D2959)
private val Divider = Color(0x332D3748)
private val Blue = Color(0xFF4179FC)
private val CardRadius = 12.dp
private val OtherColor = Color(0xFFB0BEC5) // “其他”扇区颜色

// 甜甜圈图颜色（前3个应用）
private val DonutColors = listOf(
    Color(0xFFFF565D),
    Color(0xFFA03CFE),
    Color(0xFF80F17D),
)

@Composable
fun AppUsageScreen(viewModel: AppUsageViewModel = koinViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current

    DisposableEffect(lifecycleOwner, viewModel) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshAfterResume()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    CleanXScaffoldPage(
        title = stringResource(R.string.app_usage),
        backgroundBrush = Brush.linearGradient(
            colors = listOf(Color(0xFFE3ECFD), Color(0xFFDFEBF5))
        ),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        permissionGateConfig = PermissionGatePresets.usageAccess(CleanXPermissionFeature.AppUsage),
    ) {
        // 1. 时间选择器
        TimeRangeSelector(
            selectedRange = uiState.selectedRange,
            onRangeSelected = viewModel::selectRange,
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 2. 甜甜圈图卡片
        PieChartCard(uiState = uiState)

        Spacer(modifier = Modifier.height(16.dp))

        // 3. 使用数据卡片（含滑动切换 Tab）
        UsageDataCard(
            uiState = uiState,
            onTabSelected = viewModel::selectTab,
            onStopApp = { packageName ->
                try {
                    context.startActivity(viewModel.appInfoIntent(packageName))
                } catch (_: ActivityNotFoundException) {
                    // 忽略
                }
            },
        )

        Spacer(modifier = Modifier.height(100.dp))
    }
}

// ==================== 时间选择器 ====================
@Composable
private fun TimeRangeSelector(
    selectedRange: AppUsageDateRange,
    onRangeSelected: (AppUsageDateRange) -> Unit,
) {
    val ranges = AppUsageDateRange.entries
    CleanXSegmentedTabs(
        items = ranges.map { range -> CleanXTabItem(title = stringResource(range.labelRes)) },
        selectedIndex = ranges.indexOf(selectedRange).coerceAtLeast(0),
        onSelected = { index -> ranges.getOrNull(index)?.let(onRangeSelected) },
        horizontalSpacing = 20.dp,
        horizontalPadding = 8.dp,
        verticalPadding = 8.dp,
        fontSize = 18.sp,
        lineHeight = 22.sp,
    )
}

// ==================== 甜甜圈图卡片（前3个应用 + 其他） ====================
@Composable
private fun PieChartCard(uiState: AppUsageUiState) {
    // 使用完整的 usages 列表，按当前 Tab 排序
    val allUsages = uiState.usages
    val sortedUsages = when (uiState.selectedTab) {
        AppUsageMetricTab.Duration -> allUsages.sortedWith(
            compareByDescending<AppUsageInfo> { it.totalForegroundMs }
                .thenByDescending { it.launchCount }
                .thenBy { it.appName.lowercase(Locale.US) }
        )
        AppUsageMetricTab.LaunchCount -> allUsages.sortedWith(
            compareByDescending<AppUsageInfo> { it.launchCount }
                .thenByDescending { it.totalForegroundMs }
                .thenBy { it.appName.lowercase(Locale.US) }
        )
    }

    val topItems = sortedUsages.take(3)          // 前3个
    val otherItems = sortedUsages.drop(3)        // 其余全部

    val donutItems = mutableListOf<DonutItem>()
    topItems.forEachIndexed { index, usage ->
        val fraction = if (uiState.totalUsageMs > 0) {
            usage.totalForegroundMs.toFloat() / uiState.totalUsageMs
        } else 0f
        donutItems.add(
            DonutItem(
                fraction = fraction.coerceAtLeast(0.02f),
                color = donutColor(index)
            )
        )
    }
    if (otherItems.isNotEmpty()) {
        val otherTotalMs = otherItems.sumOf { it.totalForegroundMs }
        val otherFraction = if (uiState.totalUsageMs > 0) {
            otherTotalMs.toFloat() / uiState.totalUsageMs
        } else 0f
        donutItems.add(
            DonutItem(
                fraction = otherFraction.coerceAtLeast(0.02f),
                color = OtherColor
            )
        )
    }

    // 无数据时显示占位环
    if (donutItems.isEmpty()) {
        donutItems.add(DonutItem(1f, NavyMuted.copy(alpha = 0.2f)))
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = CardBg,
        shape = RoundedCornerShape(CardRadius),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier.size(144.dp),
                contentAlignment = Alignment.Center,
            ) {
                DonutChart(
                    modifier = Modifier.size(144.dp),
                    items = donutItems,
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val totalText = formatDurationForCenter(uiState.totalUsageMs)
                    Text(
                        text = totalText,
                        color = Navy,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 图例：前3个应用 + “其他”
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                ) {
                    topItems.forEachIndexed { index, usage ->
                        LegendItem(
                            color = donutColor(index),
                            label = usage.appName,
                        )
                    }
                    if (otherItems.isNotEmpty()) {
                        LegendItem(
                            color = OtherColor,
                            label = stringResource(R.string.other),
                        )
                    }
                    if (allUsages.isEmpty()) {
                        LegendItem(
                            color = NavyMuted.copy(alpha = 0.3f),
                            label = stringResource(R.string.no_usage_data_available),
                        )
                    }
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
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

// ==================== 使用数据卡片（支持滑动切换 Tab） ====================
@Composable
private fun UsageDataCard(
    uiState: AppUsageUiState,
    onTabSelected: (AppUsageMetricTab) -> Unit,
    onStopApp: (String) -> Unit,
) {
    val pagerState = rememberPagerState(
        initialPage = if (uiState.selectedTab == AppUsageMetricTab.Duration) 0 else 1,
        pageCount = { 2 }
    )
    val coroutineScope = rememberCoroutineScope()

    // 当页面滑动时，同步更新 Tab
    LaunchedEffect(pagerState.currentPage) {
        val newTab = if (pagerState.currentPage == 0) AppUsageMetricTab.Duration else AppUsageMetricTab.LaunchCount
        if (newTab != uiState.selectedTab) {
            onTabSelected(newTab)
        }
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = CardBg,
        shape = RoundedCornerShape(CardRadius),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Tab 文字行（点击切换）
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(42.dp)) {
                    AppUsageHeaderTab(
                        selected = uiState.selectedTab == AppUsageMetricTab.Duration,
                        title = stringResource(AppUsageMetricTab.Duration.titleRes),
                    ) {
                        if (pagerState.currentPage != 0) {
                            coroutineScope.launch { pagerState.animateScrollToPage(0) }
                        }
                    }
                    AppUsageHeaderTab(
                        selected = uiState.selectedTab == AppUsageMetricTab.LaunchCount,
                        title = stringResource(AppUsageMetricTab.LaunchCount.titleRes),
                    ) {
                        if (pagerState.currentPage != 1) {
                            coroutineScope.launch { pagerState.animateScrollToPage(1) }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Divider, thickness = 1.dp)
            Spacer(modifier = Modifier.height(24.dp))

            // 水平滑动的 Pager
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxWidth(),
            ) { page ->
                // 两个页面显示相同列表（根据当前 Tab 排序），但指标文字会因 selectedTab 不同而变化
                if (uiState.visibleItems.isEmpty()) {
                    EmptyUsageText()
                } else {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(24.dp),
                    ) {
                        uiState.visibleItems.forEach { item ->
                            AppUsageRow(
                                item = item,
                                selectedTab = uiState.selectedTab,
                                onStopApp = onStopApp,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyUsageText() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(R.string.no_usage_data_available),
            color = NavyMuted,
            fontSize = 16.sp,
        )
    }
}

// ==================== 列表行（含进度条） ====================
@Composable
private fun AppUsageHeaderTab(
    selected: Boolean,
    title: String,
    onClick: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier =
            Modifier.clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            ),
    ) {
        Text(
            text = title,
            color = if (selected) Navy else NavyMuted,
            fontSize = 20.sp,
            fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal,
            textAlign = TextAlign.Center,
        )
        Box(
            modifier =
                Modifier
                    .width(86.dp)
                    .height(2.dp)
                    .background(if (selected) Blue else Color.Transparent),
        )
    }
}

@Composable
private fun AppUsageRow(
    item: AppUsageDisplayItem,
    selectedTab: AppUsageMetricTab,
    onStopApp: (String) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PackageAppIcon(
            packageName = item.packageName,
            fallbackText = item.iconText.take(1).ifBlank { "A" },
            color = donutColor(item.colorIndex),
            modifier = Modifier.size(44.dp)
        )

        Spacer(modifier = Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.appName,
                color = Navy,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(12.dp))
            RoundedProgressBar(
                progress = item.progress,
                width = 185.dp,
                height = 4.dp,
                trackColor = Navy.copy(alpha = 0.15f),
                fillColor = CleanXBlue,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = itemValueText(item, selectedTab),
                color = NavyMuted,
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Button(
            onClick = { onStopApp(item.packageName) },
            enabled = item.isRunning,
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .height(32.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Blue.copy(alpha = if (item.isRunning) 0.65f else 0.3f),
                disabledContainerColor = Blue.copy(alpha = 0.3f),
                contentColor = Color.White,
                disabledContentColor = Color.White,
            ),
        ) {
            Text(
                text = stringResource(R.string.stop),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

@Composable
private fun itemValueText(item: AppUsageDisplayItem, selectedTab: AppUsageMetricTab): String {
    return when (selectedTab) {
        AppUsageMetricTab.Duration -> formatDuration(item.totalForegroundMs)
        AppUsageMetricTab.LaunchCount -> pluralStringResource(
            R.plurals.launch_times_count,
            item.launchCount,
            item.launchCount
        )
    }
}

// ==================== 环形图绘制 ====================
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

        if (items.isEmpty()) {
            drawArc(
                color = NavyMuted.copy(alpha = 0.2f),
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Butt),
            )
            return@Canvas
        }

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

// ==================== 辅助函数 ====================
private fun donutColor(index: Int): Color = DonutColors[index % DonutColors.size]

@Composable
private fun formatDurationForCenter(totalMs: Long): String {
    val totalMinutes = (totalMs / 60_000L).coerceAtLeast(0L)
    val hours = totalMinutes / 60L
    val minutes = totalMinutes % 60L
    return when {
        hours > 0L && minutes > 0L -> "${hours}hr ${minutes}\nmin"
        hours > 0L -> "${hours}hr"
        else -> "${minutes}\nmin"
    }
}

@Composable
private fun formatDuration(totalMs: Long): String {
    val totalMinutes = (totalMs / 60_000L).coerceAtLeast(0L)
    val hours = totalMinutes / 60L
    val minutes = totalMinutes % 60L
    return when {
        hours > 0L && minutes > 0L -> "${hours}hour ${minutes}minutes"
        hours > 0L -> "${hours}hour"
        else -> "${minutes}minutes"
    }
}
