package com.quickcleanpro.phonecleaner.data.source.notification

import android.content.Intent

internal object NotificationServicePolicy {
    const val GLOBAL_TRIGGER_INTERVAL_MS = 30L * 60L * 1000L
    const val MAX_TRIGGERED_NOTIFICATIONS_PER_DAY = 8
    const val ACTION_PACKAGE_ADDED = Intent.ACTION_PACKAGE_ADDED
    const val ACTION_PACKAGE_REMOVED = Intent.ACTION_PACKAGE_REMOVED

    fun shouldPublishTriggeredNotification(
        nowMillis: Long,
        windowStartMillis: Long,
        windowCount: Int,
        lastGlobalMillis: Long,
        lastSceneMillis: Long,
        triggerIntervalMillis: Long,
        pushWindowMillis: Long = 24L * 60L * 60L * 1000L,
        maxPerWindow: Int = MAX_TRIGGERED_NOTIFICATIONS_PER_DAY,
        globalIntervalMillis: Long = GLOBAL_TRIGGER_INTERVAL_MS,
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
