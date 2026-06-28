package com.quickcleanpro.phonecleaner.config

import com.quickcleanpro.phonecleaner.advertise.AdAreaKeys
import com.quickcleanpro.phonecleaner.navigation.AppRoute

enum class FeatureGroup {
    HOME,
    FILES,
    TOOLBOX,
}

data class FeatureSpec(
    val key: FeatureKey,
    val route: AppRoute,
    val group: FeatureGroup,
    val entryAdKey: String? = null,
    val finishAdKey: String? = null,
)

object FeatureCatalog {
    val specs: List<FeatureSpec> =
        listOf(
            FeatureSpec(
                key = FeatureKey.JUNK_CLEAN,
                route = AppRoute.JunkClean,
                group = FeatureGroup.HOME,
                entryAdKey = AdAreaKeys.Interstitial.ENTER_JUNK_CLEAN,
                finishAdKey = AdAreaKeys.Interstitial.JUNK_CLEAN_FINISH,
            ),
            FeatureSpec(FeatureKey.ANTI_VIRUS, AppRoute.AntiVirus, FeatureGroup.HOME),
            FeatureSpec(FeatureKey.APP_LOCK, AppRoute.AppLock, FeatureGroup.HOME),
            FeatureSpec(
                key = FeatureKey.DEVICE_INFO,
                route = AppRoute.DeviceInfo,
                group = FeatureGroup.TOOLBOX,
                entryAdKey = AdAreaKeys.Interstitial.ENTER_DEVICE_INFO,
            ),
            FeatureSpec(
                key = FeatureKey.BATTERY_INFO,
                route = AppRoute.BatteryInfo,
                group = FeatureGroup.TOOLBOX,
                entryAdKey = AdAreaKeys.Interstitial.ENTER_BATTERY_INFO,
                finishAdKey = AdAreaKeys.Interstitial.BATTERY_INFO_FINISH,
            ),
            FeatureSpec(FeatureKey.APP_USAGE, AppRoute.AppUsage, FeatureGroup.TOOLBOX),
            FeatureSpec(FeatureKey.NOTIFICATION_BAR, AppRoute.NotificationBar, FeatureGroup.TOOLBOX),
            FeatureSpec(FeatureKey.NOTIFICATION_CLEANER, AppRoute.NotificationCleaner, FeatureGroup.TOOLBOX),
            FeatureSpec(
                key = FeatureKey.WHATSAPP_CLEANER,
                route = AppRoute.WhatsAppCleaner,
                group = FeatureGroup.TOOLBOX,
                entryAdKey = AdAreaKeys.Interstitial.ENTER_WHATSAPP_CLEANER,
                finishAdKey = AdAreaKeys.Interstitial.WHATSAPP_CLEAN_FINISH,
            ),
            FeatureSpec(FeatureKey.NETWORK_USAGE, AppRoute.NetworkUsage, FeatureGroup.TOOLBOX),
            FeatureSpec(FeatureKey.NETWORK_SCAN, AppRoute.NetworkScan, FeatureGroup.TOOLBOX),
            FeatureSpec(FeatureKey.NETWORK_SPEED, AppRoute.NetworkSpeed, FeatureGroup.TOOLBOX),
            FeatureSpec(
                key = FeatureKey.PHOTOS,
                route = AppRoute.PhotosManager,
                group = FeatureGroup.FILES,
                entryAdKey = AdAreaKeys.Interstitial.ENTER_FILE_MANAGE,
                finishAdKey = AdAreaKeys.Interstitial.FILE_MANAGE_FINISH,
            ),
            FeatureSpec(
                key = FeatureKey.SIMILAR_PHOTOS,
                route = AppRoute.SimilarPhotosManager,
                group = FeatureGroup.FILES,
                entryAdKey = AdAreaKeys.Interstitial.ENTER_FILE_MANAGE,
                finishAdKey = AdAreaKeys.Interstitial.FILE_MANAGE_FINISH,
            ),
            FeatureSpec(
                key = FeatureKey.PHOTO_PRIVACY,
                route = AppRoute.PhotoPrivacyManager,
                group = FeatureGroup.FILES,
                entryAdKey = AdAreaKeys.Interstitial.ENTER_FILE_MANAGE,
                finishAdKey = AdAreaKeys.Interstitial.FILE_MANAGE_FINISH,
            ),
            FeatureSpec(
                key = FeatureKey.SCREENSHOTS,
                route = AppRoute.ScreenshotsManager,
                group = FeatureGroup.FILES,
                entryAdKey = AdAreaKeys.Interstitial.ENTER_FILE_MANAGE,
                finishAdKey = AdAreaKeys.Interstitial.FILE_MANAGE_FINISH,
            ),
            FeatureSpec(
                key = FeatureKey.VIDEOS,
                route = AppRoute.VideosManager,
                group = FeatureGroup.FILES,
                entryAdKey = AdAreaKeys.Interstitial.ENTER_FILE_MANAGE,
                finishAdKey = AdAreaKeys.Interstitial.FILE_MANAGE_FINISH,
            ),
            FeatureSpec(
                key = FeatureKey.AUDIOS,
                route = AppRoute.AudiosManager,
                group = FeatureGroup.FILES,
                entryAdKey = AdAreaKeys.Interstitial.ENTER_FILE_MANAGE,
                finishAdKey = AdAreaKeys.Interstitial.FILE_MANAGE_FINISH,
            ),
            FeatureSpec(
                key = FeatureKey.LARGE_FILES,
                route = AppRoute.LargeFilesManager,
                group = FeatureGroup.FILES,
                entryAdKey = AdAreaKeys.Interstitial.ENTER_FILE_MANAGE,
                finishAdKey = AdAreaKeys.Interstitial.FILE_MANAGE_FINISH,
            ),
            FeatureSpec(
                key = FeatureKey.DUPLICATE_FILES,
                route = AppRoute.DuplicateFilesManager,
                group = FeatureGroup.FILES,
                entryAdKey = AdAreaKeys.Interstitial.ENTER_FILE_MANAGE,
                finishAdKey = AdAreaKeys.Interstitial.FILE_MANAGE_FINISH,
            ),
            FeatureSpec(
                key = FeatureKey.DOCUMENTS,
                route = AppRoute.DocumentsManager,
                group = FeatureGroup.FILES,
                entryAdKey = AdAreaKeys.Interstitial.ENTER_FILE_MANAGE,
                finishAdKey = AdAreaKeys.Interstitial.FILE_MANAGE_FINISH,
            ),
        )

