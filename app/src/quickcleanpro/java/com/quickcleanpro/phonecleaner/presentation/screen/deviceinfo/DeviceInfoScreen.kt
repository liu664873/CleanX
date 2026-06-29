package com.quickcleanpro.phonecleaner.presentation.screen.deviceinfo

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import org.koin.androidx.compose.koinViewModel
import com.quickcleanpro.phonecleaner.R
import com.quickcleanpro.phonecleaner.domain.model.BatteryStatusInfo
import com.quickcleanpro.phonecleaner.domain.model.DeviceCpuInfo
import com.quickcleanpro.phonecleaner.domain.model.DeviceHardwareInfo
import com.quickcleanpro.phonecleaner.domain.model.DeviceSensorInfo
import com.quickcleanpro.phonecleaner.presentation.common.CleanXBackground
import com.quickcleanpro.phonecleaner.presentation.common.CleanXBlue
import com.quickcleanpro.phonecleaner.presentation.common.CleanXMutedText
import com.quickcleanpro.phonecleaner.presentation.common.CleanXScaffold
import com.quickcleanpro.phonecleaner.presentation.common.CleanXText
import com.quickcleanpro.phonecleaner.presentation.theme.QuickCleanTheme
import com.quickcleanpro.phonecleaner.domain.model.device.BatteryInfo
import com.quickcleanpro.phonecleaner.domain.model.device.MemoryInfo
import com.quickcleanpro.phonecleaner.domain.model.device.StorageInfo
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max

/**
 * Device Info / Battery Info 妞ょ敻娼伴崗銉ュ經閵? *
 * 妞ょ敻娼伴崣顏囩鐠愶絾瑕嗛弻?[DeviceInfoUiState] 閸滃苯顦╅悶鍡欐晸閸涜棄鎳嗛張鐔剁皑娴犺绱濇稉宥呭晙閻╁瓨甯寸拠璇插絿缁崵绮洪張宥呭閵? */
@Composable
fun DeviceInfoScreen(
    mode: DeviceInfoMode,
    onBack: () -> Unit
) {
    DeviceInfoScreenState(
        mode = mode,
        onBack = onBack,
        viewModel = viewModel()
    )
}

@Composable
internal fun DeviceInfoRoute(
    mode: DeviceInfoMode,
    onBack: () -> Unit
) {
    DeviceInfoScreenState(
        mode = mode,
        onBack = onBack,
        viewModel = koinViewModel()
    )
}

@Composable
private fun DeviceInfoScreenState(
    mode: DeviceInfoMode,
    onBack: () -> Unit,
    viewModel: DeviceInfoViewModel
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value

    DisposableEffect(lifecycleOwner, mode, viewModel) {
        fun refreshState() {
            viewModel.load(mode, "")
            if (mode == DeviceInfoMode.Battery) {
                viewModel.startCurrentSampling()
            } else {
                viewModel.stopCurrentSampling()
            }
        }

        refreshState()
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> refreshState()
                Lifecycle.Event.ON_PAUSE,
                Lifecycle.Event.ON_STOP -> viewModel.stopCurrentSampling()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            viewModel.stopCurrentSampling()
        }
    }

    DeviceInfoScreenContent(
        mode = mode,
        uiState = uiState,
        onBack = onBack,
        onRangeSelected = viewModel::selectCurrentRange
    )
}

/** 鐠佹儳顦穱鈩冧紖妞ょ數鍑藉〒鍙夌厠鐏炲倶??*/
@Composable
private fun DeviceInfoScreenContent(
    mode: DeviceInfoMode,
    uiState: DeviceInfoUiState,
    onBack: () -> Unit,
    onRangeSelected: (BatteryCurrentRange) -> Unit
) {
    CleanXScaffold(
        titleRes = if (mode == DeviceInfoMode.Battery) R.string.battery_info else R.string.device_model,
        onBack = onBack
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(CleanXBackground)
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .navigationBarsPadding()
                    .padding(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                if (mode == DeviceInfoMode.Device) {
                    DeviceSummaryCard(
                        model = uiState.hardware?.model ?: stringResource(R.string.device_unknown),
                        androidVersion = uiState.hardware?.androidVersion ?: "Android --"
                    )
                    StateInfoCard(
                        cpuUsage = uiState.cpuUsage,
                        cpuTemperature = uiState.cpuTemperature,
                        ramLabel = uiState.ramLabel,
                        ramPercent = uiState.ramPercent,
                        storageLabel = uiState.storageLabel,
                        storagePercent = uiState.storagePercent
                    )
                    uiState.deviceRows.forEach { group ->
                        InfoSection(group = group)
                    }
                } else {
                    BatteryInfoContent(
                        uiState = uiState,
                        onRangeSelected = onRangeSelected
                    )
                }
            }
        }
    }
}

