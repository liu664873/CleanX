package com.quickcleanpro.phonecleaner.data.repository

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.BatteryManager
import android.os.Build
import com.quickcleanpro.phonecleaner.domain.model.BatteryStatusInfo
import com.quickcleanpro.phonecleaner.domain.model.DeviceCpuInfo
import com.quickcleanpro.phonecleaner.domain.model.DeviceHardwareInfo
import com.quickcleanpro.phonecleaner.domain.model.DeviceSensorInfo
import com.quickcleanpro.phonecleaner.domain.repository.DeviceInfoRepository
import com.quickcleanpro.phonecleaner.domain.model.device.BatteryInfo
import com.quickcleanpro.phonecleaner.data.source.device.DeviceInfoDataSource
import com.quickcleanpro.phonecleaner.domain.model.device.MemoryInfo
import com.quickcleanpro.phonecleaner.domain.model.device.StorageInfo
import com.quickcleanpro.phonecleaner.data.source.device.StorageDataSource
import java.io.File
import java.util.Locale

/**
 * 真实的设备信息仓库实现。
 *
 * 该类负责从 Android 系统服务、系统文件、传感器等获取真实的硬件信息，
 * 并实现 [DeviceInfoRepository] 接口。统一使用 Application Context，
 * 避免持有 Activity 引用导致内存泄漏。
 */
class DeviceInfoRepositoryImpl(context: Context) : DeviceInfoRepository {

    private val appContext = context.applicationContext

    /**
     * 获取当前电池基本信息（电量、健康度、温度、电压、技术、容量、续航时间）。
     */
    override fun batteryInfo(): BatteryInfo =
        DeviceInfoDataSource.getBatteryInfo(appContext)

    /**
     * 获取电池充放电状态（充电中、放电中、已满等）。
     */
    override fun batteryStatusInfo(): BatteryStatusInfo {
        val status = readBatteryStatus()
        return BatteryStatusInfo(
            statusText = status.toBatteryStatusText(),
            isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING
        )
    }

    /**
     * 获取内存使用情况（总内存、可用内存、使用百分比等）。
     */
    override fun memoryInfo(): MemoryInfo =
        DeviceInfoDataSource.getMemoryInfo(appContext)

    /**
     * 获取内部存储使用情况（总容量、已用容量、可用容量）。
     */
    override fun internalStorageInfo(): StorageInfo =
        StorageDataSource.getInternalStorageInfo()

