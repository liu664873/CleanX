package com.quickcleanpro.phonecleaner.data.source.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.quickcleanpro.phonecleaner.R
import com.quickcleanpro.phonecleaner.config.AppConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Manages triggered (non-persistent) notification delivery.
 *
 * Listens for system events (screen-on, power, package changes) and publishes
 * tool notifications subject to rate limiting defined in [AppConfig].
 */
internal class TriggeredNotificationEngine(
    private val context: Context,
    private val serviceScope: CoroutineScope,
    private val appInForeground: AtomicBoolean,
    private val timingPrefs: SharedPreferences,
    private val onPackageEvent: (PackageSyncAction, String) -> Unit = { _, _ -> },
) {
    private var systemEventReceiver: BroadcastReceiver? = null
    private var packageEventReceiver: BroadcastReceiver? = null

    // ---------- start / stop ----------

    fun start() {
        registerSystemEventReceiver()
    }

    fun stop() {
        runCatching { context.unregisterReceiver(systemEventReceiver) }
        runCatching { context.unregisterReceiver(packageEventReceiver) }
    }

    // ---------- inbound trigger from external callers ----------

    fun triggerAppBackground() {
        handleTrigger(NotificationTrigger.AppBackground)
    }

    // ---------- receiver registration ----------

    private fun registerSystemEventReceiver() {
        systemEventReceiver = createSystemEventReceiver().also { receiver ->
            val filter = IntentFilter().apply {
                addAction(Intent.ACTION_SCREEN_ON)
                addAction(Intent.ACTION_USER_PRESENT)
                addAction(Intent.ACTION_POWER_CONNECTED)
                addAction(Intent.ACTION_POWER_DISCONNECTED)
            }
            ContextCompat.registerReceiver(context, receiver, filter, ContextCompat.RECEIVER_EXPORTED)
        }
        packageEventReceiver = createPackageEventReceiver().also { receiver ->
            val filter = IntentFilter().apply {
                addAction(Intent.ACTION_PACKAGE_ADDED)
                addAction(Intent.ACTION_PACKAGE_REMOVED)
                addDataScheme("package")
            }
            ContextCompat.registerReceiver(context, receiver, filter, ContextCompat.RECEIVER_EXPORTED)
        }
    }

    private fun createSystemEventReceiver(): BroadcastReceiver =
        object : BroadcastReceiver() {
            override fun onReceive(c: Context?, intent: Intent?) {
                val trigger = when (intent?.action) {
                    Intent.ACTION_SCREEN_ON -> NotificationTrigger.ScreenOn
                    Intent.ACTION_USER_PRESENT -> NotificationTrigger.UserPresent
                    Intent.ACTION_POWER_CONNECTED -> NotificationTrigger.PowerConnected
                    Intent.ACTION_POWER_DISCONNECTED -> NotificationTrigger.PowerDisconnected
                    else -> null
                } ?: return
                handleTrigger(trigger)
            }
        }

    private fun createPackageEventReceiver(): BroadcastReceiver =
        object : BroadcastReceiver() {
            override fun onReceive(c: Context?, intent: Intent?) {
                val trigger = when (intent?.action) {
                    Intent.ACTION_PACKAGE_ADDED -> NotificationTrigger.PackageAdded
                    Intent.ACTION_PACKAGE_REMOVED -> NotificationTrigger.PackageRemoved
                    else -> null
                } ?: return
                val pkg = intent?.data?.schemeSpecificPart.orEmpty()
                if (intent?.getBooleanExtra(Intent.EXTRA_REPLACING, false) == true) return
                NotificationServicePolicy.packageSyncAction(intent?.action, pkg)?.let { action ->
                    onPackageEvent(action, pkg)
                }
                handleTrigger(trigger)
            }
        }

    // ---------- trigger processing ----------

    private fun handleTrigger(trigger: NotificationTrigger) {
        if (!canSendTriggeredNotification(trigger)) return
        serviceScope.launch {
            if (trigger.delayMs > 0L) delay(trigger.delayMs)
            if (appInForeground.get() || !AppConfig.hasPostNotificationsPermission(context)) return@launch
            publishToolNotification(trigger)
        }
    }

    private fun canSendTriggeredNotification(trigger: NotificationTrigger): Boolean {
        if (appInForeground.get() || !AppConfig.hasPostNotificationsPermission(context)) return false
        val now = System.currentTimeMillis()
        val windowStart = timingPrefs.getLong(KEY_PUSH_WINDOW_START, 0L)
        val count =
            if (windowStart == 0L || now - windowStart >= AppConfig.PUSH_WINDOW_MS) {
                timingPrefs.edit()
                    .putLong(KEY_PUSH_WINDOW_START, now)
                    .putInt(KEY_PUSH_WINDOW_COUNT, 0)
                    .apply()
                0
            } else {
                timingPrefs.getInt(KEY_PUSH_WINDOW_COUNT, 0)
            }
        val sceneKey = "${KEY_LAST_TRIGGER_PREFIX}${trigger.key}"
        if (!NotificationServicePolicy.shouldPublishTriggeredNotification(
                nowMillis = now,
                windowStartMillis = if (windowStart == 0L || now - windowStart >= AppConfig.PUSH_WINDOW_MS) now else windowStart,
                windowCount = count,
                lastGlobalMillis = timingPrefs.getLong(KEY_LAST_TRIGGERED_NOTIFICATION, 0L),
                lastSceneMillis = timingPrefs.getLong(sceneKey, 0L),
                triggerIntervalMillis = trigger.intervalMs,
            )) {
            return false
        }
        timingPrefs.edit()
            .putLong(KEY_LAST_TRIGGERED_NOTIFICATION, now)
            .putLong(sceneKey, now)
            .putInt(KEY_PUSH_WINDOW_COUNT, count + 1)
            .apply()
        return true
    }

    private fun publishToolNotification(trigger: NotificationTrigger) {
        if (!AppConfig.hasPostNotificationsPermission(context)) return
        val manager = NotificationManagerCompat.from(context)
        val index = notificationIndexFor(trigger)
        val item = ToolNotificationSpecs.getOrNull(index) ?: return
        val notification = ToolNotificationDataSource.buildToolNotification(context, item, index)
        try {
            manager.notify(TOOL_NOTIFICATION_BASE_ID + index, notification)
        } catch (_: SecurityException) {
            return
        } catch (_: Exception) {
            return
        }
    }

    private fun notificationIndexFor(trigger: NotificationTrigger): Int {
        val preferredTitle = when (trigger) {
            NotificationTrigger.PowerConnected, NotificationTrigger.PowerDisconnected -> R.string.battery_info
            NotificationTrigger.PackageAdded, NotificationTrigger.PackageRemoved -> R.string.junk_removal
            NotificationTrigger.ScreenOn, NotificationTrigger.UserPresent, NotificationTrigger.AppBackground -> null
        }
        val preferredIndex = preferredTitle
            ?.let { res -> ToolNotificationSpecs.indexOfFirst { it.titleRes == res } }
            ?: -1
        if (preferredIndex >= 0) return preferredIndex
        val nextIndex = (timingPrefs.getInt(KEY_NEXT_TOOL_INDEX, -1) + 1)
            .floorMod(ToolNotificationSpecs.size.coerceAtLeast(1))
        timingPrefs.edit().putInt(KEY_NEXT_TOOL_INDEX, nextIndex).apply()
        return nextIndex
    }

    // ---------- shared keys ----------

    companion object {
        const val NOTIFICATION_TIMING_PREFS = "triggered_notification_timing"
        const val TOOL_NOTIFICATION_BASE_ID = 3000
        internal const val KEY_PUSH_WINDOW_START = "push_window_start"
        internal const val KEY_PUSH_WINDOW_COUNT = "push_window_count"
        internal const val KEY_LAST_TRIGGERED_NOTIFICATION = "last_triggered_notification"
        internal const val KEY_LAST_TRIGGER_PREFIX = "last_trigger_"
        internal const val KEY_NEXT_TOOL_INDEX = "next_tool_index"
    }

    enum class NotificationTrigger(
        val key: String,
        val intervalMs: Long,
        val delayMs: Long = 0L,
    ) {
        ScreenOn("screen_on", AppConfig.DEFAULT_TRIGGER_INTERVAL_MS, AppConfig.SCREEN_ON_TRIGGER_DELAY_MS),
        UserPresent("user_present", AppConfig.DEFAULT_TRIGGER_INTERVAL_MS),
        AppBackground("app_background", AppConfig.BACKGROUND_TRIGGER_INTERVAL_MS),
        PowerConnected("power_connected", AppConfig.POWER_TRIGGER_INTERVAL_MS),
        PowerDisconnected("power_disconnected", AppConfig.POWER_TRIGGER_INTERVAL_MS),
        PackageAdded("package_added", AppConfig.DEFAULT_TRIGGER_INTERVAL_MS),
        PackageRemoved("package_removed", AppConfig.DEFAULT_TRIGGER_INTERVAL_MS),
    }
}

private fun Int.floorMod(other: Int): Int = ((this % other) + other) % other
