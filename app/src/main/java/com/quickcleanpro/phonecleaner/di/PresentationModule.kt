package com.quickcleanpro.phonecleaner.di

import com.quickcleanpro.phonecleaner.presentation.screen.antivirus.VirusScanViewModel
import com.quickcleanpro.phonecleaner.presentation.screen.applock.AppLockViewModel
import com.quickcleanpro.phonecleaner.presentation.screen.appusage.AppUsageViewModel
import com.quickcleanpro.phonecleaner.presentation.screen.cleanresult.CleanResultViewModel
import com.quickcleanpro.phonecleaner.presentation.screen.deviceinfo.DeviceInfoViewModel
import com.quickcleanpro.phonecleaner.presentation.screen.files.AudiosManagerViewModel
import com.quickcleanpro.phonecleaner.presentation.screen.files.DocumentsManagerViewModel
import com.quickcleanpro.phonecleaner.presentation.screen.files.DuplicateFilesViewModel
import com.quickcleanpro.phonecleaner.presentation.screen.files.LargeFilesManagerViewModel
import com.quickcleanpro.phonecleaner.presentation.screen.files.PhotoPrivacyManagerViewModel
import com.quickcleanpro.phonecleaner.presentation.screen.files.PhotosManagerViewModel
import com.quickcleanpro.phonecleaner.presentation.screen.files.ScreenshotsManagerViewModel
import com.quickcleanpro.phonecleaner.presentation.screen.files.SimilarPhotosManagerViewModel
import com.quickcleanpro.phonecleaner.presentation.screen.files.VideosManagerViewModel
import com.quickcleanpro.phonecleaner.presentation.screen.home.HomeViewModel
import com.quickcleanpro.phonecleaner.presentation.screen.networkscan.NetworkScanDevicesViewModel
import com.quickcleanpro.phonecleaner.presentation.screen.networkscan.NetworkScanViewModel
import com.quickcleanpro.phonecleaner.presentation.screen.networkscan.readNetworkInfo
import com.quickcleanpro.phonecleaner.presentation.screen.networkspeed.NetworkSpeedViewModel
import com.quickcleanpro.phonecleaner.presentation.screen.notificationbar.NotificationBarViewModel
import com.quickcleanpro.phonecleaner.presentation.screen.onboarding.OnboardingScanViewModel
import com.quickcleanpro.phonecleaner.presentation.screen.result.ResultViewModel
import com.quickcleanpro.phonecleaner.presentation.screen.scan.ScanViewModel
import com.quickcleanpro.phonecleaner.presentation.screen.splash.SplashViewModel
import com.quickcleanpro.phonecleaner.presentation.screen.whatsappcleaner.WhatsAppCleanerViewModel
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
        viewModel { NetworkScanViewModel(get(), { readNetworkInfo(androidContext()) }) }
        viewModel { NetworkScanDevicesViewModel(get()) }
        viewModel { NetworkSpeedViewModel(get(), { readNetworkInfo(androidContext()) }) }
        viewModel { WhatsAppCleanerViewModel(get()) }
        viewModel { NotificationBarViewModel(get()) }
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
