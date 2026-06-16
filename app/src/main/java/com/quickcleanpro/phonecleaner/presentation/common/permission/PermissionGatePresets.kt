package com.quickcleanpro.phonecleaner.presentation.common.permission

import androidx.compose.runtime.Composable
import com.quickcleanpro.phonecleaner.domain.repository.SettingsRepository

data class PermissionGateConfig(
    val permissionType: CleanXPermissionType,
    val feature: CleanXPermissionFeature = CleanXPermissionFeature.General,
    val onDenied: (() -> Unit)? = null,
    val deniedContent: (@Composable (onRetry: () -> Unit) -> Unit)? = null,
    val settingsRepository: SettingsRepository? = null,
)

/**
 * 权限配置预设，简化页面调用
 */
object PermissionGatePresets {
    fun storage(feature: CleanXPermissionFeature = CleanXPermissionFeature.General) =
        PermissionGateConfig(CleanXPermissionType.StorageFiles, feature)

    fun mediaImages(feature: CleanXPermissionFeature = CleanXPermissionFeature.General) =
        PermissionGateConfig(CleanXPermissionType.MediaImages, feature)

    fun mediaImagesWithLocation() =
        PermissionGateConfig(CleanXPermissionType.MediaImagesWithLocation)

    fun mediaVideo() =
        PermissionGateConfig(CleanXPermissionType.MediaVideo)

    fun mediaAudio() =
        PermissionGateConfig(CleanXPermissionType.MediaAudio)

    fun location() =
        PermissionGateConfig(CleanXPermissionType.Location)

    fun usageAccess(feature: CleanXPermissionFeature = CleanXPermissionFeature.General) =
        PermissionGateConfig(CleanXPermissionType.UsageAccess, feature)

    fun notificationListener() =
        PermissionGateConfig(CleanXPermissionType.NotificationListener)

    fun overlay() =
        PermissionGateConfig(CleanXPermissionType.Overlay)

    fun postNotifications(feature: CleanXPermissionFeature = CleanXPermissionFeature.General) =
        PermissionGateConfig(CleanXPermissionType.PostNotifications, feature)
}
