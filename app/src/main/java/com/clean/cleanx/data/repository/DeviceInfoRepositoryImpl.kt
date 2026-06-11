package com.clean.cleanx.data.repository

import com.clean.cleanx.data.source.system.device.DeviceInfoDataSource
import com.clean.cleanx.data.source.system.device.StorageDataSource
import com.clean.cleanx.domain.model.BatteryInfo
import com.clean.cleanx.domain.model.BatteryStatusInfo
import com.clean.cleanx.domain.model.DeviceHardwareInfo
import com.clean.cleanx.domain.model.MemoryInfo
import com.clean.cleanx.domain.model.StorageInfo
import com.clean.cleanx.domain.repository.DeviceInfoRepository


class DeviceInfoRepositoryImpl(
    private val deviceDataSource: DeviceInfoDataSource,
    private val storageDataSource: StorageDataSource
) : DeviceInfoRepository {

    override fun batteryInfo(): BatteryInfo = deviceDataSource.getBatteryInfo()
    override fun batteryStatusInfo(): BatteryStatusInfo = deviceDataSource.getBatteryStatusInfo()
    override fun memoryInfo(): MemoryInfo = deviceDataSource.getMemoryInfo()
    override fun internalStorageInfo(): StorageInfo = storageDataSource.getInternalStorageInfo()

    override fun hardwareInfo(): DeviceHardwareInfo = DeviceHardwareInfo(
        model = deviceDataSource.getDeviceModel(),
        androidVersion = deviceDataSource.getAndroidVersion(),
        screenSize = deviceDataSource.getScreenSize(),
        screenDensity = deviceDataSource.getScreenDensity(),
        multiTouchSupported = deviceDataSource.isMultiTouchSupported(),
        sensors = deviceDataSource.getSensorInfo(),
        cpu = deviceDataSource.getCpuInfo()
    )

    override fun batteryCurrentNowMa(): Float? = deviceDataSource.getBatteryCurrentNowMa()
    override fun batteryCurrentAverageMa(): Float? = deviceDataSource.getBatteryCurrentAverageMa()
}