package com.quickcleanpro.phonecleaner.presentation.common.permission

import androidx.compose.runtime.Composable

data class PermissionGateConfig(
    val cleanXFeature: CleanXFeature,
    val onDenied: (() -> Unit)? = null,
    val deniedContent: (@Composable (onRetry: () -> Unit) -> Unit)? = null,
)

object PermissionGatePresets {
    fun feature(feature: CleanXFeature) =
        PermissionGateConfig(feature)

    fun junkRemoval() =
        PermissionGateConfig(CleanXFeature.JunkRemoval)

    fun fileManager() =
        PermissionGateConfig(CleanXFeature.FileManager)

    fun networkScan() =
        PermissionGateConfig(CleanXFeature.NetworkScan)

    fun appUsage() =
        PermissionGateConfig(CleanXFeature.AppUsage)

    fun networkUsage() =
        PermissionGateConfig(CleanXFeature.NetworkUsage)

    fun notificationCleaner() =
        PermissionGateConfig(CleanXFeature.NotificationCleaner)

    fun notificationBar() =
        PermissionGateConfig(CleanXFeature.NotificationBar)

    fun appLock() =
        PermissionGateConfig(CleanXFeature.AppLock)

    fun virusDeepScan() =
        PermissionGateConfig(CleanXFeature.VirusDeepScan)

    fun whatsAppCleaner() =
        PermissionGateConfig(CleanXFeature.WhatsAppCleaner)
}
