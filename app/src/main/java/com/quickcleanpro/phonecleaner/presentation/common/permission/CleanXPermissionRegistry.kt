package com.quickcleanpro.phonecleaner.presentation.common.permission

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import com.quickcleanpro.phonecleaner.core.permission.AppPermission
import com.quickcleanpro.phonecleaner.core.permission.CommonPermission
import com.quickcleanpro.phonecleaner.core.permission.PermissionFeature
import com.quickcleanpro.phonecleaner.core.permission.PermissionManager
import com.quickcleanpro.phonecleaner.core.permission.PermissionSpec
import com.quickcleanpro.phonecleaner.core.permission.RuntimePermissionDenialStore
import com.quickcleanpro.phonecleaner.core.permission.commonPermissionHandlers
import com.quickcleanpro.phonecleaner.data.local.hasDeniedLocationRuntimePermission
import com.quickcleanpro.phonecleaner.data.local.hasDeniedNotificationRuntimePermission
import com.quickcleanpro.phonecleaner.data.local.hasRequestedLocationRuntimePermissionBefore
import com.quickcleanpro.phonecleaner.data.local.saveLocationRuntimePermissionDenied
import com.quickcleanpro.phonecleaner.data.local.saveLocationRuntimePermissionRequestedBefore
import com.quickcleanpro.phonecleaner.data.local.saveNotificationRuntimePermissionDenied

enum class CleanXProtectedAction(
    override val key: String,
) : PermissionFeature {
    JunkStartScan("junk_start_scan"),
    JunkCleanSelected("junk_clean_selected"),
    FileManagerLoadFiles("file_manager_load_files"),
    FileManagerDeleteFiles("file_manager_delete_files"),
    WhatsAppStartScan("whatsapp_start_scan"),
    WhatsAppCleanSelected("whatsapp_clean_selected"),
    VirusDeepScanStart("virus_deep_scan_start"),
    NetworkScanStart("network_scan_start"),
    AppUsageLoadStats("app_usage_load_stats"),
    NetworkUsageLoadStats("network_usage_load_stats"),
    NotificationCleanerEnable("notification_cleaner_enable"),
    NotificationBarEnable("notification_bar_enable"),
    AppLockOpenProtectedArea("app_lock_open_protected_area"),
    AppLockEnableMonitoring("app_lock_enable_monitoring"),
    AppLockRequestOverlay("app_lock_request_overlay"),
    PostNotificationsEnable("post_notifications_enable"),
}

enum class CleanXPermissionItem(
    override val key: String,
) : PermissionFeature {
    StorageFiles("storage_files"),
    Location("location"),
    UsageAccess("usage_access"),
    NotificationListener("notification_listener"),
    Overlay("overlay"),
    PostNotifications("post_notifications"),
}

object CleanXPermissionRegistry {
    val actionSpecs: List<PermissionSpec<CleanXProtectedAction>> =
        listOf(
            PermissionSpec(CleanXProtectedAction.JunkStartScan, listOf(CommonPermission.StorageFiles)),
            PermissionSpec(CleanXProtectedAction.JunkCleanSelected, listOf(CommonPermission.StorageFiles)),
            PermissionSpec(CleanXProtectedAction.FileManagerLoadFiles, listOf(CommonPermission.StorageFiles)),
            PermissionSpec(CleanXProtectedAction.FileManagerDeleteFiles, listOf(CommonPermission.StorageFiles)),
            PermissionSpec(CleanXProtectedAction.WhatsAppStartScan, listOf(CommonPermission.StorageFiles)),
            PermissionSpec(CleanXProtectedAction.WhatsAppCleanSelected, listOf(CommonPermission.StorageFiles)),
            PermissionSpec(CleanXProtectedAction.VirusDeepScanStart, listOf(CommonPermission.StorageFiles)),
            PermissionSpec(CleanXProtectedAction.NetworkScanStart, listOf(CommonPermission.Location)),
            PermissionSpec(CleanXProtectedAction.AppUsageLoadStats, listOf(CommonPermission.UsageAccess)),
            PermissionSpec(CleanXProtectedAction.NetworkUsageLoadStats, listOf(CommonPermission.UsageAccess)),
            PermissionSpec(CleanXProtectedAction.NotificationCleanerEnable, listOf(CommonPermission.NotificationListener)),
            PermissionSpec(CleanXProtectedAction.NotificationBarEnable, listOf(CommonPermission.NotificationListener)),
            PermissionSpec(CleanXProtectedAction.AppLockOpenProtectedArea, listOf(CommonPermission.UsageAccess)),
            PermissionSpec(
                CleanXProtectedAction.AppLockEnableMonitoring,
                listOf(CommonPermission.UsageAccess, CommonPermission.Overlay),
            ),
            PermissionSpec(CleanXProtectedAction.AppLockRequestOverlay, listOf(CommonPermission.Overlay)),
            PermissionSpec(CleanXProtectedAction.PostNotificationsEnable, listOf(CommonPermission.PostNotifications)),
        )

