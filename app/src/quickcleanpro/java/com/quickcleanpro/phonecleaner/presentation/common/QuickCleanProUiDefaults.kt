package com.quickcleanpro.phonecleaner.presentation.common

import android.content.Context
import com.quickcleanpro.phonecleaner.domain.repository.BatteryHistoryRepository
import com.quickcleanpro.phonecleaner.domain.repository.DeviceInfoRepository
import com.quickcleanpro.phonecleaner.domain.repository.SettingsRepository
import com.quickcleanpro.phonecleaner.domain.repository.ToolboxRepository
import com.quickcleanpro.phonecleaner.domain.state.SharedScanState
import com.quickcleanpro.phonecleaner.domain.usecase.MemoryCleanUseCase
import com.quickcleanpro.phonecleaner.domain.usecase.ScanJunkUseCase
import com.quickcleanpro.phonecleaner.data.source.battery.BatteryHistorySampler
import com.quickcleanpro.phonecleaner.presentation.screen.toolbox.NetworkInfo
import com.quickcleanpro.phonecleaner.presentation.screen.toolbox.readNetworkInfo
import org.koin.core.context.GlobalContext

internal fun deviceInfoRepositoryOrPreview(): DeviceInfoRepository = koinGet()

internal fun batteryHistoryRepositoryOrPreview(): BatteryHistoryRepository = koinGet()

internal fun batteryHistorySamplerOrPreview(): BatteryHistorySampler? =
    runCatching { koinGet<BatteryHistorySampler>() }.getOrNull()

internal fun settingsRepositoryOrPreview(): SettingsRepository = koinGet()

internal fun toolboxRepositoryOrPreview(): ToolboxRepository = koinGet()

internal fun sharedScanStateOrPreview(): SharedScanState = koinGet()

internal fun scanJunkUseCaseOrPreview(): ScanJunkUseCase = koinGet()

internal fun memoryCleanUseCaseOrPreview(): MemoryCleanUseCase = koinGet()

internal fun appContextOrPreview(): Context = GlobalContext.get().get()

internal fun appString(resId: Int, vararg args: Any): String =
    appContextOrPreview().getString(resId, *args)

internal fun appQuantityString(resId: Int, quantity: Int, vararg args: Any): String =
    appContextOrPreview().resources.getQuantityString(resId, quantity, *args)

internal fun networkInfoReaderOrPreview(): () -> NetworkInfo = {
    readNetworkInfo(appContextOrPreview())
}

private inline fun <reified T> koinGet(): T = GlobalContext.get().get()
