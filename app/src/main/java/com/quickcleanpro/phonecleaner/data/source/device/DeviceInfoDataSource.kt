package com.quickcleanpro.phonecleaner.data.source.device

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import java.lang.reflect.Method
import com.quickcleanpro.phonecleaner.domain.model.device.BatteryInfo
import com.quickcleanpro.phonecleaner.domain.model.device.MemoryInfo


object DeviceInfoDataSource {

    fun getBatteryInfo(context: Context): BatteryInfo {
        val intent = getBatteryStatusIntent(context)
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
        val capacity = getBatteryCapacity(context)

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

    fun getMemoryInfo(context: Context): MemoryInfo {
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

    private fun getBatteryStatusIntent(context: Context): Intent? =
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                context.registerReceiver(
                    null,
                    IntentFilter(Intent.ACTION_BATTERY_CHANGED),
                    Context.RECEIVER_NOT_EXPORTED
                )
            } else {
                context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            }
        } catch (_: Exception) {
            null
        }

    private fun getBatteryCapacity(context: Context): Int {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) return 0
        getBatteryCapacityByReflection(context)?.let { return it }
        return try {
            val bm = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
            val chargeCounter = bm.getLongProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER)
            val capacityPercent = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
            if (chargeCounter == Long.MIN_VALUE || capacityPercent <= 0 || capacityPercent > 100) return 0
            val totalMicroAh = chargeCounter * 100 / capacityPercent
            (totalMicroAh / 1000).toInt()
        } catch (_: Exception) {
            0
        }
    }

    private fun getBatteryCapacityByReflection(context: Context): Int? {
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
        } catch (_: Exception) {
            null
        }
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

    private const val ASSUMED_HOURLY_DRAIN = 18.5f
}
