package com.quickcleanpro.phonecleaner.presentation.common.permission

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.quickcleanpro.phonecleaner.core.permission.AppPermission
import com.quickcleanpro.phonecleaner.core.permission.PermissionManager
import com.quickcleanpro.phonecleaner.core.permission.PermissionRequestPlan
import com.quickcleanpro.phonecleaner.core.permission.PermissionStatus
import com.quickcleanpro.phonecleaner.presentation.app.LocalExternalActivityLaunchHandler
import com.quickcleanpro.phonecleaner.presentation.common.components.popups.CleanXPermissionRequiredDialog
import com.quickcleanpro.phonecleaner.presentation.common.components.popups.InlinePermissionOverlay
import kotlinx.coroutines.delay

val LocalCleanXPermissionCoordinator =
    compositionLocalOf<CleanXPermissionCoordinator> { NoOpCleanXPermissionCoordinator }

@Stable
interface CleanXPermissionCoordinator {
    fun isGranted(action: CleanXProtectedAction): Boolean

    fun guard(
        action: CleanXProtectedAction,
        onGranted: () -> Unit,
    )

    fun request(action: CleanXProtectedAction) {
        guard(action) {}
    }
}

private object NoOpCleanXPermissionCoordinator : CleanXPermissionCoordinator {
    override fun isGranted(action: CleanXProtectedAction): Boolean = false

    override fun guard(
        action: CleanXProtectedAction,
        onGranted: () -> Unit,
    ) = Unit
}

@Composable
fun CleanXPermissionCoordinatorProvider(content: @Composable () -> Unit) {
    val context = LocalContext.current
    val manager =
        remember(context) {
            CleanXPermissionRegistry.protectedActionPermissionManager(context)
        }
    val state = remember(context, manager) { CleanXPermissionCoordinatorState(context, manager) }
    val latestContent by rememberUpdatedState(content)

    CompositionLocalProvider(LocalCleanXPermissionCoordinator provides state) {
        latestContent()
        CleanXPermissionPromptHost(state = state)
    }
}

private class CleanXPermissionCoordinatorState(
    private val context: Context,
    private val permissionManager: PermissionManager<CleanXProtectedAction>,
) : CleanXPermissionCoordinator {
    var session by mutableStateOf<PermissionSession?>(null)
        private set
    var pendingLaunch by mutableStateOf<PermissionLaunch?>(null)
        private set

    override fun isGranted(action: CleanXProtectedAction): Boolean =
        status(action).granted

    override fun guard(
        action: CleanXProtectedAction,
        onGranted: () -> Unit,
    ) {
        if (session != null) return
        val status = status(action)
        if (status.granted) {
            onGranted()
            return
        }
        session =
            PermissionSession(
                action = action,
                missingPermission = status.missing.firstOrNull(),
                onGranted = onGranted,
            )
    }

    fun dismiss() {
        session = null
        pendingLaunch = null
    }

    fun onDialogSubmit() {
        val current = session ?: return
        when (val plan = permissionManager.requestPlan(context, current.action)) {
            PermissionRequestPlan.AlreadyGranted -> finishIfGranted()
            is PermissionRequestPlan.RequestRuntime -> {
                pendingLaunch = PermissionLaunch.Runtime(current.action, plan.permissions)
                session = current.copy(showDialog = false)
            }
            is PermissionRequestPlan.OpenSettings -> {
                pendingLaunch = PermissionLaunch.Settings(current.action, plan.intents)
                session = current.copy(showDialog = false, settingsLaunchPending = true)
            }
            PermissionRequestPlan.Unavailable -> dismiss()
        }
    }

    fun consumePendingLaunch(): PermissionLaunch? =
        pendingLaunch.also { pendingLaunch = null }

    fun markSettingsLaunchPending(action: CleanXProtectedAction) {
        val current = session ?: return
        if (current.action == action) {
            session = current.copy(settingsLaunchPending = true, settingsLaunchObservedPause = false)
        }
    }

    fun markSettingsLaunchObservedPause() {
        val current = session ?: return
        if (current.settingsLaunchPending) {
            session = current.copy(settingsLaunchObservedPause = true)
        }
    }

    fun onRuntimeResult(
        action: CleanXProtectedAction,
        grants: Map<String, Boolean>,
    ) {
        permissionManager.onRuntimeResult(context, action, grants)
        recheckAfterRuntimeRequest()
    }

    suspend fun onSettingsReturnIfReady() {
        val current = session ?: return
        if (!current.settingsLaunchPending || !current.settingsLaunchObservedPause) return
        delay(350L)
        recheckAfterSettingsReturn(current)
    }

    private fun recheckAfterRuntimeRequest() {
        val current = session ?: return
        val status = status(current.action)
        if (status.granted) {
            val onGranted = current.onGranted
            dismiss()
            onGranted()
            return
        }

        val nextMissing = status.missing.firstOrNull()
        if (current.missingPermission?.key != null &&
            nextMissing?.key != null &&
            current.missingPermission.key != nextMissing.key
        ) {
            session =
                current.copy(
                    missingPermission = nextMissing,
                    showDialog = true,
                    settingsLaunchPending = false,
                    settingsLaunchObservedPause = false,
                )
        } else {
            dismiss()
        }
    }

    private fun recheckAfterSettingsReturn(previous: PermissionSession) {
        val status = status(previous.action)
        if (status.granted) {
            val onGranted = previous.onGranted
            dismiss()
            onGranted()
            return
        }

        val nextMissing = status.missing.firstOrNull()
        if (previous.missingPermission?.key != null &&
            nextMissing?.key != null &&
            previous.missingPermission.key != nextMissing.key
        ) {
            session =
                previous.copy(
                    missingPermission = nextMissing,
                    showDialog = true,
                    settingsLaunchPending = false,
                    settingsLaunchObservedPause = false,
                )
        } else {
            dismiss()
        }
    }

    private fun finishIfGranted() {
        val current = session ?: return
        val status = status(current.action)
        if (status.granted) {
            val onGranted = current.onGranted
            dismiss()
            onGranted()
        } else {
            session = current.copy(missingPermission = status.missing.firstOrNull(), showDialog = true)
        }
    }

    private fun status(action: CleanXProtectedAction): PermissionStatus =
        runCatching { permissionManager.status(context, action) }
            .getOrDefault(PermissionStatus(granted = false, missing = emptyList()))
}

