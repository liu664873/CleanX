package com.clean.cleanx.data.source.system.device

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.BatteryManager
import android.os.Build
import android.util.DisplayMetrics
import android.view.WindowManager
import com.clean.cleanx.domain.model.BatteryInfo
import com.clean.cleanx.domain.model.BatteryStatusInfo
import com.clean.cleanx.domain.model.DeviceCpuInfo
import com.clean.cleanx.domain.model.DeviceSensorInfo
import com.clean.cleanx.domain.model.MemoryInfo
import java.lang.reflect.Method

class DeviceInfoDataSource(private val context: Context) {

    // ---------- 电池相关 ----------
    fun getBatteryInfo(): BatteryInfo {
        val intent = getBatteryStatusIntent()
            ?: return BatteryInfo(-1, "Unknown", 0f, -1, "Unknown", 0, "Unknown")

        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 1)
        val levelPercent = if (level >= 0 && scale > 0) {
            (level * 100 / scale.toFloat()).toInt().coerceIn(0, 100)
        } else {
            -1
        }

        val health = when (intent.getIntExtra(BatteryManager.EXTRA_HEALTH, -1)) {
            BatteryManager.BATTERY_HEALTH_COLD -> "Cold"
            BatteryManager.BATTERY_HEALTH_DEAD -> "Dead"
            BatteryManager.BATTERY_HEALTH_GOOD -> "Good"
            BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Overheat"
            BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "Overvoltage"
            BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> "Failure"
            else -> "Unknown"
        }

