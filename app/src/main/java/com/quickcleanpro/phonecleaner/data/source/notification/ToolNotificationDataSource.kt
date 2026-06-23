package com.quickcleanpro.phonecleaner.data.source.notification

import android.Manifest
import android.app.Notification
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.widget.RemoteViews
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.quickcleanpro.phonecleaner.R
import com.quickcleanpro.phonecleaner.presentation.common.route.Screen
import com.quickcleanpro.phonecleaner.utils.NotificationChannelManager

object ToolNotificationDataSource {
    const val EXTRA_TARGET_ROUTE = ToolNotificationIntentFactory.EXTRA_TARGET_ROUTE
    const val PERSISTENT_NOTIFICATION_ID = 17
    private const val TOOL_NOTIFICATION_BASE_ID = 3000

    fun showToolNotifications(context: Context) {
        val appContext = context.applicationContext
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(appContext, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        NotificationChannelManager.createAllChannels(appContext)
        val manager = NotificationManagerCompat.from(appContext)
        ToolNotificationSpecs.forEachIndexed { index, item ->
            runCatching { manager.cancel(TOOL_NOTIFICATION_BASE_ID + index) }
            val notification = buildToolNotification(appContext, item, index)
            runCatching { manager.notify(TOOL_NOTIFICATION_BASE_ID + index, notification) }
        }
    }

    fun showPersistentNotification(context: Context) {
        val appContext = context.applicationContext
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(appContext, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        NotificationChannelManager.createAllChannels(appContext)
        val notification =
            NotificationCompat
                .Builder(appContext, NotificationChannelManager.PERSISTENT_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_n_notification_cleaner)
                .setContentTitle(appContext.getString(R.string.app_name))
                .setContentText(appContext.getString(R.string.running_in_background))
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .build()
        runCatching {
            NotificationManagerCompat.from(appContext).notify(PERSISTENT_NOTIFICATION_ID, notification)
        }
    }

    internal fun buildToolNotification(
        context: Context,
        item: ToolNotificationSpec,
        requestCode: Int,
    ): Notification {
        val appContext = context.applicationContext
        return NotificationCompat
            .Builder(appContext, NotificationChannelManager.TRIGGERED_TOOLS_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_n_notification_cleaner)
            .setContentTitle(appContext.getString(item.titleRes))
            .setContentText(appContext.getString(item.descriptionRes))
            .setContentIntent(ToolNotificationIntentFactory.pendingIntent(appContext, item.route, requestCode))
            .setCustomContentView(toolNotificationCollapsedView(appContext, item))
            .setCustomBigContentView(toolNotificationExpandedView(appContext, item))
            .setCustomHeadsUpContentView(toolNotificationHeadsUpView(appContext, item))
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setAutoCancel(true)
            .setOnlyAlertOnce(false)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_RECOMMENDATION)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setShowWhen(true)
            .setWhen(System.currentTimeMillis())
            .build()
    }

    private fun toolNotificationCollapsedView(
        context: Context,
        item: ToolNotificationSpec,
    ): RemoteViews =
        RemoteViews(context.packageName, R.layout.notification_tool_collapsed).apply {
            setImageViewResource(R.id.iv_icon, item.iconRes)
            setTextViewText(R.id.tv_title, context.getString(item.titleRes))
            setTextViewText(R.id.tv_desc, context.getString(item.descriptionRes))
        }

    private fun toolNotificationExpandedView(
        context: Context,
        item: ToolNotificationSpec,
    ): RemoteViews =
        RemoteViews(context.packageName, R.layout.notification_tool_item).apply {
            setImageViewResource(R.id.iv_icon, item.iconRes)
            setTextViewText(R.id.tv_title, context.getString(item.titleRes))
            setTextViewText(R.id.tv_desc, context.getString(item.descriptionRes))
            setTextViewText(R.id.tv_action, context.getString(item.actionRes))
        }

    private fun toolNotificationHeadsUpView(
        context: Context,
        item: ToolNotificationSpec,
    ): RemoteViews =
        RemoteViews(context.packageName, R.layout.notification_tool_heads_up).apply {
            setImageViewResource(R.id.iv_icon, item.iconRes)
            setTextViewText(R.id.tv_title, context.getString(item.titleRes))
            setTextViewText(R.id.tv_desc, context.getString(item.descriptionRes))
            setTextViewText(R.id.tv_action, context.getString(item.actionRes))
        }
}

data class ToolNotificationSpec(
    @StringRes val titleRes: Int,
    @StringRes val descriptionRes: Int,
    val route: String,
    @DrawableRes val iconRes: Int,
    @StringRes val actionRes: Int,
)

val ToolNotificationSpecs: List<ToolNotificationSpec> =
    listOf(
        ToolNotificationSpec(
            titleRes = R.string.device_info,
            descriptionRes = R.string.common_tool_device_desc,
            route = Screen.DeviceInfo.route,
            iconRes = R.drawable.ic_n_device_info,
            actionRes = R.string.view_now,
        ),
        ToolNotificationSpec(
            titleRes = R.string.junk_removal,
            descriptionRes = R.string.notification_tool_junk_desc,
            route = Screen.Scan.route,
            iconRes = R.drawable.ic_n_junk_removal,
            actionRes = R.string.scan_now,
        ),
        ToolNotificationSpec(
            titleRes = R.string.battery_info,
            descriptionRes = R.string.common_tool_battery_desc,
            route = Screen.BatteryInfo.route,
            iconRes = R.drawable.ic_n_battery_info,
            actionRes = R.string.view_now,
        ),
        ToolNotificationSpec(
            titleRes = R.string.network_scan,
            descriptionRes = R.string.notification_tool_network_scan_desc,
            route = Screen.NetworkScan.route,
            iconRes = R.drawable.ic_n_network_scan,
            actionRes = R.string.scan_now,
        ),
        ToolNotificationSpec(
            titleRes = R.string.network_usage,
            descriptionRes = R.string.notification_tool_network_usage_desc,
            route = Screen.NetworkUsage.route,
            iconRes = R.drawable.ic_n_network_usage,
            actionRes = R.string.view_now,
        ),
        ToolNotificationSpec(
            titleRes = R.string.notification_bar,
            descriptionRes = R.string.common_tool_notification_bar_desc,
            route = Screen.NotificationBar.route,
            iconRes = R.drawable.ic_notification_bar,
            actionRes = R.string.check_now,
        ),
    )