/** Battery Info 缁绢垱瑕嗛弻鎾冲敶鐎瑰箍??*/
@Composable
private fun BatteryInfoContent(
    uiState: DeviceInfoUiState,
    onRangeSelected: (BatteryCurrentRange) -> Unit
) {
    BatteryTopCard(
        statusText = uiState.batteryStatusText,
        capacityText = uiState.batteryCapacityText
    )

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            BatteryMetricCard(
                iconRes = R.drawable.temperature,
                value = uiState.batteryTemperatureValue,
                label = stringResource(R.string.battery_temperature),
                modifier = Modifier.weight(1f)
            )
            BatteryMetricCard(
                iconRes = R.drawable.voltage,
                value = uiState.batteryVoltageValue,
                label = stringResource(R.string.battery_voltage),
                modifier = Modifier.weight(1f)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            BatteryMetricCard(
                iconRes = R.drawable.battery_health,
                value = localizedBatteryHealth(uiState.batteryHealthText),
                label = stringResource(R.string.battery_health),
                modifier = Modifier.weight(1f)
            )
            BatteryMetricCard(
                iconRes = R.drawable.life,
                value = uiState.batteryLifeText,
                label = stringResource(R.string.battery_life),
                modifier = Modifier.weight(1f)
            )
        }
    }

    BatteryChartCard(
        title = stringResource(R.string.battery_temperature_title),
        rows = listOf(
            stringResource(R.string.battery_realtime_temperature) to uiState.batteryTemperatureRowValue,
            stringResource(R.string.battery_average_temperature) to uiState.batteryAverageTemperatureRowValue
        ),
        samples = uiState.selectedTemperatureSamples,
        tempUnit = uiState.tempUnit,
        latestSampleTimestampMillis = uiState.latestSampleTimestampMillis
    )

    BatteryElectricCurrentCard(
        title = stringResource(R.string.battery_electric_current),
        rows = listOf(
            stringResource(R.string.battery_realtime_current) to formatBatteryCurrent(uiState.currentNow),
            stringResource(R.string.battery_average_current) to formatBatteryCurrent(uiState.selectedCurrentAverage)
        ),
        samples = uiState.selectedCurrentSamples,
        selectedRange = uiState.selectedCurrentRange,
        latestSampleTimestampMillis = uiState.latestSampleTimestampMillis,
        onRangeSelected = onRangeSelected
    )
}

@Composable
private fun BatteryTopCard(statusText: String, capacityText: String) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(134.dp),
        color = Color.White,
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 22.dp, end = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                BatterySummaryLine(label = stringResource(R.string.battery_status), value = localizedBatteryStatus(statusText))
                BatterySummaryLine(label = stringResource(R.string.battery_capacity), value = capacityText)
            }
            Image(
                painter = painterResource(id = R.drawable.battery_info),
                contentDescription = null,
                modifier = Modifier.size(width = 104.dp, height = 78.dp)
            )
        }
    }
}

