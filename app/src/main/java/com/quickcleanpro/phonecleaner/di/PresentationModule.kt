package com.quickcleanpro.phonecleaner.di

import com.quickcleanpro.phonecleaner.presentation.screen.antivirus.VirusScanViewModel
import com.quickcleanpro.phonecleaner.presentation.screen.applock.AppLockViewModel
import com.quickcleanpro.phonecleaner.presentation.screen.files.audios.AudiosManagerViewModel
import com.quickcleanpro.phonecleaner.presentation.screen.files.documents.DocumentsManagerViewModel
import com.quickcleanpro.phonecleaner.presentation.screen.files.duplicates.DuplicateFilesManagerViewModel
import com.quickcleanpro.phonecleaner.presentation.screen.files.largefiles.LargeFilesManagerViewModel
import com.quickcleanpro.phonecleaner.presentation.screen.files.photoprivacy.PhotoPrivacyManagerViewModel
import com.quickcleanpro.phonecleaner.presentation.screen.files.photos.PhotosManagerViewModel
import com.quickcleanpro.phonecleaner.presentation.screen.files.screenshots.ScreenshotsManagerViewModel
import com.quickcleanpro.phonecleaner.presentation.screen.files.similarphotos.SimilarPhotosManagerViewModel
import com.quickcleanpro.phonecleaner.presentation.screen.files.videos.VideosManagerViewModel
import com.quickcleanpro.phonecleaner.presentation.screen.home.HomeViewModel
import com.quickcleanpro.phonecleaner.presentation.screen.onboarding.OnboardingScanViewModel
import com.quickcleanpro.phonecleaner.presentation.screen.JunkClean.JunkCleanViewModel
import com.quickcleanpro.phonecleaner.presentation.screen.settings.ManagePermissionsViewModel
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
import com.quickcleanpro.phonecleaner.presentation.app.NotificationPermissionSessionViewModel
import kotlinx.coroutines.Dispatchers
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val presentationModule =
    module {
        viewModel { HomeViewModel(get(), get(), get()) }
        viewModel { NotificationPermissionSessionViewModel() }
        viewModel { AppUsageViewModel(get()) }
        viewModel { DeviceInfoViewModel(get(), get(), get(), get()) }
        viewModel { BatteryInfoViewModel(get(), get(), get(), get()) }
        viewModel { NetworkScanViewModel(get(), { readNetworkInfo(androidContext()) }) }
        viewModel { NetworkScanDevicesViewModel(get()) }
        viewModel { NetworkSpeedViewModel(get(), { readNetworkInfo(androidContext()) }) }
        viewModel { NetworkUsageViewModel(get(), Dispatchers.IO) }
        viewModel { WhatsAppCleanerViewModel(get()) }
        viewModel { NotificationBarViewModel(get()) }
        viewModel { NotificationCleanerViewModel(get()) }
        viewModel { OnboardingScanViewModel(get()) }
        viewModel { JunkCleanViewModel(get(), get(), get(), Dispatchers.IO) }
        viewModel { ManagePermissionsViewModel(Dispatchers.IO) }
        viewModel { SplashViewModel() }
        viewModel { PhotosManagerViewModel(get(), Dispatchers.IO) }
        viewModel { ScreenshotsManagerViewModel(get(), Dispatchers.IO) }
        viewModel { VideosManagerViewModel(get(), Dispatchers.IO) }
        viewModel { AudiosManagerViewModel(get(), Dispatchers.IO) }
        viewModel { SimilarPhotosManagerViewModel(get(), Dispatchers.IO) }
        viewModel { PhotoPrivacyManagerViewModel(get(), Dispatchers.IO) }
        viewModel { LargeFilesManagerViewModel(get(), Dispatchers.IO) }
        viewModel { DocumentsManagerViewModel(get(), Dispatchers.IO) }
        viewModel { DuplicateFilesManagerViewModel(get(), Dispatchers.IO) }
        viewModel { VirusScanViewModel(androidApplication()) }
        viewModel {
            AppLockViewModel(
                application = androidApplication(),
                repository = get(),
                ioDispatcher = Dispatchers.IO,
            )
        }
    }
