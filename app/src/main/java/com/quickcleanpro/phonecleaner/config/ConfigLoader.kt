package com.quickcleanpro.phonecleaner.config

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

// Loads variant and ad configurations from assets/config/*.json at startup,
// replacing the 30+ BuildConfig fields previously injected via Gradle.
object ConfigLoader {

    fun load(context: Context): VariantProfile {
        val variantJson = loadObject(context, "config/variant.json")
        val adsJson = loadObject(context, "config/ads.json")

        val enabledFeatures = parseFeatures(variantJson.optJSONArray("enabledFeatures")).toSet()

        return VariantProfile(
            variantKey = variantJson.optString("variantKey", "storagecleaner"),
            appName = variantJson.optString("appName", "Storage Cleaner"),
            themeKey = variantJson.optString("themeKey", "storage_cleaner"),
            primaryFeature = parseFeature(variantJson.optString("primaryFeature", "JUNK_CLEAN"))
                ?: FeatureKey.JUNK_CLEAN,
            enabledFeatures = enabledFeatures,
            homeFeatureOrder = parseFeatures(variantJson.optJSONArray("homeFeatureOrder")),
            fileFeatureOrder = parseFeatures(variantJson.optJSONArray("fileFeatureOrder")),
            toolboxFeatureOrder = parseFeatures(variantJson.optJSONArray("toolboxFeatureOrder")),
            adProfile = VariantAdProfile(
                unitIds = VariantAdUnitIds(
                    appId = adsJson.optString("appId"),
                    appOpen = adsJson.optString("appOpen"),
                    interstitial = adsJson.optString("interstitial"),
                    banner = adsJson.optString("banner"),
                    native = adsJson.optString("native"),
                ),
                placements = defaultAdPlacements(enabledFeatures),
            ),
            legalProfile = LegalProfile(
                termsOfServiceUrl = variantJson.optString("termsOfServiceUrl"),
                privacyPolicyUrl = variantJson.optString("privacyPolicyUrl"),
            ),
            notificationProfile = storageCleanerNotificationProfile(),
            serviceProfile = VariantServiceProfile(
                trustlookApiKey = variantJson.optString("trustlookApiKey"),
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

    private fun parseFeatures(array: JSONArray?): List<FeatureKey> {
        if (array == null) return emptyList()
        val result = mutableListOf<FeatureKey>()
        for (i in 0 until array.length()) {
            parseFeature(array.optString(i))?.let { result.add(it) }
        }
        return result.distinct()
    }

    private fun parseFeature(raw: String): FeatureKey? =
        raw.trim().takeIf { it.isNotEmpty() }?.let { key ->
            runCatching { FeatureKey.valueOf(key) }.getOrNull()
        }
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
