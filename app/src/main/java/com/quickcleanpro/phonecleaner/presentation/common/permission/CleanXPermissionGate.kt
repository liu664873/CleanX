package com.quickcleanpro.phonecleaner.presentation.common.permission

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.quickcleanpro.phonecleaner.R
import com.quickcleanpro.phonecleaner.domain.repository.SettingsRepository
import com.quickcleanpro.phonecleaner.presentation.common.components.buttons.CleanXPrimaryButton
import kotlinx.coroutines.delay

/**
 * 声明式权限门控。权限通过后渲染 content，未授权时显示占位并弹出授权说明。
 */
@Composable
fun CleanXPermissionGate(
    permission: CleanXPermissionType,
    feature: CleanXPermissionFeature = CleanXPermissionFeature.General,
    onDenied: () -> Unit = {},
    modifier: Modifier = Modifier,
    settingsRepository: SettingsRepository,
    deniedContent: @Composable ((onRetry: () -> Unit) -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var granted by remember(permission, settingsRepository) {
        mutableStateOf(checkPermissionFresh(context, permission, settingsRepository))
    }
    var showDialog by remember(permission) { mutableStateOf(!granted) }
    var pendingSettingsRecheck by remember(permission) { mutableStateOf(false) }
    var pendingDeniedAfterDialogClose by remember(permission) { mutableStateOf(false) }
    var settingsLaunchObservedPause by remember(permission) { mutableStateOf(false) }
    var deniedAlreadyDispatched by remember(permission) { mutableStateOf(false) }
    var settingsRecheckGeneration by remember(permission) { mutableIntStateOf(0) }

    fun recheckPermission(showMissingDialog: Boolean) {
        val nowGranted = checkPermissionFresh(context, permission, settingsRepository)
        granted = nowGranted
        showDialog = if (nowGranted) false else showMissingDialog
        if (nowGranted) {
            pendingDeniedAfterDialogClose = false
            deniedAlreadyDispatched = false
        }
    }

    fun dismissDialogThenDeny() {
        if (pendingDeniedAfterDialogClose || deniedAlreadyDispatched) return
        showDialog = false
        pendingSettingsRecheck = false
        settingsLaunchObservedPause = false
        pendingDeniedAfterDialogClose = true
    }

    fun markSettingsLaunchPending() {
        showDialog = false
        pendingSettingsRecheck = true
        settingsLaunchObservedPause = false
        pendingDeniedAfterDialogClose = false
        deniedAlreadyDispatched = false
    }

    val settingsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) {
        if (!pendingSettingsRecheck || pendingDeniedAfterDialogClose || deniedAlreadyDispatched) return@rememberLauncherForActivityResult
        showDialog = false
        settingsLaunchObservedPause = true
        settingsRecheckGeneration++
    }

    val runtimePermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { grants ->
        val isGranted = grants.values.all { it } && checkPermissionFresh(context, permission, settingsRepository)
        granted = isGranted
        showDialog = false
        if (isGranted) {
            deniedAlreadyDispatched = false
            pendingDeniedAfterDialogClose = false
        } else {
            rememberRuntimePermissionDenied(permission, settingsRepository)
            launchSettingsOrDeny(
                permission = permission,
                settingsRepository = settingsRepository,
                onLaunchingSettings = { markSettingsLaunchPending() },
                launcher = settingsLauncher::launch,
                onDenied = { dismissDialogThenDeny() },
            )
        }
    }

    fun requestPermission() {
        launchPermissionRequestOrSettings(
            permission = permission,
            context = context,
            settingsRepository = settingsRepository,
            runtimePermissionLauncher = runtimePermissionLauncher::launch,
            onLaunchingSettings = { markSettingsLaunchPending() },
            launcher = settingsLauncher::launch,
            onDenied = { dismissDialogThenDeny() },
        )
    }

    LaunchedEffect(permission, settingsRepository) {
        recheckPermission(showMissingDialog = true)
    }

    DisposableEffect(lifecycleOwner, permission, settingsRepository) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE,
                Lifecycle.Event.ON_STOP -> {
                    if (pendingSettingsRecheck) settingsLaunchObservedPause = true
                }
                Lifecycle.Event.ON_RESUME -> {
                    if (pendingSettingsRecheck && settingsLaunchObservedPause) {
                        settingsLaunchObservedPause = false
                        settingsRecheckGeneration++
                    } else if (!pendingSettingsRecheck) {
                        recheckPermission(showMissingDialog = showDialog)
                    }
                }
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(pendingDeniedAfterDialogClose) {
        if (!pendingDeniedAfterDialogClose) return@LaunchedEffect
        showDialog = false
        delay(200L)
        pendingDeniedAfterDialogClose = false
        deniedAlreadyDispatched = true
        onDenied()
    }

    LaunchedEffect(settingsRecheckGeneration) {
        if (!pendingSettingsRecheck) return@LaunchedEffect
        val nowGranted = checkPermissionAfterSettingsReturn(context, permission, settingsRepository)
        pendingSettingsRecheck = false
        settingsLaunchObservedPause = false
        granted = nowGranted
        showDialog = false
        if (!nowGranted) dismissDialogThenDeny()
    }

    val shouldShowPermissionDialog = showDialog && !granted
    BackHandler(enabled = shouldShowPermissionDialog) {
        dismissDialogThenDeny()
    }

    Box(modifier = modifier.fillMaxSize()) {
        if (granted) {
            content()
        } else {
            if (deniedContent != null) {
                deniedContent { requestPermission() }
            } else {
                DefaultDeniedContent(
                    copy = CleanXPermissionRequestManager.dialogCopy(permission, feature),
                    onRetry = { requestPermission() },
                )
            }
        }

        if (shouldShowPermissionDialog) {
            InlinePermissionOverlay(onDismiss = { dismissDialogThenDeny() }) {
                CleanXPermissionRequiredDialog(
                    copy = CleanXPermissionRequestManager.dialogCopy(permission, feature),
                    onSubmit = { requestPermission() },
                    onCancel = { dismissDialogThenDeny() },
                )
            }
        }
    }
}

