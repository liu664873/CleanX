package com.quickcleanpro.phonecleaner.config

import android.content.Context
import com.quickcleanpro.phonecleaner.BuildConfig
import com.quickcleanpro.phonecleaner.R
import org.json.JSONObject

object ConfigLoader {

    fun load(
        context: Context,
        loadAds: Boolean = true,
    ): VariantProfile {
        return VariantProfile(
            variantKey = context.getString(R.string.variant_key),
            appName = context.getString(R.string.app_name),
            themeKey = context.getString(R.string.variant_theme_key),
            adProfile = if (loadAds) loadAdProfile(context) else emptyAdProfile(),
            legalProfile = LegalProfile(
                termsOfServiceUrl = context.getString(R.string.terms_of_service_url),
                privacyPolicyUrl = context.getString(R.string.privacy_policy_url),
            ),
            notificationProfile = storageCleanerNotificationProfile(),
            serviceProfile = VariantServiceProfile(
                trustlookApiKey = BuildConfig.TRUSTLOOK_API_KEY,
            ),
        )
    }

    fun loadAdvSdkConfig(context: Context): AdvSdkConfig {
        val json = loadObject(context, "config/adv_sdk.json")

        val admob = json.getJSONObject("admob")
        val thinking = json.getJSONObject("thinking")
        val singular = json.getJSONObject("singular")
        val facebook = json.getJSONObject("facebook")
        val tiktok = json.getJSONObject("tiktok")
        val server = json.getJSONObject("server")
        val playIntegrity = json.getJSONObject("playIntegrity")
        val safe = json.getJSONObject("safe")

        return AdvSdkConfig(
            privacyUrl = json.optString("privacyUrl"),
            termsUrl = json.optString("termsUrl"),
            defaultTopic = json.optString("defaultTopic"),
            admob = AdvAdMobConfig(
                appId = admob.optString("appId"),
                bannerId = admob.optString("bannerId"),
                interstitialId = admob.optString("interstitialId"),
                nativeId = admob.optString("nativeId"),
                openId = admob.optString("openId"),
                nativeIdsJson = admob.optString("nativeIdsJson"),
            ),
            thinking = AdvThinkingConfig(
                appKey = thinking.optString("appKey"),
                serverUrl = thinking.optString("serverUrl"),
            ),
            singular = AdvSingularConfig(
                apiKey = singular.optString("apiKey"),
                secret = singular.optString("secret"),
            ),
            facebook = AdvFacebookConfig(
                appId = facebook.optString("appId"),
                clientToken = facebook.optString("clientToken"),
            ),
            tiktok = AdvTikTokConfig(
                accessToken = tiktok.optString("accessToken"),
                ttAppId = tiktok.optString("ttAppId"),
                appId = tiktok.optString("appId"),
            ),
            server = AdvServerConfig(
                releaseHost = server.optString("releaseHost"),
                testHost = server.optString("testHost"),
            ),
            playIntegrity = AdvPlayIntegrityConfig(
                parseTokenKey = playIntegrity.optString("parseTokenKey"),
                cloudProjectNumber = playIntegrity.optLong("cloudProjectNumber", 0L),
            ),
            safe = AdvSafeConfig(
                expectedSignatures = safe.optString("expectedSignatures"),
            ),
        )
    }

    private fun loadObject(context: Context, path: String): JSONObject =
        context.assets.open(path).bufferedReader().use { reader ->
            JSONObject(reader.readText())
        }

    private fun loadAdProfile(context: Context): VariantAdProfile {
        val adsJson = loadObject(context, "config/ads.json")

        return VariantAdProfile(
            unitIds = VariantAdUnitIds(
                appId = adsJson.optString("appId"),
                appOpen = adsJson.optString("appOpen"),
                interstitial = adsJson.optString("interstitial"),
                banner = adsJson.optString("banner"),
                native = adsJson.optString("native"),
            ),
            placements = defaultAdPlacements(),
        )
    }

    private fun emptyAdProfile(): VariantAdProfile =
        VariantAdProfile(
            unitIds = VariantAdUnitIds(
                appId = "",
                appOpen = "",
                interstitial = "",
                banner = "",
                native = "",
            ),
            placements = VariantAdPlacements(
                featureEntry = emptyMap(),
                featureCompletion = emptyMap(),
            ),
        )
}

data class AdvSdkConfig(
    val privacyUrl: String,
    val termsUrl: String,
    val defaultTopic: String,
    val admob: AdvAdMobConfig,
    val thinking: AdvThinkingConfig,
    val singular: AdvSingularConfig,
    val facebook: AdvFacebookConfig,
    val tiktok: AdvTikTokConfig,
    val server: AdvServerConfig,
    val playIntegrity: AdvPlayIntegrityConfig,
    val safe: AdvSafeConfig,
)

data class AdvAdMobConfig(
    val appId: String,
    val bannerId: String,
    val interstitialId: String,
    val nativeId: String,
    val openId: String,
    val nativeIdsJson: String,
)

data class AdvThinkingConfig(
    val appKey: String,
    val serverUrl: String,
)

data class AdvSingularConfig(
    val apiKey: String,
    val secret: String,
)

data class AdvFacebookConfig(
    val appId: String,
    val clientToken: String,
)

data class AdvTikTokConfig(
    val accessToken: String,
    val ttAppId: String,
    val appId: String,
)

data class AdvServerConfig(
    val releaseHost: String,
    val testHost: String,
)

data class AdvPlayIntegrityConfig(
    val parseTokenKey: String,
    val cloudProjectNumber: Long,
)

data class AdvSafeConfig(
    val expectedSignatures: String,
)
