package com.quickcleanpro.phonecleaner.presentation.screen.tools.batteryinfo

import android.graphics.Paint
import android.graphics.Typeface
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.quickcleanpro.phonecleaner.R
import com.quickcleanpro.phonecleaner.presentation.common.components.CleanXScaffoldPage
import com.quickcleanpro.phonecleaner.presentation.common.components.animations.CleanSpiralAnimation
import com.quickcleanpro.phonecleaner.presentation.common.components.popups.StopScanDialog
import com.quickcleanpro.phonecleaner.presentation.common.components.styles.CleanXBackground
import com.quickcleanpro.phonecleaner.presentation.common.components.styles.CleanXBlue
import com.quickcleanpro.phonecleaner.presentation.common.components.styles.CleanXMutedText
import com.quickcleanpro.phonecleaner.presentation.common.components.styles.CleanXText
import com.quickcleanpro.phonecleaner.presentation.common.route.LocalRouter
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.components.FileManagerNavy
import com.quickcleanpro.phonecleaner.presentation.screen.tools.common.device.BatteryInfoViewModel
import com.quickcleanpro.phonecleaner.presentation.screen.tools.common.device.BatteryCurrentRange
import com.quickcleanpro.phonecleaner.presentation.screen.tools.common.device.BatteryCurrentSample
import com.quickcleanpro.phonecleaner.presentation.screen.tools.common.device.DeviceInfoMode
import com.quickcleanpro.phonecleaner.presentation.screen.tools.common.device.DeviceInfoUiState
import com.quickcleanpro.phonecleaner.presentation.screen.tools.common.device.displayBatteryTemperatureValue
import com.quickcleanpro.phonecleaner.presentation.screen.tools.common.device.formatBatteryCurrent
import com.quickcleanpro.phonecleaner.presentation.screen.tools.common.device.formatBatteryTemperature
import com.quickcleanpro.phonecleaner.presentation.screen.tools.common.device.formatBatteryVoltage
import kotlinx.coroutines.delay
import org.koin.androidx.compose.koinViewModel
import java.util.Locale
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max

private val ChartGrid = Color(0xFF9AA6B8).copy(alpha = 0.72f)
private val ChartFill = Color(0xFF5666F5).copy(alpha = 0.14f)
private val TabBackground = Color(0xFFE7E9F1)

@Composable
fun BatteryInfoScreen(viewModel: BatteryInfoViewModel = koinViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current
    val router = LocalRouter.current
    var showScanning by remember { mutableStateOf(true) }
    var showExitDialog by remember { mutableStateOf(false) }
    var scanSession by remember { mutableStateOf(0) }

    DisposableEffect(lifecycleOwner, viewModel) {
        fun startPage() {
            showScanning = true
            showExitDialog = false
            scanSession += 1
            viewModel.load(DeviceInfoMode.Battery)
            viewModel.startBatterySampling()
        }

        startPage()
        val observer =
            LifecycleEventObserver { _, event ->
                when (event) {
                    Lifecycle.Event.ON_RESUME -> startPage()
                    Lifecycle.Event.ON_PAUSE,
                    Lifecycle.Event.ON_STOP,
                    -> viewModel.stopBatterySampling()
                    else -> Unit
                }
            }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            viewModel.stopBatterySampling()
        }
    }

    LaunchedEffect(scanSession, showScanning, showExitDialog) {
        if (!showScanning || showExitDialog) return@LaunchedEffect
        delay(1500L)
        showScanning = false
    }

    fun handleBack() {
        if (showScanning) {
            showExitDialog = true
        } else {
            router.goBack()
        }
    }

    CleanXScaffoldPage(
        title = stringResource(R.string.battery_info),
        backgroundBrush = Brush.linearGradient(
            colors = listOf(CleanXBackground, CleanXBackground),
        ),
        onBack = { handleBack() },
        contentPadding = if (showScanning) PaddingValues(0.dp) else PaddingValues(horizontal = 16.dp),
        scrollEnabled = !showScanning,
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        if (showScanning) {
            BackHandler { handleBack() }

            BatteryScanningContent()
        } else {
            BatteryTopCard(
                statusText = localizedBatteryStatus(uiState.batteryStatus.statusText),
                capacityText = uiState.batteryCapacityText,
            )
            BatteryMetricGrid(uiState)
            BatteryTemperatureCard(uiState)
            BatteryElectricCurrentCard(
                uiState = uiState,
                onRangeSelected = viewModel::selectCurrentRange,
            )
            Spacer(modifier = Modifier.navigationBarsPadding().height(24.dp))
        }
    }

    if (showExitDialog) {
        StopScanDialog(
            onQuit = {
                showExitDialog = false
                router.goBack()
            },
            onResume = {
                showExitDialog = false
                scanSession += 1
            },
        )
    }
}