    val byKey: Map<FeatureKey, FeatureSpec> = specs.associateBy(FeatureSpec::key)

    val byRoute: Map<String, FeatureSpec> = specs.associateBy { it.route.value } +
        mapOf(
            AppRoute.VirusQuickScan.value to spec(FeatureKey.ANTI_VIRUS),
            AppRoute.VirusDeepScan.value to spec(FeatureKey.ANTI_VIRUS),
            AppRoute.VirusResult.value to spec(FeatureKey.ANTI_VIRUS),
            AppRoute.NoVirusResult.value to spec(FeatureKey.ANTI_VIRUS),
            AppRoute.NetworkScanDevices.value to spec(FeatureKey.NETWORK_SCAN),
        )

    fun spec(key: FeatureKey): FeatureSpec =
        byKey.getValue(key)

    fun routeFor(feature: FeatureKey): AppRoute? =
        byKey[feature]?.route

    fun featureForRoute(route: String): FeatureKey? =
        byRoute[route]?.key

    fun groupFeatures(group: FeatureGroup): Set<FeatureKey> =
        specs.filter { it.group == group }.mapTo(linkedSetOf(), FeatureSpec::key)
}

val fileFeatures: Set<FeatureKey> = FeatureCatalog.groupFeatures(FeatureGroup.FILES)

val toolboxFeatures: Set<FeatureKey> = FeatureCatalog.groupFeatures(FeatureGroup.TOOLBOX)

fun featureForRoute(route: String): FeatureKey? =
    FeatureCatalog.featureForRoute(route)
