package com.quickcleanpro.phonecleaner.presentation.screen.files.photos

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ScrollState
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
import com.quickcleanpro.phonecleaner.presentation.common.route.LocalRouter
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.FileManagerDeleteConfirmDialog
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.FileManagerErrorToastEffect
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.FileManagerNoResultsDialog
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.FileManagerScaffold
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.FileManagerStartEffect
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.FileManagerStopOperationDialog
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.FileOperationPhase
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.components.FileManagerPageBrush
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.components.FileManagerTopAction
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.leaveBack
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.leaveHome
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.rememberFileManagerPermissionState
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.toFileDetailDisplayItem
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.views.FileOperationPhaseContent
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.views.detail.FileManagerDetailView
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.views.list.FileManagerGalleryBrowserView
import com.quickcleanpro.phonecleaner.utils.FileSizeFormatter
import org.koin.androidx.compose.koinViewModel

@Composable
fun PhotosManagerScreen() {
    PhotosManagerScreenState(
        viewModel = koinViewModel(),
    )
}

@Composable
private fun PhotosManagerScreenState(
    viewModel: PhotosManagerViewModel,
) {
    val router = LocalRouter.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val permissionState = rememberFileManagerPermissionState()
    var showStopDialog by remember { mutableStateOf(false) }
    var blockedPhase by remember { mutableStateOf<FileOperationPhase?>(null) }
    val displayState = blockedPhase?.let { uiState.copy(phase = it) } ?: uiState
    val scrollStates = remember { mutableMapOf<Int, ScrollState>() }
    fun scrollStateForTab(index: Int): ScrollState =
        scrollStates.getOrPut(index) { ScrollState(0) }
    val isDetailMode = displayState.detailStartIndex != null
    val currentItems = displayState.currentDisplayItems
    val showDetail = (displayState.phase == FileOperationPhase.Browsing ||
        displayState.phase == FileOperationPhase.ConfirmDelete) &&
        isDetailMode &&
        currentItems.isNotEmpty()

    fun handleBack() {
        when {
            isDetailMode -> viewModel.closeDetail()
            !permissionState.granted -> {
                permissionState.leaveBack(router)
            }
            displayState.phase == FileOperationPhase.Scanning || displayState.phase == FileOperationPhase.Deleting -> {
                blockedPhase = displayState.phase
                showStopDialog = true
            }
            displayState.phase == FileOperationPhase.ConfirmDelete -> viewModel.cancelDelete()
            else -> permissionState.leaveHome(router)
        }
    }

    FileManagerErrorToastEffect(
        errorMessage = uiState.errorMessage,
        onConsumed = viewModel::clearError
    )

    FileManagerStartEffect(permissionState, viewModel::startIfNeeded) {
        permissionState.leaveHome(router)
    }

    BackHandler(enabled = permissionState.granted) { handleBack() }

    FileManagerScaffold(
        title = stringResource(R.string.nav_photos),
        onBack = ::handleBack,
        actions = {
            val actionText = when {
                showDetail -> stringResource(R.string.file_delete_count, displayState.selectedIds.size)
                displayState.phase == FileOperationPhase.Browsing || displayState.phase == FileOperationPhase.ConfirmDelete -> {
                    stringResource(if (displayState.allSelected) R.string.file_unselect_all else R.string.file_select_all)
                }
                else -> null
            }
            FileManagerTopAction(
                actionText = actionText,
                actionEnabled = !showDetail || displayState.selectedIds.isNotEmpty(),
                onAction = {
                    if (showDetail) {
                        viewModel.requestDelete()
                    } else {
                        viewModel.toggleVisibleItems()
                    }
                },
            )
        },
        bottomBar = {
            if (permissionState.granted && displayState.phase == FileOperationPhase.Browsing && !showDetail) {
                CleanXBottomActionBar(
                    enabled = displayState.selectedIds.isNotEmpty(),
                    text = if (displayState.selectedIds.isNotEmpty()) {
                        stringResource(R.string.file_delete_size, FileSizeFormatter.format(displayState.selectedSizeBytes))
                    } else {
                        stringResource(R.string.file_delete)
                    },
                    onClick = viewModel::requestDelete,
                    backgroundColor = Color.Transparent,
                    buttonModifier = Modifier.height(52.dp),
                    buttonCornerRadius = 10.dp,
                    buttonFontSize = 20.sp,
                )
            }
        }
    ) {
        PhotosManagerContent(
            uiState = displayState,
            showDetail = showDetail,
            scrollState = scrollStateForTab(displayState.selectedTabIndex),
            onTabSelected = viewModel::selectTab,
            onSelect = viewModel::toggleSelection,
            onSelectAll = viewModel::toggleVisibleItems,
            onOpenDetail = { item ->
                viewModel.openDetail(currentItems.indexOfFirst { it.id == item.id })
            },
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

    FileManagerDeleteConfirmDialog(
        visible = displayState.phase == FileOperationPhase.ConfirmDelete,
        permissionGranted = permissionState.granted,
        selectedUris = displayState.selectedUris,
        onCancel = viewModel::cancelDelete,
        onBeforeDeleteRequest = viewModel::closeDetail,
        onDeleteReady = viewModel::deleteSelectedFiles,
        onRejected = viewModel::rejectSystemDelete,
    )

    FileManagerNoResultsDialog(
        visible = displayState.phase == FileOperationPhase.NoResults,
        permissionGranted = permissionState.granted,
        onBack = { permissionState.leaveBack(router) },
    )
}

@Composable
private fun PhotosManagerContent(
    uiState: PhotosManagerUiState,
    showDetail: Boolean,
    scrollState: ScrollState,
    onTabSelected: (Int) -> Unit,
    onSelect: (Int) -> Unit,
    onSelectAll: () -> Unit,
    onOpenDetail: (com.quickcleanpro.phonecleaner.presentation.screen.files.common.FileImageDisplayItem) -> Unit,
    onContinue: () -> Unit,
) {
    val currentItems = uiState.currentDisplayItems
    if (showDetail) {
        FileManagerDetailView(
            items = currentItems.map { it.toFileDetailDisplayItem() },
            initialIndex = uiState.detailStartIndex ?: 0,
            selectedIds = uiState.selectedIds,
            selectedSize = uiState.selectedSizeBytes,
            onToggleSelection = onSelect,
        )
        return
    }

    val result = uiState.resultSize
    Column(
        modifier = Modifier
            .background(FileManagerPageBrush)
            .padding(horizontal = 16.dp),
    ) {
        FileOperationPhaseContent(
            phase = uiState.phase,
            scanningText = stringResource(R.string.file_scanning_photos),
            deletingText = stringResource(R.string.file_deleting_photos),
            resultAmount = result.first,
            resultUnit = result.second,
            resultCaption = stringResource(R.string.file_deleted_in_cleanup),
            onContinue = onContinue,
        ) {
            FileManagerGalleryBrowserView(
                tabs = uiState.displayTabs,
                selectedTabIndex = uiState.selectedTabIndex,
                items = currentItems,
                selectedIds = uiState.selectedIds,
                scrollState = scrollState,
                onTabSelected = onTabSelected,
                onSelect = onSelect,
                onSelectAll = onSelectAll,
                onOpenDetail = onOpenDetail,
            )
        }
    }
}
