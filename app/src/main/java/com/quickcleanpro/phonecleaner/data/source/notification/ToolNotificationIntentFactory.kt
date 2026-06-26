package com.quickcleanpro.phonecleaner.data.source.notification

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.quickcleanpro.phonecleaner.BuildConfig
import com.quickcleanpro.phonecleaner.MainActivity
import com.quickcleanpro.phonecleaner.presentation.common.route.Screen

object ToolNotificationIntentFactory {
    const val EXTRA_TARGET_ROUTE = "quickclean_target_route"
    const val ROUTE_HOME_FILE_MANAGER = "home_file_manager"
    const val ROUTE_HOME_TOOLBOX = "home_toolbox"
    private const val VARIANT_STORAGE_CLEANER = "storagecleaner"
    val homeTabRoutes: Set<String> =
        setOf(
            ROUTE_HOME_FILE_MANAGER,
            ROUTE_HOME_TOOLBOX,
        )

    private const val TOOL_CONTENT_REQUEST_BASE_CODE = 3000
    private const val ACTION_OPEN_TOOL = "com.quickcleanpro.phonecleaner.notification.OPEN_TOOL"

    fun pendingIntent(
        context: Context,
        route: String,
        requestCode: Int,
    ): PendingIntent {
        val appContext = context.applicationContext
        val intent =
            Intent(appContext, MainActivity::class.java).apply {
                action = "$ACTION_OPEN_TOOL.$route"
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
                putExtra(EXTRA_TARGET_ROUTE, route)
            }
        return PendingIntent.getActivity(
            appContext,
            TOOL_CONTENT_REQUEST_BASE_CODE + requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    fun targetRoute(intent: Intent?): String? =
        intent
            ?.getStringExtra(EXTRA_TARGET_ROUTE)
            ?.takeIf(::isValidRoute)

    fun isValidRoute(route: String): Boolean = route in validRoutes

    private val validRoutes: Set<String> =
        buildSet {
            addAll(ToolNotificationSpecs.map { it.route })
            add(Screen.NotificationCleaner.route)
            add(Screen.NotificationBar.route)
            if (BuildConfig.VARIANT_KEY == VARIANT_STORAGE_CLEANER) {
                addAll(homeTabRoutes)
            }
        }
}