@Composable
private fun BatteryScanningContent() {
    val text = stringResource(R.string.battery_info)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 67.dp),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CleanSpiralAnimation {
                Image(
                    painter = painterResource(R.drawable.ic_phone),
                    contentDescription = text,
                    modifier = Modifier.size(70.dp),
                )
            }
        }
    }
}

@Composable
private fun BatteryMetricGrid(uiState: DeviceInfoUiState) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            BatteryMetricCard(
                iconRes = R.drawable.temperature,
                value = formatBatteryTemperature(uiState.displayTemperatureC, uiState.temperatureUnit),
                label = stringResource(R.string.battery_temperature),
                modifier = Modifier.weight(1f),
            )
            BatteryMetricCard(
                iconRes = R.drawable.voltage,
                value = formatBatteryVoltage(uiState.battery.voltage),
                label = stringResource(R.string.battery_voltage),
                modifier = Modifier.weight(1f),
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            BatteryMetricCard(
                iconRes = R.drawable.battery_health,
                value = localizedBatteryHealth(uiState.battery.health),
                label = stringResource(R.string.battery_health),
                modifier = Modifier.weight(1f),
            )
            BatteryMetricCard(
                iconRes = R.drawable.life,
                value = uiState.batteryLifeText,
                label = stringResource(R.string.battery_life),
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun BatteryTopCard(
    statusText: String,
    capacityText: String,
) {
    Surface(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(134.dp),
        color = Color.White,
        shape = RoundedCornerShape(14.dp),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(start = 22.dp, end = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                BatterySummaryLine(
                    label = stringResource(R.string.battery_status),
                    value = statusText,
                )
                BatterySummaryLine(
                    label = stringResource(R.string.battery_capacity),
                    value = capacityText,
                )
            }
            Image(
                painter = painterResource(id = R.drawable.battery),
                contentDescription = null,
                modifier = Modifier.size(width = 104.dp, height = 78.dp),
            )
        }
    }
}

@Composable
private fun BatterySummaryLine(
    label: String,
    value: String,
) {
    Column {
        Text(
            text = label,
            color = CleanXMutedText,
            fontSize = 14.sp,
            lineHeight = 18.sp,
        )
        Text(
            text = value,
            color = CleanXText,
            fontSize = 20.sp,
            lineHeight = 26.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun BatteryMetricCard(
    iconRes: Int,
    value: String,
    label: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.height(144.dp),
        color = Color.White,
        shape = RoundedCornerShape(14.dp),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                modifier = Modifier.size(48.dp),
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = value,
                color = CleanXText,
                fontSize = 20.sp,
                lineHeight = 24.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = label,
                color = CleanXMutedText,
                fontSize = 13.sp,
                lineHeight = 17.sp,
                maxLines = 1,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun BatteryTemperatureCard(uiState: DeviceInfoUiState) {
    BatteryChartCard(
        title = stringResource(R.string.battery_temperature_title),
        rows =
            listOf(
                stringResource(R.string.battery_realtime_temperature) to
                    formatBatteryTemperature(uiState.displayTemperatureC, uiState.temperatureUnit, includeSpace = false),
                stringResource(R.string.battery_average_temperature) to
                    formatBatteryTemperature(uiState.selectedTemperatureAverageC, uiState.temperatureUnit, includeSpace = false),
            ),
    ) {
        val chartPoints =
            uiState.selectedTemperatureSamples.map {
                ChartPoint(
                    timestampMillis = it.timestampMillis,
                    value = displayBatteryTemperatureValue(it.temperatureC, uiState.temperatureUnit),
                )
            }
        BatteryLineChart(
            points = chartPoints,
            windowEndMillis = oneMinuteChartWindowEnd(chartPoints, uiState.latestSampleTimestampMillis),
            windowMillis = TEMPERATURE_CHART_WINDOW_MILLIS,
            xLabels = secondsAxisLabels(),
            axisBounds = temperatureAxisBounds(uiState.temperatureUnit),
            valueLabelFormatter = { String.format(Locale.US, "%.1f\u00B0%s", it, uiState.temperatureUnit) },
        )
    }
}

@Composable
private fun BatteryElectricCurrentCard(
    uiState: DeviceInfoUiState,
    onRangeSelected: (BatteryCurrentRange) -> Unit,
) {
    BatteryChartCard(
        title = stringResource(R.string.battery_electric_current),
        rows =
            listOf(
                stringResource(R.string.battery_realtime_current) to formatBatteryCurrent(uiState.displayCurrentNow),
                stringResource(R.string.battery_average_current) to formatBatteryCurrent(uiState.selectedCurrentAverage),
            ),
    ) {
        BatteryCurrentChart(
            samples = uiState.selectedCurrentSamples,
            selectedRange = uiState.selectedCurrentRange,
            latestSampleTimestampMillis = uiState.latestSampleTimestampMillis,
        )
        Spacer(modifier = Modifier.height(14.dp))
        BatteryTimeTabs(
            selectedRange = uiState.selectedCurrentRange,
            onRangeSelected = onRangeSelected,
        )
    }
}

@Composable
private fun BatteryChartCard(
    title: String,
    rows: List<Pair<String, String>>,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shape = RoundedCornerShape(14.dp),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 18.dp),
        ) {
            SectionTitle(title)
            Spacer(modifier = Modifier.height(16.dp))
            rows.forEachIndexed { index, (label, value) ->
                InfoRow(label, value)
                if (index != rows.lastIndex) {
                    Spacer(modifier = Modifier.height(14.dp))
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            content()
        }
    }
}

@Composable
private fun BatteryCurrentChart(
    samples: List<BatteryCurrentSample>,
    selectedRange: BatteryCurrentRange,
    latestSampleTimestampMillis: Long,
) {
    val windowEndMillis =
        currentChartWindowEndMillis(
            samples = samples,
            selectedRange = selectedRange,
            latestSampleTimestampMillis = latestSampleTimestampMillis,
        )
    val chartPoints =
        currentChartPoints(
            samples = samples,
            selectedRange = selectedRange,
            windowEndMillis = windowEndMillis,
        )
    BatteryLineChart(
        points = chartPoints,
        windowEndMillis = windowEndMillis,
        windowMillis = selectedRange.durationMillis,
        xLabels = currentAxisLabels(selectedRange),
        axisBounds = currentAxisBounds(chartPoints.map { it.value }),
        valueLabelFormatter = { String.format(Locale.US, "%.2f mA", it) },
        maxPoints = currentChartMaxPoints(selectedRange),
    )
}

@Composable
private fun BatteryTimeTabs(
    selectedRange: BatteryCurrentRange,
    onRangeSelected: (BatteryCurrentRange) -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(38.dp)
                .clip(RoundedCornerShape(50))
                .background(TabBackground),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BatteryCurrentRange.entries.forEach { range ->
            val selected = range == selectedRange
            Box(
                modifier =
                    Modifier
                        .weight(1f)
                        .fillMaxSize()
                        .clip(RoundedCornerShape(50))
                        .background(if (selected) CleanXBlue else Color.Transparent)
                        .clickable { onRangeSelected(range) },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(range.labelRes),
                    color = if (selected) Color.White else CleanXMutedText,
                    fontSize = 14.sp,
                    lineHeight = 18.sp,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                )
            }
        }
    }
}

@Composable
private fun BatteryLineChart(
    points: List<ChartPoint>,
    windowEndMillis: Long,
    windowMillis: Long,
    xLabels: List<String>,
    axisBounds: ChartAxisBounds,
    valueLabelFormatter: (Float) -> String,
    maxPoints: Int? = null,
) {
    Canvas(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(154.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFFF9FAFE)),
    ) {
        val chartLeft = 28.dp.toPx()
        val chartRight = size.width - 52.dp.toPx()
        val chartTop = 16.dp.toPx()
        val chartBottom = size.height - 24.dp.toPx()
        val dash = PathEffect.dashPathEffect(floatArrayOf(4.dp.toPx(), 4.dp.toPx()), 0f)
        val textPaint = chartTextPaint(ChartGrid, 9.dp.toPx())
        val valuePaint = chartTextPaint(CleanXBlue, 9.dp.toPx(), Typeface.DEFAULT_BOLD)

        repeat(6) { index ->
            val fraction = index / 5f
            val y = chartTop + (chartBottom - chartTop) * fraction
            drawLine(
                color = ChartGrid,
                start = Offset(chartLeft, y),
                end = Offset(chartRight, y),
                strokeWidth = 1.dp.toPx(),
                pathEffect = dash,
            )
            drawContext.canvas.nativeCanvas.drawText(
                axisBounds.yLabels.getOrNull(index).orEmpty(),
                chartRight + 8.dp.toPx(),
                y + 3.dp.toPx(),
                textPaint,
            )
        }

        xLabels.forEachIndexed { index, label ->
            val x =
                if (xLabels.size == 1) {
                    chartRight
                } else {
                    chartLeft + (chartRight - chartLeft) * index / (xLabels.size - 1).toFloat()
                }
            drawLine(
                color = ChartGrid,
                start = Offset(x, chartTop),
                end = Offset(x, chartBottom),
                strokeWidth = 1.dp.toPx(),
                pathEffect = dash,
            )
            textPaint.textAlign = Paint.Align.CENTER
            drawContext.canvas.nativeCanvas.drawText(label, x, chartBottom + 14.dp.toPx(), textPaint)
            textPaint.textAlign = Paint.Align.LEFT
        }

        if (windowEndMillis <= 0L) return@Canvas
        val visiblePoints =
            points
                .asSequence()
                .filter { it.timestampMillis >= windowEndMillis - windowMillis }
                .filter { it.timestampMillis <= windowEndMillis }
                .sortedBy { it.timestampMillis }
                .toList()
        if (visiblePoints.isEmpty()) return@Canvas

        val widthBasedMaxVisiblePoints = max(2, ((chartRight - chartLeft) / 4.dp.toPx()).toInt())
        val maxVisiblePoints = maxPoints?.coerceAtLeast(2) ?: widthBasedMaxVisiblePoints
        val drawablePoints = downsampleChartPoints(visiblePoints, maxVisiblePoints)
        val offsets =
            drawablePoints.map {
                it.toOffset(
                    windowEndMillis = windowEndMillis,
                    windowMillis = windowMillis,
                    axisBounds = axisBounds,
                    chartLeft = chartLeft,
                    chartRight = chartRight,
                    chartTop = chartTop,
                    chartBottom = chartBottom,
                )
            }

        if (offsets.size == 1) {
            drawCircle(
                color = CleanXBlue,
                radius = 3.dp.toPx(),
                center = offsets.first(),
            )
        } else {
            val fillPath =
                Path().apply {
                    moveTo(offsets.first().x, chartBottom)
                    offsets.forEach { lineTo(it.x, it.y) }
                    lineTo(offsets.last().x, chartBottom)
                    close()
                }
            drawPath(fillPath, color = ChartFill)
            offsets.zipWithNext().forEach { (start, end) ->
                drawLine(
                    color = CleanXBlue,
                    start = start,
                    end = end,
                    strokeWidth = 2.dp.toPx(),
                    cap = StrokeCap.Round,
                )
            }
        }

        val minPoint = drawablePoints.minByOrNull { it.value }
        val maxPoint = drawablePoints.maxByOrNull { it.value }
        maxPoint?.let { point ->
            drawChartValueLabel(
                label = valueLabelFormatter(point.value),
                anchor =
                    point.toOffset(
                        windowEndMillis,
                        windowMillis,
                        axisBounds,
                        chartLeft,
                        chartRight,
                        chartTop,
                        chartBottom,
                    ),
                paint = valuePaint,
                chartLeft = chartLeft,
                chartRight = chartRight,
                above = true,
            )
        }
        if (minPoint != null && minPoint != maxPoint) {
            drawChartValueLabel(
                label = valueLabelFormatter(minPoint.value),
                anchor =
                    minPoint.toOffset(
                        windowEndMillis,
                        windowMillis,
                        axisBounds,
                        chartLeft,
                        chartRight,
                        chartTop,
                        chartBottom,
                    ),
                paint = valuePaint,
                chartLeft = chartLeft,
                chartRight = chartRight,
                above = false,
            )
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        color = CleanXText,
        fontSize = 16.sp,
        lineHeight = 20.sp,
        fontWeight = FontWeight.Bold,
    )
}

@Composable
private fun InfoRow(
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            color = CleanXMutedText,
            fontSize = 16.sp,
            lineHeight = 20.sp,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = value,
            color = CleanXText,
            fontSize = 16.sp,
            lineHeight = 20.sp,
            textAlign = TextAlign.End,
        )
    }
}

