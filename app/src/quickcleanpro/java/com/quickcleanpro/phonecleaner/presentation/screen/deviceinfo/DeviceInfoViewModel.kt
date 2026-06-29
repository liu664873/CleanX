package com.quickcleanpro.phonecleaner.presentation.screen.deviceinfo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.annotation.StringRes
import com.quickcleanpro.phonecleaner.R
import com.quickcleanpro.phonecleaner.data.repository.BatteryHistoryRepositoryImpl.Companion.BATTERY_HISTORY_SAMPLE_INTERVAL_MILLIS
import com.quickcleanpro.phonecleaner.data.repository.BatteryHistoryRepositoryImpl.Companion.MAX_BATTERY_HISTORY_SAMPLES
import com.quickcleanpro.phonecleaner.data.source.battery.BatteryHistoryOwner
import com.quickcleanpro.phonecleaner.data.source.battery.BatteryHistorySampler
import com.quickcleanpro.phonecleaner.domain.model.BatteryHistorySample
import com.quickcleanpro.phonecleaner.domain.model.BatteryStatusInfo
import com.quickcleanpro.phonecleaner.domain.model.DeviceHardwareInfo
import com.quickcleanpro.phonecleaner.domain.repository.BatteryHistoryRepository
import com.quickcleanpro.phonecleaner.domain.repository.DeviceInfoRepository
import com.quickcleanpro.phonecleaner.domain.repository.SettingsRepository
import com.quickcleanpro.phonecleaner.presentation.common.batteryHistoryRepositoryOrPreview
import com.quickcleanpro.phonecleaner.presentation.common.batteryHistorySamplerOrPreview
import com.quickcleanpro.phonecleaner.presentation.common.deviceInfoRepositoryOrPreview
import com.quickcleanpro.phonecleaner.presentation.common.settingsRepositoryOrPreview
import com.quickcleanpro.phonecleaner.domain.model.device.BatteryInfo
import com.quickcleanpro.phonecleaner.domain.model.device.MemoryInfo
import com.quickcleanpro.phonecleaner.domain.model.device.StorageInfo
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import com.quickcleanpro.phonecleaner.presentation.common.appString

enum class DeviceInfoMode {
    Device,
    Battery
}

internal enum class BatteryCurrentRange(@param:StringRes val labelRes: Int, val durationMillis: Long) {
    OneMinute(R.string.battery_range_1_min, 60_000L),
    OneHour(R.string.battery_range_1_hour, 60L * 60L * 1000L),
    TwentyFourHours(R.string.battery_range_24_hours, 24L * 60L * 60L * 1000L)
}

internal data class BatteryCurrentSample(
    val timestampMillis: Long,
    val currentMa: Float
)

internal data class BatteryTemperatureSample(
    val timestampMillis: Long,
    val temperatureC: Float
)

internal data class DeviceInfoRowGroup(
    @param:StringRes val titleRes: Int,
    val rows: List<DeviceInfoRow>
)

internal data class DeviceInfoRow(
    @param:StringRes val labelRes: Int,
    val value: String
)

