package com.quickcleanpro.phonecleaner.utils

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.quickcleanpro.phonecleaner.MainActivity
import com.quickcleanpro.phonecleaner.R
import com.quickcleanpro.phonecleaner.data.source.notification.QuickCleanNotificationListener
import com.quickcleanpro.phonecleaner.presentation.navigation.Screen

data class BlockableNotificationApp(
    val appName: String,
    val packageName: String,
)

object NotificationHelper {
    const val EXTRA_TARGET_ROUTE = "quickclean_target_route"

    private const val CHANNEL_NAME = "Quick Clean Tools"
    private const val PREFS = "notification_blocker"
    private const val KEY_ENABLED = "enabled"
    private const val KEY_BLOCKED_COUNT = "blocked_count"
    private const val KEY_SELECTED_PACKAGES = "selected_packages"

    // 鈹€鈹€ Tool notifications 鈹€鈹€

    fun showToolNotifications(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        createChannel(context)
        val manager =
            NotificationManagerCompat
                .from(context)
        manager.cancelAll()

        toolNotifications().forEachIndexed { index, item ->
            val notification =
                NotificationCompat
                    .Builder(context, NotificationChannelManager.TRIGGERED_TOOLS_CHANNEL_ID)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle(item.title)
                    .setContentText(item.description)
                    .setContentIntent(targetIntent(context, item.route, index))
                    .setAutoCancel(true)
                    .setOnlyAlertOnce(false)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setCategory(NotificationCompat.CATEGORY_RECOMMENDATION)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setShowWhen(true)
                    .setWhen(System.currentTimeMillis())
                    .build()
            manager.notify(3000 + index, notification)
        }
    }

    private fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel =
            NotificationChannel(
                NotificationChannelManager.TRIGGERED_TOOLS_CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH,
            ).apply {
                description = "Quick clean tool suggestions"
                setShowBadge(true)
                enableVibration(true)
                vibrationPattern = longArrayOf(0L, 100L, 200L, 300L)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }

    private fun targetIntent(
        context: Context,
        route: String,
        requestCode: Int,
    ): PendingIntent {
        val intent =
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra(EXTRA_TARGET_ROUTE, route)
            }
        return PendingIntent.getActivity(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private data class ToolNotification(
        val title: String,
        val description: String,
        val route: String,
    )

    private fun toolNotifications(): List<ToolNotification> =
        listOf(
            ToolNotification("Device Info", "View detailed specs of your phone", Screen.DeviceInfo.route),
            ToolNotification("Junk Removal", "Check for junk files", Screen.Scan.route),
            ToolNotification("Battery Info", "View phone battery information", Screen.BatteryInfo.route),
            ToolNotification("Network Scan", "Scan connected devices", Screen.NetworkScan.route),
            ToolNotification("Network Usage", "View your data usage", Screen.NetworkUsage.route),
            ToolNotification("Notification Cleaner", "Check unwanted notifications", Screen.NotificationCleaner.route),
        )

    // 鈹€鈹€ Notification blocker 鈹€鈹€

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
                        .map { a -> a.packageName }
                        .toSet()
            }.toSet()
            .ifEmpty { emptySet() }

    private fun prefs(context: Context) = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
}
