package com.quickcleanpro.phonecleaner.presentation.screen.files.photoprivacy

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import com.quickcleanpro.phonecleaner.presentation.common.components.popups.DeleteConfirmDialog
import com.quickcleanpro.phonecleaner.presentation.common.route.LocalRouter
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.FileManagerErrorToastEffect
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.FileManagerNoResultsDialog
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.FileManagerScaffold
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.FileManagerStartEffect
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.FileManagerStopOperationDialog
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.FileOperationPhase
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.components.FileManagerPageBrush
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.leaveBack
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.leaveHome
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.rememberFileManagerPermissionState
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.views.FileOperationPhaseContent
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.views.list.FileManagerPhotoPrivacyView
import org.koin.androidx.compose.koinViewModel

@Composable
fun PhotoPrivacyManagerScreen() {
    PhotoPrivacyManagerScreenState(
        viewModel = koinViewModel(),
    )
}

@Composable
private fun PhotoPrivacyManagerScreenState(
    viewModel: PhotoPrivacyManagerViewModel,
) {
    val router = LocalRouter.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val permissionState = rememberFileManagerPermissionState()
    var showStopDialog by remember { mutableStateOf(false) }
    var blockedPhase by remember { mutableStateOf<FileOperationPhase?>(null) }
    val displayState = blockedPhase?.let { uiState.copy(phase = it) } ?: uiState

    fun handleBack() {
        when {
            !permissionState.granted -> {
                permissionState.leaveBack(router)
            }
            displayState.phase == FileOperationPhase.Scanning || displayState.phase == FileOperationPhase.Deleting -> {
                blockedPhase = displayState.phase
                showStopDialog = true
            }
            displayState.phase == FileOperationPhase.ConfirmDelete -> viewModel.cancelRemoveLocation()
            else -> permissionState.leaveHome(router)
        }
    }

    FileManagerErrorToastEffect(uiState.errorMessage, viewModel::clearError)

    FileManagerStartEffect(permissionState, viewModel::startIfNeeded) {
        permissionState.leaveHome(router)
    }

    BackHandler(enabled = permissionState.granted) { handleBack() }

    FileManagerScaffold(
        title = stringResource(R.string.nav_photo_privacy),
        onBack = ::handleBack,
        bottomBar = {
            if (permissionState.granted && displayState.phase == FileOperationPhase.Browsing) {
                CleanXBottomActionBar(
                    enabled = displayState.selectedIds.isNotEmpty(),
                    text = stringResource(R.string.file_remove_location_data),
                    onClick = viewModel::requestRemoveLocation,
                    backgroundColor = Color.Transparent,
                    buttonModifier = Modifier.height(52.dp),
                    buttonCornerRadius = 10.dp,
                    buttonFontSize = 20.sp,
                )
            }
        }
    ) {
        PhotoPrivacyManagerContent(
            uiState = displayState,
            onToggleAll = viewModel::toggleVisibleItems,
            onSelect = viewModel::toggleSelection,
            onContinue = viewModel::continueManaging,
        )
    }

    FileManagerStopOperationDialog(
        visible = showStopDialog,
        permissionGranted = permissionState.granted,
        onQuit = {
            showStopDialog = false
            blockedPhase = null
            viewModel.cancelActiveOperation()
            permissionState.leaveBack(router)
        },
        onResume = {
            showStopDialog = false
            blockedPhase = null
        },
    )

    if (permissionState.granted && displayState.phase == FileOperationPhase.ConfirmDelete) {
        DeleteConfirmDialog(
            title = stringResource(R.string.file_remove_location_title),
            message = stringResource(R.string.file_remove_location_message),
            confirmText = stringResource(R.string.remove),
            onCancel = viewModel::cancelRemoveLocation,
            onDelete = viewModel::removeLocationData,
        )
    }

    FileManagerNoResultsDialog(
        visible = displayState.phase == FileOperationPhase.NoResults,
        permissionGranted = permissionState.granted,
        onBack = { permissionState.leaveBack(router) },
    )
}

@Composable
private fun PhotoPrivacyManagerContent(
    uiState: PhotoPrivacyManagerUiState,
    onToggleAll: () -> Unit,
    onSelect: (Int) -> Unit,
    onContinue: () -> Unit,
) {
    Column(
        modifier = Modifier
            .background(FileManagerPageBrush)
            .padding(horizontal = 16.dp),
    ) {
        FileOperationPhaseContent(
            phase = uiState.phase,
            scanningText = stringResource(R.string.file_scanning_photo_privacy),
            deletingText = stringResource(R.string.file_removing_location_data_progress),
            resultAmount = uiState.removedLocationCount.toString(),
            resultUnit = stringResource(R.string.file_photos),
            resultCaption = stringResource(R.string.file_location_data_removed),
            onContinue = onContinue,
        ) {
            FileManagerPhotoPrivacyView(
                items = uiState.displayItems,
                selectedIds = uiState.selectedIds,
                allSelected = uiState.allSelected,
                onToggleAll = onToggleAll,
                onSelect = onSelect,
            )
        }
    }
}