internal data class DeviceInfoUiState(
    val mode: DeviceInfoMode = DeviceInfoMode.Device,
    val tempUnit: String = "C",
    val battery: BatteryInfo? = null,
    val batteryStatus: BatteryStatusInfo = BatteryStatusInfo(statusText = "Unknown", isCharging = false),
    val memory: MemoryInfo? = null,
    val storage: StorageInfo? = null,
    val hardware: DeviceHardwareInfo? = null,
    val cpuUsage: Int = 0,
    val cpuTemperatureC: Float? = null,
    val cpuTemperature: String = "--",
    val ramLabel: String = "RAM:0B/0B",
    val ramPercent: Int = 0,
    val storageLabel: String = "Storage:0B/0B",
    val storagePercent: Int = 0,
    val deviceRows: List<DeviceInfoRowGroup> = emptyList(),
    val batteryRows: List<DeviceInfoRow> = emptyList(),
    val currentNow: Float? = null,
    val currentAverage: Float? = null,
    val selectedCurrentRange: BatteryCurrentRange = BatteryCurrentRange.OneMinute,
    val currentSamples: List<BatteryCurrentSample> = emptyList(),
    val temperatureSamples: List<BatteryTemperatureSample> = emptyList(),
    val latestSampleTimestampMillis: Long = 0L,
    val isLoading: Boolean = true,
    val errorMessage: String? = null
) {
    val batteryStatusText: String
        get() = batteryStatus.statusText

    val batteryCapacityText: String
        get() = battery?.levelPercent?.takeIf { it >= 0 }?.let { "$it%" } ?: "--"

    val batteryTemperatureValue: String
        get() = battery?.let { formatBatteryTemperature(it.temperature, tempUnit, includeSpace = true) } ?: "--"

    val batteryTemperatureRowValue: String
        get() = battery?.let { formatBatteryTemperature(it.temperature, tempUnit, includeSpace = false) } ?: "--"

    val batteryAverageTemperatureRowValue: String
        get() = selectedTemperatureAverageC
            ?.let { formatBatteryTemperature(it, tempUnit, includeSpace = false) }
            ?: "--"

    val batteryVoltageValue: String
        get() = battery?.let { formatBatteryVoltage(it.voltage) } ?: "--"

    val batteryHealthText: String
        get() = battery?.health ?: "Unknown"
    val batteryLifeText: String
        get() = battery?.availableTime?.takeIf { it.isNotBlank() && it != "Unknown" } ?: "--"

    val selectedCurrentSamples: List<BatteryCurrentSample>
        get() = filterSamplesForRange(
            samples = currentSamples,
            currentNow = currentNow,
            selectedRange = selectedCurrentRange,
            timestampMillis = latestSampleTimestampMillis.takeIf { it > 0L } ?: System.currentTimeMillis()
        )

    val selectedCurrentAverage: Float?
        get() = averageCurrent(selectedCurrentSamples, currentAverage)

    val selectedTemperatureSamples: List<BatteryTemperatureSample>
        get() = filterTemperatureSamples(
            samples = temperatureSamples,
            currentTemperatureC = battery?.temperature,
            timestampMillis = latestSampleTimestampMillis.takeIf { it > 0L } ?: System.currentTimeMillis()
        )

    val selectedTemperatureAverageC: Float?
        get() = averageTemperature(selectedTemperatureSamples)
}

