package com.quickcleanpro.phonecleaner.domain.repository

import android.content.Intent
import com.quickcleanpro.phonecleaner.domain.model.notification.BlockableNotificationApp

interface NotificationRepository {
    fun hasNotificationListenerAccess(): Boolean

    fun isNotificationBlockingEnabled(): Boolean

    fun setNotificationBlockingEnabled(enabled: Boolean)

    fun blockedNotificationCount(): Int

    fun blockedNotificationCountsByPackage(): Map<String, Int>

    fun selectedNotificationPackages(): Set<String>

    fun notificationApps(): List<BlockableNotificationApp>

    fun setNotificationPackageSelected(
        packageName: String,
        selected: Boolean,
    )

    fun clearSelectedNotificationPackages()

    fun notificationListenerSettingsIntent(): Intent

    fun appNotificationSettingsIntent(packageName: String): Intent

    fun appDetailsSettingsIntent(packageName: String): Intent
}
