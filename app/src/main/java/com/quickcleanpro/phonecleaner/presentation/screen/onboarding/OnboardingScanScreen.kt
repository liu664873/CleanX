package com.quickcleanpro.phonecleaner.presentation.screen.onboarding

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.quickcleanpro.phonecleaner.R
import com.quickcleanpro.phonecleaner.domain.model.device.StorageInfo
import com.quickcleanpro.phonecleaner.presentation.common.components.CleanXCheckBadge
import com.quickcleanpro.phonecleaner.presentation.common.components.PhoneScanIllustration
import com.quickcleanpro.phonecleaner.presentation.common.components.RoundedProgressBar
import com.quickcleanpro.phonecleaner.presentation.common.components.animations.RotatingRingAnimation
import com.quickcleanpro.phonecleaner.presentation.common.components.buttons.CleanXPrimaryButton
import com.quickcleanpro.phonecleaner.presentation.common.components.styles.CleanXBlue
import kotlinx.coroutines.delay
import org.koin.androidx.compose.koinViewModel

private const val ONBOARDING_STEP_DELAY_MILLIS = 680L
private const val OnboardingStatusRingDurationMillis = 1200
private const val OnboardingScanLineDurationMillis = 1800

private data class DeviceScanRow(
    val label: String,
    val value: String,
    val complete: Boolean,
    val active: Boolean = false
)

@Composable
fun OnboardingScanScreen(onContinueToHome: () -> Unit) {
    OnboardingScanContent(
        onContinueToHome = onContinueToHome,
    )
}

@Composable
private fun OnboardingScanContent(onContinueToHome: () -> Unit) {
    val viewModel: OnboardingScanViewModel = koinViewModel()
    val lifecycleOwner = LocalLifecycleOwner.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var completedStep by remember { mutableIntStateOf(0) }

    DisposableEffect(lifecycleOwner, viewModel) {
        val observer =
            LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME) {
                    viewModel.refresh()
                }
            }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(Unit) {
        for (step in 1..6) {
            delay(ONBOARDING_STEP_DELAY_MILLIS)
            completedStep = step
        }
        delay(ONBOARDING_STEP_DELAY_MILLIS)
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

    Scaffold(
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
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
            animation = tween(
                durationMillis = OnboardingScanLineDurationMillis,
                easing = LinearEasing
            ),
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
            color = OnboardingNavy,
            fontSize = 20.sp,
            lineHeight = 28.sp,
            fontWeight = FontWeight.SemiBold,
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
        Spacer(modifier = Modifier.height(12.dp))
        DeviceInfoSourceHint()
        Spacer(modifier = Modifier.height(24.dp))
        DeviceRows(rows)
    }
}

@Composable
private fun DeviceInfoSourceHint(){
    Text(
        text = stringResource(R.string.device_info_source_hint),
        color = OnboardingNavy,
        textAlign = TextAlign.Start,
        overflow = TextOverflow.Visible,
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp),
        fontWeight = FontWeight.W400,
        fontSize = 12.sp,
        lineHeight = 18.sp,
    )
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
private fun StorageCard(storageInfo: StorageInfo) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(96.dp)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = CardBg,
            shape = RoundedCornerShape(12.dp)
        ) {}

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 16.dp, top = 20.dp, end = 16.dp, bottom = 20.dp)
        ) {
            Text(
                text = stringResource(R.string.home_storage_label),
                color = OnboardingNavy,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = storageInfo.formattedUsed,
                    color = OnboardingNavy,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.SemiBold,
                    lineHeight = 24.sp
                )
                Text(
                    text = "/ ${storageInfo.formattedTotal}",
                    color = OnboardingNavy,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    lineHeight = 16.sp,
                    modifier = Modifier.padding(bottom = 2.dp, start = 4.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            RoundedProgressBar(
                progress = storageInfo.usagePercent / 100f,
                width = 311.dp,
                height = 16.dp,
                trackColor = Color(0x14000000),
                fillColor = CleanXBlue
            )
        }

        Image(
            painter = painterResource(id = R.drawable.robot),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = (-16).dp, y = (-40).dp)
                .size(width = 90.dp, height = 84.dp),
            contentScale = ContentScale.Fit
        )
    }
}

private val OnboardingNavy = Color(0xFF1D2959)
private val CardBg = Color(0xFFF6F7FB)
private val DividerColor = Color(0x261D2959) // rgba(29,41,89,0.15)

@Composable
private fun DeviceRows(rows: List<DeviceScanRow>) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = CardBg,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            rows.forEachIndexed { index, row ->
                if (index > 0) {
                    HorizontalDivider(
                        color = DividerColor,
                        thickness = 1.dp
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = row.label,
                        color = OnboardingNavy,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
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
                            color = OnboardingNavy,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Normal,
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

@Preview
@Composable
private fun PreviewOnboardingScanScreen() {
    OnboardingScanScreen {}
}
