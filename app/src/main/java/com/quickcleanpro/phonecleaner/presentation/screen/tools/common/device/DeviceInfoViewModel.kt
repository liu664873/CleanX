package com.quickcleanpro.phonecleaner.presentation.screen.tools.common.device

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quickcleanpro.phonecleaner.R
import com.quickcleanpro.phonecleaner.data.repository.BatteryHistoryRepositoryImpl.Companion.BATTERY_HISTORY_SAMPLE_INTERVAL_MILLIS
import com.quickcleanpro.phonecleaner.data.repository.BatteryHistoryRepositoryImpl.Companion.MAX_BATTERY_HISTORY_SAMPLES
import com.quickcleanpro.phonecleaner.data.source.battery.BatteryHistoryOwner
import com.quickcleanpro.phonecleaner.data.source.battery.BatteryHistorySampler
import com.quickcleanpro.phonecleaner.domain.model.BatteryHistorySample
import com.quickcleanpro.phonecleaner.domain.model.BatteryStatusInfo
import com.quickcleanpro.phonecleaner.domain.model.DeviceCpuInfo
import com.quickcleanpro.phonecleaner.domain.model.DeviceHardwareInfo
import com.quickcleanpro.phonecleaner.domain.model.DeviceSensorInfo
import com.quickcleanpro.phonecleaner.domain.model.device.BatteryInfo
import com.quickcleanpro.phonecleaner.domain.model.device.MemoryInfo
import com.quickcleanpro.phonecleaner.domain.model.device.StorageInfo
import com.quickcleanpro.phonecleaner.domain.repository.BatteryHistoryRepository
import com.quickcleanpro.phonecleaner.domain.repository.DeviceInfoRepository
import com.quickcleanpro.phonecleaner.domain.repository.SettingsRepository
import com.quickcleanpro.phonecleaner.utils.FileSizeFormatter
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

enum class DeviceInfoMode {
    Device,
    Battery,
}

enum class BatteryCurrentRange(
    @StringRes val labelRes: Int,
    val durationMillis: Long,
) {
    OneMinute(R.string.battery_range_1_min, 60_000L),
    OneHour(R.string.battery_range_1_hour, 60L * 60L * 1000L),
    TwentyFourHours(R.string.battery_range_24_hours, 24L * 60L * 60L * 1000L),
}

data class BatteryCurrentSample(
    val timestampMillis: Long,
    val currentMa: Float,
)

data class BatteryTemperatureSample(
    val timestampMillis: Long,
    val temperatureC: Float,
)

data class DeviceInfoUiState(
    val mode: DeviceInfoMode = DeviceInfoMode.Device,
    val battery: BatteryInfo = EMPTY_BATTERY_INFO,
    val batteryStatus: BatteryStatusInfo = EMPTY_BATTERY_STATUS,
    val memory: MemoryInfo = EMPTY_MEMORY_INFO,
    val storage: StorageInfo = EMPTY_STORAGE_INFO,
    val hardware: DeviceHardwareInfo = EMPTY_HARDWARE_INFO,
    val cpuUsagePercent: Int? = null,
    val cpuTemperatureC: Float? = null,
    val currentNow: Float? = null,
    val currentAverage: Float? = null,
    val selectedCurrentRange: BatteryCurrentRange = BatteryCurrentRange.OneMinute,
    val currentSamples: List<BatteryCurrentSample> = emptyList(),
    val temperatureSamples: List<BatteryTemperatureSample> = emptyList(),
    val latestSampleTimestampMillis: Long = 0L,
    val temperatureUnit: String = TEMPERATURE_UNIT_CELSIUS,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
) {
    val batteryCapacityText: String
        get() = battery.levelPercent.takeIf { it >= 0 }?.let { "$it%" } ?: "--"

    val batteryLifeText: String
        get() = battery.availableTime.takeIf { it.isNotBlank() && it != UNKNOWN } ?: "--"

    val selectedCurrentSamples: List<BatteryCurrentSample>
        get() =
            filterCurrentSamples(
                samples = currentSamples,
                selectedRange = selectedCurrentRange,
                timestampMillis = latestSampleTimestampMillis.takeIf { it > 0L } ?: System.currentTimeMillis(),
            )

    val selectedCurrentAverage: Float?
        get() = averageCurrent(selectedCurrentSamples, currentAverage)

    val selectedTemperatureSamples: List<BatteryTemperatureSample>
        get() =
            filterTemperatureSamples(
                samples = temperatureSamples,
                timestampMillis = latestSampleTimestampMillis.takeIf { it > 0L } ?: System.currentTimeMillis(),
            )

    val selectedTemperatureAverageC: Float?
        get() = averageTemperature(selectedTemperatureSamples, battery.temperature)

    val displayCurrentNow: Float?
        get() = selectedCurrentSamples.lastOrNull()?.currentMa ?: currentNow

    val displayTemperatureC: Float
        get() = selectedTemperatureSamples.lastOrNull()?.temperatureC ?: battery.temperature
}