private data class ChartPoint(
    val timestampMillis: Long,
    val value: Float,
)

private data class ChartAxisBounds(
    val min: Float,
    val max: Float,
    val yLabels: List<String>,
) {
    val range: Float
        get() = (max - min).takeIf { abs(it) > 0.01f } ?: 1f
}

private fun secondsAxisLabels(): List<String> = listOf("60s", "50s", "40s", "30s", "20s", "10s", "0s")

private fun currentAxisLabels(range: BatteryCurrentRange): List<String> =
    when (range) {
        BatteryCurrentRange.OneMinute -> secondsAxisLabels()
        BatteryCurrentRange.OneHour -> listOf("60m", "50m", "40m", "30m", "20m", "10m", "0m")
        BatteryCurrentRange.TwentyFourHours -> listOf("24h", "20h", "16h", "12h", "8h", "4h", "0h")
    }

private fun currentChartMaxPoints(range: BatteryCurrentRange): Int? =
    when (range) {
        BatteryCurrentRange.OneMinute -> null
        BatteryCurrentRange.OneHour -> ONE_HOUR_CURRENT_CHART_BUCKET_COUNT
        BatteryCurrentRange.TwentyFourHours -> TWENTY_FOUR_HOUR_CURRENT_CHART_BUCKET_COUNT
    }

