package com.quickcleanpro.phonecleaner.presentation.screen.tools.deviceinfo

import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
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
import com.quickcleanpro.phonecleaner.domain.model.DeviceHardwareInfo
import com.quickcleanpro.phonecleaner.presentation.common.components.CleanXScaffoldPage
import com.quickcleanpro.phonecleaner.presentation.common.components.animations.CleanSpiralAnimation
import com.quickcleanpro.phonecleaner.presentation.common.components.popups.StopScanDialog
import com.quickcleanpro.phonecleaner.presentation.common.components.styles.CleanXBackground
import com.quickcleanpro.phonecleaner.presentation.common.components.styles.CleanXMutedText
import com.quickcleanpro.phonecleaner.presentation.common.components.styles.CleanXText
import com.quickcleanpro.phonecleaner.presentation.common.route.LocalRouter
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.components.FileManagerNavy
import com.quickcleanpro.phonecleaner.presentation.screen.tools.common.device.DeviceInfoMode
import com.quickcleanpro.phonecleaner.presentation.screen.tools.common.device.DeviceInfoUiState
import com.quickcleanpro.phonecleaner.presentation.screen.tools.common.device.DeviceInfoViewModel
import com.quickcleanpro.phonecleaner.presentation.screen.tools.common.device.currentBatteryCapacityMah
import com.quickcleanpro.phonecleaner.presentation.screen.tools.common.device.formatBatteryCapacityMah
import com.quickcleanpro.phonecleaner.presentation.screen.tools.common.device.formatBatteryTemperature
import com.quickcleanpro.phonecleaner.presentation.screen.tools.common.device.formatBatteryVoltage
import com.quickcleanpro.phonecleaner.presentation.screen.tools.common.device.orPlaceholder
import kotlinx.coroutines.delay
import org.koin.compose.koinInject
import kotlin.math.roundToInt

@Composable
fun DeviceInfoScreen(viewModel: DeviceInfoViewModel = koinInject()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current
    val router = LocalRouter.current
    var showScanning by remember { mutableStateOf(true) }
    var showExitDialog by remember { mutableStateOf(false) }
    var scanSession by remember { mutableStateOf(0) }

    DisposableEffect(lifecycleOwner, viewModel) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                showScanning = true
                showExitDialog = false
                scanSession += 1
                viewModel.load(DeviceInfoMode.Device)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
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
        title = stringResource(R.string.device_model),
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

            DeviceScanningContent(
                checkingText = stringResource(R.string.onboarding_checking_device_info),
            )
        } else {
            DeviceModelCard(uiState.hardware)
            StateInfoCard(uiState)
            ScreenCard(uiState.hardware)
            BatteryCard(uiState)
            SensorsCard(uiState.hardware)
            CpuCard(uiState.hardware)
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
private fun DeviceScanningContent(checkingText: String) {
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
                    contentDescription = checkingText,
                    modifier = Modifier.size(70.dp),
                    contentScale = ContentScale.Fit,
                )
            }
            Spacer(modifier = Modifier.height(57.dp))
            Text(
                text = checkingText,
                color = FileManagerNavy,
                fontSize = 18.sp,
                lineHeight = 24.sp,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun DeviceModelCard(hardware: DeviceHardwareInfo) {
    Surface(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(114.dp),
        color = Color.White,
        shape = RoundedCornerShape(10.dp),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(start = 32.dp, end = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
            ) {
                SummaryTextLine(
                    label = stringResource(R.string.home_device_model),
                    value = deviceModelName(hardware.model),
                )
                Spacer(modifier = Modifier.height(8.dp))
                SummaryTextLine(
                    label = stringResource(R.string.home_system_version),
                    value = hardware.androidVersion.orPlaceholder(),
                )
            }

            Image(
                painter = painterResource(id = R.drawable.device_model),
                contentDescription = null,
                modifier = Modifier.size(width = 111.dp, height = 82.dp),
                contentScale = ContentScale.Fit,
            )
        }
    }
}

