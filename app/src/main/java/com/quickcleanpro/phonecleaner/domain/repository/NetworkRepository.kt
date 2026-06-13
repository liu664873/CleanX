package com.quickcleanpro.phonecleaner.domain.repository

import android.content.Intent
import com.quickcleanpro.phonecleaner.domain.model.toolbox.NetworkScanResult
import com.quickcleanpro.phonecleaner.domain.model.toolbox.NetworkSpeedProgress
import com.quickcleanpro.phonecleaner.domain.model.toolbox.NetworkSpeedResult
import com.quickcleanpro.phonecleaner.domain.model.toolbox.NetworkUsageInfo

interface NetworkRepository {
    fun isNetworkAvailable(): Boolean

    fun isWifiConnected(): Boolean

    fun isMobileConnected(): Boolean

    fun hasNetworkUsageAccess(): Boolean

    fun networkUsageSettingsIntent(): Intent

    suspend fun readNetworkUsage(): NetworkUsageInfo

    suspend fun runSpeedTest(): NetworkSpeedResult

    suspend fun runSpeedTestWithProgress(onProgress: (NetworkSpeedProgress) -> Unit): NetworkSpeedResult

    suspend fun scanWifi(): NetworkScanResult
}
