package com.quickcleanpro.phonecleaner.utils

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.provider.Settings
import com.quickcleanpro.phonecleaner.data.source.notification.QuickCleanNotificationListener
import com.quickcleanpro.phonecleaner.data.source.notification.ToolNotificationDataSource
import com.quickcleanpro.phonecleaner.data.source.notification.ToolNotificationIntentFactory

data class BlockableNotificationApp(
    val appName: String,
    val packageName: String,
)

object NotificationHelper {
    const val EXTRA_TARGET_ROUTE = ToolNotificationIntentFactory.EXTRA_TARGET_ROUTE

    private const val PREFS = "notification_blocker"
    private const val KEY_ENABLED = "enabled"
    private const val KEY_BLOCKED_COUNT = "blocked_count"
    private const val KEY_SELECTED_PACKAGES = "selected_packages"

    fun showToolNotifications(context: Context) {
        ToolNotificationDataSource.showToolNotifications(context)
    }

    fun isEnabled(context: Context): Boolean = prefs(context).getBoolean(KEY_ENABLED, false) && hasNotificationListenerAccess(context)

    fun setEnabled(
        context: Context,
        enabled: Boolean,
    ) {
        prefs(context).edit().putBoolean(KEY_ENABLED, enabled).apply()
    }

    fun blockedCount(context: Context): Int = prefs(context).getInt(KEY_BLOCKED_COUNT, 0)

    fun incrementBlocked(context: Context) {
        val p = prefs(context)
        p.edit().putInt(KEY_BLOCKED_COUNT, p.getInt(KEY_BLOCKED_COUNT, 0) + 1).apply()
    }

    fun selectedPackages(context: Context): Set<String> =
        prefs(context).getStringSet(KEY_SELECTED_PACKAGES, defaultPackages(context)) ?: emptySet()

    fun setPackageSelected(
        context: Context,
        packageName: String,
        selected: Boolean,
    ) {
        val current = selectedPackages(context).toMutableSet()
        if (selected) current += packageName else current -= packageName
        prefs(context).edit().putStringSet(KEY_SELECTED_PACKAGES, current).apply()
    }

    fun apps(context: Context): List<BlockableNotificationApp> {
        val pm = context.packageManager
        val defaults = defaultPackages(context)
        val installed =
            pm
                .getInstalledApplications(0)
                .filter { pm.getLaunchIntentForPackage(it.packageName) != null }
                .map {
                    BlockableNotificationApp(it.loadLabel(pm).toString(), it.packageName)
                }.sortedWith(
                    compareByDescending<BlockableNotificationApp> { it.packageName in defaults }
                        .thenBy { it.appName.lowercase() },
                ).take(12)
        return installed.ifEmpty {
            listOf(
                BlockableNotificationApp("Tencent Meeting", "com.tencent.wemeet.app"),
                BlockableNotificationApp("YouTube", "com.google.android.youtube"),
                BlockableNotificationApp("Spotify", "com.spotify.music"),
            )
        }
    }

    fun shouldBlock(
        context: Context,
        packageName: String,
    ): Boolean = isEnabled(context) && packageName in selectedPackages(context)

    fun hasNotificationListenerAccess(context: Context): Boolean {
        val flat = Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners")
        val component = ComponentName(context, QuickCleanNotificationListener::class.java).flattenToString()
        return flat?.split(':')?.any { it.equals(component, ignoreCase = true) } == true
    }

    fun listenerSettingsIntent(): Intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)

    private fun defaultPackages(context: Context): Set<String> =
        listOf("com.tencent.wemeet.app", "com.google.android.youtube", "com.spotify.music")
            .filter {
                it in
                    context.packageManager
                        .getInstalledApplications(0)
                        .map { app -> app.packageName }
                        .toSet()
            }.toSet()
            .ifEmpty { emptySet() }

    private fun prefs(context: Context) = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
}