    val permissionItemSpecs: List<PermissionSpec<CleanXPermissionItem>> =
        listOf(
            PermissionSpec(CleanXPermissionItem.StorageFiles, listOf(CommonPermission.StorageFiles)),
            PermissionSpec(CleanXPermissionItem.Location, listOf(CommonPermission.Location)),
            PermissionSpec(CleanXPermissionItem.UsageAccess, listOf(CommonPermission.UsageAccess)),
            PermissionSpec(
                CleanXPermissionItem.NotificationListener,
                listOf(CommonPermission.NotificationListener),
            ),
            PermissionSpec(CleanXPermissionItem.Overlay, listOf(CommonPermission.Overlay)),
            PermissionSpec(
                CleanXPermissionItem.PostNotifications,
                listOf(CommonPermission.PostNotifications),
            ),
        )

    fun protectedActionPermissionManager(context: Context): PermissionManager<CleanXProtectedAction> =
        PermissionManager(
            specs = actionSpecs,
            handlers = commonPermissionHandlers(),
            denialStore = CleanXRuntimePermissionDenialStore(context.applicationContext),
        )

    fun permissionItemManager(context: Context): PermissionManager<CleanXPermissionItem> =
        PermissionManager(
            specs = permissionItemSpecs,
            handlers = commonPermissionHandlers(),
            denialStore = CleanXRuntimePermissionDenialStore(context.applicationContext),
        )

    fun itemForAction(action: CleanXProtectedAction): CleanXPermissionItem =
        when (action) {
            CleanXProtectedAction.JunkStartScan,
            CleanXProtectedAction.JunkCleanSelected,
            CleanXProtectedAction.FileManagerLoadFiles,
            CleanXProtectedAction.FileManagerDeleteFiles,
            CleanXProtectedAction.WhatsAppStartScan,
            CleanXProtectedAction.WhatsAppCleanSelected,
            CleanXProtectedAction.VirusDeepScanStart,
            -> CleanXPermissionItem.StorageFiles
            CleanXProtectedAction.NetworkScanStart -> CleanXPermissionItem.Location
            CleanXProtectedAction.AppUsageLoadStats,
            CleanXProtectedAction.NetworkUsageLoadStats,
            CleanXProtectedAction.AppLockOpenProtectedArea,
            CleanXProtectedAction.AppLockEnableMonitoring,
            -> CleanXPermissionItem.UsageAccess
            CleanXProtectedAction.NotificationCleanerEnable,
            CleanXProtectedAction.NotificationBarEnable,
            -> CleanXPermissionItem.NotificationListener
            CleanXProtectedAction.AppLockRequestOverlay -> CleanXPermissionItem.Overlay
            CleanXProtectedAction.PostNotificationsEnable -> CleanXPermissionItem.PostNotifications
        }
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

    override fun hasRequestedBefore(permission: AppPermission): Boolean =
        when (permission.key) {
            CommonPermission.Location.key -> hasRequestedLocationRuntimePermissionBefore(context)
            else -> hasDenied(permission)
        }

    override fun markRequested(permission: AppPermission) {
        when (permission.key) {
            CommonPermission.Location.key -> saveLocationRuntimePermissionRequestedBefore(context)
        }
    }

    override fun shouldRequestRuntimePermission(
        context: Context,
        permission: AppPermission,
        runtimePermissions: Array<String>,
    ): Boolean =
        when (permission.key) {
            CommonPermission.Location.key -> {
                if (!hasRequestedBefore(permission)) {
                    true
                } else {
                    runtimePermissions.any { runtimePermission ->
                        context.findActivity()?.shouldShowRequestPermissionRationale(runtimePermission) == true
                    }
                }
            }
            else -> super<RuntimePermissionDenialStore>.shouldRequestRuntimePermission(
                context,
                permission,
                runtimePermissions,
            )
        }

    override fun markDenied(permission: AppPermission) {
        when (permission.key) {
            CommonPermission.Location.key -> saveLocationRuntimePermissionDenied(context)
            CommonPermission.PostNotifications.key -> saveNotificationRuntimePermissionDenied(context)
        }
    }
}

private tailrec fun Context.findActivity(): Activity? =
    when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }
