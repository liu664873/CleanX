package com.quickcleanpro.phonecleaner.data.repository

import android.content.Context
import android.content.Intent
import com.quickcleanpro.phonecleaner.data.source.notification.NotificationDataSource
import com.quickcleanpro.phonecleaner.domain.model.notification.BlockableNotificationApp
import com.quickcleanpro.phonecleaner.domain.repository.NotificationRepository

class NotificationRepositoryImpl(
    context: Context,
) : NotificationRepository {
    private val appContext = context.applicationContext

    override fun hasNotificationListenerAccess(): Boolean = NotificationDataSource.hasNotificationListenerAccess(appContext)

    override fun isNotificationBlockingEnabled(): Boolean = NotificationDataSource.isEnabled(appContext)

    override fun setNotificationBlockingEnabled(enabled: Boolean) {
        NotificationDataSource.setEnabled(appContext, enabled)
    }

    override fun blockedNotificationCount(): Int = NotificationDataSource.blockedCount(appContext)

    override fun blockedNotificationCountsByPackage(): Map<String, Int> = NotificationDataSource.blockedCountsByPackage(appContext)

    override fun selectedNotificationPackages(): Set<String> = NotificationDataSource.selectedPackages(appContext)

    override fun notificationApps(): List<BlockableNotificationApp> = NotificationDataSource.apps(appContext)

    override fun setNotificationPackageSelected(
        packageName: String,
        selected: Boolean,
    ) {
        NotificationDataSource.setPackageSelected(appContext, packageName, selected)
    }

    override fun clearSelectedNotificationPackages() {
        NotificationDataSource.clearSelectedPackages(appContext)
    }

    override fun notificationListenerSettingsIntent(): Intent = NotificationDataSource.listenerSettingsIntent()

    override fun appNotificationSettingsIntent(packageName: String): Intent =
        NotificationDataSource.appNotificationSettingsIntent(packageName)

    override fun appDetailsSettingsIntent(packageName: String): Intent = NotificationDataSource.appDetailsSettingsIntent(packageName)
}
