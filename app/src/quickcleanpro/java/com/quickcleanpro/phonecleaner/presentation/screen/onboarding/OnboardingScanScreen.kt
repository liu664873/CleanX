package com.quickcleanpro.phonecleaner.presentation.screen.onboarding

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.quickcleanpro.phonecleaner.R
import com.quickcleanpro.phonecleaner.presentation.common.CleanXBackground
import com.quickcleanpro.phonecleaner.presentation.common.CleanXBlue
import com.quickcleanpro.phonecleaner.presentation.common.CleanXCheckBadge
import com.quickcleanpro.phonecleaner.presentation.common.CleanXPrimaryButton
import com.quickcleanpro.phonecleaner.presentation.common.CleanXText
import com.quickcleanpro.phonecleaner.presentation.common.RotatingRingAnimation
import com.quickcleanpro.phonecleaner.domain.model.device.StorageInfo
import kotlinx.coroutines.delay

private const val OnboardingStepDelayMillis = 680L
private const val OnboardingScanLineDurationMillis = 1800
private const val OnboardingStatusRingDurationMillis = 1200

private data class DeviceScanRow(
    val label: String,
    val value: String,
    val complete: Boolean,
    val active: Boolean = false
)

@Composable
fun OnboardingScanScreen(
    onContinueToHome: () -> Unit
) {
    OnboardingScanContent(
        viewModel = viewModel(),
        onContinueToHome = onContinueToHome
    )
}

@Composable
internal fun OnboardingScanRoute(
    onContinueToHome: () -> Unit
) {
    OnboardingScanContent(
        viewModel = koinViewModel(),
        onContinueToHome = onContinueToHome
    )
}

@Composable
private fun OnboardingScanContent(
    viewModel: OnboardingScanViewModel,
    onContinueToHome: () -> Unit
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var completedStep by remember { mutableIntStateOf(0) }

    BackHandler {}

    DisposableEffect(lifecycleOwner, viewModel) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refresh()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(Unit) {
        for (step in 1..6) {
            delay(OnboardingStepDelayMillis)
            completedStep = step
        }
        delay(OnboardingStepDelayMillis)
        completedStep = 7
    }

    val rows = listOf(
        DeviceScanRow(
            stringResource(R.string.home_device_model),
            uiState.deviceModel,
            complete = completedStep > 1,
            active = completedStep == 1
        ),
        DeviceScanRow(
            stringResource(R.string.home_system_version),
            if (completedStep > 2) uiState.androidVersion else "--",
            complete = completedStep > 2,
            active = completedStep == 2
        ),
        DeviceScanRow(
            stringResource(R.string.device_screen_size),
            if (completedStep > 3) uiState.screenSize else "--",
            complete = completedStep > 3,
            active = completedStep == 3
        ),
        DeviceScanRow(
            stringResource(R.string.battery_health_status),
            if (completedStep > 4) localizedOnboardingDeviceValue(uiState.batteryHealth) else "--",
            complete = completedStep > 4,
            active = completedStep == 4
        ),
        DeviceScanRow(
            stringResource(R.string.battery_status),
            if (completedStep > 5) uiState.batteryStatusText else "--",
            complete = completedStep > 5,
            active = completedStep == 5
        ),
        DeviceScanRow(
            stringResource(R.string.onboarding_generating_cleanup_plan),
            if (completedStep > 6) stringResource(R.string.onboarding_done) else "--",
            complete = completedStep > 6,
            active = completedStep == 6
        )
    )
    val complete = completedStep > rows.size

    Scaffold(containerColor = CleanXBackground) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(CleanXBackground)
                .padding(paddingValues)
                .padding(horizontal = 13.dp)
        ) {
            if (complete) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 20.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = stringResource(R.string.onboarding_skip),
                        color = CleanXBlue,
                        fontSize = 14.sp,
                        modifier = Modifier
                            .clickable { onContinueToHome() }
                            .padding(end = 12.dp)
                    )
                }
            }

            if (complete) {
                OnboardingResultContent(
                    rows = rows,
                    storageInfo = uiState.storageInfo,
                    modifier = Modifier.weight(1f)
                )
            } else {
                OnboardingScanningContent(
                    rows = rows,
                    modifier = Modifier.weight(1f)
                )
            }

            if (complete) {
                CleanXPrimaryButton(
                    text = stringResource(R.string.onboarding_get_started),
                    onClick = onContinueToHome,
                    modifier = Modifier.padding(bottom = 20.dp)
                )
            }
        }
    }
}
@Composable
private fun OnboardingScanningContent(
    rows: List<DeviceScanRow>,
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "deviceScan")
    val scanLineProgress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = OnboardingScanLineDurationMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scanLine"
    )

    Column(modifier = modifier) {
        Spacer(modifier = Modifier.height(12.dp))
        PhoneScanIllustration(
            scanLineProgress = scanLineProgress,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .size(width = 196.dp, height = 192.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.onboarding_checking_device_info),
            color = CleanXText,
            fontSize = 22.sp,
            lineHeight = 30.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.height(22.dp))
        DeviceRows(rows)
    }
}

@Composable
private fun OnboardingResultContent(
    rows: List<DeviceScanRow>,
    storageInfo: StorageInfo,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Spacer(modifier = Modifier.height(20.dp))
        StorageCard(storageInfo = storageInfo)
        Spacer(modifier = Modifier.height(16.dp))
        DeviceRows(rows)
    }
}

private val RowBg = Color(0xFFEEF4F9)
private val LabelColor = Color(0xFF2D3748)
private val ValueColor = Color(0xFF8190A5)

