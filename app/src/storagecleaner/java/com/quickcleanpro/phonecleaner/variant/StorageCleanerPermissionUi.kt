package com.quickcleanpro.phonecleaner.variant

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import com.quickcleanpro.phonecleaner.R
import com.quickcleanpro.phonecleaner.core.permission.CommonPermission
import com.quickcleanpro.phonecleaner.presentation.common.components.popups.AppLockOverlayPermissionDialog
import com.quickcleanpro.phonecleaner.presentation.common.components.popups.AppLockUsageAccessPermissionDialog
import com.quickcleanpro.phonecleaner.presentation.common.components.popups.CleanXPermissionCopy
import com.quickcleanpro.phonecleaner.presentation.common.components.popups.CleanXPermissionRequiredDialog
import com.quickcleanpro.phonecleaner.presentation.common.components.popups.InlinePermissionOverlay
import com.quickcleanpro.phonecleaner.presentation.common.permission.CleanXPermissionItem
import com.quickcleanpro.phonecleaner.presentation.common.permission.CleanXProtectedAction
import com.quickcleanpro.phonecleaner.presentation.common.permission.PermissionRequestTarget

object StorageCleanerPermissionUi : VariantPermissionUi {
    @Composable
    override fun PermissionPrompt(
        request: PermissionPromptRequest,
        onSubmit: () -> Unit,
        onDismiss: () -> Unit,
    ) {
        when (request.missingPermission?.key) {
            CommonPermission.UsageAccess.key -> {
                AppLockUsageAccessPermissionDialog(
                    onManagePermission = onSubmit,
                    onDismissToHome = onDismiss,
                )
            }
            CommonPermission.Overlay.key -> {
                AppLockOverlayPermissionDialog(
                    onAllowNow = onSubmit,
                    onCancel = onDismiss,
                )
            }
            else -> {
                BackHandler { onDismiss() }
                InlinePermissionOverlay(onDismiss = onDismiss) {
                    CleanXPermissionRequiredDialog(
                        copy = request.copyForStorageCleaner(),
                        onSubmit = onSubmit,
                        onCancel = onDismiss,
                    )
                }
            }
        }
    }
}

private fun PermissionPromptRequest.copyForStorageCleaner(): CleanXPermissionCopy =
    when (val requestTarget = target) {
        is PermissionRequestTarget.Action ->
            copyFor(requestTarget.action, missingPermission?.key)
        is PermissionRequestTarget.Item ->
            copyFor(requestTarget.item, missingPermission?.key)
    }

private fun copyFor(
    item: CleanXPermissionItem,
    missingPermissionKey: String?,
): CleanXPermissionCopy {
    if (missingPermissionKey == CommonPermission.Overlay.key) {
        return overlayCopy()
    }
    if (missingPermissionKey == CommonPermission.PostNotifications.key) {
        return postNotificationsCopy()
    }
    val titleRes = R.string.permission_title_required
    val noPersonalRes = R.string.permission_hint_no_personal
    return when (item) {
        CleanXPermissionItem.StorageFiles -> CleanXPermissionCopy(
            titleRes = titleRes,
            descriptionRes = R.string.permission_storage_files_desc,
            hint1Res = R.string.permission_hint_files_safe,
            hint2Res = noPersonalRes,
        )
        CleanXPermissionItem.Location -> CleanXPermissionCopy(
            titleRes = titleRes,
            descriptionRes = R.string.permission_location_desc,
            hint1Res = R.string.permission_hint_network_scan,
            hint2Res = noPersonalRes,
        )
        CleanXPermissionItem.UsageAccess -> CleanXPermissionCopy(
            titleRes = titleRes,
            descriptionRes = R.string.permission_usage_desc,
            hint1Res = R.string.permission_hint_usage_read,
            hint2Res = noPersonalRes,
        )
        CleanXPermissionItem.NotificationListener -> CleanXPermissionCopy(
            titleRes = titleRes,
            descriptionRes = R.string.permission_notification_desc,
            hint1Res = R.string.permission_hint_notifications,
            hint2Res = noPersonalRes,
        )
        CleanXPermissionItem.Overlay -> overlayCopy()
        CleanXPermissionItem.PostNotifications -> postNotificationsCopy()
    }
}

private fun copyFor(
    action: CleanXProtectedAction,
    missingPermissionKey: String?,
): CleanXPermissionCopy {
    if (missingPermissionKey == CommonPermission.Overlay.key) {
        return overlayCopy()
    }
    if (missingPermissionKey == CommonPermission.PostNotifications.key) {
        return postNotificationsCopy()
    }
    val titleRes = R.string.permission_title_required
    val noPersonalRes = R.string.permission_hint_no_personal
    return when (action) {
        CleanXProtectedAction.JunkStartScan,
        CleanXProtectedAction.JunkCleanSelected,
        -> CleanXPermissionCopy(
            titleRes = titleRes,
            descriptionRes = R.string.permission_storage_desc,
            hint1Res = R.string.permission_hint_junk_deleted,
            hint2Res = noPersonalRes,
        )
        CleanXProtectedAction.VirusDeepScanStart -> CleanXPermissionCopy(
            titleRes = titleRes,
            descriptionRes = R.string.permission_virus_storage_desc,
            hint1Res = R.string.permission_hint_threat_files,
            hint2Res = noPersonalRes,
        )
        CleanXProtectedAction.WhatsAppStartScan,
        CleanXProtectedAction.WhatsAppCleanSelected,
        -> CleanXPermissionCopy(
            titleRes = titleRes,
            descriptionRes = R.string.permission_whatsapp_storage_desc,
            hint1Res = R.string.permission_hint_whatsapp_files,
            hint2Res = noPersonalRes,
        )
        CleanXProtectedAction.NetworkUsageLoadStats -> CleanXPermissionCopy(
            titleRes = titleRes,
            descriptionRes = R.string.permission_network_usage_desc,
            hint1Res = R.string.permission_hint_usage_read,
            hint2Res = noPersonalRes,
        )
        CleanXProtectedAction.AppLockOpenProtectedArea,
        CleanXProtectedAction.AppLockEnableMonitoring,
        -> CleanXPermissionCopy(
            titleRes = titleRes,
            descriptionRes = R.string.permission_app_lock_usage_desc,
            hint1Res = R.string.permission_hint_usage_read,
            hint2Res = noPersonalRes,
        )
        else -> copyFor(itemForAction(action), missingPermissionKey)
    }
}

private fun itemForAction(action: CleanXProtectedAction): CleanXPermissionItem =
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
