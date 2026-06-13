package com.quickcleanpro.phonecleaner.presentation.navigation

import androidx.annotation.StringRes
import com.quickcleanpro.phonecleaner.R

interface AppDestination {
    val route: String

    @get:StringRes
    val titleRes: Int
}

enum class CoreDestination(
    override val route: String,
    @param:StringRes override val titleRes: Int
) : AppDestination {
    Splash(Screen.Splash.route, R.string.nav_splash),
    OnboardingScan(Screen.OnboardingScan.route, R.string.nav_onboarding_scan),
    Home(Screen.Home.route, R.string.nav_home),
    Settings(Screen.Settings.route, R.string.nav_settings),
    ManagePermissions(Screen.ManagePermissions.route, R.string.nav_manage_permissions)
}

enum class CleanDestination(
    override val route: String,
    @param:StringRes override val titleRes: Int
) : AppDestination {
    Scan(Screen.Scan.route, R.string.nav_scan),
    Result(Screen.Result.route, R.string.nav_result),
    CleanResult(Screen.CleanResult.route, R.string.nav_clean_result)
}

enum class ToolboxDestination(
    override val route: String,
    @param:StringRes override val titleRes: Int
) : AppDestination {
    DeviceInfo(Screen.DeviceInfo.route, R.string.nav_device_info),
    BatteryInfo(Screen.BatteryInfo.route, R.string.nav_battery_info),
    AppUsage(Screen.AppUsage.route, R.string.nav_app_usage),
    NetworkUsage(Screen.NetworkUsage.route, R.string.nav_network_usage),
    NetworkScan(Screen.NetworkScan.route, R.string.nav_network_scan),
    NetworkScanDevices(Screen.NetworkScanDevices.route, R.string.nav_network_scan_devices),
    NetworkSpeed(Screen.NetworkSpeed.route, R.string.nav_network_speed),
    WhatsAppCleaner(Screen.WhatsAppCleaner.route, R.string.nav_whatsapp_cleaner),
    NotificationCleaner(Screen.NotificationCleaner.route, R.string.nav_notification_cleaner),
    NotificationBar(Screen.NotificationBar.route, R.string.nav_notification_bar)
}

enum class FileDestination(
    override val route: String,
    @param:StringRes override val titleRes: Int
) : AppDestination {
    Photos(Screen.PhotosManager.route, R.string.nav_photos),
    SimilarPhotos(Screen.SimilarPhotosManager.route, R.string.nav_similar_photos),
    PhotoPrivacy(Screen.PhotoPrivacyManager.route, R.string.nav_photo_privacy),
    Screenshots(Screen.ScreenshotsManager.route, R.string.nav_screenshots),
    Videos(Screen.VideosManager.route, R.string.nav_videos),
    Audios(Screen.AudiosManager.route, R.string.nav_audios),
    LargeFiles(Screen.LargeFilesManager.route, R.string.nav_large_files),
    DuplicateFiles(Screen.DuplicateFilesManager.route, R.string.nav_duplicate_files),
    Documents(Screen.DocumentsManager.route, R.string.nav_documents)
}

enum class SecurityDestination(
    override val route: String,
    @param:StringRes override val titleRes: Int
) : AppDestination {
    AntiVirus(Screen.AntiVirus.route, R.string.nav_anti_virus),
    VirusQuickScan(Screen.VirusQuickScan.route, R.string.nav_virus_quick_scan),
    VirusDeepScan(Screen.VirusDeepScan.route, R.string.nav_virus_deep_scan),
    VirusResult(Screen.VirusResult.route, R.string.nav_virus_result),
    NoVirusResult(Screen.NoVirusResult.route, R.string.nav_no_virus_result),
    AppLock(Screen.AppLock.route, R.string.nav_app_lock)
}
