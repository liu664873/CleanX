package com.quickcleanpro.phonecleaner.presentation.common.permission

import android.content.ActivityNotFoundException
import android.content.Intent
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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.quickcleanpro.phonecleaner.R
import com.quickcleanpro.phonecleaner.core.permission.AppPermission
import com.quickcleanpro.phonecleaner.core.permission.PermissionManager
import com.quickcleanpro.phonecleaner.core.permission.PermissionRequestPlan
import com.quickcleanpro.phonecleaner.core.permission.PermissionStatus
import com.quickcleanpro.phonecleaner.presentation.common.components.buttons.CleanXPrimaryButton
import com.quickcleanpro.phonecleaner.presentation.common.components.popups.CleanXPermissionRequiredDialog
import com.quickcleanpro.phonecleaner.presentation.common.components.popups.InlinePermissionOverlay
import com.quickcleanpro.phonecleaner.presentation.app.LocalExternalActivityLaunchHandler
import kotlinx.coroutines.delay

/**
 * 声明式权限门控。权限通过后渲染 content，未授权时显示占位并弹出授权说明。
 */
@Composable
fun CleanXPermissionGate(
    feature: CleanXFeature,
    onDenied: () -> Unit = {},
    onPermissionGrantedChanged: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier,
    deniedContent: @Composable ((onRetry: () -> Unit) -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val externalActivityLaunchHandler = LocalExternalActivityLaunchHandler.current
    val permissionManager = remember(context) {
        CleanXPermissionRegistry.permissionManager(context)
    }
    var granted by remember(feature, permissionManager) {
        mutableStateOf(checkPermissionFresh(context, feature, permissionManager))
    }
    var showDialog by remember(feature) { mutableStateOf(false) }
    var pendingSettingsRecheck by remember(feature) { mutableStateOf(false) }
    var pendingDeniedAfterDialogClose by remember(feature) { mutableStateOf(false) }
    var settingsLaunchObservedPause by remember(feature) { mutableStateOf(false) }
    var deniedAlreadyDispatched by remember(feature) { mutableStateOf(false) }
    var settingsRecheckGeneration by remember(feature) { mutableIntStateOf(0) }
    var missingPermission by remember(feature) {
        mutableStateOf(firstMissingPermission(context, feature, permissionManager))
    }

    fun recheckPermission(showMissingDialog: Boolean) {
        val status = permissionStatusFresh(context, feature, permissionManager)
        val nowGranted = status.granted
        missingPermission = status.missing.firstOrNull()
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
        permissionManager.onRuntimeResult(context, feature, grants)
        val status = permissionStatusFresh(context, feature, permissionManager)
        missingPermission = status.missing.firstOrNull()
        val isGranted = grants.values.all { it } && status.granted
        granted = isGranted
        showDialog = false
        if (isGranted) {
            deniedAlreadyDispatched = false
            pendingDeniedAfterDialogClose = false
        } else {
            launchSettingsOrDeny(
                feature = feature,
                permissionManager = permissionManager,
                context = context,
                settingsOnly = true,
                onLaunchingSettings = {
                    externalActivityLaunchHandler.markLaunch()
                    markSettingsLaunchPending()
                },
                onSettingsLaunchFailed = externalActivityLaunchHandler.cancelLaunch,
                launcher = settingsLauncher::launch,
                onDenied = { dismissDialogThenDeny() },
            )
        }
    }

    fun requestPermission() {
        launchPermissionRequestOrSettings(
            feature = feature,
            context = context,
            permissionManager = permissionManager,
            runtimePermissionLauncher = runtimePermissionLauncher::launch,
            onLaunchingSettings = {
                externalActivityLaunchHandler.markLaunch()
                markSettingsLaunchPending()
            },
            onSettingsLaunchFailed = externalActivityLaunchHandler.cancelLaunch,
            launcher = settingsLauncher::launch,
            onDenied = { dismissDialogThenDeny() },
        )
    }

    LaunchedEffect(feature, permissionManager) {
        recheckPermission(showMissingDialog = false)
    }

    LaunchedEffect(granted) {
        onPermissionGrantedChanged(granted)
    }

    DisposableEffect(lifecycleOwner, feature, permissionManager) {
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
        val previousMissingPermission = missingPermission
        val status = permissionStatusAfterSettingsReturn(context, feature, permissionManager)
        val nowGranted = status.granted
        val nextMissingPermission = status.missing.firstOrNull()
        pendingSettingsRecheck = false
        settingsLaunchObservedPause = false
        missingPermission = nextMissingPermission
        granted = nowGranted
        showDialog = false
        if (nowGranted) {
            deniedAlreadyDispatched = false
            pendingDeniedAfterDialogClose = false
        } else if (shouldContinuePermissionFlow(previousMissingPermission, nextMissingPermission)) {
            deniedAlreadyDispatched = false
            pendingDeniedAfterDialogClose = false
            showDialog = true
        } else {
            dismissDialogThenDeny()
        }
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
                    copy = CleanXPermissionRegistry.copyFor(feature, missingPermission),
                    onRetry = { requestPermission() },
                )
            }
        }

        if (shouldShowPermissionDialog) {
            InlinePermissionOverlay(onDismiss = { dismissDialogThenDeny() }) {
                CleanXPermissionRequiredDialog(
                    copy = CleanXPermissionRegistry.copyFor(feature, missingPermission),
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
    feature: CleanXFeature,
    permissionManager: PermissionManager<CleanXFeature>,
    context: android.content.Context,
    settingsOnly: Boolean = false,
    onLaunchingSettings: () -> Unit,
    onSettingsLaunchFailed: () -> Unit,
    launcher: (Intent) -> Unit,
    onDenied: () -> Unit,
) {
    val plan =
        if (settingsOnly) {
            permissionManager.settingsPlan(context, feature)
        } else {
            permissionManager.requestPlan(context, feature)
        }
    val intents = (plan as? PermissionRequestPlan.OpenSettings)?.intents.orEmpty()
    for (intent in intents) {
        try {
            onLaunchingSettings()
            launcher(intent)
            return
        } catch (_: ActivityNotFoundException) {
            onSettingsLaunchFailed()
        } catch (_: Exception) {
            onSettingsLaunchFailed()
        }
    }
    onDenied()
}

private fun launchPermissionRequestOrSettings(
    feature: CleanXFeature,
    context: android.content.Context,
    permissionManager: PermissionManager<CleanXFeature>,
    runtimePermissionLauncher: (Array<String>) -> Unit,
    onLaunchingSettings: () -> Unit,
    onSettingsLaunchFailed: () -> Unit,
    launcher: (Intent) -> Unit,
    onDenied: () -> Unit,
) {
    when (val plan = permissionManager.requestPlan(context, feature)) {
        PermissionRequestPlan.AlreadyGranted -> return
        is PermissionRequestPlan.RequestRuntime -> {
            try {
                runtimePermissionLauncher(plan.permissions)
                return
            } catch (_: Exception) {
            }
        }
        is PermissionRequestPlan.OpenSettings -> {
            launchSettingsIntentsOrDeny(
                intents = plan.intents,
                onLaunchingSettings = onLaunchingSettings,
                onSettingsLaunchFailed = onSettingsLaunchFailed,
                launcher = launcher,
                onDenied = onDenied,
            )
            return
        }
        PermissionRequestPlan.Unavailable -> Unit
    }
    launchSettingsOrDeny(
        feature = feature,
        permissionManager = permissionManager,
        context = context,
        onLaunchingSettings = onLaunchingSettings,
        onSettingsLaunchFailed = onSettingsLaunchFailed,
        launcher = launcher,
        onDenied = onDenied,
    )
}

private fun launchSettingsIntentsOrDeny(
    intents: List<Intent>,
    onLaunchingSettings: () -> Unit,
    onSettingsLaunchFailed: () -> Unit,
    launcher: (Intent) -> Unit,
    onDenied: () -> Unit,
) {
    for (intent in intents) {
        try {
            onLaunchingSettings()
            launcher(intent)
            return
        } catch (_: ActivityNotFoundException) {
            onSettingsLaunchFailed()
        } catch (_: Exception) {
            onSettingsLaunchFailed()
        }
    }
    onDenied()
}

private fun checkPermissionFresh(
    context: android.content.Context,
    feature: CleanXFeature,
    permissionManager: PermissionManager<CleanXFeature>,
): Boolean =
    permissionStatusFresh(context, feature, permissionManager).granted

private fun permissionStatusFresh(
    context: android.content.Context,
    feature: CleanXFeature,
    permissionManager: PermissionManager<CleanXFeature>,
) =
    runCatching { permissionManager.status(context, feature) }
        .getOrDefault(PermissionStatus(false, emptyList()))

private fun firstMissingPermission(
    context: android.content.Context,
    feature: CleanXFeature,
    permissionManager: PermissionManager<CleanXFeature>,
): AppPermission? =
    permissionStatusFresh(context, feature, permissionManager).missing.firstOrNull()

private suspend fun permissionStatusAfterSettingsReturn(
    context: android.content.Context,
    feature: CleanXFeature,
    permissionManager: PermissionManager<CleanXFeature>,
): PermissionStatus {
    delay(350L)
    val first = permissionStatusFresh(context, feature, permissionManager)
    if (feature != CleanXFeature.FileManager || !first.granted) return first
    delay(350L)
    return permissionStatusFresh(context, feature, permissionManager)
}

private fun shouldContinuePermissionFlow(
    previousMissingPermission: AppPermission?,
    nextMissingPermission: AppPermission?,
): Boolean =
    previousMissingPermission != null &&
        nextMissingPermission != null &&
        previousMissingPermission.key != nextMissingPermission.key

private val CleanXBackground = Color(0xFFF0F3F7)
