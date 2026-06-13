package com.quickcleanpro.phonecleaner.domain.repository

import com.quickcleanpro.phonecleaner.domain.model.BatteryStatusInfo
import com.quickcleanpro.phonecleaner.domain.model.DeviceHardwareInfo
import com.quickcleanpro.phonecleaner.domain.model.device.BatteryInfo
import com.quickcleanpro.phonecleaner.domain.model.device.MemoryInfo
import com.quickcleanpro.phonecleaner.domain.model.device.StorageInfo

/**
 * 设备信息仓库接口。
 *
 * 表现层（Presenter / ViewModel）通过此接口获取设备、电池、内存和存储等相关信息，
 * 避免直接依赖 Android 系统服务或底层工具类，遵循依赖倒置原则，
 * 便于单元测试、预览以及将来替换实现。
 */
interface DeviceInfoRepository {
    /**
     * 获取当前电池的基础信息。
     *
     * @return [BatteryInfo] 包含电量百分比、健康度、温度、电压、技术类型、容量、预估剩余时间等。
     */
    fun batteryInfo(): BatteryInfo

    /**
     * 获取当前电池的充放电状态。
     *
     * @return [BatteryStatusInfo] 包含可读状态文本（如 "Charging"）以及是否正在充电的布尔值。
     */
    fun batteryStatusInfo(): BatteryStatusInfo

    /**
     * 获取当前内存（RAM）的使用情况。
     *
     * @return [MemoryInfo] 包含总内存、可用内存、已用内存、使用百分比以及总内存是否有效等。
     */
    fun memoryInfo(): MemoryInfo

    /**
     * 获取当前内部存储的使用情况。
     *
     * @return [StorageInfo] 包含总容量、已用容量、可用容量。
     */
    fun internalStorageInfo(): StorageInfo

    /**
     * 获取设备的硬件综合信息。
     *
     * 包括：设备型号、Android 版本、屏幕尺寸与密度、多点触控支持、传感器支持情况、CPU 信息（硬件、型号、核心数、最大频率）。
     *
     * @return [DeviceHardwareInfo] 硬件信息聚合对象。
     */
    fun hardwareInfo(): DeviceHardwareInfo

    /**
     * 获取当前瞬时电流，单位为毫安（mA）。
     *
     * 需要 Android 5.0（API 21）及以上，且设备硬件支持电流监测。
     * 若系统不支持或获取失败，则返回 `null`。
     *
     * @return 瞬时电流值（毫安），可能为 `null`。
     */
    fun batteryCurrentNowMa(): Float?

    /**
     * 获取系统提供的平均电流，单位为毫安（mA）。
     *
     * 同样需要 API 21 及以上且硬件支持，代表一段时间内的平均耗电情况。
     * 若系统不支持或获取失败，则返回 `null`。
     *
     * @return 平均电流值（毫安），可能为 `null`。
     */
    fun batteryCurrentAverageMa(): Float?
}