@Composable
private fun StateInfoCard(uiState: DeviceInfoUiState) {
    val animatedCpuProgress by animateFloatAsState(
        targetValue = uiState.cpuUsagePercent?.let { it / 100f } ?: 0f,
        animationSpec = tween(500),
        label = "cpuProgress",
    )
    val animatedRamProgress by animateFloatAsState(
        targetValue = uiState.memory.usagePercent / 100f,
        animationSpec = tween(500),
        label = "ramProgress",
    )
    val animatedStorageProgress by animateFloatAsState(
        targetValue = uiState.storage.usagePercent / 100f,
        animationSpec = tween(500),
        label = "storageProgress",
    )
    val cpuUsageText = uiState.cpuUsagePercent?.let { "$it%" } ?: "--"
    val cpuTemperatureText = formatBatteryTemperature(uiState.cpuTemperatureC, uiState.temperatureUnit, includeSpace = false)
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shape = RoundedCornerShape(10.dp),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 17.dp),
        ) {
            SectionTitle(stringResource(R.string.device_state_info))
            Spacer(modifier = Modifier.height(18.dp))
            ProgressInfoRow(
                label = "${stringResource(R.string.device_progress_cpu_label)}$cpuUsageText/$cpuTemperatureText",
                progress = animatedCpuProgress,
                color = DeviceCpuProgressColor,
                trackColor = DeviceCpuProgressTrackColor,
            )
            Spacer(modifier = Modifier.height(16.dp))
            ProgressInfoRow(
                label = "${stringResource(R.string.device_progress_ram_label)}${formatCompactBytes(uiState.memory.usedBytes)}/${formatCompactBytes(uiState.memory.totalBytes)}",
                progress = animatedRamProgress,
                color = DeviceRamProgressColor,
                trackColor = DeviceRamProgressTrackColor,
            )
            Spacer(modifier = Modifier.height(16.dp))
            ProgressInfoRow(
                label = "${stringResource(R.string.device_progress_storage_label)}${formatCompactBytes(uiState.storage.usedBytes)}/${formatCompactBytes(uiState.storage.totalBytes)}",
                progress = animatedStorageProgress,
                color = DeviceStorageProgressColor,
                trackColor = DeviceStorageProgressTrackColor,
            )
        }
    }
}

@Composable
private fun ProgressInfoRow(
    label: String,
    progress: Float?,
    color: Color,
    trackColor: Color,
) {
    Column {
        Text(
            text = label,
            color = CleanXMutedText,
            fontSize = 16.sp,
            lineHeight = 20.sp,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .background(trackColor, RoundedCornerShape(50)),
        ) {
            progress?.let { value ->
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth(value.coerceIn(0f, 1f))
                            .height(12.dp)
                            .background(color, RoundedCornerShape(50)),
                )
            }
        }
    }
}

private val DeviceCpuProgressColor = Color(0xFF41B7FC)
private val DeviceCpuProgressTrackColor = Color(0xFFEEF4F9)
private val DeviceRamProgressColor = Color(0xFFFCD341)
private val DeviceRamProgressTrackColor = Color(0xFFF9F6EE)
private val DeviceStorageProgressColor = Color(0xFF41FC89)
private val DeviceStorageProgressTrackColor = Color(0xFFF9EEF8)

@Composable
private fun InfoSection(
    title: String,
    rows: List<Pair<String, String>>,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shape = RoundedCornerShape(10.dp),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 17.dp),
        ) {
            SectionTitle(title)
            Spacer(modifier = Modifier.height(18.dp))
            rows.forEachIndexed { index, (label, value) ->
                InfoRow(label, value)
                if (index != rows.lastIndex) {
                    Spacer(modifier = Modifier.height(17.dp))
                }
            }
        }
    }
}

