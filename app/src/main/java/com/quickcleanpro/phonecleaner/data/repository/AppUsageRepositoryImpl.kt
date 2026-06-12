package com.quickcleanpro.phonecleaner.data.repository

import android.content.Context
import android.content.Intent
import com.quickcleanpro.phonecleaner.data.source.toolbox.AppUsageDataSource
import com.quickcleanpro.phonecleaner.domain.model.toolbox.AppUsageInfo
import com.quickcleanpro.phonecleaner.domain.repository.AppUsageRepository

class AppUsageRepositoryImpl(context: Context) : AppUsageRepository {
    private val appContext = context.applicationContext

    override fun hasAppUsageAccess(): Boolean =
        AppUsageDataSource.hasUsageAccess(appContext)

    override fun resetAppUsagePermissionCache() {
        AppUsageDataSource.resetPermissionCache()
    }

    override fun appUsageSettingsIntent(): Intent =
        AppUsageDataSource.settingsIntent()

    override fun appInfoIntent(packageName: String): Intent =
        AppUsageDataSource.appInfoIntent(packageName)

    override suspend fun appUsageBetween(startMillis: Long, endMillis: Long): List<AppUsageInfo> =
        AppUsageDataSource.usageBetween(appContext, startMillis, endMillis)

    override suspend fun runningPackages(packageNames: Set<String>): Set<String> =
        AppUsageDataSource.runningPackages(appContext, packageNames)
}
