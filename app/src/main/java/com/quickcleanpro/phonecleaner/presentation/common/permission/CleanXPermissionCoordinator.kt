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

sealed interface PermissionRequestTarget {
    val key: String

    data class Action(
        val action: CleanXProtectedAction,
    ) : PermissionRequestTarget {
        override val key: String = "action:${action.key}"
    }

    data class Item(
        val item: CleanXPermissionItem,
    ) : PermissionRequestTarget {
        override val key: String = "item:${item.key}"
    }
}

@Stable
interface CleanXPermissionCoordinator {
    fun isGranted(action: CleanXProtectedAction): Boolean

    fun isGranted(item: CleanXPermissionItem): Boolean

    fun guard(
        action: CleanXProtectedAction,
        onGranted: () -> Unit,
    ) {
        guard(action, onGranted, onRejected = {})
    }

    fun guard(
        action: CleanXProtectedAction,
        onGranted: () -> Unit,
        onRejected: () -> Unit = {},
    )

    fun guardDirect(
        action: CleanXProtectedAction,
        onGranted: () -> Unit,
    ) {
        guardDirect(action, onGranted, onRejected = {})
    }

    fun guardDirect(
        action: CleanXProtectedAction,
        onGranted: () -> Unit,
        onRejected: () -> Unit = {},
    )

    fun request(
        item: CleanXPermissionItem,
        onGranted: () -> Unit = {},
    ) {
        request(item, onGranted, onRejected = {})
    }

    fun request(
        item: CleanXPermissionItem,
        onGranted: () -> Unit,
        onRejected: () -> Unit = {},
    )

    fun openSettings(
        item: CleanXPermissionItem,
        onGranted: () -> Unit = {},
        onRejected: () -> Unit = {},
    )

    fun request(action: CleanXProtectedAction) {
        guard(action, onGranted = {})
    }
}

private object NoOpCleanXPermissionCoordinator : CleanXPermissionCoordinator {
    override fun isGranted(action: CleanXProtectedAction): Boolean = false

    override fun isGranted(item: CleanXPermissionItem): Boolean = false

    override fun guard(
        action: CleanXProtectedAction,
        onGranted: () -> Unit,
        onRejected: () -> Unit,
    ) = Unit

    override fun guardDirect(
        action: CleanXProtectedAction,
        onGranted: () -> Unit,
        onRejected: () -> Unit,
    ) = Unit

    override fun request(
        item: CleanXPermissionItem,
        onGranted: () -> Unit,
        onRejected: () -> Unit,
    ) = Unit

    override fun openSettings(
        item: CleanXPermissionItem,
        onGranted: () -> Unit,
        onRejected: () -> Unit,
    ) = Unit
}

@Composable
fun CleanXPermissionCoordinatorProvider(content: @Composable () -> Unit) {
    val context = LocalContext.current
    val actionManager =
        remember(context) {
            CleanXPermissionRegistry.protectedActionPermissionManager(context)
        }
    val itemManager =
        remember(context) {
            CleanXPermissionRegistry.permissionItemManager(context)
        }
    val state = remember(context, actionManager, itemManager) {
        CleanXPermissionCoordinatorState(context, actionManager, itemManager)
    }
    val latestContent by rememberUpdatedState(content)

    CompositionLocalProvider(LocalCleanXPermissionCoordinator provides state) {
        latestContent()
        CleanXPermissionPromptHost(state = state)
    }
}

