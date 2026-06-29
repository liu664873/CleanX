package com.quickcleanpro.phonecleaner.config

import com.quickcleanpro.phonecleaner.advertise.AdAreaKeys
import com.quickcleanpro.phonecleaner.navigation.AppRoute

data class VariantAdUnitIds(
    val appId: String,
    val appOpen: String,
    val interstitial: String,
    val banner: String,
    val native: String,
)

data class VariantAdPlacements(
    val featureEntry: Map<String, String>,
    val featureCompletion: Map<String, String>,
    val banner: Map<String, String> = emptyMap(),
    val native: Map<String, String> = emptyMap(),
) {
    fun entryFor(route: String): String? = featureEntry[route]

    fun completionFor(route: String): String? = featureCompletion[route]
}

data class VariantAdProfile(
    val unitIds: VariantAdUnitIds,
    val placements: VariantAdPlacements,
)

data class LegalProfile(
    val termsOfServiceUrl: String,
    val privacyPolicyUrl: String,
)

data class NotificationProfile(
    val persistentShortcutCompactLayoutName: String? = null,
    val persistentShortcutExpandedLayoutName: String? = null,
    val persistentShortcuts: List<NotificationShortcutProfile> = emptyList(),
    val enabledToolRoutes: Set<String> = emptySet(),
)

data class NotificationShortcutProfile(
    val viewIdName: String,
    val route: String,
    val requestCode: Int,
)

data class VariantServiceProfile(
    val trustlookApiKey: String,
)

data class VariantProfile(
    val variantKey: String,
    val appName: String,
    val themeKey: String,
    val adProfile: VariantAdProfile,
    val legalProfile: LegalProfile,
    val notificationProfile: NotificationProfile,
    val serviceProfile: VariantServiceProfile,
) {
    val trustlookApiKey: String get() = serviceProfile.trustlookApiKey
    val adUnitIds: VariantAdUnitIds get() = adProfile.unitIds
    val adPlacements: VariantAdPlacements get() = adProfile.placements
    val termsOfServiceUrl: String get() = legalProfile.termsOfServiceUrl
    val privacyPolicyUrl: String get() = legalProfile.privacyPolicyUrl
}

typealias VariantConfig = VariantProfile

object VariantConfigs {
    lateinit var current: VariantProfile
        private set

    fun initialize(
        context: android.content.Context,
        loadAds: Boolean = true,
    ) {
        if (::current.isInitialized) return
        current = ConfigLoader.load(context, loadAds = loadAds)
    }
}

fun storageCleanerNotificationProfile(): NotificationProfile =
    NotificationProfile(
        persistentShortcutCompactLayoutName = "notification_persistent_shortcuts_compact",
        persistentShortcutExpandedLayoutName = "notification_persistent_shortcuts",
        persistentShortcuts =
            listOf(
                NotificationShortcutProfile(
                    viewIdName = "persistent_shortcut_clean",
                    route = AppRoute.JunkClean.value,
                    requestCode = 1711,
                ),
                NotificationShortcutProfile(
                    viewIdName = "persistent_shortcut_file",
                    route = "home_file_manager",
                    requestCode = 1712,
                ),
                NotificationShortcutProfile(
                    viewIdName = "persistent_shortcut_tools",
                    route = "home_toolbox",
                    requestCode = 1713,
                ),
                NotificationShortcutProfile(
                    viewIdName = "persistent_shortcut_battery",
                    route = AppRoute.BatteryInfo.value,
                    requestCode = 1714,
                ),
            ),
        enabledToolRoutes =
            setOf(
                AppRoute.DeviceInfo.value,
                AppRoute.JunkClean.value,
                AppRoute.BatteryInfo.value,
                AppRoute.NetworkScan.value,
                AppRoute.NetworkUsage.value,
                AppRoute.NotificationBar.value,
            ),
    )

fun defaultAdPlacements(): VariantAdPlacements {
    val featureEntry =
        FeatureCatalog.specs
            .mapNotNull { spec -> spec.entryAdKey?.let { spec.route.value to it } }
            .toMap()

    val featureCompletion =
        FeatureCatalog.specs
            .mapNotNull { spec -> spec.finishAdKey?.let { spec.route.value to it } }
            .toMap()

    return VariantAdPlacements(
        featureEntry = featureEntry,
        featureCompletion = featureCompletion,
        banner =
            mapOf(
                "home_bottom" to AdAreaKeys.Banner.HOME_BOTTOM,
                "toolbox_bottom" to AdAreaKeys.Banner.TOOLBOX_BOTTOM,
                "clean_page_bottom" to AdAreaKeys.Banner.CLEAN_PAGE_BOTTOM,
                "file_page_bottom" to AdAreaKeys.Banner.FILE_PAGE_BOTTOM,
            ),
        native =
            mapOf(
                "home" to AdAreaKeys.Native.HOME,
                "home_bottom" to AdAreaKeys.Native.HOME_BOTTOM,
                "toolbox_bottom" to AdAreaKeys.Native.TOOLBOX_BOTTOM,
                "finish_page" to AdAreaKeys.Native.FINISH_PAGE,
                "file_access_dialog" to AdAreaKeys.Native.FILE_ACCESS_DIALOG,
                "quit_app_dialog" to AdAreaKeys.Native.QUIT_APP_DIALOG,
            ),
    )
}
