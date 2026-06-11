package com.clean.cleanx.domain.model

/**
 * 电池充放电状态快照。
 *
 * Repository 将系统广播中的状态码转换成页面可直接使用的文本和充电标记，
 * 避免页面层理解 Android 电池状态常量。
 */
data class BatteryStatusInfo(
    val statusText: String,
    val isCharging: Boolean
)

/**
 * 设备硬件与屏幕信息快照。
 */
data class DeviceHardwareInfo(
    val model: String,
    val androidVersion: String,
    val screenSize: String,
    val screenDensity: String,
    val multiTouchSupported: Boolean,
    val sensors: DeviceSensorInfo,
    val cpu: DeviceCpuInfo
)

/**
 * 设备传感器支持情况。
 *
 * 每个字段对应页面中展示的一项传感器能力，使用布尔值便于 ViewModel 统一映射文案。
 */
data class DeviceSensorInfo(
    val accelerometer: Boolean,
    val magneticField: Boolean,
    val orientation: Boolean,
    val gyroscope: Boolean,
    val light: Boolean,
    val proximity: Boolean,
    val ambientTemperature: Boolean
)

/**
 * CPU 基础信息。
 *
 * 频率读取可能因设备权限或系统裁剪失败，失败时由 data 层提供 Unknown 文案。
 */
data class DeviceCpuInfo(
    val hardware: String,
    val model: String,
    val cores: Int,
    val maxFrequency: String
)