internal class DeviceInfoViewModel private constructor(
    private val repository: DeviceInfoRepository,
    private val batteryHistoryRepository: BatteryHistoryRepository,
    private val batteryHistorySampler: BatteryHistorySampler?,
    private val settingsRepository: SettingsRepository,
    private val ioDispatcher: CoroutineDispatcher,
    private val nowMillis: () -> Long,
    private val testLoader: (((suspend () -> Unit)) -> Unit)?,
    private val maxSampleCount: Int
) : ViewModel() {

    constructor(
        repository: DeviceInfoRepository,
        batteryHistoryRepository: BatteryHistoryRepository,
        batteryHistorySampler: BatteryHistorySampler?,
        settingsRepository: SettingsRepository,
        ioDispatcher: CoroutineDispatcher
    ) : this(
        repository,
        batteryHistoryRepository,
        batteryHistorySampler,
        settingsRepository,
        ioDispatcher,
        System::currentTimeMillis,
        null,
        MAX_BATTERY_HISTORY_SAMPLES
    )

    constructor(
        repository: DeviceInfoRepository = deviceInfoRepositoryOrPreview(),
        batteryHistoryRepository: BatteryHistoryRepository = batteryHistoryRepositoryOrPreview(),
        batteryHistorySampler: BatteryHistorySampler? = batteryHistorySamplerOrPreview(),
        nowMillis: () -> Long = System::currentTimeMillis,
        testLoader: (((suspend () -> Unit)) -> Unit)? = null,
        maxSampleCount: Int = MAX_BATTERY_HISTORY_SAMPLES,
        settingsRepository: SettingsRepository = settingsRepositoryOrPreview(),
        ioDispatcher: CoroutineDispatcher = Dispatchers.IO
    ) : this(
        repository,
        batteryHistoryRepository,
        batteryHistorySampler,
        settingsRepository,
        ioDispatcher,
        nowMillis,
        testLoader,
        maxSampleCount
    )

    private val _uiState = MutableStateFlow(DeviceInfoUiState())
    private var historyCollectionJob: Job? = null

    val uiState: StateFlow<DeviceInfoUiState> = _uiState.asStateFlow()

    fun load(mode: DeviceInfoMode, tempUnit: String) {
        val resolvedTempUnit = tempUnit.ifBlank { settingsRepository.readTemperatureUnit() }
        _uiState.update {
            it.copy(
                mode = mode,
                tempUnit = resolvedTempUnit,
                isLoading = true,
                errorMessage = null
            )
        }
        if (mode != DeviceInfoMode.Battery) {
            stopCurrentSampling()
        } else {
            sampleOnce()
        }
        launchLoad {
            val previousState = _uiState.value
            runCatching { buildState(mode, resolvedTempUnit, previousState) }
                .onSuccess { loaded ->
                    _uiState.value = loaded
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: appString(R.string.device_info_load_failed)
                        )
                    }
                }
        }
    }

    fun refresh(tempUnit: String = _uiState.value.tempUnit) {
        load(_uiState.value.mode, tempUnit)
    }

    fun selectCurrentRange(range: BatteryCurrentRange) {
        _uiState.update { it.copy(selectedCurrentRange = range) }
    }

    internal fun sampleOnce() {
        val sample = batteryHistorySampler?.sampleOnce(force = true)
        if (sample == null) {
            sampleCurrent()
        } else {
            applyHistorySamples(batteryHistoryRepository.samples.value, nowMillis())
        }
    }

    fun startCurrentSampling() {
        val timestamp = nowMillis()
        applyHistorySamples(batteryHistoryRepository.loadRecent(timestamp), timestamp)
        startHistoryCollection()
        sampleOnce()
        batteryHistorySampler?.start(BatteryHistoryOwner.BatteryPage)
    }

    fun stopCurrentSampling() {
        batteryHistorySampler?.stop(BatteryHistoryOwner.BatteryPage)
        historyCollectionJob?.cancel()
        historyCollectionJob = null
    }

    override fun onCleared() {
        stopCurrentSampling()
        super.onCleared()
    }

    private fun buildState(
        mode: DeviceInfoMode,
        tempUnit: String,
        previousState: DeviceInfoUiState
    ): DeviceInfoUiState {
        val battery = repository.batteryInfo()
        val batteryStatus = repository.batteryStatusInfo()
        val memory = repository.memoryInfo()
        val storage = repository.internalStorageInfo()
        val hardware = repository.hardwareInfo()
        val currentNow = repository.batteryCurrentNowMa()
        val currentAverage = repository.batteryCurrentAverageMa()
        val cpuTemperatureC = repository.cpuTemperatureC() ?: previousState.cpuTemperatureC
        val timestamp = nowMillis()
        val historySamples = if (mode == DeviceInfoMode.Battery) {
            batteryHistoryRepository.loadRecent(timestamp)
        } else {
            emptyList()
        }

        return DeviceInfoUiState(
            mode = mode,
            tempUnit = tempUnit,
            battery = battery,
            batteryStatus = batteryStatus,
            memory = memory,
            storage = storage,
            hardware = hardware,
            cpuUsage = (100 - memory.usagePercent).coerceIn(0, 100),
            cpuTemperatureC = cpuTemperatureC,
            cpuTemperature = formatCpuTemperature(cpuTemperatureC, tempUnit),
            ramLabel = "RAM:${formatCompactBytes(memory.usedBytes)}/${formatCompactBytes(memory.totalBytes)}",
            ramPercent = memory.usagePercent,
            storageLabel = "Storage:${formatCompactBytes(storage.usedBytes)}/${formatCompactBytes(storage.totalBytes)}",
            storagePercent = storage.usagePercent,
            deviceRows = buildDeviceRows(hardware, battery, batteryStatus, tempUnit, cpuTemperatureC),
            batteryRows = buildBatteryRows(battery, batteryStatus, tempUnit),
            currentNow = currentNow,
            currentAverage = currentAverage,
            selectedCurrentRange = previousState.selectedCurrentRange,
            currentSamples = historySamples.toCurrentSamples(timestamp, maxSampleCount),
            temperatureSamples = historySamples.toTemperatureSamples(timestamp),
            latestSampleTimestampMillis = timestamp,
            isLoading = false,
            errorMessage = null
        )
    }

    private fun sampleCurrent() {
        val timestamp = nowMillis()
        val battery = runCatching { repository.batteryInfo() }.getOrNull()
        val currentNow = runCatching { repository.batteryCurrentNowMa() }.getOrNull()
        val currentAverage = runCatching { repository.batteryCurrentAverageMa() }.getOrNull()
        if (battery != null) {
            batteryHistoryRepository.append(
                BatteryHistorySample(
                    timestampMillis = timestamp,
                    currentMa = currentNow,
                    temperatureC = battery.temperature
                ),
                timestamp
            )
        }
        _uiState.update { state ->
            state.copy(
                battery = battery ?: state.battery,
                currentNow = currentNow,
                currentAverage = currentAverage
            ).withHistorySamples(
                samples = batteryHistoryRepository.samples.value,
                timestampMillis = timestamp,
                maxSampleCount = maxSampleCount
            )
        }
    }

    private fun startHistoryCollection() {
        if (testLoader != null) return
        if (historyCollectionJob?.isActive == true) return
        historyCollectionJob = viewModelScope.launch(ioDispatcher) {
            batteryHistoryRepository.samples.collect { samples ->
                applyHistorySamples(samples, nowMillis())
            }
        }
    }

    private fun applyHistorySamples(
        samples: List<BatteryHistorySample>,
        timestampMillis: Long
    ) {
        _uiState.update { state ->
            state.withHistorySamples(samples, timestampMillis, maxSampleCount)
        }
    }

    private fun launchLoad(block: suspend () -> Unit) {
        val loader = testLoader
        if (loader != null) {
            loader(block)
        } else {
            viewModelScope.launch(ioDispatcher) { block() }
        }
    }

}

