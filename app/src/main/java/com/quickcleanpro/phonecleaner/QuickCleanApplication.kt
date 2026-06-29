package com.quickcleanpro.phonecleaner

import android.app.Application
import com.quickcleanpro.phonecleaner.advertise.AdvertiseInitializer
import com.quickcleanpro.phonecleaner.advertise.AdvertiseRuntimeCapabilities
import com.quickcleanpro.phonecleaner.config.VariantConfigs
import com.quickcleanpro.phonecleaner.di.currentVariantModules
import com.quickcleanpro.phonecleaner.di.dataModule
import com.quickcleanpro.phonecleaner.di.presentationModule
import com.quickcleanpro.phonecleaner.utils.SharedPreferencesUtils
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class QuickCleanApplication : Application() {
    companion object {
        lateinit var instance: QuickCleanApplication
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        VariantConfigs.initialize(
            context = this,
            loadAds = AdvertiseRuntimeCapabilities.ADVERTISE_SDK_ENABLED,
        )
        SharedPreferencesUtils.init(this)
        startKoin {
            androidLogger()
            androidContext(this@QuickCleanApplication)
            modules(listOf(dataModule, presentationModule) + currentVariantModules().modules)
        }
        AdvertiseInitializer.initialize(this)
    }
}
