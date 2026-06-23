package com.quickcleanpro.phonecleaner.presentation.screen.files.duplicates

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.quickcleanpro.phonecleaner.R
import com.quickcleanpro.phonecleaner.presentation.common.components.CleanXBottomActionBar
import com.quickcleanpro.phonecleaner.presentation.common.components.CleanXScaffoldPage
import com.quickcleanpro.phonecleaner.presentation.common.components.popups.DeleteConfirmDialog
import com.quickcleanpro.phonecleaner.presentation.common.components.popups.NoResultsDialog
import com.quickcleanpro.phonecleaner.presentation.common.components.popups.StopScanDialog
import com.quickcleanpro.phonecleaner.presentation.common.permission.PermissionGateConfig
import com.quickcleanpro.phonecleaner.presentation.common.permission.rememberPermissionGranted
import com.quickcleanpro.phonecleaner.presentation.common.route.LocalRouter
import com.quickcleanpro.phonecleaner.presentation.screen.files.duplicates.DuplicateFilesManagerViewModel
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.FileManagerPhase
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.requestMediaStoreDeleteOrDeleteDirectly
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.components.FileManagerPageBrush
import com.quickcleanpro.phonecleaner.presentation.screen.files.duplicates.views.DuplicateFilesManagerContentView
import com.quickcleanpro.phonecleaner.utils.FileSizeFormatter
import org.koin.androidx.compose.koinViewModel

@Composable
fun DuplicateFilesManagerScreen(
    permissionGateConfig: PermissionGateConfig? = null
) {
    DuplicateFilesManagerScreenState(
        viewModel = koinViewModel(),
        permissionGateConfig = permissionGateConfig
    )
}

@Composable
private fun DuplicateFilesManagerScreenState(
    viewModel: DuplicateFilesManagerViewModel,
    permissionGateConfig: PermissionGateConfig? = null
) {
    val router = LocalRouter.current
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val observedPermissionGranted = rememberPermissionGranted(permissionGateConfig)
    var permissionGranted by remember(permissionGateConfig?.cleanXFeature) {
        mutableStateOf(observedPermissionGranted)
    }
    var showStopDialog by remember { mutableStateOf(false) }
    var blockedOperationPhase by remember { mutableStateOf<FileManagerPhase?>(null) }
    var isLeavingPage by remember { mutableStateOf(false) }
    val displayState =
        blockedOperationPhase
            ?.let { blockedPhase -> uiState.copy(phase = blockedPhase) }
            ?: uiState
    val groupListScrollState = remember { ScrollState(0) }
    val groupDetailScrollStates = remember { mutableMapOf<Int, ScrollState>() }
    fun scrollStateForGroup(groupId: Int): ScrollState =
        groupDetailScrollStates.getOrPut(groupId) { ScrollState(0) }
    val selectedGroup = displayState.selectedGroup
    val deleteLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            viewModel.deleteSelectedFiles()
        } else {
            viewModel.rejectSystemDelete()
        }
    }

    FileManagerErrorToastEffect(
        errorMessage = uiState.errorMessage,
        onConsumed = viewModel::clearError
    )

    LaunchedEffect(permissionGranted, viewModel) {
        if (permissionGranted && !isLeavingPage) {
            viewModel.startIfNeeded()
        }
    }

    LaunchedEffect(observedPermissionGranted) {
        permissionGranted = observedPermissionGranted
    }

    fun handleBack() {
        when {
            selectedGroup != null -> viewModel.closeGroup()
            !permissionGranted -> {
                isLeavingPage = true
                router.goBack()
            }
            displayState.phase == FileManagerPhase.Scanning || displayState.phase == FileManagerPhase.Deleting -> {
                blockedOperationPhase = displayState.phase
                showStopDialog = true
            }
            displayState.phase == FileManagerPhase.Result -> router.goHome()
            else -> {
                isLeavingPage = true
                router.goBack()
            }
        }
    }

    BackHandler(enabled = permissionGranted) { handleBack() }

    CleanXScaffoldPage(
        title = stringResource(R.string.nav_duplicate_files),
        scrollEnabled = false,
        contentPadding = PaddingValues(0.dp),
        backgroundBrush = FileManagerPageBrush,
        onBack = ::handleBack,
        permissionGateConfig = permissionGateConfig,
        onPermissionGrantedChanged = { permissionGranted = it },
        bottomBar = {
            if (permissionGranted && (displayState.phase == FileManagerPhase.Browsing || displayState.phase == FileManagerPhase.ConfirmDelete) && selectedGroup == null) {
                CleanXBottomActionBar(
                    enabled = displayState.filesToDelete.isNotEmpty(),
                    text = stringResource(R.string.file_clean_up_size, formatDuplicateCleanupSize(displayState.selectedDeleteSize)),
                    onClick = viewModel::requestDelete,
                    backgroundColor = Color.Transparent,
                    buttonModifier = Modifier.height(52.dp),
                    buttonCornerRadius = 10.dp,
                    buttonFontSize = 20.sp,
                )
            }
        }
    ) {
        DuplicateFilesManagerContentView(
            uiState = displayState,
            groupListScrollState = groupListScrollState,
            scrollStateForGroup = ::scrollStateForGroup,
            onToggleAll = viewModel::toggleAll,
            onOpenGroup = viewModel::openGroup,
            onToggleFile = viewModel::toggleFile,
            onAutoSelect = viewModel::autoSelectCurrentGroup,
            onToggleGroupSelection = viewModel::toggleCurrentGroupSelection,
            onContinue = viewModel::continueManaging,
        )
    }

    if (permissionGranted && showStopDialog) {
        StopScanDialog(
            onQuit = {
                showStopDialog = false
                blockedOperationPhase = null
                isLeavingPage = true
                viewModel.cancelActiveOperation()
                router.goBack()
            },
            onResume = {
                showStopDialog = false
                blockedOperationPhase = null
            }
        )
    }

    if (permissionGranted && displayState.phase == FileManagerPhase.ConfirmDelete) {
        DeleteConfirmDialog(
            onCancel = viewModel::cancelDelete,
            onDelete = {
                requestMediaStoreDeleteOrDeleteDirectly(
                    context = context,
                    uris = displayState.selectedUris,
                    launchRequest = deleteLauncher::launch,
                    deleteDirectly = viewModel::deleteSelectedFiles
                )
            }
        )
    }

    if (permissionGranted && displayState.phase == FileManagerPhase.NoResults) {
        NoResultsDialog(onBack = { router.goBack() })
    }
}

@Composable
private fun FileManagerErrorToastEffect(
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

private fun formatDuplicateCleanupSize(bytes: Long): String =
    if (bytes == 0L) "0KB" else FileSizeFormatter.format(bytes)