@Composable
private fun BatterySummaryLine(label: String, value: String) {
    Column {
        Text(
            text = label,
            color = CleanXMutedText,
            fontSize = 14.sp,
            lineHeight = 18.sp
        )
        Text(
            text = value,
            color = CleanXText,
            fontSize = 20.sp,
            lineHeight = 26.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun BatteryMetricCard(
    iconRes: Int,
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.height(144.dp),
        color = Color.White,
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = value,
                color = CleanXText,
                fontSize = 20.sp,
                lineHeight = 24.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = label,
                color = CleanXMutedText,
                fontSize = 13.sp,
                lineHeight = 17.sp,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun BatteryChartCard(
    title: String,
    rows: List<Pair<String, String>>,
    samples: List<BatteryTemperatureSample>,
    tempUnit: String,
    latestSampleTimestampMillis: Long
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 18.dp)) {
            SectionTitle(title)
            Spacer(modifier = Modifier.height(16.dp))
            rows.forEachIndexed { index, (label, value) ->
                InfoRow(label, value)
                if (index != rows.lastIndex) {
                    Spacer(modifier = Modifier.height(14.dp))
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            BatteryLineChart(
                points = samples.map {
                    ChartPoint(
                        timestampMillis = it.timestampMillis,
                        value = toDisplayTemperature(it.temperatureC, tempUnit)
                    )
                },
                latestTimestampMillis = latestSampleTimestampMillis,
                windowMillis = TEMPERATURE_CHART_WINDOW_MILLIS,
                xLabels = secondsAxisLabels(),
                axisBounds = temperatureAxisBounds(tempUnit),
                valueLabelFormatter = { formatChartTemperature(it, tempUnit) }
            )
        }
    }
}

@Composable
private fun BatteryElectricCurrentCard(
    title: String,
    rows: List<Pair<String, String>>,
    samples: List<BatteryCurrentSample>,
    selectedRange: BatteryCurrentRange,
    latestSampleTimestampMillis: Long,
    onRangeSelected: (BatteryCurrentRange) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 18.dp)) {
            SectionTitle(title)
            Spacer(modifier = Modifier.height(16.dp))
            rows.forEachIndexed { index, (label, value) ->
                InfoRow(label, value)
                if (index != rows.lastIndex) {
                    Spacer(modifier = Modifier.height(14.dp))
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            BatteryCurrentChart(
                samples = samples,
                selectedRange = selectedRange,
                latestSampleTimestampMillis = latestSampleTimestampMillis
            )
            Spacer(modifier = Modifier.height(14.dp))
            BatteryTimeTabs(
                selectedRange = selectedRange,
                onRangeSelected = onRangeSelected
            )
        }
    }
}

@Composable
private fun BatteryCurrentChart(
    samples: List<BatteryCurrentSample>,
    selectedRange: BatteryCurrentRange,
    latestSampleTimestampMillis: Long
) {
    val chartPoints = currentChartPoints(
        samples = samples,
        selectedRange = selectedRange,
        latestTimestampMillis = latestSampleTimestampMillis
    )
    BatteryLineChart(
        points = chartPoints,
        latestTimestampMillis = latestSampleTimestampMillis,
        windowMillis = selectedRange.durationMillis,
        xLabels = currentAxisLabels(selectedRange),
        axisBounds = currentAxisBounds(chartPoints.map { it.value }),
        valueLabelFormatter = { String.format(java.util.Locale.US, "%.2f mA", it) }
    )
}

@Composable
private fun BatteryLineChart(
    points: List<ChartPoint>,
    latestTimestampMillis: Long,
    windowMillis: Long,
    xLabels: List<String>,
    axisBounds: ChartAxisBounds,
    valueLabelFormatter: (Float) -> String,
    maxPoints: Int? = null
) {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(154.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFFF9FAFE))
    ) {
        val chartLeft = 28.dp.toPx()
        val chartRight = size.width - 52.dp.toPx()
        val chartTop = 16.dp.toPx()
        val chartBottom = size.height - 24.dp.toPx()
        val gridColor = Color(0xFF9AA6B8).copy(alpha = 0.72f)
        val dash = PathEffect.dashPathEffect(floatArrayOf(4.dp.toPx(), 4.dp.toPx()), 0f)
        val textPaint = chartTextPaint(color = Color(0xFF9AA6B8), textSizePx = 9.dp.toPx())
        val valuePaint = chartTextPaint(
            color = CleanXBlue,
            textSizePx = 9.dp.toPx(),
            typeface = Typeface.DEFAULT_BOLD
        )

        repeat(6) { index ->
            val fraction = index / 5f
            val y = chartTop + (chartBottom - chartTop) * fraction
            drawLine(
                color = gridColor,
                start = Offset(chartLeft, y),
                end = Offset(chartRight, y),
                strokeWidth = 1.dp.toPx(),
                pathEffect = dash
            )
            val label = axisBounds.yLabels.getOrNull(index).orEmpty()
            drawContext.canvas.nativeCanvas.drawText(
                label,
                chartRight + 8.dp.toPx(),
                y + 3.dp.toPx(),
                textPaint
            )
        }
        repeat(xLabels.size) { index ->
            val x = if (xLabels.size == 1) {
                chartRight
            } else {
                chartLeft + (chartRight - chartLeft) * index / (xLabels.size - 1).toFloat()
            }
            drawLine(
                color = gridColor,
                start = Offset(x, chartTop),
                end = Offset(x, chartBottom),
                strokeWidth = 1.dp.toPx(),
                pathEffect = dash
            )
            textPaint.textAlign = Paint.Align.CENTER
            drawContext.canvas.nativeCanvas.drawText(
                xLabels[index],
                x,
                chartBottom + 14.dp.toPx(),
                textPaint
            )
            textPaint.textAlign = Paint.Align.LEFT
        }

        val resolvedLatestTimestamp = resolveChartLatestTimestamp(
            points = points,
            latestTimestampMillis = latestTimestampMillis,
            nowMillis = System.currentTimeMillis()
        )
        val normalizedPoints = points
            .filter { it.timestampMillis >= resolvedLatestTimestamp - windowMillis }
            .sortedBy { it.timestampMillis }
        if (normalizedPoints.isEmpty()) return@Canvas

        val widthBasedMaxVisiblePoints = max(2, ((chartRight - chartLeft) / 4.dp.toPx()).toInt())
        val maxVisiblePoints = maxPoints?.coerceAtLeast(2) ?: widthBasedMaxVisiblePoints
        val drawablePoints = downsampleChartPoints(normalizedPoints, maxVisiblePoints)
        val lineOffsets = drawablePoints.map {
            it.toOffset(
                latestTimestampMillis = resolvedLatestTimestamp,
                windowMillis = windowMillis,
                axisBounds = axisBounds,
                chartLeft = chartLeft,
                chartRight = chartRight,
                chartTop = chartTop,
                chartBottom = chartBottom
            )
        }

        if (lineOffsets.size == 1) {
            drawCircle(CleanXBlue, radius = 3.dp.toPx(), center = lineOffsets.first())
        } else {
            val fillPath = Path().apply {
                moveTo(lineOffsets.first().x, chartBottom)
                lineOffsets.forEach { lineTo(it.x, it.y) }
                lineTo(lineOffsets.last().x, chartBottom)
                close()
            }
            drawPath(fillPath, color = Color(0xFF5666F5).copy(alpha = 0.14f))
            lineOffsets.zipWithNext().forEach { (start, end) ->
                drawLine(
                    color = CleanXBlue,
                    start = start,
                    end = end,
                    strokeWidth = 2.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
        }

        val minPoint = normalizedPoints.minByOrNull { it.value }
        val maxPoint = normalizedPoints.maxByOrNull { it.value }
        maxPoint?.let {
            val offset = it.toOffset(resolvedLatestTimestamp, windowMillis, axisBounds, chartLeft, chartRight, chartTop, chartBottom)
            drawChartValueLabel(valueLabelFormatter(it.value), offset, valuePaint, chartLeft, chartRight, above = true)
        }
        if (minPoint != null && minPoint != maxPoint) {
            val offset = minPoint.toOffset(resolvedLatestTimestamp, windowMillis, axisBounds, chartLeft, chartRight, chartTop, chartBottom)
            drawChartValueLabel(valueLabelFormatter(minPoint.value), offset, valuePaint, chartLeft, chartRight, above = false)
        }
    }
}

internal data class ChartPoint(
    val timestampMillis: Long,
    val value: Float
)

private data class ChartAxisBounds(
    val min: Float,
    val max: Float,
    val yLabels: List<String>
) {
    val range: Float
        get() = (max - min).takeIf { abs(it) > 0.01f } ?: 1f
}

private fun secondsAxisLabels(): List<String> =
    listOf("60s", "50s", "40s", "30s", "20s", "10s", "0s")

private fun currentAxisLabels(range: BatteryCurrentRange): List<String> =
    when (range) {
        BatteryCurrentRange.OneMinute -> secondsAxisLabels()
        BatteryCurrentRange.OneHour -> listOf("60m", "50m", "40m", "30m", "20m", "10m", "0m")
        BatteryCurrentRange.TwentyFourHours -> listOf("24h", "20h", "16h", "12h", "8h", "4h", "0h")
    }

internal fun currentChartMaxPoints(range: BatteryCurrentRange): Int? =
    when (range) {
        BatteryCurrentRange.OneMinute -> null
        BatteryCurrentRange.OneHour,
        BatteryCurrentRange.TwentyFourHours -> LONG_RANGE_CURRENT_CHART_MAX_POINTS
    }

internal fun currentChartPoints(
    samples: List<BatteryCurrentSample>,
    selectedRange: BatteryCurrentRange,
    latestTimestampMillis: Long
): List<ChartPoint> {
    val latestTimestamp = latestTimestampMillis.takeIf { it > 0L }
        ?: samples.maxOfOrNull { it.timestampMillis }
        ?: return emptyList()
    val cutoff = latestTimestamp - selectedRange.durationMillis
    val visibleSamples = samples
        .asSequence()
        .filter { it.timestampMillis >= cutoff && it.timestampMillis <= latestTimestamp }
        .sortedBy { it.timestampMillis }
        .toList()

    val bucketMillis = currentChartBucketMillis(selectedRange)
    if (bucketMillis == null) {
        return visibleSamples.map {
            ChartPoint(
                timestampMillis = it.timestampMillis,
                value = abs(it.currentMa)
            )
        }
    }

    val completedBucketEnd = latestTimestamp - (latestTimestamp % bucketMillis)
    return visibleSamples
        .asSequence()
        .filter { it.timestampMillis < completedBucketEnd }
        .groupBy { sample ->
            ((sample.timestampMillis / bucketMillis) + 1L) * bucketMillis
        }
        .toSortedMap()
        .map { (bucketEnd, bucketSamples) ->
            ChartPoint(
                timestampMillis = bucketEnd,
                value = bucketSamples.map { abs(it.currentMa) }.average().toFloat()
            )
        }
}

internal fun currentChartBucketMillis(range: BatteryCurrentRange): Long? =
    when (range) {
        BatteryCurrentRange.OneMinute -> null
        BatteryCurrentRange.OneHour -> ONE_HOUR_CURRENT_CHART_BUCKET_MILLIS
        BatteryCurrentRange.TwentyFourHours -> TWENTY_FOUR_HOUR_CURRENT_CHART_BUCKET_MILLIS
    }

private fun temperatureAxisBounds(tempUnit: String): ChartAxisBounds =
    if (tempUnit == "F") {
        ChartAxisBounds(
            min = 0f,
            max = 120f,
            yLabels = listOf("120", "100", "80", "60", "40", "20")
        )
    } else {
        ChartAxisBounds(
            min = 0f,
            max = 50f,
            yLabels = listOf("50", "40", "30", "20", "10", "0")
        )
    }

private fun currentAxisBounds(values: List<Float>): ChartAxisBounds {
    if (values.isEmpty()) {
        return ChartAxisBounds(
            min = 0f,
            max = 1000f,
            yLabels = listOf("1000", "800", "600", "400", "200", "0")
        )
    }
    val minValue = values.minOrNull() ?: 0f
    val maxValue = values.maxOrNull() ?: 0f
    val padding = ((maxValue - minValue) * 0.16f).takeIf { it > 1f } ?: 120f
    val min = floor(max(0f, minValue - padding) / 120f) * 120f
    val max = ceil((maxValue + padding) / 120f) * 120f
    val resolvedMax = if (max <= min) min + 600f else max
    val step = (resolvedMax - min) / 5f
    val labels = (0..5).map { index ->
        ((resolvedMax - step * index).toInt()).toString()
    }
    return ChartAxisBounds(min = min, max = resolvedMax, yLabels = labels)
}

private fun toDisplayTemperature(temperatureC: Float, tempUnit: String): Float =
    if (tempUnit == "F") temperatureC * 9f / 5f + 32f else temperatureC

private fun formatChartTemperature(value: Float, tempUnit: String): String =
    String.format(java.util.Locale.US, "%.1f 掳%s", value, tempUnit)

internal fun resolveChartLatestTimestamp(
    points: List<ChartPoint>,
    latestTimestampMillis: Long,
    nowMillis: Long
): Long {
    val latestPointTimestamp = points.maxOfOrNull { it.timestampMillis }
    if (latestPointTimestamp != null &&
        nowMillis - latestPointTimestamp in 0L..CHART_LATEST_SAMPLE_FRESH_MILLIS
    ) {
        return latestPointTimestamp
    }
    return nowMillis
        .takeIf { latestPointTimestamp != null }
        ?: latestTimestampMillis.takeIf { it > 0L }
        ?: nowMillis
}

internal fun downsampleChartPoints(points: List<ChartPoint>, maxPoints: Int): List<ChartPoint> {
    if (points.size <= maxPoints) return points
    if (maxPoints <= 2) return listOf(points.first(), points.last())

    val middleMaxPoints = maxPoints - 2
    val middle = points.subList(1, points.lastIndex)
    if (middle.isEmpty()) return listOf(points.first(), points.last())

    val sampledMiddle = (0 until middleMaxPoints).mapNotNull { bucketIndex ->
        val start = floor(bucketIndex * middle.size / middleMaxPoints.toFloat()).toInt()
        val end = floor((bucketIndex + 1) * middle.size / middleMaxPoints.toFloat()).toInt()
            .coerceAtLeast(start + 1)
            .coerceAtMost(middle.size)
        val bucket = middle.subList(start, end).takeIf { it.isNotEmpty() } ?: return@mapNotNull null
        val peak = bucket.maxByOrNull { abs(it.value - bucket.first().value) } ?: bucket.first()
        ChartPoint(timestampMillis = peak.timestampMillis, value = peak.value)
    }
    return listOf(points.first()) + sampledMiddle.take(middleMaxPoints) + points.last()
}

private fun ChartPoint.toOffset(
    latestTimestampMillis: Long,
    windowMillis: Long,
    axisBounds: ChartAxisBounds,
    chartLeft: Float,
    chartRight: Float,
    chartTop: Float,
    chartBottom: Float
): Offset {
    val elapsed = (latestTimestampMillis - timestampMillis).coerceIn(0L, windowMillis)
    val xFraction = 1f - elapsed / windowMillis.toFloat()
    val yFraction = ((value - axisBounds.min) / axisBounds.range).coerceIn(0f, 1f)
    return Offset(
        x = chartLeft + (chartRight - chartLeft) * xFraction,
        y = chartBottom - (chartBottom - chartTop) * yFraction
    )
}

private fun chartTextPaint(
    color: Color,
    textSizePx: Float,
    typeface: Typeface = Typeface.DEFAULT
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
    above: Boolean
) {
    val nativeCanvas = drawContext.canvas.nativeCanvas
    val labelWidth = paint.measureText(label)
    val x = (anchor.x + 4.dp.toPx()).coerceIn(chartLeft, chartRight - labelWidth)
    val yOffset = if (above) -5.dp.toPx() else 12.dp.toPx()
    val y = (anchor.y + yOffset).coerceIn(11.dp.toPx(), size.height - 8.dp.toPx())
    nativeCanvas.drawText(label, x, y, paint)
}

private const val TEMPERATURE_CHART_WINDOW_MILLIS = 60_000L
private const val CHART_LATEST_SAMPLE_FRESH_MILLIS = 5_000L
private const val LONG_RANGE_CURRENT_CHART_MAX_POINTS = 30
private const val ONE_HOUR_CURRENT_CHART_BUCKET_MILLIS = 2L * 60L * 1000L
private const val TWENTY_FOUR_HOUR_CURRENT_CHART_BUCKET_MILLIS = 60L * 60L * 1000L

@Composable
private fun BatteryTimeTabs(
    selectedRange: BatteryCurrentRange,
    onRangeSelected: (BatteryCurrentRange) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(38.dp)
            .clip(RoundedCornerShape(50))
            .background(Color(0xFFE7E9F1)),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BatteryCurrentRange.values().forEach { range ->
            val selected = range == selectedRange
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .clip(RoundedCornerShape(50))
                    .background(if (selected) CleanXBlue else Color.Transparent)
                    .clickable { onRangeSelected(range) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(range.labelRes),
                    color = if (selected) Color.White else CleanXMutedText,
                    fontSize = 14.sp,
                    lineHeight = 18.sp,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}

@Composable
private fun DeviceSummaryCard(
    model: String,
    androidVersion: String
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(114.dp),
        color = Color.White,
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 32.dp, end = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.home_device_model),
                    color = CleanXMutedText,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
                Text(
                    text = model,
                    color = CleanXText,
                    fontSize = 17.sp,
                    lineHeight = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.home_system_version),
                    color = CleanXMutedText,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
                Text(
                    text = androidVersion,
                    color = CleanXText,
                    fontSize = 17.sp,
                    lineHeight = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Image(
                painter = painterResource(id = R.drawable.device_model),
                contentDescription = null,
                modifier = Modifier.size(111.dp, 82.dp)
            )
        }
    }
}

@Composable
private fun StateInfoCard(
    cpuUsage: Int,
    cpuTemperature: String,
    ramLabel: String,
    ramPercent: Int,
    storageLabel: String,
    storagePercent: Int
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 17.dp)) {
            SectionTitle(stringResource(R.string.device_state_info))
            Spacer(modifier = Modifier.height(18.dp))
            ProgressInfoRow("CPU:${cpuUsage}%/$cpuTemperature", cpuUsage / 100f, CleanXBlue)
            Spacer(modifier = Modifier.height(16.dp))
            ProgressInfoRow(ramLabel, ramPercent / 100f, Color(0xFFE9B10D))
            Spacer(modifier = Modifier.height(16.dp))
            ProgressInfoRow(storageLabel, storagePercent / 100f, Color(0xFFAF27E8))
        }
    }
}