@Composable
private fun DeviceRows(rows: List<DeviceScanRow>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        rows.forEach { row ->
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                color = RowBg,
                shape = RoundedCornerShape(10.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 14.dp, top = 14.dp, end = 14.dp, bottom = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = row.label,
                        color = LabelColor,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 16.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = row.value,
                            color = ValueColor,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Normal,
                            lineHeight = 16.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        StatusBadge(row = row)
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusBadge(row: DeviceScanRow) {
    Box(
        modifier = Modifier.size(24.dp),
        contentAlignment = Alignment.Center
    ) {
        when {
            row.complete -> CleanXCheckBadge(checked = true, size = 24.dp)
            row.active -> RotatingRingAnimation(
                modifier = Modifier.size(24.dp),
                ringWidth = 1.8.dp,
                ringColor = CleanXBlue,
                backgroundColor = Color(0xFFC8D2DE),
                animationDurationMillis = OnboardingStatusRingDurationMillis,
                arcLength = 180f
            )
            else -> CleanXCheckBadge(checked = false, size = 24.dp)
        }
    }
}

@Composable
private fun localizedOnboardingDeviceValue(value: String): String =
    when (value) {
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
private fun StorageCard(storageInfo: StorageInfo) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shape = RoundedCornerShape(10.dp),
        shadowElevation = 4.dp,
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 14.dp, top = 18.dp, end = 14.dp, bottom = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = stringResource(R.string.home_storage_label),
                    color = Color(0xFF2D3748),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 16.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = storageInfo.formattedUsed,
                        color = CleanXBlue,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 24.sp
                    )
                    Text(
                        text = "/ ${storageInfo.formattedTotal}",
                        color = Color(0xFF8190A5),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Normal,
                        lineHeight = 14.sp,
                        modifier = Modifier.padding(bottom = 2.dp, start = 4.dp)
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(16.dp)
                        .clip(RoundedCornerShape(14.5.dp))
                        .background(Color(0xFFEEF4F9))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(storageInfo.usagePercent / 100f)
                            .height(16.dp)
                            .clip(RoundedCornerShape(14.5.dp))
                            .background(CleanXBlue)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))
            Image(
                painter = painterResource(id = R.drawable.trash_can),
                contentDescription = null,
                modifier = Modifier.size(88.dp),
                contentScale = ContentScale.Fit
            )
        }
    }
}

@Composable
private fun PhoneScanIllustration(
    scanLineProgress: Float,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Image(
            painter = painterResource(id = R.drawable.scan_phone_bg),
            contentDescription = null,
            modifier = Modifier.matchParentSize(),
            contentScale = ContentScale.Fit
        )
        ScanCornerFrame(modifier = Modifier.matchParentSize())
        Image(
            painter = painterResource(id = R.drawable.scan_phone),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.Center)
                .size(width = 96.dp, height = 150.dp),
            contentScale = ContentScale.Fit
        )
        Image(
            painter = painterResource(id = R.drawable.scan_phone_border),
            contentDescription = null,
            modifier = Modifier.matchParentSize(),
            contentScale = ContentScale.Fit
        )
        Image(
            painter = painterResource(id = R.drawable.scan_phone_line),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = (56 + 90 * scanLineProgress).dp)
                .size(width = 192.dp, height = 18.dp),
            contentScale = ContentScale.FillBounds
        )
    }
}

@Composable
private fun ScanCornerFrame(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val color = CleanXBlue
        val stroke = 3.4.dp.toPx()
        val radius = 8.dp.toPx()
        val leg = 13.dp.toPx()
        val left = 10.dp.toPx()
        val right = size.width - 10.dp.toPx()
        val top = 10.dp.toPx()
        val bottom = size.height - 18.dp.toPx()
        val arcSize = Size(radius * 2f, radius * 2f)

        fun drawTopLeft() {
            drawLine(color, Offset(left, top + radius + leg), Offset(left, top + radius), stroke, StrokeCap.Round)
            drawArc(color, 180f, 90f, false, Offset(left, top), arcSize, style = Stroke(stroke, cap = StrokeCap.Round))
            drawLine(color, Offset(left + radius, top), Offset(left + radius + leg, top), stroke, StrokeCap.Round)
        }

        fun drawTopRight() {
            drawLine(color, Offset(right, top + radius + leg), Offset(right, top + radius), stroke, StrokeCap.Round)
            drawArc(color, 270f, 90f, false, Offset(right - radius * 2f, top), arcSize, style = Stroke(stroke, cap = StrokeCap.Round))
            drawLine(color, Offset(right - radius, top), Offset(right - radius - leg, top), stroke, StrokeCap.Round)
        }

        fun drawBottomLeft() {
            drawLine(color, Offset(left, bottom - radius - leg), Offset(left, bottom - radius), stroke, StrokeCap.Round)
            drawArc(color, 90f, 90f, false, Offset(left, bottom - radius * 2f), arcSize, style = Stroke(stroke, cap = StrokeCap.Round))
            drawLine(color, Offset(left + radius, bottom), Offset(left + radius + leg, bottom), stroke, StrokeCap.Round)
        }

        fun drawBottomRight() {
            drawLine(color, Offset(right, bottom - radius - leg), Offset(right, bottom - radius), stroke, StrokeCap.Round)
            drawArc(color, 0f, 90f, false, Offset(right - radius * 2f, bottom - radius * 2f), arcSize, style = Stroke(stroke, cap = StrokeCap.Round))
            drawLine(color, Offset(right - radius, bottom), Offset(right - radius - leg, bottom), stroke, StrokeCap.Round)
        }

        drawTopLeft()
        drawTopRight()
        drawBottomLeft()
        drawBottomRight()
    }
}

@Preview
@Composable
private fun PreviewOnboardingScanScreen() {
    OnboardingScanScreen {}
}
