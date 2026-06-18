package com.quickcleanpro.phonecleaner.presentation.app

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.quickcleanpro.phonecleaner.R
import com.quickcleanpro.phonecleaner.presentation.common.components.popups.CleanXPermissionRequiredDialog
import com.quickcleanpro.phonecleaner.presentation.common.components.popups.InlinePermissionOverlay
import com.quickcleanpro.phonecleaner.presentation.common.permission.CleanXPermissionCopy

@Composable
internal fun NotificationPermissionPrompt(
    isHomeVisible: Boolean,
    hasNotificationPermission: () -> Boolean,
    hasDeniedNotificationPermission: () -> Boolean,
    rememberNotificationDenied: () -> Unit,
    openAppSettings: () -> Unit,
    onPermissionGranted: () -> Unit,
) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return

    val lifecycleOwner = LocalLifecycleOwner.current
    var granted by remember { mutableStateOf(hasNotificationPermission()) }
    var showDialog by remember { mutableStateOf(false) }
    var notificationSettingsLaunchPending by rememberSaveable { mutableStateOf(false) }
    var homePromptSuppressed by rememberSaveable { mutableStateOf(false) }

    val permissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission(),
        ) { isGranted ->
            granted = isGranted
            showDialog = false
            if (isGranted) {
                onPermissionGranted()
            } else {
                rememberNotificationDenied()
                notificationSettingsLaunchPending = true
                homePromptSuppressed = true
                openAppSettings()
            }
        }

    fun refreshPermission(returningFromSettings: Boolean) {
        val nowGranted = hasNotificationPermission()
        granted = nowGranted
        if (nowGranted) {
            showDialog = false
            onPermissionGranted()
        } else {
            showDialog = isHomeVisible && !returningFromSettings && !homePromptSuppressed
        }
    }

    LaunchedEffect(isHomeVisible) {
        if (isHomeVisible) {
            refreshPermission(returningFromSettings = false)
        } else {
            showDialog = false
        }
    }

    DisposableEffect(lifecycleOwner, isHomeVisible) {
        val observer =
            LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME && isHomeVisible) {
                    val returningFromSettings = notificationSettingsLaunchPending
                    notificationSettingsLaunchPending = false
                    refreshPermission(returningFromSettings = returningFromSettings)
                }
            }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    if (showDialog && !granted && isHomeVisible) {
        InlinePermissionOverlay(
            onDismiss = { showDialog = false },
        ) {
            CleanXPermissionRequiredDialog(
                copy =
                    CleanXPermissionCopy(
                        titleRes = R.string.permission_title_required,
                        descriptionRes = R.string.permission_post_notifications_desc,
                        hint1Res = R.string.permission_hint_app_notifications,
                        hint2Res = R.string.permission_hint_no_personal,
                    ),
                onSubmit = {
                    showDialog = false
                    if (hasDeniedNotificationPermission()) {
                        notificationSettingsLaunchPending = true
                        homePromptSuppressed = true
                        openAppSettings()
                    } else {
                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                },
                onCancel = { showDialog = false },
            )
        }
    }
}