@Composable
private fun ProgressInfoRow(label: String, progress: Float, color: Color) {
    Column {
        Text(
            text = label,
            color = CleanXMutedText,
            fontSize = 16.sp,
            lineHeight = 20.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .clip(RoundedCornerShape(50))
                .background(color.copy(alpha = 0.09f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress.coerceIn(0f, 1f))
                    .height(12.dp)
                    .clip(RoundedCornerShape(50))
                    .background(color)
            )
        }
    }
}

@Composable
private fun InfoSection(group: DeviceInfoRowGroup) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 17.dp)) {
            SectionTitle(stringResource(group.titleRes))
            Spacer(modifier = Modifier.height(18.dp))
            group.rows.forEachIndexed { index, row ->
                InfoRow(stringResource(row.labelRes), localizedDeviceValue(row.value))
                if (index != group.rows.lastIndex) {
                    Spacer(modifier = Modifier.height(17.dp))
                }
            }
        }
    }
}

@Composable
private fun localizedDeviceValue(value: String): String =
    when (value) {
        "Supported" -> stringResource(R.string.device_supported)
        "Not Supported" -> stringResource(R.string.device_not_supported)
        "Charging" -> stringResource(R.string.battery_charging)
        "Discharging" -> stringResource(R.string.battery_discharging)
        "Full" -> stringResource(R.string.battery_full)
        "Not Charging" -> stringResource(R.string.battery_not_charging)
        "Good" -> stringResource(R.string.battery_health_good)
        "Cold" -> stringResource(R.string.battery_health_cold)
        "Dead" -> stringResource(R.string.battery_health_dead)
        "Overheat" -> stringResource(R.string.battery_health_overheat)
        "Overvoltage" -> stringResource(R.string.battery_health_overvoltage)
        "Failure" -> stringResource(R.string.battery_health_failure)
        "Unknown" -> stringResource(R.string.device_unknown)
        else -> value
    }