private fun currentChartPoints(
    samples: List<BatteryCurrentSample>,
    selectedRange: BatteryCurrentRange,
    windowEndMillis: Long,
): List<ChartPoint> {
    if (windowEndMillis <= 0L) return emptyList()
    val windowStartMillis = windowEndMillis - selectedRange.durationMillis
    val visibleSamples =
        samples
            .asSequence()
            .filter { it.timestampMillis in windowStartMillis..windowEndMillis }
            .sortedBy { it.timestampMillis }
            .toList()

    val bucketMillis = currentChartBucketMillis(selectedRange)
    if (bucketMillis == null) {
        return visibleSamples.map {
            ChartPoint(
                timestampMillis = it.timestampMillis,
                value = abs(it.currentMa),
            )
        }
    }

    val bucketCount = currentChartBucketCount(selectedRange)
    return visibleSamples
        .groupBy { sample ->
            val elapsedMillis = sample.timestampMillis - windowStartMillis
            if (elapsedMillis <= 0L) {
                0
            } else {
                ((elapsedMillis - 1L) / bucketMillis)
                    .toInt()
                    .coerceIn(0, bucketCount - 1)
            }
        }
        .toSortedMap()
        .map { (bucketIndex, bucketSamples) ->
            ChartPoint(
                timestampMillis = windowStartMillis + (bucketIndex + 1L) * bucketMillis,
                value = bucketSamples.map { abs(it.currentMa) }.average().toFloat(),
            )
        }
}

