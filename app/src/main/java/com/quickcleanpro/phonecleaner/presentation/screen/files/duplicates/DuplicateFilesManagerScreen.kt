package com.quickcleanpro.phonecleaner.presentation.screen.files.duplicates

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.quickcleanpro.phonecleaner.R
import com.quickcleanpro.phonecleaner.presentation.common.components.CleanXBottomActionBar
import com.quickcleanpro.phonecleaner.presentation.common.route.LocalRouter
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.FileManagerDeleteConfirmDialog
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.FileManagerErrorToastEffect
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.FileManagerNoResultsDialog
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.FileManagerScaffold
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.FileManagerStartEffect
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.FileManagerStopOperationDialog
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.FileOperationPhase
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.leaveBack
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.leaveHome
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.rememberFileManagerPermissionState
import com.quickcleanpro.phonecleaner.presentation.screen.files.duplicates.views.DuplicateFilesManagerContentView
import com.quickcleanpro.phonecleaner.utils.FileSizeFormatter
import org.koin.androidx.compose.koinViewModel

@Composable
fun DuplicateFilesManagerScreen() {
    DuplicateFilesManagerScreenState(
        viewModel = koinViewModel(),
    )
}

@Composable
private fun DuplicateFilesManagerScreenState(
    viewModel: DuplicateFilesManagerViewModel,
) {
    val router = LocalRouter.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val permissionState = rememberFileManagerPermissionState()
    var showStopDialog by remember { mutableStateOf(false) }
    var blockedOperationPhase by remember { mutableStateOf<FileOperationPhase?>(null) }
    val displayState =
        blockedOperationPhase
            ?.let { blockedPhase -> uiState.copy(phase = blockedPhase) }
            ?: uiState
    val groupListScrollState = remember { ScrollState(0) }
    val groupDetailScrollStates = remember { mutableMapOf<Int, ScrollState>() }
    fun scrollStateForGroup(groupId: Int): ScrollState =
        groupDetailScrollStates.getOrPut(groupId) { ScrollState(0) }
    val selectedGroup = displayState.selectedGroup

    FileManagerErrorToastEffect(
        errorMessage = uiState.errorMessage,
        onConsumed = viewModel::clearError
    )

    FileManagerStartEffect(permissionState, viewModel::startIfNeeded) {
        permissionState.leaveHome(router)
    }

    fun handleBack() {
        when {
            selectedGroup != null -> viewModel.closeGroup()
            !permissionState.granted -> {
                permissionState.leaveBack(router)
            }
            displayState.phase == FileOperationPhase.Scanning || displayState.phase == FileOperationPhase.Deleting -> {
                blockedOperationPhase = displayState.phase
                showStopDialog = true
            }
            displayState.phase == FileOperationPhase.Result -> permissionState.leaveHome(router)
            else -> {
                permissionState.leaveBack(router)
            }
        }
    }

    BackHandler(enabled = permissionState.granted) { handleBack() }

    FileManagerScaffold(
        title = stringResource(R.string.nav_duplicate_files),
        onBack = ::handleBack,
        bottomBar = {
            if (
                permissionState.granted &&
                (displayState.phase == FileOperationPhase.Browsing || displayState.phase == FileOperationPhase.ConfirmDelete) &&
                selectedGroup == null
            ) {
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

    FileManagerStopOperationDialog(
        visible = showStopDialog,
        permissionGranted = permissionState.granted,
        onQuit = {
            showStopDialog = false
            blockedOperationPhase = null
            viewModel.cancelActiveOperation()
            permissionState.leaveBack(router)
        },
        onResume = {
            showStopDialog = false
            blockedOperationPhase = null
        },
    )

    FileManagerDeleteConfirmDialog(
        visible = displayState.phase == FileOperationPhase.ConfirmDelete,
        permissionGranted = permissionState.granted,
        selectedUris = displayState.selectedUris,
        onCancel = viewModel::cancelDelete,
        onDeleteReady = viewModel::deleteSelectedFiles,
        onRejected = viewModel::rejectSystemDelete,
    )

    FileManagerNoResultsDialog(
        visible = displayState.phase == FileOperationPhase.NoResults,
        permissionGranted = permissionState.granted,
        onBack = { permissionState.leaveBack(router) },
    )
}

private fun formatDuplicateCleanupSize(bytes: Long): String =
    if (bytes == 0L) "0KB" else FileSizeFormatter.format(bytes)
