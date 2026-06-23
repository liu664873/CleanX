package com.quickcleanpro.phonecleaner.di

import com.quickcleanpro.phonecleaner.data.repository.AppLockRepositoryImpl
import com.quickcleanpro.phonecleaner.data.repository.AppUsageRepositoryImpl
import com.quickcleanpro.phonecleaner.data.repository.BatteryHistoryRepositoryImpl
import com.quickcleanpro.phonecleaner.data.repository.CleanRepositoryImpl
import com.quickcleanpro.phonecleaner.data.repository.DeviceInfoRepositoryImpl
import com.quickcleanpro.phonecleaner.data.repository.FileRepositoryImpl
import com.quickcleanpro.phonecleaner.data.repository.NetworkRepositoryImpl
import com.quickcleanpro.phonecleaner.data.repository.NotificationRepositoryImpl
import com.quickcleanpro.phonecleaner.data.repository.SettingsRepositoryImpl
import com.quickcleanpro.phonecleaner.data.repository.ToolboxRepositoryImpl
import com.quickcleanpro.phonecleaner.data.source.battery.BatteryHistorySampler
import com.quickcleanpro.phonecleaner.domain.repository.AppLockRepository
import com.quickcleanpro.phonecleaner.domain.repository.AppUsageRepository
import com.quickcleanpro.phonecleaner.domain.repository.BatteryHistoryRepository
import com.quickcleanpro.phonecleaner.domain.repository.CleanRepository
import com.quickcleanpro.phonecleaner.domain.repository.DeviceInfoRepository
import com.quickcleanpro.phonecleaner.domain.repository.FileRepository
import com.quickcleanpro.phonecleaner.domain.repository.NetworkRepository
import com.quickcleanpro.phonecleaner.domain.repository.NotificationRepository
import com.quickcleanpro.phonecleaner.domain.repository.SettingsRepository
import com.quickcleanpro.phonecleaner.domain.repository.ToolboxRepository
import com.quickcleanpro.phonecleaner.domain.state.SharedScanState
import com.quickcleanpro.phonecleaner.domain.usecase.CleanJunkUseCase
import com.quickcleanpro.phonecleaner.domain.usecase.MemoryCleanUseCase
import com.quickcleanpro.phonecleaner.domain.usecase.ScanJunkUseCase
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

val dataModule: Module =
    module {
        single<DeviceInfoRepository> { DeviceInfoRepositoryImpl(androidContext()) }
        single<AppLockRepository> { AppLockRepositoryImpl(androidContext()) }
        single<AppUsageRepository> { AppUsageRepositoryImpl(androidContext()) }
        single<NetworkRepository> { NetworkRepositoryImpl(androidContext()) }
        single<NotificationRepository> { NotificationRepositoryImpl(androidContext()) }
        single<FileRepository> { FileRepositoryImpl(androidContext()) }
        single { SharedScanState() }
        single<CleanRepository> { CleanRepositoryImpl(androidContext(), get()) }
        single { ScanJunkUseCase(get()) }
        single { CleanJunkUseCase(get()) }
        single { MemoryCleanUseCase(get()) }
        single<ToolboxRepository> { ToolboxRepositoryImpl(get(), get(), get()) }
        single<SettingsRepository> { SettingsRepositoryImpl(androidContext()) }
        single<BatteryHistoryRepository> { BatteryHistoryRepositoryImpl(androidContext()) }
        single {
            BatteryHistorySampler(
                deviceInfoRepository = get(),
                historyRepository = get(),
            )
        }
    }
