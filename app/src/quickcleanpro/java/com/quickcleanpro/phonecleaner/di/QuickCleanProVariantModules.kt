package com.quickcleanpro.phonecleaner.di

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
import com.quickcleanpro.phonecleaner.presentation.screen.notification.NotificationBlockerViewModel
import com.quickcleanpro.phonecleaner.presentation.screen.settings.SettingsViewModel
import com.quickcleanpro.phonecleaner.presentation.screen.toolbox.AppUsageViewModel
import com.quickcleanpro.phonecleaner.presentation.screen.toolbox.NetworkScanDevicesViewModel
import com.quickcleanpro.phonecleaner.presentation.screen.toolbox.NetworkScanViewModel
import com.quickcleanpro.phonecleaner.presentation.screen.toolbox.NetworkSpeedViewModel
import com.quickcleanpro.phonecleaner.presentation.screen.toolbox.NetworkUsageViewModel
import com.quickcleanpro.phonecleaner.presentation.screen.toolbox.WhatsAppCleanerViewModel
import kotlinx.coroutines.Dispatchers
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

fun currentVariantModules(): VariantKoinModules = QuickCleanProKoinModules

object QuickCleanProKoinModules : VariantKoinModules {
    override val modules =
        listOf(
            module {
                viewModel { SettingsViewModel(get()) }
                viewModel { WhatsAppCleanerViewModel(get()) }
                viewModel { DeviceInfoViewModel(get(), get(), get(), get(), Dispatchers.IO) }
                viewModel { AppUsageViewModel(get()) }
                viewModel { NetworkSpeedViewModel(get(), androidContext()) }
                viewModel { NetworkScanViewModel(get(), androidContext()) }
                viewModel { NetworkScanDevicesViewModel(get()) }
                viewModel { NetworkUsageViewModel(get()) }
                viewModel { NotificationBlockerViewModel(get(), Dispatchers.IO) }
                viewModel { PhotosManagerViewModel(get(), Dispatchers.IO) }
                viewModel { ScreenshotsManagerViewModel(get(), Dispatchers.IO) }
                viewModel { VideosManagerViewModel(get(), Dispatchers.IO) }
                viewModel { AudiosManagerViewModel(get(), Dispatchers.IO) }
                viewModel { SimilarPhotosManagerViewModel(get(), Dispatchers.IO) }
                viewModel { PhotoPrivacyManagerViewModel(get(), Dispatchers.IO) }
                viewModel { LargeFilesManagerViewModel(get(), Dispatchers.IO) }
                viewModel { DocumentsManagerViewModel(get(), Dispatchers.IO) }
                viewModel { DuplicateFilesViewModel(get(), Dispatchers.IO) }
            },
        )
}
