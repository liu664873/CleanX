package com.quickcleanpro.phonecleaner.advertise

import android.app.Application
import android.content.pm.PackageManager
import com.quickcleanpro.phonecleaner.BuildConfig
import com.quickcleanpro.phonecleaner.config.AdvSdkConfig
import com.quickcleanpro.phonecleaner.config.VariantConfigs

object AdvertiseConfigValidator {
    private val expectedAreaKeys =
        setOf(
            AdAreaKeys.Open.OPEN_PAGE,
            AdAreaKeys.Open.FOREGROUND,
            AdAreaKeys.Interstitial.ENTER_JUNK_CLEAN,
            AdAreaKeys.Interstitial.JUNK_CLEAN_FINISH,
            AdAreaKeys.Interstitial.ENTER_FILE_MANAGE,
            AdAreaKeys.Interstitial.FILE_MANAGE_FINISH,
            AdAreaKeys.Interstitial.RETURN_HOME_PAGE,
            AdAreaKeys.Interstitial.FILE_ACCESS_CANCEL,
        )

    fun validate(context: Application, advSdkConfig: AdvSdkConfig) {
        validateAdMobAppId(context, advSdkConfig)
        validateAreaKeys()
        validateTestIds(advSdkConfig)
    }

    private fun validateAdMobAppId(context: Application, config: AdvSdkConfig) {
        val manifestAppId =
            runCatching {
                val appInfo =
                    context.packageManager.getApplicationInfo(
                        context.packageName,
                        PackageManager.GET_META_DATA,
                    )
                appInfo.metaData?.getString("com.google.android.gms.ads.APPLICATION_ID").orEmpty()
            }.getOrDefault("")
        when {
            manifestAppId.isBlank() ->
                AdEventLogger.validationWarning("Manifest AdMob App ID is blank.")
            manifestAppId != config.admob.appId ->
                AdEventLogger.validationWarning("Manifest AdMob App ID does not match adv_sdk.json admob.appId.")
        }
    }

    private fun validateAreaKeys() {
        val profile = VariantConfigs.current
        val configured =
            profile.adProfile.placements.featureEntry.values +
                profile.adProfile.placements.featureCompletion.values +
                profile.adProfile.placements.banner.values +
                profile.adProfile.placements.native.values +
                listOf(
                    AdAreaKeys.Open.OPEN_PAGE,
                    AdAreaKeys.Open.FOREGROUND,
                    AdAreaKeys.Interstitial.RETURN_HOME_PAGE,
                    AdAreaKeys.Interstitial.FILE_ACCESS_CANCEL,
                )
        val missing = expectedAreaKeys - configured.toSet()
        if (missing.isNotEmpty()) {
            AdEventLogger.validationWarning("Missing expected area keys in variant profile: $missing")
        }
    }

    private fun validateTestIds(config: AdvSdkConfig) {
        if (!BuildConfig.DEBUG && config.admob.appId.contains("3940256099942544")) {
            AdEventLogger.validationWarning("Release build is using Google test AdMob App ID.")
        }
    }
}