open class DeviceInfoViewModel(
    private val repository: DeviceInfoRepository,
    private val batteryHistoryRepository: BatteryHistoryRepository,
    private val batteryHistorySampler: BatteryHistorySampler,
    private val settingsRepository: SettingsRepository,
    private val initialMode: DeviceInfoMode = DeviceInfoMode.Device,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val nowMillis: () -> Long = System::currentTimeMillis,
    private val maxSampleCount: Int = MAX_BATTERY_HISTORY_SAMPLES,
) : ViewModel() {
    private val _uiState = MutableStateFlow(DeviceInfoUiState())
    val uiState: StateFlow<DeviceInfoUiState> = _uiState.asStateFlow()

    private var historyCollectionJob: Job? = null

    init {
        load(initialMode)
    }

    fun load(mode: DeviceInfoMode) {
        _uiState.update {
            it.copy(
                mode = mode,
                isLoading = true,
                errorMessage = null,
            )
        }
        if (mode == DeviceInfoMode.Battery) {
            startHistoryCollection()
        } else {
            stopBatterySampling()
        }
        viewModelScope.launch(ioDispatcher) {
            val timestamp = nowMillis()
            val previousState = _uiState.value
            runCatching {
                buildState(
                    mode = mode,
                    previousState = previousState,
                    timestampMillis = timestamp,
                )
            }.onSuccess { loaded ->
                _uiState.value = loaded
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = error.message,
                    )
                }
            }
        }
    }

    fun startBatterySampling() {
        startHistoryCollection()
        viewModelScope.launch(ioDispatcher) {
            val timestamp = nowMillis()
            applyHistorySamples(batteryHistoryRepository.loadRecent(timestamp), timestamp)
            val forceSample = shouldForceBatterySample(timestamp)
            sampleBatterySnapshot(forceSample = forceSample)
            batteryHistorySampler.start(BatteryHistoryOwner.BatteryPage)
        }
    }

    fun stopBatterySampling() {
        batteryHistorySampler.stop(BatteryHistoryOwner.BatteryPage)
        historyCollectionJob?.cancel()
        historyCollectionJob = null
    }

    fun selectCurrentRange(range: BatteryCurrentRange) {
        _uiState.update { it.copy(selectedCurrentRange = range) }
    }

    override fun onCleared() {
        stopBatterySampling()
        super.onCleared()
    }

    private suspend fun buildState(
        mode: DeviceInfoMode,
        previousState: DeviceInfoUiState,
        timestampMillis: Long,
    ): DeviceInfoUiState {
        val battery = repository.batteryInfo()
        val batteryStatus = repository.batteryStatusInfo()
        val memory = repository.memoryInfo()
        val storage = repository.internalStorageInfo()
        val hardware = repository.hardwareInfo()
        val cpuTemperatureC = repository.cpuTemperatureC()
        val currentNow = repository.batteryCurrentNowMa().normalizedBatteryCurrent()
        val currentAverage = repository.batteryCurrentAverageMa().normalizedBatteryCurrent()
        val historySamples =
            if (mode == DeviceInfoMode.Battery) {
                batteryHistoryRepository.loadRecent(timestampMillis)
            } else {
                batteryHistoryRepository.samples.value
            }

        return DeviceInfoUiState(
            mode = mode,
            battery = battery,
            batteryStatus = batteryStatus,
            memory = memory,
            storage = storage,
            hardware = hardware,
            cpuUsagePercent = readCpuUsagePercent(previousState.cpuUsagePercent),
            cpuTemperatureC = cpuTemperatureC ?: previousState.cpuTemperatureC,
            currentNow = currentNow,
            currentAverage = currentAverage,
            selectedCurrentRange = previousState.selectedCurrentRange,
            currentSamples = historySamples.toCurrentSamples(timestampMillis, maxSampleCount),
            temperatureSamples = historySamples.toTemperatureSamples(timestampMillis),
            latestSampleTimestampMillis = timestampMillis,
            temperatureUnit = readTemperatureUnit(),
            isLoading = false,
            errorMessage = null,
        ).withHistorySamples(historySamples, timestampMillis, maxSampleCount)
    }

    private fun sampleBatterySnapshot(forceSample: Boolean) {
        if (forceSample) {
            batteryHistorySampler.sampleOnce(force = true)
        }
        val timestamp = nowMillis()
        val battery = runCatching { repository.batteryInfo() }.getOrNull()
        val batteryStatus = runCatching { repository.batteryStatusInfo() }.getOrNull()
        val currentNow = runCatching { repository.batteryCurrentNowMa().normalizedBatteryCurrent() }.getOrNull()
        val currentAverage = runCatching { repository.batteryCurrentAverageMa().normalizedBatteryCurrent() }.getOrNull()
        val samples = batteryHistoryRepository.loadRecent(timestamp)

        _uiState.update { state ->
            state
                .copy(
                    battery = battery ?: state.battery,
                    batteryStatus = batteryStatus ?: state.batteryStatus,
                    currentNow = currentNow ?: state.currentNow,
                    currentAverage = currentAverage ?: state.currentAverage,
                    temperatureUnit = readTemperatureUnit(),
                    isLoading = false,
                ).withHistorySamples(samples, timestamp, maxSampleCount)
        }
    }

    private fun startHistoryCollection() {
        if (historyCollectionJob?.isActive == true) return
        historyCollectionJob =
            viewModelScope.launch(ioDispatcher) {
                batteryHistoryRepository.samples.collect { samples ->
                    applyHistorySamples(samples, nowMillis())
                }
            }
    }

    private fun applyHistorySamples(
        samples: List<BatteryHistorySample>,
        timestampMillis: Long,
    ) {
        _uiState.update { state ->
            state
                .copy(temperatureUnit = readTemperatureUnit())
                .withHistorySamples(samples, timestampMillis, maxSampleCount)
        }
    }

    private fun readTemperatureUnit(): String = settingsRepository.readTemperatureUnit().normalizeTemperatureUnit()

    private fun shouldForceBatterySample(timestampMillis: Long): Boolean {
        val latestSample =
            batteryHistoryRepository.samples.value
                .lastOrNull()
                ?: batteryHistoryRepository.loadRecent(timestampMillis).lastOrNull()
                ?: return true
        return timestampMillis - latestSample.timestampMillis !in 0L..BATTERY_HISTORY_FRESH_SAMPLE_MILLIS
    }
}

