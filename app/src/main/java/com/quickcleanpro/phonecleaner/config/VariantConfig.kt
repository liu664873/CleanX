package com.quickcleanpro.phonecleaner.config

import com.quickcleanpro.phonecleaner.BuildConfig
import com.quickcleanpro.phonecleaner.R
import com.quickcleanpro.phonecleaner.presentation.common.route.Screen

enum class VariantFeature {
    JUNK_CLEAN,
    ANTI_VIRUS,
    APP_LOCK,
    DEVICE_INFO,
    BATTERY_INFO,
    APP_USAGE,
    NOTIFICATION_BAR,
    NOTIFICATION_CLEANER,
    WHATSAPP_CLEANER,
    NETWORK_USAGE,
    NETWORK_SCAN,
    NETWORK_SPEED,
    PHOTOS,
    SIMILAR_PHOTOS,
    PHOTO_PRIVACY,
    SCREENSHOTS,
    VIDEOS,
    AUDIOS,
    LARGE_FILES,
    DUPLICATE_FILES,
    DOCUMENTS,
}

data class VariantAdUnitIds(
    val appId: String,
    val appOpen: String,
    val interstitial: String,
    val banner: String,
    val native: String,
)

data class VariantConfig(
    val variantKey: String,
    val appName: String,
    val themeKey: String,
    val primaryFeature: VariantFeature,
    val enabledFeatures: Set<VariantFeature>,
    val homeFeatureOrder: List<VariantFeature>,
    val fileFeatureOrder: List<VariantFeature>,
    val toolboxFeatureOrder: List<VariantFeature>,
    val trustlookApiKey: String,
    val adUnitIds: VariantAdUnitIds,
    val termsOfServiceUrl: String,
    val privacyPolicyUrl: String,
) {
    fun isEnabled(feature: VariantFeature): Boolean = feature in enabledFeatures

    fun orderedHomeFeatures(): List<VariantFeature> =
        homeFeatureOrder.filter(::isEnabled)

    fun orderedFileFeatures(): List<VariantFeature> =
        fileFeatureOrder.filter(::isEnabled)

    fun orderedToolboxFeatures(): List<VariantFeature> =
        toolboxFeatureOrder.filter(::isEnabled)
}

object VariantConfigs {
    val current: VariantConfig =
        VariantConfig(
            variantKey = BuildConfig.VARIANT_KEY,
            appName = appNameFor(BuildConfig.VARIANT_KEY),
            themeKey = BuildConfig.THEME_KEY,
            primaryFeature = parseFeature(BuildConfig.PRIMARY_FEATURE) ?: VariantFeature.JUNK_CLEAN,
            enabledFeatures = parseFeatures(BuildConfig.ENABLED_FEATURES).toSet(),
            homeFeatureOrder = parseFeatures(BuildConfig.HOME_FEATURE_ORDER),
            fileFeatureOrder = parseFeatures(BuildConfig.FILE_FEATURE_ORDER),
            toolboxFeatureOrder = parseFeatures(BuildConfig.TOOLBOX_FEATURE_ORDER),
            trustlookApiKey = BuildConfig.TRUSTLOOK_API_KEY,
            adUnitIds =
                VariantAdUnitIds(
                    appId = BuildConfig.ADMOB_APP_ID,
                    appOpen = BuildConfig.ADMOB_APP_OPEN_UNIT_ID,
                    interstitial = BuildConfig.ADMOB_INTERSTITIAL_UNIT_ID,
                    banner = BuildConfig.ADMOB_BANNER_UNIT_ID,
                    native = BuildConfig.ADMOB_NATIVE_UNIT_ID,
                ),
            termsOfServiceUrl = BuildConfig.TERMS_OF_SERVICE_URL,
            privacyPolicyUrl = BuildConfig.PRIVACY_POLICY_URL,
        )

    private fun parseFeatures(raw: String): List<VariantFeature> =
        raw.split(',')
            .mapNotNull { parseFeature(it) }
            .distinct()

    private fun parseFeature(raw: String): VariantFeature? =
        raw.trim()
            .takeIf { it.isNotEmpty() }
            ?.let { key ->
                runCatching { VariantFeature.valueOf(key) }.getOrNull()
            }

    private fun appNameFor(variantKey: String): String =
        when (variantKey) {
            "storagecleaner" -> "Storage Cleaner"
            "cleanmaster" -> "Clean Master"
            "securityguard" -> "Security Guard"
            else -> "Quick Clean PRO"
        }
}