private fun currentChartBucketMillis(range: BatteryCurrentRange): Long? =
    when (range) {
        BatteryCurrentRange.OneMinute -> null
        BatteryCurrentRange.OneHour -> ONE_HOUR_CURRENT_CHART_BUCKET_MILLIS
        BatteryCurrentRange.TwentyFourHours -> TWENTY_FOUR_HOUR_CURRENT_CHART_BUCKET_MILLIS
    }

private fun currentChartBucketCount(range: BatteryCurrentRange): Int =
    when (range) {
        BatteryCurrentRange.OneMinute -> 0
        BatteryCurrentRange.OneHour -> ONE_HOUR_CURRENT_CHART_BUCKET_COUNT
        BatteryCurrentRange.TwentyFourHours -> TWENTY_FOUR_HOUR_CURRENT_CHART_BUCKET_COUNT
    }

private fun currentChartWindowEndMillis(
    samples: List<BatteryCurrentSample>,
    selectedRange: BatteryCurrentRange,
    latestSampleTimestampMillis: Long,
): Long {
    val latestTimestamp =
        latestSampleTimestampMillis
            .takeIf { it > 0L }
            ?: samples.maxOfOrNull { it.timestampMillis }
            ?: return 0L
    val bucketMillis = currentChartBucketMillis(selectedRange) ?: return latestTimestamp
    return alignUpToBucket(latestTimestamp, bucketMillis)
}