@Composable
private fun localizedBatteryStatus(value: String): String = localizedDeviceValue(value)

@Composable
private fun localizedBatteryHealth(value: String): String = localizedDeviceValue(value)

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        color = CleanXText,
        fontSize = 16.sp,
        lineHeight = 20.sp,
        fontWeight = FontWeight.Bold
    )
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = CleanXMutedText,
            fontSize = 16.sp,
            lineHeight = 20.sp,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            color = CleanXText,
            fontSize = 16.sp,
            lineHeight = 20.sp
        )
    }
}

/** 妫板嫯顫嶇拋鎯ь槵娣団剝浼呮い鐢靛Ц閹降??*/
private fun previewDeviceInfoUiState(): DeviceInfoUiState {
    val battery = BatteryInfo(
        levelPercent = 76,
        health = "Good",
        temperature = 31.4f,
        voltage = 4120,
        technology = "Li-ion",
        capacity = 4500,
        availableTime = "4h6m"
    )
    val batteryStatus = BatteryStatusInfo(statusText = "Charging", isCharging = true)
    val hardware = DeviceHardwareInfo(
        model = "Preview Phone",
        androidVersion = "Android 16",
        screenSize = "1080x2400",
        screenDensity = "420 DPI",
        multiTouchSupported = true,
        sensors = DeviceSensorInfo(
            accelerometer = true,
            magneticField = true,
            orientation = true,
            gyroscope = true,
            light = true,
            proximity = true,
            ambientTemperature = false
        ),
        cpu = DeviceCpuInfo(
            hardware = "preview-soc",
            model = "Preview Chip",
            cores = 8,
            maxFrequency = "3.1 GHz"
        )
    )
    val memory = MemoryInfo(
        totalBytes = 8L * 1024 * 1024 * 1024,
        availableBytes = 3L * 1024 * 1024 * 1024,
        usedBytes = 5L * 1024 * 1024 * 1024,
        usagePercent = 62,
        isTotalValid = true
    )
    val storage = StorageInfo(
        totalBytes = 256L * 1024 * 1024 * 1024,
        availableBytes = 120L * 1024 * 1024 * 1024,
        usedBytes = 136L * 1024 * 1024 * 1024
    )
    return DeviceInfoUiState(
        mode = DeviceInfoMode.Device,
        tempUnit = "C",
        battery = battery,
        batteryStatus = batteryStatus,
        memory = memory,
        storage = storage,
        hardware = hardware,
        cpuUsage = 38,
        cpuTemperature = formatCpuTemperature(43.8f, "C"),
        ramLabel = "RAM:${formatCompactBytes(memory.usedBytes)}/${formatCompactBytes(memory.totalBytes)}",
        ramPercent = memory.usagePercent,
        storageLabel = "Storage:${formatCompactBytes(storage.usedBytes)}/${formatCompactBytes(storage.totalBytes)}",
        storagePercent = storage.usagePercent,
        deviceRows = buildDeviceRows(hardware, battery, batteryStatus, "C"),
        batteryRows = buildBatteryRows(battery, batteryStatus, "C"),
        currentNow = 612.35f,
        currentAverage = 504.80f,
        currentSamples = listOf(
            BatteryCurrentSample(0L, 420f),
            BatteryCurrentSample(10_000L, 510f),
            BatteryCurrentSample(20_000L, 605f),
            BatteryCurrentSample(30_000L, 560f),
            BatteryCurrentSample(40_000L, 680f),
            BatteryCurrentSample(50_000L, 470f),
            BatteryCurrentSample(60_000L, 612.35f)
        ),
        temperatureSamples = listOf(
            BatteryTemperatureSample(0L, 31.0f),
            BatteryTemperatureSample(10_000L, 31.2f),
            BatteryTemperatureSample(20_000L, 31.3f),
            BatteryTemperatureSample(30_000L, 31.4f),
            BatteryTemperatureSample(40_000L, 31.4f),
            BatteryTemperatureSample(50_000L, 31.5f),
            BatteryTemperatureSample(60_000L, 31.4f)
        ),
        latestSampleTimestampMillis = 60_000L,
        isLoading = false
    )
}

private fun previewBatteryInfoUiState(): DeviceInfoUiState =
    previewDeviceInfoUiState().copy(mode = DeviceInfoMode.Battery)

@Preview(showBackground = true, backgroundColor = 0xFFF4F8FF)
@Composable
private fun PreviewDeviceInfoScreen() {
    QuickCleanTheme {
        DeviceInfoScreenContent(
            mode = DeviceInfoMode.Device,
            uiState = previewDeviceInfoUiState(),
            onBack = {},
            onRangeSelected = {}
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF4F8FF)
@Composable
private fun PreviewBatteryInfoScreen() {
    QuickCleanTheme {
        DeviceInfoScreenContent(
            mode = DeviceInfoMode.Battery,
            uiState = previewBatteryInfoUiState(),
            onBack = {},
            onRangeSelected = {}
        )
    }
}