private class CleanXPermissionCoordinatorState(
    private val context: Context,
    private val actionPermissionManager: PermissionManager<CleanXProtectedAction>,
    private val itemPermissionManager: PermissionManager<CleanXPermissionItem>,
) : CleanXPermissionCoordinator {
    var session by mutableStateOf<PermissionSession?>(null)
        private set
    var pendingLaunch by mutableStateOf<PermissionLaunch?>(null)
        private set

    override fun isGranted(action: CleanXProtectedAction): Boolean =
        actionStatus(action).granted

    override fun isGranted(item: CleanXPermissionItem): Boolean =
        itemStatus(item).granted

    override fun guard(
        action: CleanXProtectedAction,
        onGranted: () -> Unit,
        onRejected: () -> Unit,
    ) {
        startSession(
            target = PermissionRequestTarget.Action(action),
            status = actionStatus(action),
            onGranted = onGranted,
            onRejected = onRejected,
        )
    }

    override fun guardDirect(
        action: CleanXProtectedAction,
        onGranted: () -> Unit,
        onRejected: () -> Unit,
    ) {
        startDirectSession(
            target = PermissionRequestTarget.Action(action),
            status = actionStatus(action),
            onGranted = onGranted,
            onRejected = onRejected,
        )
    }

    override fun request(
        item: CleanXPermissionItem,
        onGranted: () -> Unit,
        onRejected: () -> Unit,
    ) {
        startSession(
            target = PermissionRequestTarget.Item(item),
            status = itemStatus(item),
            onGranted = onGranted,
            onRejected = onRejected,
        )
    }

    override fun openSettings(
        item: CleanXPermissionItem,
        onGranted: () -> Unit,
        onRejected: () -> Unit,
    ) {
        startSettingsSession(
            target = PermissionRequestTarget.Item(item),
            status = itemStatus(item),
            onGranted = onGranted,
            onRejected = onRejected,
        )
    }

    fun dismiss(notifyRejected: Boolean = true) {
        val onRejected = if (notifyRejected) session?.onRejected else null
        session = null
        pendingLaunch = null
        onRejected?.invoke()
    }

    fun onDialogSubmit() {
        val current = session ?: return
        launchPermissionPlan(current, showSettingsDialog = false)
    }

    fun consumePendingLaunch(): PermissionLaunch? =
        pendingLaunch.also { pendingLaunch = null }

    fun markSettingsLaunchPending(target: PermissionRequestTarget) {
        val current = session ?: return
        if (current.target.key == target.key) {
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
        target: PermissionRequestTarget,
        grants: Map<String, Boolean>,
    ) {
        when (target) {
            is PermissionRequestTarget.Action ->
                actionPermissionManager.onRuntimeResult(context, target.action, grants)
            is PermissionRequestTarget.Item ->
                itemPermissionManager.onRuntimeResult(context, target.item, grants)
        }
        recheckAfterRuntimeRequest()
    }

    suspend fun onSettingsReturnIfReady() {
        val current = session ?: return
        if (!current.settingsLaunchPending || !current.settingsLaunchObservedPause) return
        delay(350L)
        recheckAfterSettingsReturn(current)
    }

    private fun startSession(
        target: PermissionRequestTarget,
        status: PermissionStatus,
        onGranted: () -> Unit,
        onRejected: () -> Unit,
    ) {
        if (session != null) return
        if (status.granted) {
            onGranted()
            return
        }
        session =
            PermissionSession(
                target = target,
                missingPermission = status.missing.firstOrNull(),
                onGranted = onGranted,
                onRejected = onRejected,
            )
    }

    private fun startDirectSession(
        target: PermissionRequestTarget,
        status: PermissionStatus,
        onGranted: () -> Unit,
        onRejected: () -> Unit,
    ) {
        if (session != null) return
        if (status.granted) {
            onGranted()
            return
        }
        val current =
            PermissionSession(
                target = target,
                missingPermission = status.missing.firstOrNull(),
                onGranted = onGranted,
                onRejected = onRejected,
                showDialog = false,
            )
        session = current
        launchPermissionPlan(current, showSettingsDialog = true)
    }

    private fun startSettingsSession(
        target: PermissionRequestTarget,
        status: PermissionStatus,
        onGranted: () -> Unit,
        onRejected: () -> Unit,
    ) {
        if (session != null) return
        val current =
            PermissionSession(
                target = target,
                missingPermission = status.missing.firstOrNull(),
                onGranted = onGranted,
                onRejected = onRejected,
                showDialog = false,
            )
        session = current
        when (val plan = settingsPlan(target)) {
            PermissionRequestPlan.AlreadyGranted -> finishIfGranted()
            is PermissionRequestPlan.OpenSettings -> {
                pendingLaunch = PermissionLaunch.Settings(current.target, plan.intents)
                session = current.copy(settingsLaunchPending = true)
            }
            PermissionRequestPlan.Unavailable,
            is PermissionRequestPlan.RequestRuntime,
            -> dismiss()
        }
    }

    private fun launchPermissionPlan(
        current: PermissionSession,
        showSettingsDialog: Boolean,
    ) {
        when (val plan = requestPlan(current.target)) {
            PermissionRequestPlan.AlreadyGranted -> finishIfGranted()
            is PermissionRequestPlan.RequestRuntime -> {
                pendingLaunch = PermissionLaunch.Runtime(current.target, plan.permissions)
                session = current.copy(showDialog = false)
            }
            is PermissionRequestPlan.OpenSettings -> {
                if (showSettingsDialog) {
                    session = current.copy(showDialog = true)
                } else {
                    pendingLaunch = PermissionLaunch.Settings(current.target, plan.intents)
                    session = current.copy(showDialog = false, settingsLaunchPending = true)
                }
            }
            PermissionRequestPlan.Unavailable -> dismiss()
        }
    }

    private fun recheckAfterRuntimeRequest() {
        val current = session ?: return
        val status = status(current.target)
        if (status.granted) {
            val onGranted = current.onGranted
            dismiss(notifyRejected = false)
            onGranted()
            return
        }

        val nextMissing = status.missing.firstOrNull()
        if (shouldContinuePermissionFlow(current.missingPermission, nextMissing)) {
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
        val status = status(previous.target)
        if (status.granted) {
            val onGranted = previous.onGranted
            dismiss(notifyRejected = false)
            onGranted()
            return
        }

        val nextMissing = status.missing.firstOrNull()
        if (shouldContinuePermissionFlow(previous.missingPermission, nextMissing)) {
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
        val status = status(current.target)
        if (status.granted) {
            val onGranted = current.onGranted
            dismiss(notifyRejected = false)
            onGranted()
        } else {
            session = current.copy(missingPermission = status.missing.firstOrNull(), showDialog = true)
        }
    }

    private fun requestPlan(target: PermissionRequestTarget): PermissionRequestPlan =
        when (target) {
            is PermissionRequestTarget.Action ->
                runCatching { actionPermissionManager.requestPlan(context, target.action) }
                    .getOrDefault(PermissionRequestPlan.Unavailable)
            is PermissionRequestTarget.Item ->
                runCatching { itemPermissionManager.requestPlan(context, target.item) }
                    .getOrDefault(PermissionRequestPlan.Unavailable)
        }

    private fun settingsPlan(target: PermissionRequestTarget): PermissionRequestPlan =
        when (target) {
            is PermissionRequestTarget.Action ->
                runCatching { actionPermissionManager.settingsPlan(context, target.action) }
                    .getOrDefault(PermissionRequestPlan.Unavailable)
            is PermissionRequestTarget.Item ->
                runCatching { itemPermissionManager.settingsPlan(context, target.item) }
                    .getOrDefault(PermissionRequestPlan.Unavailable)
        }

    private fun status(target: PermissionRequestTarget): PermissionStatus =
        when (target) {
            is PermissionRequestTarget.Action -> actionStatus(target.action)
            is PermissionRequestTarget.Item -> itemStatus(target.item)
        }

    private fun actionStatus(action: CleanXProtectedAction): PermissionStatus =
        runCatching { actionPermissionManager.status(context, action) }
            .getOrDefault(PermissionStatus(granted = false, missing = emptyList()))

    private fun itemStatus(item: CleanXPermissionItem): PermissionStatus =
        runCatching { itemPermissionManager.status(context, item) }
            .getOrDefault(PermissionStatus(granted = false, missing = emptyList()))
}

private data class PermissionSession(
    val target: PermissionRequestTarget,
    val missingPermission: AppPermission?,
    val onGranted: () -> Unit,
    val onRejected: () -> Unit,
    val showDialog: Boolean = true,
    val settingsLaunchPending: Boolean = false,
    val settingsLaunchObservedPause: Boolean = false,
)

private sealed interface PermissionLaunch {
    val target: PermissionRequestTarget

    data class Runtime(
        override val target: PermissionRequestTarget,
        val permissions: Array<String>,
    ) : PermissionLaunch

    data class Settings(
        override val target: PermissionRequestTarget,
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
            val target = state.session?.target ?: return@rememberLauncherForActivityResult
            state.onRuntimeResult(target, grants)
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
                        state.markSettingsLaunchPending(launch.target)
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
                copy =
                    when (val target = session.target) {
                        is PermissionRequestTarget.Action ->
                            CleanXPermissionRegistry.copyFor(target.action, session.missingPermission)
                        is PermissionRequestTarget.Item ->
                            CleanXPermissionRegistry.copyFor(target.item, session.missingPermission)
                    },
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

@Composable
fun rememberPermissionGranted(item: CleanXPermissionItem): Boolean {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val manager =
        remember(context) {
            CleanXPermissionRegistry.permissionItemManager(context)
        }

    fun checkGranted(): Boolean =
        manager.status(context, item).granted

    var granted by remember(item, manager) { mutableStateOf(checkGranted()) }

    DisposableEffect(lifecycleOwner, item, manager) {
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

private fun shouldContinuePermissionFlow(
    previousMissingPermission: AppPermission?,
    nextMissingPermission: AppPermission?,
): Boolean =
    previousMissingPermission?.key != null &&
        nextMissingPermission?.key != null &&
        previousMissingPermission.key != nextMissingPermission.key