private fun oneMinuteChartWindowEnd(
    points: List<ChartPoint>,
    latestSampleTimestampMillis: Long,
): Long =
    latestSampleTimestampMillis
        .takeIf { it > 0L }
        ?: points.maxOfOrNull { it.timestampMillis }
        ?: 0L

private fun alignUpToBucket(
    timestampMillis: Long,
    bucketMillis: Long,
): Long {
    if (timestampMillis <= 0L || bucketMillis <= 0L) return timestampMillis
    val remainder = timestampMillis % bucketMillis
    return if (remainder == 0L) timestampMillis else timestampMillis + bucketMillis - remainder
}

private fun temperatureAxisBounds(unit: String): ChartAxisBounds =
    if (unit.equals("F", ignoreCase = true)) {
        ChartAxisBounds(
            min = 32f,
            max = 122f,
            yLabels = listOf("122", "104", "86", "68", "50", "32"),
        )
    } else {
        ChartAxisBounds(
            min = 0f,
            max = 50f,
            yLabels = listOf("50", "40", "30", "20", "10", "0"),
        )
    }

private fun currentAxisBounds(values: List<Float>): ChartAxisBounds {
    val maxValue = values.maxOrNull() ?: 0f
    val resolvedMax =
        if (maxValue <= DEFAULT_CURRENT_AXIS_MAX_MA) {
            DEFAULT_CURRENT_AXIS_MAX_MA
        } else {
            ceil(maxValue / CURRENT_AXIS_MAX_STEP_MA) * CURRENT_AXIS_MAX_STEP_MA
        }
    val step = resolvedMax / 5f
    return ChartAxisBounds(
        min = 0f,
        max = resolvedMax,
        yLabels = (0..5).map { index -> ((resolvedMax - step * index).toInt()).toString() },
    )
}

private fun ChartPoint.toOffset(
    windowEndMillis: Long,
    windowMillis: Long,
    axisBounds: ChartAxisBounds,
    chartLeft: Float,
    chartRight: Float,
    chartTop: Float,
    chartBottom: Float,
): Offset {
    val elapsed = (windowEndMillis - timestampMillis).coerceIn(0L, windowMillis)
    val xFraction = 1f - elapsed / windowMillis.toFloat()
    val yFraction = ((value - axisBounds.min) / axisBounds.range).coerceIn(0f, 1f)
    return Offset(
        x = chartLeft + (chartRight - chartLeft) * xFraction,
        y = chartBottom - (chartBottom - chartTop) * yFraction,
    )
}