@Composable
private fun DefaultDeniedContent(
    copy: CleanXPermissionCopy,
    onRetry: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CleanXBackground)
            .padding(16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            shape = RoundedCornerShape(12.dp),
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    text = stringResource(copy.titleRes),
                    color = Color(0xFF1D2959),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = stringResource(copy.descriptionRes),
                    color = Color(0xFF8190A5),
                    fontSize = 15.sp,
                    lineHeight = 20.sp,
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(6.dp))
                CleanXPrimaryButton(
                    text = stringResource(copy.allowRes),
                    onClick = onRetry,
                )
            }
        }
    }
}

private fun launchSettingsOrDeny(
    permission: CleanXPermissionType,
    settingsRepository: SettingsRepository,
    onLaunchingSettings: () -> Unit,
    launcher: (Intent) -> Unit,
    onDenied: () -> Unit,
) {
    val intents = listOfNotNull(
        CleanXPermissionRequestManager.primarySettingsIntent(permission, settingsRepository),
        CleanXPermissionRequestManager.fallbackSettingsIntent(permission, settingsRepository),
    )
    for (intent in intents) {
        try {
            onLaunchingSettings()
            launcher(intent)
            return
        } catch (_: ActivityNotFoundException) {
        } catch (_: Exception) {
        }
    }
    onDenied()
}

private fun launchPermissionRequestOrSettings(
    permission: CleanXPermissionType,
    context: Context,
    settingsRepository: SettingsRepository,
    runtimePermissionLauncher: (Array<String>) -> Unit,
    onLaunchingSettings: () -> Unit,
    launcher: (Intent) -> Unit,
    onDenied: () -> Unit,
) {
    val runtimePermissions = runtimePermissionsToRequest(context, permission, settingsRepository)
    if (runtimePermissions.isNotEmpty()) {
        try {
            runtimePermissionLauncher(runtimePermissions)
            return
        } catch (_: Exception) {
        }
    }
    launchSettingsOrDeny(
        permission = permission,
        settingsRepository = settingsRepository,
        onLaunchingSettings = onLaunchingSettings,
        launcher = launcher,
        onDenied = onDenied,
    )
}

private fun runtimePermissionsToRequest(
    context: Context,
    permission: CleanXPermissionType,
    settingsRepository: SettingsRepository,
): Array<String> {
    if (!shouldRequestRuntimePermission(permission, settingsRepository)) return emptyArray()
    return CleanXPermissionRequestManager.runtimePermissions(permission)
        .filter { runtimePermission ->
            ContextCompat.checkSelfPermission(context, runtimePermission) != PackageManager.PERMISSION_GRANTED
        }
        .toTypedArray()
}

private fun shouldRequestRuntimePermission(
    permission: CleanXPermissionType,
    settingsRepository: SettingsRepository,
): Boolean =
    when (permission) {
        CleanXPermissionType.Location ->
            !runCatching { settingsRepository.hasDeniedLocationRuntimePermission() }.getOrDefault(false)
        CleanXPermissionType.PostNotifications ->
            !runCatching { settingsRepository.hasDeniedNotificationRuntimePermission() }.getOrDefault(false)
        CleanXPermissionType.StorageFiles,
        CleanXPermissionType.MediaImages,
        CleanXPermissionType.MediaImagesWithLocation,
        CleanXPermissionType.MediaVideo,
        CleanXPermissionType.MediaAudio -> true
        CleanXPermissionType.UsageAccess,
        CleanXPermissionType.NotificationListener,
        CleanXPermissionType.Overlay -> false
    }

private fun rememberRuntimePermissionDenied(
    permission: CleanXPermissionType,
    settingsRepository: SettingsRepository,
) {
    when (permission) {
        CleanXPermissionType.Location ->
            runCatching { settingsRepository.saveLocationRuntimePermissionDenied() }
        CleanXPermissionType.PostNotifications ->
            runCatching { settingsRepository.saveNotificationRuntimePermissionDenied() }
        else -> Unit
    }
}

private fun checkPermissionFresh(
    context: Context,
    permission: CleanXPermissionType,
    settingsRepository: SettingsRepository,
): Boolean =
    runCatching {
        if (permission == CleanXPermissionType.UsageAccess) {
            settingsRepository.resetAppUsagePermissionCache()
        }
        CleanXPermissionRequestManager.isGranted(context, permission, settingsRepository)
    }.getOrDefault(false)

private suspend fun checkPermissionAfterSettingsReturn(
    context: Context,
    permission: CleanXPermissionType,
    settingsRepository: SettingsRepository,
): Boolean {
    delay(350L)
    val first = checkPermissionFresh(context, permission, settingsRepository)
    if (permission != CleanXPermissionType.StorageFiles || !first) return first
    delay(350L)
    return checkPermissionFresh(context, permission, settingsRepository)
}

private val CleanXBackground = Color(0xFFF0F3F7)
