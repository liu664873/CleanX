package com.quickcleanpro.phonecleaner.presentation.common.permission

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.quickcleanpro.phonecleaner.R
import com.quickcleanpro.phonecleaner.domain.repository.SettingsRepository

/**
 * 权限请求管理器，负责权限检查、Intent 获取和文案生成。
 */
object CleanXPermissionRequestManager {

    fun isGranted(
        context: Context,
        type: CleanXPermissionType,
        settingsRepository: SettingsRepository,
    ): Boolean =
        runCatching {
            when (type) {
                CleanXPermissionType.StorageFiles ->
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        settingsRepository.hasStoragePermission()
                    } else {
                        runtimePermissions(type).all { permission ->
                            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
                        }
                    }
                CleanXPermissionType.MediaImages,
                CleanXPermissionType.MediaImagesWithLocation,
                CleanXPermissionType.MediaVideo,
                CleanXPermissionType.MediaAudio,
                CleanXPermissionType.Location ->
                    runtimePermissions(type).all { permission ->
                        ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
                    }
                CleanXPermissionType.PostNotifications -> settingsRepository.hasPostNotificationsPermission()
                CleanXPermissionType.UsageAccess -> settingsRepository.hasAppUsageAccess()
                CleanXPermissionType.NotificationListener -> settingsRepository.hasNotificationListenerAccess()
                CleanXPermissionType.Overlay -> settingsRepository.hasOverlayPermission()
            }
        }.getOrDefault(false)

    fun runtimePermissions(type: CleanXPermissionType): Array<String> =
        when (type) {
            CleanXPermissionType.StorageFiles -> storageRuntimePermissions()
            CleanXPermissionType.MediaImages -> mediaRuntimePermissions(Manifest.permission.READ_MEDIA_IMAGES)
            CleanXPermissionType.MediaImagesWithLocation -> mediaRuntimePermissions(
                Manifest.permission.READ_MEDIA_IMAGES,
                includeMediaLocation = true,
            )
            CleanXPermissionType.MediaVideo -> mediaRuntimePermissions(Manifest.permission.READ_MEDIA_VIDEO)
            CleanXPermissionType.MediaAudio -> mediaRuntimePermissions(Manifest.permission.READ_MEDIA_AUDIO)
            CleanXPermissionType.Location -> arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
            CleanXPermissionType.PostNotifications ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS)
                } else {
                    emptyArray()
                }
            CleanXPermissionType.UsageAccess,
            CleanXPermissionType.NotificationListener,
            CleanXPermissionType.Overlay -> emptyArray()
        }

    fun primarySettingsIntent(
        type: CleanXPermissionType,
        settingsRepository: SettingsRepository,
    ): Intent? =
        runCatching {
            when (type) {
                CleanXPermissionType.StorageFiles ->
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) settingsRepository.storagePermissionIntent() else null
                CleanXPermissionType.UsageAccess -> settingsRepository.appUsageSettingsIntent()
                CleanXPermissionType.NotificationListener -> settingsRepository.notificationListenerSettingsIntent()
                CleanXPermissionType.Overlay -> settingsRepository.overlayPermissionIntent()
                CleanXPermissionType.PostNotifications -> settingsRepository.postNotificationsSettingsIntent()
                CleanXPermissionType.MediaImages,
                CleanXPermissionType.MediaImagesWithLocation,
                CleanXPermissionType.MediaVideo,
                CleanXPermissionType.MediaAudio,
                CleanXPermissionType.Location -> null
            }
        }.getOrNull()

    fun fallbackSettingsIntent(
        type: CleanXPermissionType,
        settingsRepository: SettingsRepository,
    ): Intent? =
        runCatching {
            when (type) {
                CleanXPermissionType.StorageFiles ->
                    settingsRepository.storagePermissionFallbackIntent() ?: settingsRepository.appSettingsIntent()
                CleanXPermissionType.PostNotifications,
                CleanXPermissionType.UsageAccess,
                CleanXPermissionType.NotificationListener,
                CleanXPermissionType.Overlay,
                CleanXPermissionType.MediaImages,
                CleanXPermissionType.MediaImagesWithLocation,
                CleanXPermissionType.MediaVideo,
                CleanXPermissionType.MediaAudio,
                CleanXPermissionType.Location -> settingsRepository.appSettingsIntent()
            }
        }.getOrNull()

    fun dialogCopy(
        type: CleanXPermissionType,
        feature: CleanXPermissionFeature = CleanXPermissionFeature.General,
    ): CleanXPermissionCopy {
        val titleRes = R.string.permission_title_required
        val noPersonalRes = R.string.permission_hint_no_personal
        return when (type) {
            CleanXPermissionType.StorageFiles -> CleanXPermissionCopy(
                titleRes = titleRes,
                descriptionRes = when (feature) {
                    CleanXPermissionFeature.JunkRemoval -> R.string.permission_storage_desc
                    CleanXPermissionFeature.VirusDeepScan -> R.string.permission_virus_storage_desc
                    CleanXPermissionFeature.WhatsAppCleaner -> R.string.permission_whatsapp_storage_desc
                    else -> R.string.permission_storage_files_desc
                },
                hint1Res = when (feature) {
                    CleanXPermissionFeature.JunkRemoval -> R.string.permission_hint_junk_deleted
                    CleanXPermissionFeature.VirusDeepScan -> R.string.permission_hint_threat_files
                    CleanXPermissionFeature.WhatsAppCleaner -> R.string.permission_hint_whatsapp_files
                    else -> R.string.permission_hint_files_safe
                },
                hint2Res = noPersonalRes,
            )
            CleanXPermissionType.MediaImages,
            CleanXPermissionType.MediaImagesWithLocation -> CleanXPermissionCopy(
                titleRes = titleRes,
                descriptionRes = R.string.permission_media_images_desc,
                hint1Res = R.string.permission_hint_files_safe,
                hint2Res = noPersonalRes,
            )
            CleanXPermissionType.MediaVideo -> CleanXPermissionCopy(
                titleRes = titleRes,
                descriptionRes = R.string.permission_media_video_desc,
                hint1Res = R.string.permission_hint_files_safe,
                hint2Res = noPersonalRes,
            )
            CleanXPermissionType.MediaAudio -> CleanXPermissionCopy(
                titleRes = titleRes,
                descriptionRes = R.string.permission_media_audio_desc,
                hint1Res = R.string.permission_hint_files_safe,
                hint2Res = noPersonalRes,
            )
            CleanXPermissionType.Location -> CleanXPermissionCopy(
                titleRes = titleRes,
                descriptionRes = R.string.permission_location_desc,
                hint1Res = R.string.permission_hint_network_scan,
                hint2Res = noPersonalRes,
            )
            CleanXPermissionType.UsageAccess -> CleanXPermissionCopy(
                titleRes = titleRes,
                descriptionRes = when (feature) {
                    CleanXPermissionFeature.NetworkUsage -> R.string.permission_network_usage_desc
                    CleanXPermissionFeature.AppLock -> R.string.permission_app_lock_usage_desc
                    else -> R.string.permission_usage_desc
                },
                hint1Res = R.string.permission_hint_usage_read,
                hint2Res = noPersonalRes,
            )
            CleanXPermissionType.NotificationListener -> CleanXPermissionCopy(
                titleRes = titleRes,
                descriptionRes = R.string.permission_notification_desc,
                hint1Res = R.string.permission_hint_notifications,
                hint2Res = noPersonalRes,
            )
            CleanXPermissionType.Overlay -> CleanXPermissionCopy(
                titleRes = titleRes,
                descriptionRes = R.string.permission_overlay_desc,
                hint1Res = R.string.permission_hint_overlay,
                hint2Res = noPersonalRes,
            )
            CleanXPermissionType.PostNotifications -> CleanXPermissionCopy(
                titleRes = titleRes,
                descriptionRes = R.string.permission_post_notifications_desc,
                hint1Res = R.string.permission_hint_app_notifications,
                hint2Res = noPersonalRes,
            )
        }
    }

    private fun storageRuntimePermissions(): Array<String> {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) return emptyArray()
        return buildList {
            add(Manifest.permission.READ_EXTERNAL_STORAGE)
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
                add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }.toTypedArray()
    }

    private fun mediaRuntimePermissions(
        android13Permission: String,
        includeMediaLocation: Boolean = false,
    ): Array<String> =
        buildList {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                add(android13Permission)
            } else {
                add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
            if (includeMediaLocation && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                add(Manifest.permission.ACCESS_MEDIA_LOCATION)
            }
        }.toTypedArray()
}
