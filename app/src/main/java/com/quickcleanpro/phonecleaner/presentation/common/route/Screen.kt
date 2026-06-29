package com.quickcleanpro.phonecleaner.presentation.common.route

import com.quickcleanpro.phonecleaner.navigation.AppRoute

/**
 * Legacy compatibility wrapper for older UI code.
 *
 * New code should prefer AppRoute + NavigationCommand + AppNavigator.
 */
sealed class Screen(
    val route: String,
) {
    data object Splash : Screen(AppRoute.Splash.value)
    data object OnboardingScan : Screen(AppRoute.OnboardingScan.value)
    data object Home : Screen(AppRoute.Home.value)
    data object Settings : Screen(AppRoute.Settings.value)
    data object ManagePermissions : Screen(AppRoute.ManagePermissions.value)
    data object Scan : Screen(AppRoute.JunkClean.value)
    data object Result : Screen(AppRoute.Result.value)
    data object CleanResult : Screen(AppRoute.CleanResult.value)
    data object AntiVirus : Screen(AppRoute.AntiVirus.value)
    data object VirusQuickScan : Screen(AppRoute.VirusQuickScan.value)
    data object VirusDeepScan : Screen(AppRoute.VirusDeepScan.value)
    data object VirusResult : Screen(AppRoute.VirusResult.value)
    data object NoVirusResult : Screen(AppRoute.NoVirusResult.value)
    data object AppLock : Screen(AppRoute.AppLock.value)
    data object DeviceInfo : Screen(AppRoute.DeviceInfo.value)
    data object BatteryInfo : Screen(AppRoute.BatteryInfo.value)
    data object AppUsage : Screen(AppRoute.AppUsage.value)
    data object NetworkUsage : Screen(AppRoute.NetworkUsage.value)
    data object NetworkScan : Screen(AppRoute.NetworkScan.value)
    data object NetworkScanDevices : Screen(AppRoute.NetworkScanDevices.value)
    data object NetworkSpeed : Screen(AppRoute.NetworkSpeed.value)
    data object WhatsAppCleaner : Screen(AppRoute.WhatsAppCleaner.value)
    data object NotificationCleaner : Screen(AppRoute.NotificationCleaner.value)
    data object NotificationBar : Screen(AppRoute.NotificationBar.value)
    data object PhotosManager : Screen(AppRoute.PhotosManager.value)
    data object SimilarPhotosManager : Screen(AppRoute.SimilarPhotosManager.value)
    data object PhotoPrivacyManager : Screen(AppRoute.PhotoPrivacyManager.value)
    data object ScreenshotsManager : Screen(AppRoute.ScreenshotsManager.value)
    data object VideosManager : Screen(AppRoute.VideosManager.value)
    data object AudiosManager : Screen(AppRoute.AudiosManager.value)
    data object LargeFilesManager : Screen(AppRoute.LargeFilesManager.value)
    data object DuplicateFilesManager : Screen(AppRoute.DuplicateFilesManager.value)
    data object DocumentsManager : Screen(AppRoute.DocumentsManager.value)
}

fun Screen.toAppRoute(): AppRoute = AppRoute(route)