fun VariantFeature.screenOrNull(): Screen? =
    when (this) {
        VariantFeature.JUNK_CLEAN -> Screen.Scan
        VariantFeature.ANTI_VIRUS -> Screen.AntiVirus
        VariantFeature.APP_LOCK -> Screen.AppLock
        VariantFeature.DEVICE_INFO -> Screen.DeviceInfo
        VariantFeature.BATTERY_INFO -> Screen.BatteryInfo
        VariantFeature.APP_USAGE -> Screen.AppUsage
        VariantFeature.NOTIFICATION_BAR -> Screen.NotificationBar
        VariantFeature.NOTIFICATION_CLEANER -> Screen.NotificationCleaner
        VariantFeature.WHATSAPP_CLEANER -> Screen.WhatsAppCleaner
        VariantFeature.NETWORK_USAGE -> Screen.NetworkUsage
        VariantFeature.NETWORK_SCAN -> Screen.NetworkScan
        VariantFeature.NETWORK_SPEED -> Screen.NetworkSpeed
        VariantFeature.PHOTOS -> Screen.PhotosManager
        VariantFeature.SIMILAR_PHOTOS -> Screen.SimilarPhotosManager
        VariantFeature.PHOTO_PRIVACY -> Screen.PhotoPrivacyManager
        VariantFeature.SCREENSHOTS -> Screen.ScreenshotsManager
        VariantFeature.VIDEOS -> Screen.VideosManager
        VariantFeature.AUDIOS -> Screen.AudiosManager
        VariantFeature.LARGE_FILES -> Screen.LargeFilesManager
        VariantFeature.DUPLICATE_FILES -> Screen.DuplicateFilesManager
        VariantFeature.DOCUMENTS -> Screen.DocumentsManager
    }

fun VariantFeature.titleRes(): Int =
    when (this) {
        VariantFeature.JUNK_CLEAN -> R.string.home_remove_junk
        VariantFeature.ANTI_VIRUS -> R.string.home_virus_title
        VariantFeature.APP_LOCK -> R.string.home_app_lock_title
        VariantFeature.DEVICE_INFO -> R.string.nav_device_info
        VariantFeature.BATTERY_INFO -> R.string.nav_battery_info
        VariantFeature.APP_USAGE -> R.string.nav_app_usage
        VariantFeature.NOTIFICATION_BAR -> R.string.nav_notification_bar
        VariantFeature.NOTIFICATION_CLEANER -> R.string.nav_notification_cleaner
        VariantFeature.WHATSAPP_CLEANER -> R.string.nav_whatsapp_cleaner
        VariantFeature.NETWORK_USAGE -> R.string.nav_network_usage
        VariantFeature.NETWORK_SCAN -> R.string.nav_network_scan
        VariantFeature.NETWORK_SPEED -> R.string.nav_network_speed
        VariantFeature.PHOTOS -> R.string.nav_photos
        VariantFeature.SIMILAR_PHOTOS -> R.string.nav_similar_photos
        VariantFeature.PHOTO_PRIVACY -> R.string.nav_photo_privacy
        VariantFeature.SCREENSHOTS -> R.string.nav_screenshots
        VariantFeature.VIDEOS -> R.string.nav_videos
        VariantFeature.AUDIOS -> R.string.nav_audios
        VariantFeature.LARGE_FILES -> R.string.nav_large_files
        VariantFeature.DUPLICATE_FILES -> R.string.nav_duplicate_files
        VariantFeature.DOCUMENTS -> R.string.nav_documents
    }

fun VariantFeature.iconRes(): Int =
    when (this) {
        VariantFeature.JUNK_CLEAN -> R.drawable.trash_can
        VariantFeature.ANTI_VIRUS -> R.drawable.virus_shield
        VariantFeature.APP_LOCK -> R.drawable.app_lock
        VariantFeature.DEVICE_INFO -> R.drawable.ic_device_phone
        VariantFeature.BATTERY_INFO -> R.drawable.battery
        VariantFeature.APP_USAGE -> R.drawable.ic_app_usage
        VariantFeature.NOTIFICATION_BAR -> R.drawable.ic_notification_bar
        VariantFeature.NOTIFICATION_CLEANER -> R.drawable.ic_n_notification_cleaner
        VariantFeature.WHATSAPP_CLEANER -> R.drawable.ic_whatsapp_cleaner
        VariantFeature.NETWORK_USAGE -> R.drawable.ic_network_usage
        VariantFeature.NETWORK_SCAN -> R.drawable.ic_network_scan
        VariantFeature.NETWORK_SPEED -> R.drawable.ic_network_speed
        VariantFeature.PHOTOS -> R.drawable.ic_photos
        VariantFeature.SIMILAR_PHOTOS -> R.drawable.ic_similar_photos
        VariantFeature.PHOTO_PRIVACY -> R.drawable.ic_photo_privacy
        VariantFeature.SCREENSHOTS -> R.drawable.ic_screenshots
        VariantFeature.VIDEOS -> R.drawable.ic_videos
        VariantFeature.AUDIOS -> R.drawable.ic_audios
        VariantFeature.LARGE_FILES -> R.drawable.ic_large_files
        VariantFeature.DUPLICATE_FILES -> R.drawable.ic_file_yellow
        VariantFeature.DOCUMENTS -> R.drawable.ic_documents
    }