private fun DeviceInfoUiState.withHistorySamples(
    samples: List<BatteryHistorySample>,
    timestampMillis: Long,
    maxSampleCount: Int
): DeviceInfoUiState {
    val recentSamples = samples.recentBatteryHistory(timestampMillis)
    val latestSample = recentSamples.lastOrNull()
    val freshLatestSample = latestSample?.takeIf { sample ->
        timestampMillis - sample.timestampMillis in 0L..BATTERY_HISTORY_FRESH_SAMPLE_MILLIS
    }
    return copy(
        battery = freshLatestSample?.let { latest ->
            battery?.copy(temperature = latest.temperatureC)
        } ?: battery,
        currentNow = freshLatestSample?.currentMa ?: currentNow,
        currentSamples = recentSamples.toCurrentSamples(timestampMillis, maxSampleCount),
        temperatureSamples = recentSamples.toTemperatureSamples(timestampMillis),
        latestSampleTimestampMillis = latestSample?.timestampMillis ?: timestampMillis
    )
}

private fun List<BatteryHistorySample>.toCurrentSamples(
    timestampMillis: Long,
    maxSampleCount: Int
): List<BatteryCurrentSample> =
    recentBatteryHistory(timestampMillis)
        .mapNotNull { sample ->
            sample.currentMa?.let { current ->
                BatteryCurrentSample(sample.timestampMillis, current)
            }
        }
        .takeLast(maxSampleCount)

private fun List<BatteryHistorySample>.toTemperatureSamples(
    timestampMillis: Long
): List<BatteryTemperatureSample> =
    recentBatteryHistory(timestampMillis)
        .asSequence()
        .filter { it.timestampMillis >= timestampMillis - TEMPERATURE_WINDOW_MILLIS }
        .map { BatteryTemperatureSample(it.timestampMillis, it.temperatureC) }
        .toList()

private fun List<BatteryHistorySample>.recentBatteryHistory(
    timestampMillis: Long
): List<BatteryHistorySample> {
    val cutoff = timestampMillis - BatteryCurrentRange.TwentyFourHours.durationMillis
    val latestAllowed = timestampMillis + BATTERY_HISTORY_FUTURE_TOLERANCE_MILLIS
    return asSequence()
        .filter { it.timestampMillis in cutoff..latestAllowed }
        .sortedBy { it.timestampMillis }
        .toList()
}


