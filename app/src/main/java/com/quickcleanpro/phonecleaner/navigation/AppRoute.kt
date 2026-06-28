package com.quickcleanpro.phonecleaner.navigation

import java.net.URLEncoder

@JvmInline
value class AppRoute(val value: String) {
    init {
        require(value.isNotBlank()) { "Route value must not be blank." }
    }

    fun withArgs(args: Map<String, String> = emptyMap()): String {
        if (args.isEmpty()) return value
        val query =
            args.entries.joinToString("&") { (key, rawValue) ->
                "${key.encodeQueryPart()}=${rawValue.encodeQueryPart()}"
            }
        return "$value?$query"
    }

    override fun toString(): String = value

    companion object {
        val Splash = AppRoute("splash")
        val OnboardingScan = AppRoute("onboarding_scan")
        val Home = AppRoute("home")
        val Settings = AppRoute("settings")
        val ManagePermissions = AppRoute("manage_permissions")
        val JunkClean = AppRoute("scan")
        val AntiVirus = AppRoute("anti_virus")
        val VirusQuickScan = AppRoute("virus_quick_scan")
        val VirusDeepScan = AppRoute("virus_deep_scan")
        val VirusResult = AppRoute("virus_result")
        val NoVirusResult = AppRoute("no_virus_result")
        val AppLock = AppRoute("app_lock")
        val DeviceInfo = AppRoute("device_info")
        val BatteryInfo = AppRoute("battery_info")
        val AppUsage = AppRoute("app_usage")
        val NetworkUsage = AppRoute("network_usage")
        val NetworkScan = AppRoute("network_scan")
        val NetworkScanDevices = AppRoute("network_scan_devices")
        val NetworkSpeed = AppRoute("network_speed")
        val WhatsAppCleaner = AppRoute("whatsapp_cleaner")
        val NotificationCleaner = AppRoute("notification_cleaner")
        val NotificationBar = AppRoute("notification_bar")
        val PhotosManager = AppRoute("photos_manager")
        val SimilarPhotosManager = AppRoute("similar_photos_manager")
        val PhotoPrivacyManager = AppRoute("photo_privacy_manager")
        val ScreenshotsManager = AppRoute("screenshots_manager")
        val VideosManager = AppRoute("videos_manager")
        val AudiosManager = AppRoute("audios_manager")
        val LargeFilesManager = AppRoute("large_files_manager")
        val DuplicateFilesManager = AppRoute("duplicate_files_manager")
        val DocumentsManager = AppRoute("documents_manager")
    }
}

private fun String.encodeQueryPart(): String =
    URLEncoder.encode(this, Charsets.UTF_8.name())
