package com.quickcleanpro.phonecleaner.data.source.notification

import android.content.Intent
import com.quickcleanpro.phonecleaner.config.AppConfig

internal object NotificationServicePolicy {
    const val ACTION_PACKAGE_ADDED = Intent.ACTION_PACKAGE_ADDED
    const val ACTION_PACKAGE_REMOVED = Intent.ACTION_PACKAGE_REMOVED

    fun shouldPublishTriggeredNotification(
        nowMillis: Long,
        windowStartMillis: Long,
        windowCount: Int,
        lastGlobalMillis: Long,
        lastSceneMillis: Long,
        triggerIntervalMillis: Long,
        pushWindowMillis: Long = AppConfig.PUSH_WINDOW_MS,
        maxPerWindow: Int = AppConfig.MAX_TRIGGERED_NOTIFICATIONS_PER_DAY,
        globalIntervalMillis: Long = AppConfig.GLOBAL_TRIGGER_INTERVAL_MS,
    ): Boolean {
        if (windowCount >= maxPerWindow && nowMillis - windowStartMillis < pushWindowMillis) return false
        if (nowMillis - lastGlobalMillis < globalIntervalMillis) return false
        if (nowMillis - lastSceneMillis < triggerIntervalMillis) return false
        return true
    }

    fun packageSyncAction(
        action: String?,
        packageName: String,
    ): PackageSyncAction? =
        when {
            packageName.isBlank() -> null
            action == ACTION_PACKAGE_ADDED -> PackageSyncAction.Added
            action == ACTION_PACKAGE_REMOVED -> PackageSyncAction.Removed
            else -> null
        }
}

internal enum class PackageSyncAction {
    Added,
    Removed,
}
