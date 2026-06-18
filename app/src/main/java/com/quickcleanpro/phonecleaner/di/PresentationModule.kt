package com.quickcleanpro.phonecleaner.di

import com.quickcleanpro.phonecleaner.presentation.screen.antivirus.VirusScanViewModel
import com.quickcleanpro.phonecleaner.presentation.screen.applock.AppLockViewModel
import com.quickcleanpro.phonecleaner.presentation.screen.cleanresult.CleanResultViewModel
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.AudiosManagerViewModel
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.DocumentsManagerViewModel
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.DuplicateFilesViewModel
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.LargeFilesManagerViewModel
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.PhotoPrivacyManagerViewModel
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.PhotosManagerViewModel
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.ScreenshotsManagerViewModel
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.SimilarPhotosManagerViewModel
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.VideosManagerViewModel
import com.quickcleanpro.phonecleaner.presentation.screen.home.HomeViewModel
import com.quickcleanpro.phonecleaner.presentation.screen.onboarding.OnboardingScanViewModel
import com.quickcleanpro.phonecleaner.presentation.screen.result.ResultViewModel
import com.quickcleanpro.phonecleaner.presentation.screen.scan.ScanViewModel
import com.quickcleanpro.phonecleaner.presentation.screen.splash.SplashViewModel
import com.quickcleanpro.phonecleaner.presentation.screen.tools.appusage.AppUsageViewModel
import com.quickcleanpro.phonecleaner.presentation.screen.tools.common.device.BatteryInfoViewModel
import com.quickcleanpro.phonecleaner.presentation.screen.tools.common.device.DeviceInfoViewModel
import com.quickcleanpro.phonecleaner.presentation.screen.tools.common.network.readNetworkInfo
import com.quickcleanpro.phonecleaner.presentation.screen.tools.common.notification.NotificationCleanerViewModel
import com.quickcleanpro.phonecleaner.presentation.screen.tools.common.notification.NotificationBarViewModel
import com.quickcleanpro.phonecleaner.presentation.screen.tools.networkscan.NetworkScanDevicesViewModel
import com.quickcleanpro.phonecleaner.presentation.screen.tools.networkscan.NetworkScanViewModel
import com.quickcleanpro.phonecleaner.presentation.screen.tools.networkspeed.NetworkSpeedViewModel
import com.quickcleanpro.phonecleaner.presentation.screen.tools.networkusage.NetworkUsageViewModel
import com.quickcleanpro.phonecleaner.presentation.screen.tools.whatsappcleaner.WhatsAppCleanerViewModel
import kotlinx.coroutines.Dispatchers
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val presentationModule =
    module {
        viewModel { HomeViewModel(get(), get()) }
        viewModel { AppUsageViewModel(get()) }
        viewModel { DeviceInfoViewModel(get(), get(), get()) }
        viewModel { BatteryInfoViewModel(get(), get(), get()) }
        viewModel { NetworkScanViewModel(get(), { readNetworkInfo(androidContext()) }) }
        viewModel { NetworkScanDevicesViewModel(get()) }
        viewModel { NetworkSpeedViewModel(get(), { readNetworkInfo(androidContext()) }) }
        viewModel { NetworkUsageViewModel(get(), Dispatchers.IO) }
        viewModel { WhatsAppCleanerViewModel(get()) }
        viewModel { NotificationBarViewModel(get()) }
        viewModel { NotificationCleanerViewModel(get()) }
        viewModel { OnboardingScanViewModel(get()) }
        viewModel { ScanViewModel(get(), get(), Dispatchers.IO) }
        viewModel { ResultViewModel(get(), get(), Dispatchers.IO) }
        viewModel { CleanResultViewModel(get()) }
        viewModel { SplashViewModel() }
        viewModel { PhotosManagerViewModel(get(), Dispatchers.IO) }
        viewModel { ScreenshotsManagerViewModel(get(), Dispatchers.IO) }
        viewModel { VideosManagerViewModel(get(), Dispatchers.IO) }
        viewModel { AudiosManagerViewModel(get(), Dispatchers.IO) }
        viewModel { SimilarPhotosManagerViewModel(get(), Dispatchers.IO) }
        viewModel { PhotoPrivacyManagerViewModel(get(), Dispatchers.IO) }
        viewModel { LargeFilesManagerViewModel(get(), Dispatchers.IO) }
        viewModel { DocumentsManagerViewModel(get(), Dispatchers.IO) }
        viewModel { DuplicateFilesViewModel(get(), Dispatchers.IO) }
        viewModel { VirusScanViewModel(androidApplication()) }
        viewModel {
            AppLockViewModel(
                application = androidApplication(),
                repository = get(),
                ioDispatcher = Dispatchers.IO,
            )
        }
    }
