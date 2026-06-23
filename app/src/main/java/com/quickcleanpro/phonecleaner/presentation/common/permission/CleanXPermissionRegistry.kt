package com.quickcleanpro.phonecleaner.presentation.common.permission

import android.content.Context
import com.quickcleanpro.phonecleaner.R
import com.quickcleanpro.phonecleaner.core.permission.AppPermission
import com.quickcleanpro.phonecleaner.core.permission.CommonPermission
import com.quickcleanpro.phonecleaner.core.permission.PermissionFeature
import com.quickcleanpro.phonecleaner.core.permission.PermissionManager
import com.quickcleanpro.phonecleaner.core.permission.PermissionSpec
import com.quickcleanpro.phonecleaner.core.permission.RuntimePermissionDenialStore
import com.quickcleanpro.phonecleaner.core.permission.commonPermissionHandlers
import com.quickcleanpro.phonecleaner.data.local.hasDeniedLocationRuntimePermission
import com.quickcleanpro.phonecleaner.data.local.hasDeniedNotificationRuntimePermission
import com.quickcleanpro.phonecleaner.data.local.saveLocationRuntimePermissionDenied
import com.quickcleanpro.phonecleaner.data.local.saveNotificationRuntimePermissionDenied

enum class CleanXFeature(
    override val key: String,
) : PermissionFeature {
    General("general"),
    JunkRemoval("junk_removal"),
    FileManager("file_manager"),
    Photos("photos"),
    Videos("videos"),
    Audios("audios"),
    NetworkScan("network_scan"),
    AppUsage("app_usage"),
    NetworkUsage("network_usage"),
    NotificationCleaner("notification_cleaner"),
    NotificationBar("notification_bar"),
    AppLock("app_lock"),
    Overlay("overlay"),
    VirusDeepScan("virus_deep_scan"),
    WhatsAppCleaner("whatsapp_cleaner"),
    PostNotifications("post_notifications"),
}

data class CleanXPermissionManageItem(
    val feature: CleanXFeature,
    val labelRes: Int,
)

object CleanXPermissionRegistry {
    val specs: List<PermissionSpec<CleanXFeature>> =
        listOf(
            PermissionSpec(CleanXFeature.General, listOf(CommonPermission.StorageFiles)),
            PermissionSpec(CleanXFeature.JunkRemoval, listOf(CommonPermission.StorageFiles)),
            PermissionSpec(CleanXFeature.FileManager, listOf(CommonPermission.StorageFiles)),
            PermissionSpec(CleanXFeature.Photos, listOf(CommonPermission.StorageFiles)),
            PermissionSpec(CleanXFeature.Videos, listOf(CommonPermission.StorageFiles)),
            PermissionSpec(CleanXFeature.Audios, listOf(CommonPermission.StorageFiles)),
            PermissionSpec(CleanXFeature.NetworkScan, listOf(CommonPermission.Location)),
            PermissionSpec(CleanXFeature.AppUsage, listOf(CommonPermission.UsageAccess)),
            PermissionSpec(CleanXFeature.NetworkUsage, listOf(CommonPermission.UsageAccess)),
            PermissionSpec(CleanXFeature.NotificationCleaner, listOf(CommonPermission.NotificationListener)),
            PermissionSpec(CleanXFeature.NotificationBar, listOf(CommonPermission.NotificationListener)),
            PermissionSpec(CleanXFeature.AppLock, listOf(CommonPermission.UsageAccess, CommonPermission.Overlay)),
            PermissionSpec(CleanXFeature.Overlay, listOf(CommonPermission.Overlay)),
            PermissionSpec(CleanXFeature.VirusDeepScan, listOf(CommonPermission.StorageFiles)),
            PermissionSpec(CleanXFeature.WhatsAppCleaner, listOf(CommonPermission.StorageFiles)),
            PermissionSpec(CleanXFeature.PostNotifications, listOf(CommonPermission.PostNotifications)),
        )

    val manageItems: List<CleanXPermissionManageItem> =
        listOf(
            CleanXPermissionManageItem(CleanXFeature.FileManager, R.string.settings_storage_permission),
            CleanXPermissionManageItem(CleanXFeature.AppUsage, R.string.settings_usage_data_permission),
            CleanXPermissionManageItem(CleanXFeature.NetworkScan, R.string.settings_location_permission),
            CleanXPermissionManageItem(CleanXFeature.NotificationBar, R.string.settings_notification_permission),
            CleanXPermissionManageItem(CleanXFeature.Overlay, R.string.settings_overlay_permission),
            CleanXPermissionManageItem(CleanXFeature.PostNotifications, R.string.settings_post_notifications_permission),
        )

    fun specOf(feature: CleanXFeature): PermissionSpec<CleanXFeature> =
        specs.first { it.feature == feature }

