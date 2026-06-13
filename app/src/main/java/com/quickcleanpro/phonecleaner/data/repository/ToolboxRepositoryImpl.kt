package com.quickcleanpro.phonecleaner.data.repository

import android.content.Intent
import com.quickcleanpro.phonecleaner.domain.model.notification.BlockableNotificationApp
import com.quickcleanpro.phonecleaner.domain.model.toolbox.AppUsageInfo
import com.quickcleanpro.phonecleaner.domain.model.toolbox.NetworkScanResult
import com.quickcleanpro.phonecleaner.domain.model.toolbox.NetworkSpeedProgress
import com.quickcleanpro.phonecleaner.domain.model.toolbox.NetworkSpeedResult
import com.quickcleanpro.phonecleaner.domain.model.toolbox.NetworkUsageInfo
import com.quickcleanpro.phonecleaner.domain.repository.AppUsageRepository
import com.quickcleanpro.phonecleaner.domain.repository.NetworkRepository
import com.quickcleanpro.phonecleaner.domain.repository.NotificationRepository
import com.quickcleanpro.phonecleaner.domain.repository.ToolboxRepository

class ToolboxRepositoryImpl(
    private val appUsageRepository: AppUsageRepository,
    private val networkRepository: NetworkRepository,
    private val notificationRepository: NotificationRepository,
) : ToolboxRepository {
    override fun hasAppUsageAccess(): Boolean = appUsageRepository.hasAppUsageAccess()

    override fun resetAppUsagePermissionCache() {
        appUsageRepository.resetAppUsagePermissionCache()
    }

    override fun appUsageSettingsIntent(): Intent = appUsageRepository.appUsageSettingsIntent()

    override fun appInfoIntent(packageName: String): Intent = appUsageRepository.appInfoIntent(packageName)

    override suspend fun appUsageBetween(
        startMillis: Long,
        endMillis: Long,
    ): List<AppUsageInfo> = appUsageRepository.appUsageBetween(startMillis, endMillis)

    override suspend fun runningPackages(packageNames: Set<String>): Set<String> = appUsageRepository.runningPackages(packageNames)

    override fun isNetworkAvailable(): Boolean = networkRepository.isNetworkAvailable()

    override fun isWifiConnected(): Boolean = networkRepository.isWifiConnected()

    override fun isMobileConnected(): Boolean = networkRepository.isMobileConnected()

    override fun hasNetworkUsageAccess(): Boolean = networkRepository.hasNetworkUsageAccess()

    override fun networkUsageSettingsIntent(): Intent = networkRepository.networkUsageSettingsIntent()

    override suspend fun readNetworkUsage(): NetworkUsageInfo = networkRepository.readNetworkUsage()

    override suspend fun runSpeedTest(): NetworkSpeedResult = networkRepository.runSpeedTest()

    override suspend fun runSpeedTestWithProgress(onProgress: (NetworkSpeedProgress) -> Unit): NetworkSpeedResult =
        networkRepository.runSpeedTestWithProgress(onProgress)

    override suspend fun scanWifi(): NetworkScanResult = networkRepository.scanWifi()

    override fun hasNotificationListenerAccess(): Boolean = notificationRepository.hasNotificationListenerAccess()

    override fun isNotificationBlockingEnabled(): Boolean = notificationRepository.isNotificationBlockingEnabled()

    override fun setNotificationBlockingEnabled(enabled: Boolean) {
        notificationRepository.setNotificationBlockingEnabled(enabled)
    }

    override fun blockedNotificationCount(): Int = notificationRepository.blockedNotificationCount()

    override fun blockedNotificationCountsByPackage(): Map<String, Int> = notificationRepository.blockedNotificationCountsByPackage()

    override fun selectedNotificationPackages(): Set<String> = notificationRepository.selectedNotificationPackages()

    override fun notificationApps(): List<BlockableNotificationApp> = notificationRepository.notificationApps()

    override fun setNotificationPackageSelected(
        packageName: String,
        selected: Boolean,
    ) {
        notificationRepository.setNotificationPackageSelected(packageName, selected)
    }

    override fun notificationListenerSettingsIntent(): Intent = notificationRepository.notificationListenerSettingsIntent()

    override fun appNotificationSettingsIntent(packageName: String): Intent =
        notificationRepository.appNotificationSettingsIntent(packageName)

    override fun appDetailsSettingsIntent(packageName: String): Intent = notificationRepository.appDetailsSettingsIntent(packageName)
}