    /**
     * 获取设备硬件综合信息：设备型号、Android 版本、屏幕尺寸/密度、多点触控支持、
     * 传感器支持情况、CPU 硬件信息（型号、核心数、最大频率）。
     */
    override fun hardwareInfo(): DeviceHardwareInfo {
        val metrics = appContext.resources.displayMetrics
        val packageManager = appContext.packageManager
        return DeviceHardwareInfo(
            model = Build.MODEL.takeIf { it.isNotBlank() } ?: UNKNOWN,
            androidVersion = "Android ${Build.VERSION.RELEASE}",
            screenSize = "${metrics.widthPixels}x${metrics.heightPixels}",
            screenDensity = "${metrics.densityDpi} DPI",
            multiTouchSupported = packageManager.hasSystemFeature(PackageManager.FEATURE_TOUCHSCREEN_MULTITOUCH),
            sensors = readSensorInfo(),
            cpu = DeviceCpuInfo(
                hardware = Build.HARDWARE.takeIf { it.isNotBlank() } ?: UNKNOWN,
                model = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && Build.SOC_MODEL.isNotBlank()) {
                    Build.SOC_MODEL
                } else {
                    UNKNOWN
                },
                cores = Runtime.getRuntime().availableProcessors().coerceAtLeast(1),
                maxFrequency = readCpuMaxFrequency()
            )
        )
    }

    /**
     * 获取当前瞬时电流（毫安）。系统不支持或 API 低于 21 时返回 null。
     */
    override fun batteryCurrentNowMa(): Float? =
        readBatteryCurrentMa(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)

    /**
     * 获取系统提供的平均电流（毫安）。系统不支持或 API 低于 21 时返回 null。
     */
    override fun batteryCurrentAverageMa(): Float? =
        readBatteryCurrentMa(BatteryManager.BATTERY_PROPERTY_CURRENT_AVERAGE)

    /**
     * 读取电池状态（通过注册空广播接收器获取最后一次粘性广播）。
     *
     * 注意：Android 14+ 要求显式指定 RECEIVER_NOT_EXPORTED 标志。
     */
    private fun readBatteryStatus(): Int? =
        try {
            val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                appContext.registerReceiver(
                    null,
                    IntentFilter(Intent.ACTION_BATTERY_CHANGED),
                    Context.RECEIVER_NOT_EXPORTED
                )
            } else {
                appContext.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            }
            intent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        } catch (_: Exception) {
            null
        }

    /**
     * 读取设备支持的传感器类型。
     *
     * 通过 SensorManager.getDefaultSensor 判断每种传感器是否存在默认传感器。
     */
    private fun readSensorInfo(): DeviceSensorInfo {
        val sensorManager = appContext.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        fun supported(type: Int): Boolean = sensorManager?.getDefaultSensor(type) != null
        return DeviceSensorInfo(
            accelerometer = supported(Sensor.TYPE_ACCELEROMETER),
            magneticField = supported(Sensor.TYPE_MAGNETIC_FIELD),
            orientation = supported(Sensor.TYPE_ORIENTATION),
            gyroscope = supported(Sensor.TYPE_GYROSCOPE),
            light = supported(Sensor.TYPE_LIGHT),
            proximity = supported(Sensor.TYPE_PROXIMITY),
            ambientTemperature = supported(Sensor.TYPE_AMBIENT_TEMPERATURE)
        )
    }

    /**
     * 读取指定电池电流属性（微安）并转换为毫安。
     *
     * @param property BatteryManager.BATTERY_PROPERTY_CURRENT_NOW 或 AVERAGE
     * @return 电流值（毫安），设备不支持或出错时返回 null
     */
    private fun readBatteryCurrentMa(property: Int): Float? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) return null
        return try {
            val batteryManager = appContext.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
            val microAmps = batteryManager.getIntProperty(property)
            if (microAmps == Int.MIN_VALUE) null else microAmps / 1000f
        } catch (_: Exception) {
            null
        }
    }

    /**
     * 从 sysfs 文件系统中读取 CPU 最大频率。
     *
     * 尝试常见路径（如 /sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_max_freq）。
     * 如果读取失败或文件不存在，返回 "Unknown"。
     */
    private fun readCpuMaxFrequency(): String {
        return try {
            CPU_FREQUENCY_PATHS.forEach { path ->
                val freq = File(path).readText().trim().toLongOrNull()
                if (freq != null && freq > 0) {
                    val ghz = freq / 1_000_000f
                    return if (ghz >= 1f) {
                        String.format(Locale.US, "%.1f GHz", ghz)
                    } else {
                        "${freq / 1000} MHz"
                    }
                }
            }
            UNKNOWN
        } catch (_: Exception) {
            UNKNOWN
        }
    }

    private companion object {
        private const val UNKNOWN = "Unknown"
        // CPU 最大频率的可能 sysfs 路径
        private val CPU_FREQUENCY_PATHS = listOf(
            "/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_max_freq",
            "/sys/devices/system/cpu/cpu0/cpufreq/scaling_max_freq"
        )
    }
}

/**
 * 将 Android 电池状态码转换为可读文本。
 */
private fun Int?.toBatteryStatusText(): String =
    when (this) {
        BatteryManager.BATTERY_STATUS_CHARGING -> "Charging"
        BatteryManager.BATTERY_STATUS_DISCHARGING -> "Discharging"
        BatteryManager.BATTERY_STATUS_FULL -> "Full"
        BatteryManager.BATTERY_STATUS_NOT_CHARGING -> "Not Charging"
        else -> "Unknown"
    }