internal fun initialSamples(currentNow: Float?, timestampMillis: Long = System.currentTimeMillis()): List<BatteryCurrentSample> =
    currentNow?.let { listOf(BatteryCurrentSample(timestampMillis, it)) }.orEmpty()

internal fun initialTemperatureSamples(
    temperatureC: Float?,
    timestampMillis: Long = System.currentTimeMillis()
): List<BatteryTemperatureSample> =
    temperatureC?.let { listOf(BatteryTemperatureSample(timestampMillis, it)) }.orEmpty()


internal fun buildDeviceRows(
    hardware: DeviceHardwareInfo,
    battery: BatteryInfo,
    batteryStatus: BatteryStatusInfo,
    tempUnit: String,
    cpuTemperatureC: Float? = null
): List<DeviceInfoRowGroup> = listOf(
    DeviceInfoRowGroup(
        titleRes = R.string.device_screen,
        rows = listOf(
            DeviceInfoRow(R.string.device_screen_size, hardware.screenSize),
            DeviceInfoRow(R.string.device_screen_density, hardware.screenDensity),
            DeviceInfoRow(R.string.device_screen_multi_touch, hardware.multiTouchSupported.toSupportText())
        )
    ),
    DeviceInfoRowGroup(
        titleRes = R.string.battery_info,
        rows = buildBatteryRows(battery, batteryStatus, tempUnit)
    ),
    DeviceInfoRowGroup(
        titleRes = R.string.device_sensors,
        rows = listOf(
            DeviceInfoRow(R.string.device_accelerometer_sensor, hardware.sensors.accelerometer.toSupportText()),
            DeviceInfoRow(R.string.device_magnetic_field_sensor, hardware.sensors.magneticField.toSupportText()),
            DeviceInfoRow(R.string.device_orientation_sensor, hardware.sensors.orientation.toSupportText()),
            DeviceInfoRow(R.string.device_gyroscope_sensor, hardware.sensors.gyroscope.toSupportText()),
            DeviceInfoRow(R.string.device_light_sensor, hardware.sensors.light.toSupportText()),
            DeviceInfoRow(R.string.device_distance_sensor, hardware.sensors.proximity.toSupportText()),
            DeviceInfoRow(R.string.device_temperature_sensor, hardware.sensors.ambientTemperature.toSupportText())
        )
    ),
    DeviceInfoRowGroup(
        titleRes = R.string.device_cpu_hardware,
        rows = listOf(
            DeviceInfoRow(R.string.device_cpu_hardware, hardware.cpu.hardware),
            DeviceInfoRow(R.string.device_cpu_model, hardware.cpu.model),
            DeviceInfoRow(R.string.device_cpu_cores, hardware.cpu.cores.toString()),
            DeviceInfoRow(R.string.device_cpu_frequency, hardware.cpu.maxFrequency),
            DeviceInfoRow(R.string.device_cpu_temperature, formatCpuTemperature(cpuTemperatureC, tempUnit))
        )
    )
)


internal fun buildBatteryRows(
    battery: BatteryInfo,
    batteryStatus: BatteryStatusInfo,
    tempUnit: String
): List<DeviceInfoRow> = listOf(
    DeviceInfoRow(R.string.battery_health_status, battery.health),
    DeviceInfoRow(R.string.battery_current_capacity, "${battery.capacity.coerceAtLeast(0)} mAh"),
    DeviceInfoRow(R.string.battery_total_capacity, "${battery.capacity.coerceAtLeast(0).takeIf { it > 0 } ?: 100} mAh"),
    DeviceInfoRow(R.string.battery_voltage, formatBatteryVoltage(battery.voltage)),
    DeviceInfoRow(R.string.battery_temperature, formatBatteryTemperature(battery.temperature, tempUnit, includeSpace = true)),
    DeviceInfoRow(R.string.battery_status, batteryStatus.statusText),
    DeviceInfoRow(R.string.battery_charging_status, if (batteryStatus.isCharging) "Charging" else "Not Charging"),
    DeviceInfoRow(R.string.battery_technology, battery.technology),
    DeviceInfoRow(R.string.battery_available_time, battery.availableTime)
)


internal fun Boolean.toSupportText(): String =
    if (this) "Supported" else "Not Supported"


