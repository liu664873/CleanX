package com.quickcleanpro.phonecleaner.advertise

import android.app.Application
import android.util.Log
import com.pdffox.adv.AdvertiseSdk
import com.quickcleanpro.phonecleaner.BuildConfig
import com.quickcleanpro.phonecleaner.config.ConfigLoader
import kotlinx.coroutines.runBlocking

object AdvertiseInitializer {
    private const val TAG = "AdvertiseInitializer"

    fun initialize(application: Application) {
        if (!AdvertiseRuntimeCapabilities.ADVERTISE_SDK_ENABLED) {
            Log.i(TAG, "Advertise SDK disabled; skipping initialization")
            return
        }
        runCatching {
            runBlocking {
                val advSdkConfig = ConfigLoader.loadAdvSdkConfig(application)
                AdvertiseConfigValidator.validate(application, advSdkConfig)
                AdvertiseSdk.init(
                    context = application,
                    isTest = BuildConfig.DEBUG,
                    sdkConfig = AdvertiseConfigFactory.create(application, advSdkConfig),
                )
                AdEventLogger.initialized()
            }
        }.onFailure { throwable ->
            Log.e(TAG, "Advertise SDK initialization failed; continuing app startup", throwable)
        }
    }
}
