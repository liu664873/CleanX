package com.quickcleanpro.phonecleaner.utils

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import com.quickcleanpro.phonecleaner.R

object NotificationChannelManager {
    const val TOOLS_CHANNEL_ID = "quickclean_tools"
    const val TRIGGERED_TOOLS_CHANNEL_ID = "quickclean_triggered_tools"
    const val PERSISTENT_CHANNEL_ID = "quickclean_persistent"

    fun createAllChannels(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(createToolsChannel(context))
        notificationManager.createNotificationChannel(createTriggeredToolsChannel(context))
        notificationManager.createNotificationChannel(createPersistentChannel(context))
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createToolsChannel(context: Context): NotificationChannel =
        NotificationChannel(
            TOOLS_CHANNEL_ID,
            context.getString(R.string.notification_channel_tools),
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = context.getString(R.string.notification_channel_tools_desc)
            setShowBadge(true)
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createTriggeredToolsChannel(context: Context): NotificationChannel =
        NotificationChannel(
            TRIGGERED_TOOLS_CHANNEL_ID,
            context.getString(R.string.notification_channel_tools),
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = context.getString(R.string.notification_channel_tools_desc)
            setShowBadge(true)
            enableVibration(true)
            vibrationPattern = longArrayOf(0L, 100L, 200L, 300L)
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createPersistentChannel(context: Context): NotificationChannel =
        NotificationChannel(
            PERSISTENT_CHANNEL_ID,
            context.getString(R.string.running_in_background),
            NotificationManager.IMPORTANCE_LOW,
        ).apply {
            description = context.getString(R.string.running_in_background)
            setShowBadge(false)
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        }
}