@Composable
private fun SummaryTextLine(
    label: String,
    value: String,
) {
    Column {
        Text(
            text = label,
            color = CleanXMutedText,
            fontSize = 12.sp,
            lineHeight = 16.sp,
        )
        Text(
            text = value,
            color = CleanXText,
            fontSize = 17.sp,
            lineHeight = 22.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun ScreenCard(hardware: DeviceHardwareInfo) {
    InfoSection(
        title = stringResource(R.string.device_screen),
        rows =
            listOf(
                stringResource(R.string.device_screen_size) to hardware.screenSize.orPlaceholder(),
                stringResource(R.string.device_screen_density) to hardware.screenDensity.orPlaceholder(),
                stringResource(R.string.device_screen_multi_touch) to supportText(hardware.multiTouchSupported),
            ),
    )
}

@Composable
private fun BatteryCard(uiState: DeviceInfoUiState) {
    InfoSection(
        title = stringResource(R.string.battery_info),
        rows =
            listOf(
                stringResource(R.string.battery_health_status) to localizedBatteryHealth(uiState.battery.health),
                stringResource(R.string.battery_current_capacity) to
                    formatBatteryCapacityMah(currentBatteryCapacityMah(uiState.battery)),
                stringResource(R.string.battery_total_capacity) to formatBatteryCapacityMah(uiState.battery.capacity),
                stringResource(R.string.battery_voltage) to formatBatteryVoltage(uiState.battery.voltage),
                stringResource(R.string.battery_temperature) to
                    formatBatteryTemperature(uiState.battery.temperature, uiState.temperatureUnit),
                stringResource(R.string.battery_status) to localizedBatteryStatus(uiState.batteryStatus.statusText),
                stringResource(R.string.battery_charging_status) to
                    if (uiState.batteryStatus.isCharging) {
                        stringResource(R.string.battery_charging)
                    } else {
                        stringResource(R.string.battery_not_charging)
                    },
                stringResource(R.string.battery_technology) to uiState.battery.technology.orPlaceholder(),
                stringResource(R.string.battery_available_time) to uiState.batteryLifeText,
            ),
    )
}

@Composable
private fun SensorsCard(hardware: DeviceHardwareInfo) {
    InfoSection(
        title = stringResource(R.string.device_sensors),
        rows =
            listOf(
                stringResource(R.string.device_accelerometer_sensor) to supportText(hardware.sensors.accelerometer),
                stringResource(R.string.device_magnetic_field_sensor) to supportText(hardware.sensors.magneticField),
                stringResource(R.string.device_orientation_sensor) to supportText(hardware.sensors.orientation),
                stringResource(R.string.device_gyroscope_sensor) to supportText(hardware.sensors.gyroscope),
                stringResource(R.string.device_light_sensor) to supportText(hardware.sensors.light),
                stringResource(R.string.device_distance_sensor) to supportText(hardware.sensors.proximity),
                stringResource(R.string.device_temperature_sensor) to supportText(hardware.sensors.ambientTemperature),
            ),
    )
}

@Composable
private fun CpuCard(hardware: DeviceHardwareInfo) {
    val cpuCoreCount =
        hardware.cpu.cores
            .takeIf { it > 0 }
            ?.toString()
            ?: "--"
    InfoSection(
        title = stringResource(R.string.device_cpu_hardware),
        rows =
            listOf(
                stringResource(R.string.device_cpu_hardware) to hardware.cpu.hardware.orPlaceholder(),
                stringResource(R.string.device_cpu_model) to hardware.cpu.model.orPlaceholder(),
                stringResource(R.string.device_cpu_cores) to cpuCoreCount,
                stringResource(R.string.device_cpu_frequency) to hardware.cpu.maxFrequency.orPlaceholder(),
            ),
    )
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

@Composable
private fun supportText(supported: Boolean): String =
    if (supported) {
        stringResource(R.string.device_supported)
    } else {
        stringResource(R.string.device_not_supported)
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

private fun deviceModelName(model: String): String {
    val resolvedModel = model.orPlaceholder()
    val manufacturer = Build.MANUFACTURER.orEmpty().trim()
    if (resolvedModel == "--" || manufacturer.isBlank()) return resolvedModel
    return if (resolvedModel.contains(manufacturer, ignoreCase = true)) {
        resolvedModel
    } else {
        "$manufacturer-$resolvedModel"
    }
}

private fun formatCompactBytes(bytes: Long): String {
    if (bytes <= 0L) return "0B"
    val gb = bytes / (1024f * 1024f * 1024f)
    val mb = bytes / (1024f * 1024f)
    return if (gb >= 1f) {
        "${(gb * 10f).roundToInt() / 10f}GB"
    } else {
        "${mb.roundToInt()}MB"
    }
}
