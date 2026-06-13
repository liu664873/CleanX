package com.quickcleanpro.phonecleaner.data.source.toolbox

import android.app.AppOpsManager
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Process
import android.provider.Settings
import com.quickcleanpro.phonecleaner.domain.model.toolbox.AppUsageInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar
import kotlin.math.max

object AppUsageDataSource {
    @Volatile
    private var usageAccessGranted: Boolean? = null

    fun hasUsageAccess(context: Context): Boolean {
        usageAccessGranted?.let { return it }
        val mode =
            try {
                val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
                appOps.checkOpNoThrow(
                    AppOpsManager.OPSTR_GET_USAGE_STATS,
                    Process.myUid(),
                    context.packageName,
                )
            } catch (_: Exception) {
                AppOpsManager.MODE_ERRORED
            }
        val granted = mode == AppOpsManager.MODE_ALLOWED
        usageAccessGranted = granted
        return granted
    }

    fun resetPermissionCache() {
        usageAccessGranted = null
    }

    fun settingsIntent(): Intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)

    fun appInfoIntent(packageName: String): Intent =
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", packageName, null)
        }

    fun openAppInfo(
        context: Context,
        packageName: String,
    ): Boolean =
        runCatching {
            context.startActivity(appInfoIntent(packageName))
            true
        }.getOrDefault(false)

    fun stopApp(
        context: Context,
        packageName: String,
    ): Boolean = if (packageName == context.packageName) false else openAppInfo(context, packageName)

    fun isAppRunning(
        context: Context,
        packageName: String,
    ): Boolean {
        if (packageName == context.packageName) return true
        return runningPackages(context, setOf(packageName)).contains(packageName)
    }

    fun runningPackages(
        context: Context,
        packageNames: Set<String>,
    ): Set<String> {
        if (packageNames.isEmpty() || !hasUsageAccess(context)) return emptySet()

        val manager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val now = System.currentTimeMillis()
        val events =
            runCatching {
                manager.queryEvents((now - USAGE_STATE_LOOKBACK_MILLIS).coerceAtLeast(0L), now)
            }.getOrNull() ?: return emptySet()
        val event = UsageEvents.Event()
        val running = mutableSetOf<String>()

        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            val packageName = event.packageName ?: continue
            if (packageName !in packageNames) continue
            when (event.eventType) {
                UsageEvents.Event.MOVE_TO_FOREGROUND,
                UsageEvents.Event.ACTIVITY_RESUMED,
                -> running.add(packageName)
                UsageEvents.Event.MOVE_TO_BACKGROUND,
                UsageEvents.Event.ACTIVITY_PAUSED,
                UsageEvents.Event.ACTIVITY_STOPPED,
                -> running.remove(packageName)
            }
        }
        return running
    }

    fun formatDuration(durationMs: Long): String {
        val minutes = (durationMs / 60000L).coerceAtLeast(0L)
        if (minutes < 60L) return "${minutes}m"

        val hours = minutes / 60L
        val remainingMinutes = minutes % 60L
        return if (remainingMinutes == 0L) {
            "${hours}h"
        } else {
            "${hours}h ${remainingMinutes}m"
        }
    }

    suspend fun todayUsage(context: Context): List<AppUsageInfo> {
        val startOfDay =
            Calendar
                .getInstance()
                .apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis
        return usageBetween(context, startOfDay, System.currentTimeMillis())
    }

    suspend fun usageBetween(
        context: Context,
        startMillis: Long,
        endMillis: Long,
    ): List<AppUsageInfo> =
        withContext(Dispatchers.IO) {
            if (!hasUsageAccess(context) || endMillis <= startMillis) return@withContext emptyList()

            val manager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
            val packageManager = context.packageManager
            val durationByPackage = mutableMapOf<String, Long>()
            val launchCounts = mutableMapOf<String, Int>()
            val foregroundStartByPackage = mutableMapOf<String, Long>()
            val queryStartMillis = (startMillis - USAGE_STATE_LOOKBACK_MILLIS).coerceAtLeast(0L)
            val events =
                runCatching { manager.queryEvents(queryStartMillis, endMillis) }.getOrNull()
                    ?: return@withContext emptyList()
            val event = UsageEvents.Event()

            while (events.hasNextEvent()) {
                events.getNextEvent(event)
                val packageName = event.packageName ?: continue
                when (event.eventType) {
                    UsageEvents.Event.MOVE_TO_FOREGROUND -> {
                        foregroundStartByPackage[packageName] = event.timeStamp.coerceAtLeast(startMillis)
                        if (event.timeStamp >= startMillis) {
                            launchCounts[packageName] = (launchCounts[packageName] ?: 0) + 1
                        }
                    }

                    UsageEvents.Event.MOVE_TO_BACKGROUND -> {
                        val start = foregroundStartByPackage.remove(packageName) ?: continue
                        val end = event.timeStamp.coerceAtMost(endMillis)
                        if (end > start) {
                            durationByPackage[packageName] = (durationByPackage[packageName] ?: 0L) + (end - start)
                        }
                    }
                }
            }

            foregroundStartByPackage.forEach { (packageName, startTime) ->
                val clippedStart = max(startTime, startMillis)
                if (endMillis > clippedStart) {
                    durationByPackage[packageName] = (durationByPackage[packageName] ?: 0L) + (endMillis - clippedStart)
                }
            }

            durationByPackage
                .map { (packageName, totalForegroundMs) ->
                    AppUsageInfo(
                        packageName = packageName,
                        appName = appLabel(packageManager, packageName),
                        totalForegroundMs = totalForegroundMs,
                        launchCount = launchCounts[packageName] ?: 0,
                    )
                }.filter { it.totalForegroundMs > 0L || it.launchCount > 0 }
                .sortedWith(
                    compareByDescending<AppUsageInfo> { it.totalForegroundMs }
                        .thenByDescending { it.launchCount }
                        .thenBy { it.appName.lowercase() },
                )
        }

    private fun appLabel(
        packageManager: PackageManager,
        packageName: String,
    ): String =
        runCatching {
            packageManager
                .getApplicationLabel(
                    packageManager.getApplicationInfo(packageName, 0),
                ).toString()
        }.getOrDefault(packageName.substringAfterLast('.'))

    private const val USAGE_STATE_LOOKBACK_MILLIS = 31L * 24L * 60L * 60L * 1000L
}