        val tempRaw = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1)
        val temperature = if (tempRaw >= 0) tempRaw / 10f else 0f
        val voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1)
        val technology = intent.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY) ?: "Unknown"
        val capacity = getBatteryCapacity()

        return BatteryInfo(
            levelPercent = levelPercent,
            health = health,
            temperature = temperature,
            voltage = voltage,
            technology = technology,
            capacity = capacity,
            availableTime = estimateAvailableTime(levelPercent)
        )
    }

    fun getBatteryStatusInfo(): BatteryStatusInfo {
        val intent = getBatteryStatusIntent() ?: return BatteryStatusInfo("Unknown", false)
        val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL
        val statusText = when (status) {
            BatteryManager.BATTERY_STATUS_CHARGING -> "Charging"
            BatteryManager.BATTERY_STATUS_DISCHARGING -> "Discharging"
            BatteryManager.BATTERY_STATUS_FULL -> "Full"
            BatteryManager.BATTERY_STATUS_NOT_CHARGING -> "Not charging"
            else -> "Unknown"
        }
        return BatteryStatusInfo(statusText, isCharging)
    }

    fun getMemoryInfo(): MemoryInfo {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memInfo)
        val avail = memInfo.availMem

        val total = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            memInfo.totalMem
        } else {
            (avail * 3).coerceAtMost(1024L * 1024 * 1024)
        }
        val isTotalValid = Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN
        val used = if (total > 0) total - avail else 0L
        val percent = if (total > 0) ((used.toFloat() / total) * 100).toInt() else 0
        return MemoryInfo(total, avail, used, percent, isTotalValid)
    }

    fun getBatteryCurrentNowMa(): Float? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) return null
        return try {
            val bm = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
            val current = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)
            if (current == Integer.MIN_VALUE) null else current.toFloat() / 1000f
        } catch (e: Exception) {
            null
        }
    }

    fun getBatteryCurrentAverageMa(): Float? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) return null
        return try {
            val bm = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
            val current = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_AVERAGE)
            if (current == Integer.MIN_VALUE) null else current.toFloat() / 1000f
        } catch (e: Exception) {
            null
        }
    }

    // ---------- 硬件信息 ----------
    fun getDeviceModel(): String = "${Build.MANUFACTURER} ${Build.MODEL}".trim()
    fun getAndroidVersion(): String = Build.VERSION.RELEASE
    fun getScreenSize(): String {
        val metrics = DisplayMetrics()
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        wm.defaultDisplay.getMetrics(metrics)
        val width = metrics.widthPixels / metrics.density
        val height = metrics.heightPixels / metrics.density
        return String.format("%.1f\"", kotlin.math.sqrt(width * width + height * height))
    }
    fun getScreenDensity(): String {
        val metrics = DisplayMetrics()
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        wm.defaultDisplay.getMetrics(metrics)
        return "${metrics.densityDpi} dpi"
    }

    fun isMultiTouchSupported(): Boolean = context.packageManager.hasSystemFeature(PackageManager.FEATURE_TOUCHSCREEN_MULTITOUCH)

    fun getSensorInfo(): DeviceSensorInfo = DeviceSensorInfo(
        accelerometer = context.packageManager.hasSystemFeature(PackageManager.FEATURE_SENSOR_ACCELEROMETER),
        magneticField = context.packageManager.hasSystemFeature(PackageManager.FEATURE_SENSOR_COMPASS),
        orientation = context.packageManager.hasSystemFeature(PackageManager.FEATURE_SENSOR_COMPASS),
        gyroscope = context.packageManager.hasSystemFeature(PackageManager.FEATURE_SENSOR_GYROSCOPE),
        light = context.packageManager.hasSystemFeature(PackageManager.FEATURE_SENSOR_LIGHT),
        proximity = context.packageManager.hasSystemFeature(PackageManager.FEATURE_SENSOR_PROXIMITY),
        ambientTemperature = context.packageManager.hasSystemFeature(PackageManager.FEATURE_SENSOR_AMBIENT_TEMPERATURE)
    )

    fun getCpuInfo(): DeviceCpuInfo {
        val hardware = Build.HARDWARE ?: "Unknown"
        val model = Build.BOARD ?: "Unknown"
        val cores = Runtime.getRuntime().availableProcessors()
        val maxFreq = readCpuMaxFreq()
        return DeviceCpuInfo(hardware, model, cores, maxFreq)
    }

    private fun readCpuMaxFreq(): String {
        val path = "/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_max_freq"
        return try {
            val content = java.io.File(path).readText().trim()
            val freqKhz = content.toLongOrNull() ?: 0L
            if (freqKhz > 0) "${freqKhz / 1000} MHz" else "Unknown"
        } catch (e: Exception) {
            "Unknown"
        }
    }

    // ---------- 私有辅助 ----------
    private fun getBatteryStatusIntent(): Intent? =
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED), Context.RECEIVER_NOT_EXPORTED)
            } else {
                context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            }
        } catch (_: Exception) { null }

    private fun getBatteryCapacity(): Int {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) return 0
        getBatteryCapacityByReflection()?.let { return it }
        return try {
            val bm = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
            val chargeCounter = bm.getLongProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER)
            val capacityPercent = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
            if (chargeCounter == Long.MIN_VALUE || capacityPercent <= 0 || capacityPercent > 100) return 0
            val totalMicroAh = chargeCounter * 100 / capacityPercent
            (totalMicroAh / 1000).toInt()
        } catch (_: Exception) { 0 }
    }

    private fun getBatteryCapacityByReflection(): Int? {
        return try {
            val powerProfileClass = Class.forName("com.android.internal.os.PowerProfile")
            val constructor = powerProfileClass.getConstructor(Context::class.java)
            val instance = constructor.newInstance(context)
            val method = getBatteryCapacityMethod(powerProfileClass)
            val capacity = if (method.parameterTypes.isEmpty()) {
                method.invoke(instance) as Double
            } else {
                method.invoke(instance, "battery.capacity") as Double
            }
            capacity.takeIf { it > 0.0 }?.toInt()
        } catch (_: Exception) { null }
    }

    private fun getBatteryCapacityMethod(powerProfileClass: Class<*>): Method {
        return try {
            powerProfileClass.getMethod("getBatteryCapacity")
        } catch (_: NoSuchMethodException) {
            powerProfileClass.getMethod("getAveragePower", String::class.java)
        }
    }

    private fun estimateAvailableTime(levelPercent: Int): String {
        val validLevel = levelPercent.coerceIn(0, 100)
        if (validLevel <= 0) return "0m"
        val totalMinutes = ((validLevel / ASSUMED_HOURLY_DRAIN) * 60f).toInt().coerceAtLeast(0)
        val hours = totalMinutes / 60
        val mins = totalMinutes % 60
        return if (hours > 0) "${hours}h${mins}m" else "${mins}m"
    }

    companion object {
        private const val ASSUMED_HOURLY_DRAIN = 18.5f
    }
}