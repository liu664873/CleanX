package com.quickcleanpro.phonecleaner.di

import com.quickcleanpro.phonecleaner.presentation.screen.appusage.AppUsageViewModel
import com.quickcleanpro.phonecleaner.presentation.screen.deviceinfo.DeviceInfoViewModel
import com.quickcleanpro.phonecleaner.presentation.screen.home.HomeViewModel
import com.quickcleanpro.phonecleaner.presentation.screen.networkscan.NetworkScanDevicesViewModel
import com.quickcleanpro.phonecleaner.presentation.screen.networkscan.NetworkScanViewModel
import com.quickcleanpro.phonecleaner.presentation.screen.networkscan.readNetworkInfo
import com.quickcleanpro.phonecleaner.presentation.screen.networkspeed.NetworkSpeedViewModel
import com.quickcleanpro.phonecleaner.presentation.screen.notificationbar.NotificationBarViewModel
import com.quickcleanpro.phonecleaner.presentation.screen.onboarding.OnboardingScanViewModel
import com.quickcleanpro.phonecleaner.presentation.screen.splash.SplashViewModel
import com.quickcleanpro.phonecleaner.presentation.screen.whatsappcleaner.WhatsAppCleanerViewModel
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
        viewModel { SplashViewModel() }
    }