    fun copyFor(
        feature: CleanXFeature,
        missingPermission: AppPermission? = null,
    ): CleanXPermissionCopy {
        if (missingPermission?.key == CommonPermission.Overlay.key) {
            return overlayCopy()
        }
        if (missingPermission?.key == CommonPermission.PostNotifications.key) {
            return postNotificationsCopy()
        }
        val titleRes = R.string.permission_title_required
        val noPersonalRes = R.string.permission_hint_no_personal
        return when (feature) {
            CleanXFeature.JunkRemoval -> CleanXPermissionCopy(
                titleRes = titleRes,
                descriptionRes = R.string.permission_storage_desc,
                hint1Res = R.string.permission_hint_junk_deleted,
                hint2Res = noPersonalRes,
            )
            CleanXFeature.VirusDeepScan -> CleanXPermissionCopy(
                titleRes = titleRes,
                descriptionRes = R.string.permission_virus_storage_desc,
                hint1Res = R.string.permission_hint_threat_files,
                hint2Res = noPersonalRes,
            )
            CleanXFeature.WhatsAppCleaner -> CleanXPermissionCopy(
                titleRes = titleRes,
                descriptionRes = R.string.permission_whatsapp_storage_desc,
                hint1Res = R.string.permission_hint_whatsapp_files,
                hint2Res = noPersonalRes,
            )
            CleanXFeature.Photos -> mediaImagesCopy(titleRes, noPersonalRes)
            CleanXFeature.Videos -> CleanXPermissionCopy(
                titleRes = titleRes,
                descriptionRes = R.string.permission_media_video_desc,
                hint1Res = R.string.permission_hint_files_safe,
                hint2Res = noPersonalRes,
            )
            CleanXFeature.Audios -> CleanXPermissionCopy(
                titleRes = titleRes,
                descriptionRes = R.string.permission_media_audio_desc,
                hint1Res = R.string.permission_hint_files_safe,
                hint2Res = noPersonalRes,
            )
            CleanXFeature.NetworkScan -> CleanXPermissionCopy(
                titleRes = titleRes,
                descriptionRes = R.string.permission_location_desc,
                hint1Res = R.string.permission_hint_network_scan,
                hint2Res = noPersonalRes,
            )
            CleanXFeature.NetworkUsage -> CleanXPermissionCopy(
                titleRes = titleRes,
                descriptionRes = R.string.permission_network_usage_desc,
                hint1Res = R.string.permission_hint_usage_read,
                hint2Res = noPersonalRes,
            )
            CleanXFeature.AppLock -> CleanXPermissionCopy(
                titleRes = titleRes,
                descriptionRes = R.string.permission_app_lock_usage_desc,
                hint1Res = R.string.permission_hint_usage_read,
                hint2Res = noPersonalRes,
            )
            CleanXFeature.NotificationCleaner,
            CleanXFeature.NotificationBar,
            -> CleanXPermissionCopy(
                titleRes = titleRes,
                descriptionRes = R.string.permission_notification_desc,
                hint1Res = R.string.permission_hint_notifications,
                hint2Res = noPersonalRes,
            )
            CleanXFeature.PostNotifications -> CleanXPermissionCopy(
                titleRes = titleRes,
                descriptionRes = R.string.permission_post_notifications_desc,
                hint1Res = R.string.permission_hint_app_notifications,
                hint2Res = noPersonalRes,
            )
            CleanXFeature.Overlay -> overlayCopy()
            CleanXFeature.General,
            CleanXFeature.FileManager,
            -> CleanXPermissionCopy(
                titleRes = titleRes,
                descriptionRes = R.string.permission_storage_files_desc,
                hint1Res = R.string.permission_hint_files_safe,
                hint2Res = noPersonalRes,
            )
            CleanXFeature.AppUsage -> CleanXPermissionCopy(
                titleRes = titleRes,
                descriptionRes = R.string.permission_usage_desc,
                hint1Res = R.string.permission_hint_usage_read,
                hint2Res = noPersonalRes,
            )
        }
    }

    fun permissionManager(context: Context): PermissionManager<CleanXFeature> =
        PermissionManager(
            specs = specs,
            handlers = commonPermissionHandlers(),
            denialStore = CleanXRuntimePermissionDenialStore(context.applicationContext),
        )

    private fun mediaImagesCopy(
        titleRes: Int,
        noPersonalRes: Int,
    ): CleanXPermissionCopy =
        CleanXPermissionCopy(
            titleRes = titleRes,
            descriptionRes = R.string.permission_media_images_desc,
            hint1Res = R.string.permission_hint_files_safe,
            hint2Res = noPersonalRes,
        )

    private fun overlayCopy(): CleanXPermissionCopy =
        CleanXPermissionCopy(
            titleRes = R.string.permission_title_required,
            descriptionRes = R.string.permission_overlay_desc,
            hint1Res = R.string.permission_hint_overlay,
            hint2Res = R.string.permission_hint_no_personal,
        )

    private fun postNotificationsCopy(): CleanXPermissionCopy =
        CleanXPermissionCopy(
            titleRes = R.string.permission_title_required,
            descriptionRes = R.string.permission_post_notifications_desc,
            hint1Res = R.string.permission_hint_app_notifications,
            hint2Res = R.string.permission_hint_no_personal,
        )
}

class CleanXRuntimePermissionDenialStore(
    private val context: Context,
) : RuntimePermissionDenialStore {
    override fun hasDenied(permission: AppPermission): Boolean =
        when (permission.key) {
            CommonPermission.Location.key -> hasDeniedLocationRuntimePermission(context)
            CommonPermission.PostNotifications.key -> hasDeniedNotificationRuntimePermission(context)
            else -> false
        }

    override fun markDenied(permission: AppPermission) {
        when (permission.key) {
            CommonPermission.Location.key -> saveLocationRuntimePermissionDenied(context)
            CommonPermission.PostNotifications.key -> saveNotificationRuntimePermissionDenied(context)
        }
    }
}