internal fun formatBatteryTemperature(tempC: Float, tempUnit: String, includeSpace: Boolean): String {
    val separator = if (includeSpace) " " else ""
    return if (tempUnit == "F") {
        String.format(java.util.Locale.US, "%.1f%s\u00B0F", tempC * 9f / 5f + 32f, separator)
    } else {
        String.format(java.util.Locale.US, "%.1f%s\u00B0C", tempC, separator)
    }
}

internal fun formatCpuTemperature(tempC: Float?, tempUnit: String): String =
    tempC?.takeIf { it.isFinite() }
        ?.let { formatBatteryTemperature(it, tempUnit, includeSpace = false) }
        ?: "--"


internal fun formatBatteryVoltage(voltageMilliVolts: Int): String {
    if (voltageMilliVolts <= 0) return "--"
    return String.format(java.util.Locale.US, "%.1f V", voltageMilliVolts / 1000f)
}


internal fun formatBatteryCurrent(currentMa: Float?): String =
    currentMa?.let { String.format(java.util.Locale.US, "%.2f mA", it) } ?: "-- mA"


internal fun formatCompactBytes(bytes: Long): String {
    if (bytes <= 0) return "0B"
    val gb = bytes / (1024f * 1024f * 1024f)
    val mb = bytes / (1024f * 1024f)
    return if (gb >= 1f) {
        "${(gb * 10f).roundToInt() / 10f}GB"
    } else {
        "${mb.roundToInt()}MB"
    }
}


internal fun filterSamplesForRange(
    samples: List<BatteryCurrentSample>,
    currentNow: Float?,
    selectedRange: BatteryCurrentRange,
    timestampMillis: Long = System.currentTimeMillis()
): List<BatteryCurrentSample> {
    val cutoff = timestampMillis - selectedRange.durationMillis
    return samples.filter { it.timestampMillis >= cutoff }
}


internal fun averageCurrent(samples: List<BatteryCurrentSample>, fallback: Float?): Float? =
    samples.takeIf { it.isNotEmpty() }
        ?.map { it.currentMa }
        ?.average()
        ?.toFloat()
        ?: fallback

internal fun filterTemperatureSamples(
    samples: List<BatteryTemperatureSample>,
    currentTemperatureC: Float?,
    timestampMillis: Long = System.currentTimeMillis()
): List<BatteryTemperatureSample> {
    val cutoff = timestampMillis - TEMPERATURE_WINDOW_MILLIS
    return samples.filter { it.timestampMillis >= cutoff }
}


internal fun averageTemperature(samples: List<BatteryTemperatureSample>, fallback: Float? = null): Float? =
    samples.takeIf { it.isNotEmpty() }
        ?.map { it.temperatureC }
        ?.average()
        ?.toFloat()
        ?: fallback

private fun appendCurrentSample(
    samples: List<BatteryCurrentSample>,
    currentNow: Float?,
    timestampMillis: Long,
    maxSampleCount: Int
): List<BatteryCurrentSample> {
    val retained = samples
        .filter { it.timestampMillis >= timestampMillis - BatteryCurrentRange.TwentyFourHours.durationMillis }
        .filterNot { it.timestampMillis == timestampMillis }
    val appended = currentNow?.let { retained + BatteryCurrentSample(timestampMillis, it) } ?: retained
    return appended.takeLast(maxSampleCount)
}

private fun appendTemperatureSample(
    samples: List<BatteryTemperatureSample>,
    temperatureC: Float?,
    timestampMillis: Long
): List<BatteryTemperatureSample> {
    val retained = samples
        .filter { it.timestampMillis >= timestampMillis - TEMPERATURE_WINDOW_MILLIS }
        .filterNot { it.timestampMillis == timestampMillis }
    return temperatureC?.let { retained + BatteryTemperatureSample(timestampMillis, it) } ?: retained
}

private const val TEMPERATURE_WINDOW_MILLIS = 60_000L
private const val BATTERY_HISTORY_FUTURE_TOLERANCE_MILLIS = 60_000L
private const val BATTERY_HISTORY_FRESH_SAMPLE_MILLIS =
    BATTERY_HISTORY_SAMPLE_INTERVAL_MILLIS + 5_000L

