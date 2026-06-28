package com.pdffox.adv.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

/** 通知通道创建工具，负责持久通知和普通通知使用的默认通道。 */
object NotificationChannelManager {

    /** 普通通知通道 ID。 */
    const val NORMAL_CHANNEL_ID = "normal_notification_channel"
    /** 普通通知通道名称。 */
    const val NORMAL_CHANNEL_NAME = "normal_notification_notice"

    /** Android O 及以上创建高优先级普通通知通道。 */
    fun createNormalChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NORMAL_CHANNEL_ID,
                NORMAL_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                setShowBadge(true)
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 100, 200, 300)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
}