class BatteryInfoViewModel(
    repository: DeviceInfoRepository,
    batteryHistoryRepository: BatteryHistoryRepository,
    batteryHistorySampler: BatteryHistorySampler,
    settingsRepository: SettingsRepository,
    ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : DeviceInfoViewModel(
    repository = repository,
    batteryHistoryRepository = batteryHistoryRepository,
    batteryHistorySampler = batteryHistorySampler,
    settingsRepository = settingsRepository,
    initialMode = DeviceInfoMode.Battery,
    ioDispatcher = ioDispatcher,
)

internal fun formatBatteryTemperature(
    tempC: Float?,
    unit: String = TEMPERATURE_UNIT_CELSIUS,
    includeSpace: Boolean = true,
): String {
    val temperature = tempC?.takeIf { it.isFiniteValue() } ?: return "--"
    val normalizedUnit = unit.normalizeTemperatureUnit()
    val displayTemperature = if (normalizedUnit == TEMPERATURE_UNIT_FAHRENHEIT) {
        celsiusToFahrenheit(temperature)
    } else {
        temperature
    }
    val separator = if (includeSpace) " " else ""
    return String.format(Locale.US, "%.1f%s\u00B0%s", displayTemperature, separator, normalizedUnit)
}

internal fun displayBatteryTemperatureValue(
    tempC: Float,
    unit: String,
): Float =
    if (unit.normalizeTemperatureUnit() == TEMPERATURE_UNIT_FAHRENHEIT) {
        celsiusToFahrenheit(tempC)
    } else {
        tempC
    }

internal fun formatBatteryVoltage(voltageMilliVolts: Int): String =
    if (voltageMilliVolts > 0) {
        String.format(Locale.US, "%.1f V", voltageMilliVolts / 1000f)
    } else {
        "--"
    }

internal fun formatBatteryCurrent(currentMa: Float?): String =
    currentMa
        ?.takeIf { it.isFiniteValue() }
        ?.let { String.format(Locale.US, "%.2f mA", abs(it)) }
        ?: "--"

internal fun formatBatteryCapacityMah(capacityMah: Int?): String =
    capacityMah
        ?.takeIf { it > 0 }
        ?.let { "$it mAh" }
        ?: "--"

internal fun currentBatteryCapacityMah(battery: BatteryInfo): Int? {
    val totalCapacity = battery.capacity.takeIf { it > 0 } ?: return null
    val level = battery.levelPercent.takeIf { it >= 0 } ?: return null
    return (totalCapacity * level / 100f).roundToInt()
}

internal fun formatBytesOrPlaceholder(bytes: Long): String =
    if (bytes > 0L) {
        FileSizeFormatter.format(bytes)
    } else {
        "--"
    }

internal fun String.orPlaceholder(): String = takeIf { isNotBlank() && this != UNKNOWN } ?: "--"

private fun DeviceInfoUiState.withHistorySamples(
    samples: List<BatteryHistorySample>,
    timestampMillis: Long,
    maxSampleCount: Int,
): DeviceInfoUiState {
    val recentSamples = samples.recentBatteryHistory(timestampMillis)
    val latestSample = recentSamples.lastOrNull()
    val freshLatestSample =
        latestSample?.takeIf { sample ->
            timestampMillis - sample.timestampMillis in 0L..BATTERY_HISTORY_FRESH_SAMPLE_MILLIS
        }
    return copy(
        battery =
            freshLatestSample?.let { latest ->
                battery.copy(temperature = latest.temperatureC)
            } ?: battery,
        currentNow = freshLatestSample?.currentMa?.normalizedBatteryCurrent() ?: currentNow,
        currentSamples = recentSamples.toCurrentSamples(timestampMillis, maxSampleCount),
        temperatureSamples = recentSamples.toTemperatureSamples(timestampMillis),
        latestSampleTimestampMillis = latestSample?.timestampMillis ?: timestampMillis,
    )
}

private fun List<BatteryHistorySample>.toCurrentSamples(
    timestampMillis: Long,
    maxSampleCount: Int,
): List<BatteryCurrentSample> {
    val currentSamples =
        recentBatteryHistory(timestampMillis)
            .asSequence()
            .mapNotNull { sample ->
                sample.currentMa
                    ?.takeIf { it.isFiniteValue() }
                    ?.let { BatteryCurrentSample(sample.timestampMillis, abs(it)) }
            }.toList()
    return currentSamples.takeLast(maxSampleCount)
}

private fun List<BatteryHistorySample>.toTemperatureSamples(timestampMillis: Long): List<BatteryTemperatureSample> =
    recentBatteryHistory(timestampMillis)
        .asSequence()
        .filter { it.timestampMillis >= timestampMillis - TEMPERATURE_WINDOW_MILLIS }
        .filter { it.temperatureC.isFiniteValue() }
        .map { BatteryTemperatureSample(it.timestampMillis, it.temperatureC) }
        .toList()

private fun List<BatteryHistorySample>.recentBatteryHistory(timestampMillis: Long): List<BatteryHistorySample> {
    val cutoff = timestampMillis - BatteryCurrentRange.TwentyFourHours.durationMillis
    val latestAllowed = timestampMillis + BATTERY_HISTORY_FUTURE_TOLERANCE_MILLIS
    return asSequence()
        .filter { it.timestampMillis in cutoff..latestAllowed }
        .sortedBy { it.timestampMillis }
        .toList()
}

private fun filterCurrentSamples(
    samples: List<BatteryCurrentSample>,
    selectedRange: BatteryCurrentRange,
    timestampMillis: Long,
): List<BatteryCurrentSample> {
    val cutoff = timestampMillis - selectedRange.durationMillis
    return samples
        .asSequence()
        .filter { it.timestampMillis in cutoff..timestampMillis }
        .sortedBy { it.timestampMillis }
        .toList()
}

private fun filterTemperatureSamples(
    samples: List<BatteryTemperatureSample>,
    timestampMillis: Long,
): List<BatteryTemperatureSample> {
    val cutoff = timestampMillis - TEMPERATURE_WINDOW_MILLIS
    return samples
        .asSequence()
        .filter { it.timestampMillis in cutoff..timestampMillis }
        .sortedBy { it.timestampMillis }
        .toList()
}

private fun averageCurrent(
    samples: List<BatteryCurrentSample>,
    fallback: Float?,
): Float? =
    samples
        .takeIf { it.isNotEmpty() }
        ?.map { abs(it.currentMa) }
        ?.average()
        ?.toFloat()
        ?: fallback.normalizedBatteryCurrent()

private fun averageTemperature(
    samples: List<BatteryTemperatureSample>,
    fallback: Float?,
): Float? =
    samples
        .takeIf { it.isNotEmpty() }
        ?.map { it.temperatureC }
        ?.average()
        ?.toFloat()
        ?: fallback

private suspend fun readCpuUsagePercent(previousPercent: Int? = null): Int? {
    readCpuUsagePercentFromProcStat()?.let { return it }
    readCpuUsagePercentFromLoadAverage()?.let { return it }
    readCpuUsagePercentFromFrequencies()?.let { return it }
    return previousPercent?.coerceIn(0, 100)
}

private suspend fun readCpuUsagePercentFromProcStat(): Int? {
    val first = readCpuStat() ?: return null
    delay(CPU_USAGE_SAMPLE_DELAY_MILLIS)
    val second = readCpuStat() ?: return null
    val totalDiff = second.total - first.total
    val idleDiff = second.idle - first.idle
    if (totalDiff <= 0L || idleDiff < 0L) return null
    return (((totalDiff - idleDiff) * 100f) / totalDiff).toCpuUsagePercent()
}

private fun readCpuStat(): CpuStat? =
    runCatching {
        val parts =
            File("/proc/stat")
                .useLines { lines -> lines.firstOrNull().orEmpty() }
                .trim()
                .split(Regex("\\s+"))
        if (parts.size < 5 || parts.first() != "cpu") return@runCatching null
        val values = parts.drop(1).mapNotNull { it.toLongOrNull() }
        if (values.size < 4) return@runCatching null
        val idle = values[3] + values.getOrElse(4) { 0L }
        CpuStat(
            idle = idle,
            total = values.sum(),
        )
    }.getOrNull()

private fun readCpuUsagePercentFromLoadAverage(): Int? =
    runCatching {
        val loadAverage =
            File(CPU_LOAD_AVERAGE_PATH)
                .useLines { lines -> lines.firstOrNull().orEmpty() }
                .trim()
                .split(Regex("\\s+"))
                .firstOrNull()
                ?.toFloatOrNull()
        val coreCount = Runtime.getRuntime().availableProcessors().coerceAtLeast(1)
        loadAverage
            ?.takeIf { it.isFiniteValue() && it >= 0f }
            ?.let { ((it / coreCount) * 100f).toCpuUsagePercent() }
    }.getOrNull()

private fun readCpuUsagePercentFromFrequencies(): Int? =
    runCatching {
        val ratios =
            File(CPU_SYSTEM_PATH)
                .listFiles { file -> file.isDirectory && CPU_CORE_NAME_REGEX.matches(file.name) }
                .orEmpty()
                .mapNotNull { cpuDir ->
                    val current = readFirstCpuFrequency(cpuDir, CPU_CURRENT_FREQUENCY_FILES)
                    val max = readFirstCpuFrequency(cpuDir, CPU_MAX_FREQUENCY_FILES)
                    if (current > 0L && max > 0L) {
                        current.toFloat() / max
                    } else {
                        null
                    }
                }
        ratios
            .takeIf { it.isNotEmpty() }
            ?.average()
            ?.times(100.0)
            ?.toFloat()
            ?.toCpuUsagePercent()
    }.getOrNull()

private fun readFirstCpuFrequency(
    cpuDir: File,
    fileNames: List<String>,
): Long =
    fileNames
        .asSequence()
        .mapNotNull { fileName ->
            runCatching {
                File(cpuDir, "cpufreq/$fileName")
                    .readText()
                    .trim()
                    .toLongOrNull()
            }.getOrNull()
        }.firstOrNull()
        ?: 0L

private data class CpuStat(
    val idle: Long,
    val total: Long,
)

private fun Float.toCpuUsagePercent(): Int =
    roundToInt().coerceIn(MIN_REPORTED_CPU_USAGE_PERCENT, 100)

private fun Float.isFiniteValue(): Boolean = !isNaN() && !isInfinite()

private fun Float?.normalizedBatteryCurrent(): Float? = this?.takeIf { it.isFiniteValue() }?.let { abs(it) }

private fun String.normalizeTemperatureUnit(): String =
    if (equals(TEMPERATURE_UNIT_FAHRENHEIT, ignoreCase = true)) {
        TEMPERATURE_UNIT_FAHRENHEIT
    } else {
        TEMPERATURE_UNIT_CELSIUS
    }

private fun celsiusToFahrenheit(value: Float): Float = value * 9f / 5f + 32f

private const val UNKNOWN = "Unknown"
private const val TEMPERATURE_UNIT_CELSIUS = "C"
private const val TEMPERATURE_UNIT_FAHRENHEIT = "F"
private const val TEMPERATURE_WINDOW_MILLIS = 60_000L
private const val BATTERY_HISTORY_FUTURE_TOLERANCE_MILLIS = 60_000L
private const val BATTERY_HISTORY_FRESH_SAMPLE_MILLIS = BATTERY_HISTORY_SAMPLE_INTERVAL_MILLIS + 5_000L
private const val CPU_USAGE_SAMPLE_DELAY_MILLIS = 360L
private const val CPU_SYSTEM_PATH = "/sys/devices/system/cpu"
private const val CPU_LOAD_AVERAGE_PATH = "/proc/loadavg"
private const val MIN_REPORTED_CPU_USAGE_PERCENT = 1

private val CPU_CORE_NAME_REGEX = Regex("cpu\\d+")
private val CPU_CURRENT_FREQUENCY_FILES = listOf("scaling_cur_freq", "cpuinfo_cur_freq")
private val CPU_MAX_FREQUENCY_FILES = listOf("cpuinfo_max_freq", "scaling_max_freq")

private val EMPTY_BATTERY_INFO = BatteryInfo(-1, UNKNOWN, 0f, -1, UNKNOWN, 0, UNKNOWN)
private val EMPTY_BATTERY_STATUS = BatteryStatusInfo(statusText = UNKNOWN, isCharging = false)
private val EMPTY_MEMORY_INFO = MemoryInfo(0L, 0L, 0L, 0, false)
private val EMPTY_STORAGE_INFO = StorageInfo(0L, 0L, 0L)
private val EMPTY_HARDWARE_INFO =
    DeviceHardwareInfo(
        model = UNKNOWN,
        androidVersion = "Android --",
        screenSize = "--",
        screenDensity = "--",
        multiTouchSupported = false,
        sensors =
            DeviceSensorInfo(
                accelerometer = false,
                magneticField = false,
                orientation = false,
                gyroscope = false,
                light = false,
                proximity = false,
                ambientTemperature = false,
            ),
        cpu =
            DeviceCpuInfo(
                hardware = UNKNOWN,
                model = UNKNOWN,
                cores = 0,
                maxFrequency = UNKNOWN,
            ),
    )
