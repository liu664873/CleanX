package com.quickcleanpro.phonecleaner.presentation.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import com.quickcleanpro.phonecleaner.R

sealed class Screen(
    val route: String,
    @StringRes val titleRes: Int,
    val icon: ImageVector? = null
) {
    data object Splash : Screen("splash", R.string.nav_splash)
    data object OnboardingScan : Screen("onboarding_scan", R.string.nav_onboarding_scan)

    data object Home : Screen("home", R.string.nav_home, Icons.Default.Home)
    data object Settings : Screen("settings", R.string.nav_settings, Icons.Default.Settings)
    data object ManagePermissions : Screen("manage_permissions", R.string.nav_manage_permissions)

    data object Scan : Screen("scan", R.string.nav_scan, Icons.Default.CleaningServices)
    data object Result : Screen("result", R.string.nav_result)
    data object CleanResult : Screen("clean_result", R.string.nav_clean_result)

    data object AntiVirus : Screen("anti_virus", R.string.nav_anti_virus)
    data object VirusQuickScan : Screen("virus_quick_scan", R.string.nav_virus_quick_scan)
    data object VirusDeepScan : Screen("virus_deep_scan", R.string.nav_virus_deep_scan)
    data object VirusResult : Screen("virus_result", R.string.nav_virus_result)
    data object NoVirusResult : Screen("no_virus_result", R.string.nav_no_virus_result)
    data object AppLock : Screen("app_lock", R.string.nav_app_lock)

    data object DeviceInfo : Screen("device_info", R.string.nav_device_info)
    data object BatteryInfo : Screen("battery_info", R.string.nav_battery_info)

    data object AppUsage : Screen("app_usage", R.string.nav_app_usage)
    data object NetworkUsage : Screen("network_usage", R.string.nav_network_usage)
    data object NetworkScan : Screen("network_scan", R.string.nav_network_scan)
    data object NetworkScanDevices : Screen("network_scan_devices", R.string.nav_network_scan_devices)
    data object NetworkSpeed : Screen("network_speed", R.string.nav_network_speed)
    data object WhatsAppCleaner : Screen("whatsapp_cleaner", R.string.nav_whatsapp_cleaner)
    data object NotificationCleaner : Screen("notification_cleaner", R.string.nav_notification_cleaner)
    data object NotificationBar : Screen("notification_bar", R.string.nav_notification_bar)

    data object PhotosManager : Screen("photos_manager", R.string.nav_photos)
    data object SimilarPhotosManager : Screen("similar_photos_manager", R.string.nav_similar_photos)
    data object PhotoPrivacyManager : Screen("photo_privacy_manager", R.string.nav_photo_privacy)
    data object ScreenshotsManager : Screen("screenshots_manager", R.string.nav_screenshots)
    data object VideosManager : Screen("videos_manager", R.string.nav_videos)
    data object AudiosManager : Screen("audios_manager", R.string.nav_audios)
    data object LargeFilesManager : Screen("large_files_manager", R.string.nav_large_files)
    data object DuplicateFilesManager : Screen("duplicate_files_manager", R.string.nav_duplicate_files)
    data object DocumentsManager : Screen("documents_manager", R.string.nav_documents)
}
