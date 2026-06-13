package com.quickcleanpro.phonecleaner.data.repository

import android.content.Context
import android.content.Intent
import com.quickcleanpro.phonecleaner.data.source.toolbox.NetworkToolboxDataSource
import com.quickcleanpro.phonecleaner.domain.model.toolbox.NetworkScanResult
import com.quickcleanpro.phonecleaner.domain.model.toolbox.NetworkSpeedProgress
import com.quickcleanpro.phonecleaner.domain.model.toolbox.NetworkSpeedResult
import com.quickcleanpro.phonecleaner.domain.model.toolbox.NetworkUsageInfo
import com.quickcleanpro.phonecleaner.domain.repository.NetworkRepository

class NetworkRepositoryImpl(
    context: Context,
) : NetworkRepository {
    private val appContext = context.applicationContext

    override fun isNetworkAvailable(): Boolean = NetworkToolboxDataSource.isNetworkAvailable(appContext)

    override fun isWifiConnected(): Boolean = NetworkToolboxDataSource.isWifiConnected(appContext)

    override fun isMobileConnected(): Boolean = NetworkToolboxDataSource.isMobileConnected(appContext)

    override fun hasNetworkUsageAccess(): Boolean = NetworkToolboxDataSource.hasUsageAccess(appContext)

    override fun networkUsageSettingsIntent(): Intent = NetworkToolboxDataSource.usageAccessIntent()

    override suspend fun readNetworkUsage(): NetworkUsageInfo = NetworkToolboxDataSource.readUsage(appContext)

    override suspend fun runSpeedTest(): NetworkSpeedResult = NetworkToolboxDataSource.runSpeedTest(appContext)

    override suspend fun runSpeedTestWithProgress(onProgress: (NetworkSpeedProgress) -> Unit): NetworkSpeedResult =
        NetworkToolboxDataSource.runSpeedTestWithProgress(appContext, onProgress)

    override suspend fun scanWifi(): NetworkScanResult = NetworkToolboxDataSource.scanWifi(appContext)
}
