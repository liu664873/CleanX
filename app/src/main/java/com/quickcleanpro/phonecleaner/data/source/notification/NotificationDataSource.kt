package com.quickcleanpro.phonecleaner.data.source.notification

import android.app.AppOpsManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.quickcleanpro.phonecleaner.domain.model.notification.BlockableNotificationApp

/** Android notification blocker state and system intents. */
object NotificationDataSource {
    private const val PREFS = "notification_blocker"
    private const val KEY_ENABLED = "enabled"
    private const val KEY_BLOCKED_COUNT = "blocked_count"
    private const val KEY_BLOCKED_COUNTS_BY_PACKAGE = "blocked_counts_by_package"
    private const val KEY_SELECTED_PACKAGES = "selected_packages"
    private const val MAX_VISIBLE_APPS = 30
    private const val OPSTR_POST_NOTIFICATION = "android:post_notification"

    fun isEnabled(context: Context): Boolean = prefs(context).getBoolean(KEY_ENABLED, false) && hasNotificationListenerAccess(context)

    fun setEnabled(
        context: Context,
        enabled: Boolean,
    ) {
        prefs(context).edit().putBoolean(KEY_ENABLED, enabled).apply()
    }

    fun blockedCount(context: Context): Int = prefs(context).getInt(KEY_BLOCKED_COUNT, 0)

    fun incrementBlocked(
        context: Context,
        packageName: String,
    ) {
        val p = prefs(context)
        val packageCounts = blockedCountsByPackage(context).toMutableMap()
        packageCounts[packageName] = (packageCounts[packageName] ?: 0) + 1
        p
            .edit()
            .putInt(KEY_BLOCKED_COUNT, p.getInt(KEY_BLOCKED_COUNT, 0) + 1)
            .putString(KEY_BLOCKED_COUNTS_BY_PACKAGE, encodeBlockedCounts(packageCounts))
            .apply()
    }

    fun blockedCountsByPackage(context: Context): Map<String, Int> =
        decodeBlockedCounts(prefs(context).getString(KEY_BLOCKED_COUNTS_BY_PACKAGE, null))

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
        val selected = selectedPackages(context)
        val installedAppInfos =
            pm
                .getInstalledApplications(0)
                .filter { pm.getLaunchIntentForPackage(it.packageName) != null }
        val installed =
            installedAppInfos
                .map { BlockableNotificationApp(it.loadLabel(pm).toString(), it.packageName) }
                .sortedWith(
                    compareByDescending<BlockableNotificationApp> { it.packageName in defaults }
                        .thenByDescending { it.packageName in selected }
                        .thenBy { it.appName.lowercase() },
                )
        val notificationAllowedPackages =
            installedAppInfos
                .filter { notificationsAllowed(context, it) }
                .mapTo(mutableSetOf()) { it.packageName }
        val filtered =
            installed.filter { app ->
                app.packageName in notificationAllowedPackages || app.packageName in selected
            }
        return limitVisibleApps(if (filtered.isNotEmpty()) filtered else installed, selected).ifEmpty {
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

    fun appNotificationSettingsIntent(packageName: String): Intent =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
            }
        } else {
            appDetailsSettingsIntent(packageName)
        }

    fun appDetailsSettingsIntent(packageName: String): Intent =
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.parse("package:$packageName")
        }

    private fun defaultPackages(context: Context): Set<String> {
        val installed =
            context.packageManager
                .getInstalledApplications(0)
                .map { it.packageName }
                .toSet()
        return listOf("com.tencent.wemeet.app", "com.google.android.youtube", "com.spotify.music")
            .filter { it in installed }
            .toSet()
    }

    private fun notificationsAllowed(
        context: Context,
        appInfo: ApplicationInfo,
    ): Boolean =
        runCatching {
            val appOps =
                context.getSystemService(AppOpsManager::class.java)
                    ?: return@runCatching true
            val mode = notificationOpMode(appOps, appInfo.uid, appInfo.packageName)
            mode == AppOpsManager.MODE_ALLOWED || mode == AppOpsManager.MODE_DEFAULT
        }.getOrDefault(true)

    private fun notificationOpMode(
        appOps: AppOpsManager,
        uid: Int,
        packageName: String,
    ): Int =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(OPSTR_POST_NOTIFICATION, uid, packageName)
        } else {
            @Suppress("DEPRECATION")
            appOps.checkOpNoThrow(OPSTR_POST_NOTIFICATION, uid, packageName)
        }

    private fun limitVisibleApps(
        apps: List<BlockableNotificationApp>,
        selected: Set<String>,
    ): List<BlockableNotificationApp> {
        if (apps.size <= MAX_VISIBLE_APPS) return apps
        val selectedApps = apps.filter { it.packageName in selected }
        val remainingSlots = (MAX_VISIBLE_APPS - selectedApps.size).coerceAtLeast(0)
        return (selectedApps + apps.filterNot { it.packageName in selected }.take(remainingSlots))
            .distinctBy { it.packageName }
    }

    private fun prefs(context: Context) = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    private fun encodeBlockedCounts(counts: Map<String, Int>): String =
        counts.entries
            .filter { it.key.isNotBlank() && it.value > 0 }
            .joinToString(";") { "${it.key}=${it.value}" }

    private fun decodeBlockedCounts(value: String?): Map<String, Int> {
        if (value.isNullOrBlank()) return emptyMap()
        return value
            .split(';')
            .mapNotNull { entry ->
                val separatorIndex = entry.lastIndexOf('=')
                if (separatorIndex <= 0 || separatorIndex == entry.lastIndex) return@mapNotNull null
                val packageName = entry.substring(0, separatorIndex)
                val count = entry.substring(separatorIndex + 1).toIntOrNull()
                if (packageName.isBlank() || count == null || count <= 0) null else packageName to count
            }.toMap()
    }
}

/** Notification listener service entry point used by the reusable blocker. */
class QuickCleanNotificationListener : NotificationListenerService() {
    override fun onNotificationPosted(sbn: StatusBarNotification) {
        if (NotificationDataSource.shouldBlock(this, sbn.packageName)) {
            cancelNotification(sbn.key)
            NotificationDataSource.incrementBlocked(this, sbn.packageName)
        }
    }
}
