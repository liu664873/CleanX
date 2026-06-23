package com.quickcleanpro.phonecleaner.core.permission

import android.Manifest
import android.app.AppOpsManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Process
import android.provider.Settings
import androidx.core.content.ContextCompat

fun commonPermissionHandlers(): List<PermissionHandler> =
    listOf(
        StorageFilesPermissionHandler,
        MediaImagesPermissionHandler,
        MediaImagesWithLocationPermissionHandler,
        MediaVideoPermissionHandler,
        MediaAudioPermissionHandler,
        LocationPermissionHandler,
        UsageAccessPermissionHandler,
        NotificationListenerPermissionHandler(),
        OverlayPermissionHandler,
        PostNotificationsPermissionHandler,
    )

object StorageFilesPermissionHandler : PermissionHandler {
    override val permission: AppPermission = CommonPermission.StorageFiles

    override fun isGranted(context: Context): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            runCatching { Environment.isExternalStorageManager() }.getOrDefault(false)
        } else {
            runtimePermissions(context).all { context.hasRuntimePermission(it) }
        }

    override fun runtimePermissions(context: Context): List<String> =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            emptyList()
        } else {
            buildList {
                add(Manifest.permission.READ_EXTERNAL_STORAGE)
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
                    add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }
        }

    override fun settingsIntents(context: Context): List<Intent> =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            listOf(
                Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                    data = Uri.parse("package:${context.packageName}")
                },
                Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION),
                appSettingsIntent(context),
            )
        } else {
            listOf(appSettingsIntent(context))
        }
}

object MediaImagesPermissionHandler : PermissionHandler {
    override val permission: AppPermission = CommonPermission.MediaImages

    override fun isGranted(context: Context): Boolean = runtimePermissions(context).all { context.hasRuntimePermission(it) }

    override fun runtimePermissions(context: Context): List<String> =
        listOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                Manifest.permission.READ_MEDIA_IMAGES
            } else {
                Manifest.permission.READ_EXTERNAL_STORAGE
            },
        )

    override fun settingsIntents(context: Context): List<Intent> = listOf(appSettingsIntent(context))
}

object MediaImagesWithLocationPermissionHandler : PermissionHandler {
    override val permission: AppPermission = CommonPermission.MediaImagesWithLocation

    override fun isGranted(context: Context): Boolean = runtimePermissions(context).all { context.hasRuntimePermission(it) }

    override fun runtimePermissions(context: Context): List<String> =
        buildList {
            addAll(MediaImagesPermissionHandler.runtimePermissions(context))
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                add(Manifest.permission.ACCESS_MEDIA_LOCATION)
            }
        }

    override fun settingsIntents(context: Context): List<Intent> = listOf(appSettingsIntent(context))
}

object MediaVideoPermissionHandler : PermissionHandler {
    override val permission: AppPermission = CommonPermission.MediaVideo

    override fun isGranted(context: Context): Boolean = runtimePermissions(context).all { context.hasRuntimePermission(it) }

    override fun runtimePermissions(context: Context): List<String> =
        listOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                Manifest.permission.READ_MEDIA_VIDEO
            } else {
                Manifest.permission.READ_EXTERNAL_STORAGE
            },
        )

    override fun settingsIntents(context: Context): List<Intent> = listOf(appSettingsIntent(context))
}

object MediaAudioPermissionHandler : PermissionHandler {
    override val permission: AppPermission = CommonPermission.MediaAudio

    override fun isGranted(context: Context): Boolean = runtimePermissions(context).all { context.hasRuntimePermission(it) }

    override fun runtimePermissions(context: Context): List<String> =
        listOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                Manifest.permission.READ_MEDIA_AUDIO
            } else {
                Manifest.permission.READ_EXTERNAL_STORAGE
            },
        )

    override fun settingsIntents(context: Context): List<Intent> = listOf(appSettingsIntent(context))
}

object LocationPermissionHandler : PermissionHandler {
    override val permission: AppPermission = CommonPermission.Location

    override fun isGranted(context: Context): Boolean =
        Manifest.permission.ACCESS_FINE_LOCATION.let(context::hasRuntimePermission)

    override fun runtimePermissions(context: Context): List<String> = listOf(Manifest.permission.ACCESS_FINE_LOCATION)

    override fun settingsIntents(context: Context): List<Intent> = listOf(appSettingsIntent(context))
}

object UsageAccessPermissionHandler : PermissionHandler {
    override val permission: AppPermission = CommonPermission.UsageAccess

    override fun isGranted(context: Context): Boolean {
        val mode =
            runCatching {
                val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
                appOps.checkOpNoThrow(
                    AppOpsManager.OPSTR_GET_USAGE_STATS,
                    Process.myUid(),
                    context.packageName,
                )
            }.getOrDefault(AppOpsManager.MODE_ERRORED)
        return mode == AppOpsManager.MODE_ALLOWED
    }

    override fun runtimePermissions(context: Context): List<String> = emptyList()

    override fun settingsIntents(context: Context): List<Intent> =
        listOf(
            Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS),
            appSettingsIntent(context),
        )
}

class NotificationListenerPermissionHandler(
    private val listenerComponentProvider: ((Context) -> ComponentName)? = null,
) : PermissionHandler {
    override val permission: AppPermission = CommonPermission.NotificationListener

    override fun isGranted(context: Context): Boolean {
        val flat = Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners")
        val enabledComponents = flat?.split(':').orEmpty()
        val listenerComponent = listenerComponentProvider?.invoke(context)?.flattenToString()
        return if (listenerComponent != null) {
            enabledComponents.any { it.equals(listenerComponent, ignoreCase = true) }
        } else {
            enabledComponents.any { it.contains(context.packageName, ignoreCase = true) }
        }
    }

    override fun runtimePermissions(context: Context): List<String> = emptyList()

    override fun settingsIntents(context: Context): List<Intent> =
        listOf(
            Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS),
            appSettingsIntent(context),
        )
}

object OverlayPermissionHandler : PermissionHandler {
    override val permission: AppPermission = CommonPermission.Overlay

    override fun isGranted(context: Context): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(context)

    override fun runtimePermissions(context: Context): List<String> = emptyList()

    override fun settingsIntents(context: Context): List<Intent> =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            listOf(
                Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:${context.packageName}"),
                ),
                appSettingsIntent(context),
            )
        } else {
            listOf(appSettingsIntent(context))
        }
}

object PostNotificationsPermissionHandler : PermissionHandler {
    override val permission: AppPermission = CommonPermission.PostNotifications

    override fun isGranted(context: Context): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            Manifest.permission.POST_NOTIFICATIONS.let(context::hasRuntimePermission)

    override fun runtimePermissions(context: Context): List<String> =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            listOf(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            emptyList()
        }

    override fun settingsIntents(context: Context): List<Intent> =
        listOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                    putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                }
            } else {
                appSettingsIntent(context)
            },
            appSettingsIntent(context),
        )
}

fun appSettingsIntent(context: Context): Intent =
    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.fromParts("package", context.packageName, null)
    }

private fun Context.hasRuntimePermission(permission: String): Boolean =
    ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED

