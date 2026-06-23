package com.quickcleanpro.phonecleaner.presentation.screen.files.common

import android.app.Activity
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.quickcleanpro.phonecleaner.presentation.common.components.CleanXScaffoldPage
import com.quickcleanpro.phonecleaner.presentation.common.components.popups.DeleteConfirmDialog
import com.quickcleanpro.phonecleaner.presentation.common.components.popups.NoResultsDialog
import com.quickcleanpro.phonecleaner.presentation.common.components.popups.StopScanDialog
import com.quickcleanpro.phonecleaner.presentation.common.permission.CleanXProtectedAction
import com.quickcleanpro.phonecleaner.presentation.common.permission.LocalCleanXPermissionCoordinator
import com.quickcleanpro.phonecleaner.presentation.common.permission.PermissionGateConfig
import com.quickcleanpro.phonecleaner.presentation.common.permission.rememberPermissionGranted
import com.quickcleanpro.phonecleaner.presentation.common.route.RouteManager
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.components.FileManagerPageBrush

internal enum class FileOperationPhase {
    Scanning,
    Browsing,
    ConfirmDelete,
    Deleting,
    CompleteAnimation,
    Result,
    NoResults
}

@Composable
internal fun rememberFileManagerPermissionState(
    permissionGateConfig: PermissionGateConfig?,
): FileManagerPermissionState {
    val observedPermissionGranted = rememberPermissionGranted(permissionGateConfig)
    var permissionGranted by remember(permissionGateConfig?.cleanXFeature) {
        mutableStateOf(observedPermissionGranted)
    }
    var isLeavingPage by remember { mutableStateOf(false) }

    LaunchedEffect(observedPermissionGranted) {
        permissionGranted = observedPermissionGranted
    }

    return FileManagerPermissionState(
        granted = permissionGranted,
        leavingPage = isLeavingPage,
        markLeaving = { isLeavingPage = true },
        onPermissionChanged = { permissionGranted = it },
    )
}

internal class FileManagerPermissionState(
    val granted: Boolean,
    val leavingPage: Boolean,
    val markLeaving: () -> Unit,
    val onPermissionChanged: (Boolean) -> Unit,
)

internal fun FileManagerPermissionState.leaveBack(router: RouteManager) {
    markLeaving()
    router.goBack()
}

internal fun FileManagerPermissionState.leaveHome(router: RouteManager) {
    markLeaving()
    router.goHome()
}

@Composable
internal fun FileManagerScaffold(
    title: String,
    permissionGateConfig: PermissionGateConfig?,
    permissionState: FileManagerPermissionState,
    onBack: () -> Unit,
    actions: @Composable (androidx.compose.foundation.layout.RowScope.() -> Unit)? = null,
    bottomBar: @Composable () -> Unit = {},
    content: @Composable () -> Unit,
) {
    CleanXScaffoldPage(
        title = title,
        scrollEnabled = false,
        contentPadding = PaddingValues(0.dp),
        backgroundBrush = FileManagerPageBrush,
        onBack = onBack,
        permissionGateConfig = permissionGateConfig,
        onPermissionGrantedChanged = permissionState.onPermissionChanged,
        actions = actions,
        bottomBar = bottomBar,
    ) {
        content()
    }
}

@Composable
internal fun FileManagerErrorToastEffect(
    errorMessage: String?,
    onConsumed: () -> Unit
) {
    val context = LocalContext.current
    LaunchedEffect(errorMessage) {
        val message = errorMessage ?: return@LaunchedEffect
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        onConsumed()
    }
}

@Composable
internal fun FileManagerStartEffect(
    permissionState: FileManagerPermissionState,
    onStartIfNeeded: () -> Unit,
) {
    val latestStartIfNeeded by rememberUpdatedState(onStartIfNeeded)
    LaunchedEffect(permissionState.granted, permissionState.leavingPage) {
        if (permissionState.granted && !permissionState.leavingPage) {
            latestStartIfNeeded()
        }
    }
}

@Composable
internal fun FileManagerStopOperationDialog(
    visible: Boolean,
    permissionGranted: Boolean,
    onQuit: () -> Unit,
    onResume: () -> Unit,
) {
    if (permissionGranted && visible) {
        StopScanDialog(
            onQuit = onQuit,
            onResume = onResume,
        )
    }
}

@Composable
internal fun FileManagerDeleteConfirmDialog(
    visible: Boolean,
    permissionGranted: Boolean,
    selectedUris: List<Uri>,
    onCancel: () -> Unit,
    onBeforeDeleteRequest: () -> Unit = {},
    onDeleteReady: () -> Unit,
    onRejected: () -> Unit,
) {
    val context = LocalContext.current
    val permissionCoordinator = LocalCleanXPermissionCoordinator.current
    val (launchDeleteRequest, deleteDirectly) = rememberMediaStoreDeleteLauncher(
        onConfirmed = onDeleteReady,
        onRejected = onRejected,
    )
    if (permissionGranted && visible) {
        DeleteConfirmDialog(
            onCancel = onCancel,
            onDelete = {
                permissionCoordinator.guard(CleanXProtectedAction.FileManagerDeleteFiles) {
                    onBeforeDeleteRequest()
                    requestMediaStoreDeleteOrDeleteDirectly(
                        context = context,
                        uris = selectedUris,
                        launchRequest = launchDeleteRequest,
                        deleteDirectly = deleteDirectly,
                    )
                }
            },
        )
    }
}

@Composable
internal fun FileManagerNoResultsDialog(
    visible: Boolean,
    permissionGranted: Boolean,
    onBack: () -> Unit,
) {
    if (permissionGranted && visible) {
        NoResultsDialog(onBack = onBack)
    }
}

@Composable
internal fun rememberMediaStoreDeleteLauncher(
    onConfirmed: () -> Unit,
    onRejected: () -> Unit,
): Pair<(IntentSenderRequest) -> Unit, () -> Unit> {
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            onConfirmed()
        } else {
            onRejected()
        }
    }
    return ({ request: IntentSenderRequest -> launcher.launch(request) }) to onConfirmed
}
