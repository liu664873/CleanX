package com.quickcleanpro.phonecleaner

import android.app.Application
import android.util.Log
import com.pdffox.adv.AdvertiseSdk
import com.quickcleanpro.phonecleaner.advertise.AdEventLogger
import com.quickcleanpro.phonecleaner.advertise.AdvertiseConfigFactory
import com.quickcleanpro.phonecleaner.advertise.AdvertiseConfigValidator
import com.quickcleanpro.phonecleaner.config.ConfigLoader
import com.quickcleanpro.phonecleaner.config.VariantConfigs
import com.quickcleanpro.phonecleaner.di.currentVariantModules
import com.quickcleanpro.phonecleaner.di.dataModule
import com.quickcleanpro.phonecleaner.di.presentationModule
import com.quickcleanpro.phonecleaner.utils.SharedPreferencesUtils
import kotlinx.coroutines.runBlocking
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class QuickCleanApplication : Application() {
    companion object {
        private const val TAG = "QuickCleanApplication"

        lateinit var instance: QuickCleanApplication
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        VariantConfigs.initialize(this)
        SharedPreferencesUtils.init(this)
        startKoin {
            androidLogger()
            androidContext(this@QuickCleanApplication)
            modules(listOf(dataModule, presentationModule) + currentVariantModules().modules)
        }
        initAdvertiseSdk()
    }

    private fun initAdvertiseSdk() {
        runCatching {
            runBlocking {
                val advSdkConfig = ConfigLoader.loadAdvSdkConfig(this@QuickCleanApplication)
                AdvertiseConfigValidator.validate(this@QuickCleanApplication, advSdkConfig)
                AdvertiseSdk.init(
                    context = this@QuickCleanApplication,
                    isTest = BuildConfig.DEBUG,
                    sdkConfig = AdvertiseConfigFactory.create(this@QuickCleanApplication, advSdkConfig),
                )
                AdEventLogger.initialized()
            }
        }.onFailure { throwable ->
            Log.e(TAG, "Advertise SDK initialization failed; continuing app startup", throwable)
        }
    }
}