private data class PermissionSession(
    val action: CleanXProtectedAction,
    val missingPermission: AppPermission?,
    val onGranted: () -> Unit,
    val showDialog: Boolean = true,
    val settingsLaunchPending: Boolean = false,
    val settingsLaunchObservedPause: Boolean = false,
)

private sealed interface PermissionLaunch {
    val action: CleanXProtectedAction

    data class Runtime(
        override val action: CleanXProtectedAction,
        val permissions: Array<String>,
    ) : PermissionLaunch

    data class Settings(
        override val action: CleanXProtectedAction,
        val intents: List<Intent>,
    ) : PermissionLaunch
}

@Composable
private fun CleanXPermissionPromptHost(state: CleanXPermissionCoordinatorState) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val externalActivityLaunchHandler = LocalExternalActivityLaunchHandler.current
    val settingsReturnGeneration = remember { mutableStateOf(0) }
    val runtimeLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions(),
        ) { grants ->
            val action = state.session?.action ?: return@rememberLauncherForActivityResult
            state.onRuntimeResult(action, grants)
        }
    val settingsLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.StartActivityForResult(),
        ) {
            settingsReturnGeneration.value += 1
        }

    DisposableEffect(lifecycleOwner, state) {
        val observer =
            LifecycleEventObserver { _, event ->
                when (event) {
                    Lifecycle.Event.ON_PAUSE,
                    Lifecycle.Event.ON_STOP,
                    -> state.markSettingsLaunchObservedPause()
                    Lifecycle.Event.ON_RESUME -> settingsReturnGeneration.value += 1
                    else -> Unit
                }
            }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(state.pendingLaunch) {
        when (val launch = state.consumePendingLaunch()) {
            is PermissionLaunch.Runtime -> runtimeLauncher.launch(launch.permissions)
            is PermissionLaunch.Settings -> {
                var launched = false
                for (intent in launch.intents) {
                    try {
                        externalActivityLaunchHandler.markLaunch()
                        state.markSettingsLaunchPending(launch.action)
                        settingsLauncher.launch(intent)
                        launched = true
                        break
                    } catch (_: ActivityNotFoundException) {
                        externalActivityLaunchHandler.cancelLaunch()
                    } catch (_: Exception) {
                        externalActivityLaunchHandler.cancelLaunch()
                    }
                }
                if (!launched) {
                    state.dismiss()
                }
            }
            null -> Unit
        }
    }

    LaunchedEffect(settingsReturnGeneration.value) {
        state.onSettingsReturnIfReady()
    }

    val session = state.session
    if (session?.showDialog == true) {
        BackHandler { state.dismiss() }
        InlinePermissionOverlay(onDismiss = state::dismiss) {
            CleanXPermissionRequiredDialog(
                copy = CleanXPermissionRegistry.copyFor(session.action, session.missingPermission),
                onSubmit = state::onDialogSubmit,
                onCancel = state::dismiss,
            )
        }
    }
}

@Composable
fun rememberPermissionGranted(action: CleanXProtectedAction): Boolean {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val manager =
        remember(context) {
            CleanXPermissionRegistry.protectedActionPermissionManager(context)
        }

    fun checkGranted(): Boolean =
        manager.status(context, action).granted

    var granted by remember(action, manager) { mutableStateOf(checkGranted()) }

    DisposableEffect(lifecycleOwner, action, manager) {
        val observer =
            LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME) {
                    granted = checkGranted()
                }
            }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    return granted
}
