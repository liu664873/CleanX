package com.quickcleanpro.phonecleaner.data.source.notification

import android.app.usage.UsageEvents
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import com.quickcleanpro.phonecleaner.data.repository.AppLockRepositoryImpl
import com.quickcleanpro.phonecleaner.data.source.applock.LockScreenOverlayService
import com.quickcleanpro.phonecleaner.utils.AppLockManager
import com.quickcleanpro.phonecleaner.utils.AppLockPermissionUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Monitors the foreground app and shows the AppLock overlay when a locked
 * app is detected. Owns its own coroutine.
 */
internal class AppLockMonitorHandler(
    private val context: Context,
    private val repository: AppLockRepositoryImpl,
    private val scope: CoroutineScope,
) {
    private var monitorJob: Job? = null
    private var lastForegroundPackage = ""
    private var lockScreenShowing = false
    private var monitoringEnabled = false

    // ---------- public API ----------

    fun syncMonitoringState() {
        if (canMonitor()) enableMonitoring() else disableMonitoring()
    }

    fun enableMonitoring(): Boolean {
        if (!canMonitor()) {
            disableMonitoring()
            return false
        }
        if (monitoringEnabled) return true
        monitoringEnabled = true
        startMonitoring()
        return true
    }

    fun disableMonitoring() {
        monitoringEnabled = false
        monitorJob?.cancel()
        monitorJob = null
        lastForegroundPackage = ""
        lockScreenShowing = false
    }

    fun dismissLockScreen() {
        lockScreenShowing = false
    }

    fun canMonitor(): Boolean =
        runCatching {
            repository.isPinSet() &&
                AppLockManager.isMonitoringEnabled() &&
                repository.lockedAppCount() > 0 &&
                AppLockPermissionUtils.canDrawOverlays(context) &&
                AppLockPermissionUtils.hasUsageStatsPermission(context)
        }.getOrDefault(false)

    // ---------- monitoring loop ----------

    private fun startMonitoring() {
        if (monitorJob?.isActive == true) return
        monitorJob = scope.launch {
            while (isActive && monitoringEnabled) {
                if (!canMonitor()) {
                    disableMonitoring()
                    break
                }
                checkForegroundApp()
                delay(CHECK_INTERVAL_MS)
            }
        }
    }

    private suspend fun checkForegroundApp() {
        val pkg = withContext(Dispatchers.IO) { foregroundPackage() } ?: return
        if (pkg == context.packageName || pkg == lastForegroundPackage) return
        lastForegroundPackage = pkg
        if (!lockScreenShowing && AppLockManager.isAppLocked(pkg)) {
            showLockScreen(pkg)
        }
    }

    private fun showLockScreen(packageName: String) {
        lockScreenShowing = true
        val intent = Intent(context, LockScreenOverlayService::class.java).apply {
            putExtra(LockScreenOverlayService.EXTRA_TARGET_PACKAGE, packageName)
        }
        runCatching { context.startService(intent) }
            .onFailure { lockScreenShowing = false }
    }

    private fun foregroundPackage(): String? {
        val manager = runCatching {
            context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        }.getOrNull() ?: return null
        val now = System.currentTimeMillis()
        val events = runCatching {
            manager.queryEvents((now - EVENT_LOOKBACK_MS).coerceAtLeast(0L), now)
        }.getOrNull()
        val event = UsageEvents.Event()
        var foreground: String? = null
        if (events != null) {
            while (events.hasNextEvent()) {
                events.getNextEvent(event)
                val eventPackage = event.packageName ?: continue
                when (event.eventType) {
                    UsageEvents.Event.MOVE_TO_FOREGROUND,
                    UsageEvents.Event.ACTIVITY_RESUMED,
                    -> foreground = eventPackage
                    UsageEvents.Event.MOVE_TO_BACKGROUND,
                    UsageEvents.Event.ACTIVITY_PAUSED,
                    UsageEvents.Event.ACTIVITY_STOPPED,
                    -> { if (foreground == eventPackage) foreground = null }
                }
            }
        }
        return foreground ?: foregroundPackageFromStats(manager, now)
    }

    private fun foregroundPackageFromStats(manager: UsageStatsManager, now: Long): String? =
        runCatching {
            manager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY,
                (now - STATS_LOOKBACK_MS).coerceAtLeast(0L), now)
        }.getOrNull()
            ?.maxByOrNull(UsageStats::getLastTimeUsed)
            ?.packageName

    companion object {
        private const val CHECK_INTERVAL_MS = 500L
        private const val EVENT_LOOKBACK_MS = 3_000L
        private const val STATS_LOOKBACK_MS = 10_000L
    }
}