private fun downsampleChartPoints(
    points: List<ChartPoint>,
    maxPoints: Int,
): List<ChartPoint> {
    if (points.size <= maxPoints) return points
    if (maxPoints <= 2) return listOf(points.first(), points.last())

    val middleMaxPoints = maxPoints - 2
    val middle = points.subList(1, points.lastIndex)
    if (middle.isEmpty()) return listOf(points.first(), points.last())

    val sampledMiddle =
        (0 until middleMaxPoints).mapNotNull { bucketIndex ->
            val start = floor(bucketIndex * middle.size / middleMaxPoints.toFloat()).toInt()
            val end =
                floor((bucketIndex + 1) * middle.size / middleMaxPoints.toFloat())
                    .toInt()
                    .coerceAtLeast(start + 1)
                    .coerceAtMost(middle.size)
            val bucket = middle.subList(start, end).takeIf { it.isNotEmpty() } ?: return@mapNotNull null
            bucket.maxByOrNull { abs(it.value - bucket.first().value) } ?: bucket.first()
        }
    return listOf(points.first()) + sampledMiddle.take(middleMaxPoints) + points.last()
}

private fun chartTextPaint(
    color: Color,
    textSizePx: Float,
    typeface: Typeface = Typeface.DEFAULT,
): Paint =
    Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = color.toArgb()
        textSize = textSizePx
        this.typeface = typeface
        textAlign = Paint.Align.LEFT
    }

private fun DrawScope.drawChartValueLabel(
    label: String,
    anchor: Offset,
    paint: Paint,
    chartLeft: Float,
    chartRight: Float,
    above: Boolean,
) {
    val labelWidth = paint.measureText(label)
    val x = (anchor.x + 4.dp.toPx()).coerceIn(chartLeft, chartRight - labelWidth)
    val yOffset = if (above) -5.dp.toPx() else 12.dp.toPx()
    val y = (anchor.y + yOffset).coerceIn(11.dp.toPx(), size.height - 8.dp.toPx())
    drawContext.canvas.nativeCanvas.drawText(label, x, y, paint)
}

@Composable
private fun localizedBatteryHealth(health: String): String =
    when (health) {
        "Good" -> stringResource(R.string.battery_health_good)
        "Cold" -> stringResource(R.string.battery_health_cold)
        "Dead" -> stringResource(R.string.battery_health_dead)
        "Overheat" -> stringResource(R.string.battery_health_overheat)
        "Overvoltage" -> stringResource(R.string.battery_health_overvoltage)
        "Failure" -> stringResource(R.string.battery_health_failure)
        else -> stringResource(R.string.device_unknown)
    }

@Composable
private fun localizedBatteryStatus(status: String): String =
    when (status) {
        "Charging" -> stringResource(R.string.battery_charging)
        "Discharging" -> stringResource(R.string.battery_discharging)
        "Full" -> stringResource(R.string.battery_full)
        "Not Charging" -> stringResource(R.string.battery_not_charging)
        else -> stringResource(R.string.device_unknown)
    }

private const val TEMPERATURE_CHART_WINDOW_MILLIS = 60_000L
private const val DEFAULT_CURRENT_AXIS_MAX_MA = 3000f
private const val CURRENT_AXIS_MAX_STEP_MA = 1000f
private const val ONE_HOUR_CURRENT_CHART_BUCKET_COUNT = 30
private const val TWENTY_FOUR_HOUR_CURRENT_CHART_BUCKET_COUNT = 24
private const val ONE_HOUR_CURRENT_CHART_BUCKET_MILLIS = 2L * 60L * 1000L
private const val TWENTY_FOUR_HOUR_CURRENT_CHART_BUCKET_MILLIS = 60L * 60L * 1000L
