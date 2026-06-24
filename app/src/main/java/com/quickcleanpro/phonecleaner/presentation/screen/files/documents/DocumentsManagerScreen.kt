package com.quickcleanpro.phonecleaner.presentation.screen.files.documents

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
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
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.components.FileManagerListView
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.components.FileManagerPageBrush
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.components.FileManagerTopAction
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.leaveBack
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.leaveHome
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.rememberFileManagerPermissionState
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.toFileDetailDisplayItem
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.views.FileOperationPhaseContent
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.views.detail.FileManagerDetailView
import com.quickcleanpro.phonecleaner.utils.FileSizeFormatter
import org.koin.androidx.compose.koinViewModel

@Composable
fun DocumentsManagerScreen() {
    DocumentsManagerScreenState(
        viewModel = koinViewModel(),
    )
}

@Composable
private fun DocumentsManagerScreenState(
    viewModel: DocumentsManagerViewModel,
) {
    val router = LocalRouter.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val permissionState = rememberFileManagerPermissionState()
    var showStopDialog by remember { mutableStateOf(false) }
    var blockedPhase by remember { mutableStateOf<FileOperationPhase?>(null) }
    val displayState = blockedPhase?.let { uiState.copy(phase = it) } ?: uiState
    val latestPermissionGranted by rememberUpdatedState(permissionState.granted)
    val latestDisplayState by rememberUpdatedState(displayState)
    val latestShowStopDialog by rememberUpdatedState(showStopDialog)
    val latestLeavingPage by rememberUpdatedState(permissionState.leavingPage)
    val scrollStates = remember { mutableMapOf<Int, ScrollState>() }
    fun scrollStateForTab(index: Int): ScrollState =
        scrollStates.getOrPut(index) { ScrollState(0) }
    val isDetailMode = displayState.detailStartIndex != null
    val visibleItems = displayState.visibleDisplayItems
    val showDetail = displayState.phase == FileOperationPhase.Browsing ||
        displayState.phase == FileOperationPhase.ConfirmDelete

    DisposableEffect(lifecycleOwner, viewModel) {
        var skipInitialResume = true
        val observer = LifecycleEventObserver { _, event ->
            if (event != Lifecycle.Event.ON_RESUME) return@LifecycleEventObserver
            if (skipInitialResume) {
                skipInitialResume = false
                return@LifecycleEventObserver
            }
            val canRefresh =
                    latestPermissionGranted &&
                    !latestLeavingPage &&
                    !latestShowStopDialog &&
                    latestDisplayState.phase == FileOperationPhase.Browsing
            if (canRefresh) {
                viewModel.refresh()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

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
        title = stringResource(R.string.nav_documents),
        onBack = { handleBack() },
        actions = {
            FileManagerTopAction(
                actionText = if (isDetailMode) stringResource(R.string.file_delete_count, displayState.selectedIds.size) else null,
                actionEnabled = displayState.selectedIds.isNotEmpty(),
                onAction = viewModel::requestDelete,
            )
        },
        bottomBar = {
            if (permissionState.granted && displayState.phase == FileOperationPhase.Browsing && !isDetailMode) {
                CleanXBottomActionBar(
                    enabled = displayState.selectedIds.isNotEmpty(),
                    text = if (displayState.selectedSizeBytes > 0L) {
                        stringResource(R.string.file_delete_size, FileSizeFormatter.format(displayState.selectedSizeBytes).replace(" ", ""))
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
        DocumentsManagerContent(
            uiState = displayState,
            showDetail = showDetail && isDetailMode && visibleItems.isNotEmpty(),
            scrollState = scrollStateForTab(displayState.selectedTabIndex),
            onTabSelected = viewModel::selectTab,
            onToggleAll = viewModel::toggleVisibleItems,
            onSelect = viewModel::toggleSelection,
            onOpenDetail = { item ->
                viewModel.openDetail(visibleItems.indexOfFirst { it.id == item.id })
            },
            onContinue = viewModel::continueManaging,
        )
    }

    FileManagerStopOperationDialog(
        visible = showStopDialog,
        permissionGranted = permissionState.granted,
        onQuit = {
            showStopDialog = false
            viewModel.cancelActiveOperation()
            permissionState.leaveBack(router)
        },
        onResume = {
            showStopDialog = false
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
private fun DocumentsManagerContent(
    uiState: DocumentsManagerUiState,
    showDetail: Boolean,
    scrollState: ScrollState,
    onTabSelected: (Int) -> Unit,
    onToggleAll: () -> Unit,
    onSelect: (Int) -> Unit,
    onOpenDetail: (com.quickcleanpro.phonecleaner.presentation.screen.files.common.FileListDisplayItem) -> Unit,
    onContinue: () -> Unit,
) {
    val visibleItems = uiState.visibleDisplayItems
    if (showDetail) {
        FileManagerDetailView(
            items = visibleItems.map { it.toFileDetailDisplayItem() },
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
            scanningText = stringResource(R.string.file_scanning_documents),
            deletingText = stringResource(R.string.file_deleting_files),
            resultAmount = result.first,
            resultUnit = result.second,
            resultCaption = stringResource(R.string.file_deleted_in_cleanup),
            onContinue = onContinue,
        ) {
            FileManagerListView(
                tabs = uiState.displayTabs,
                items = visibleItems,
                selectedTabIndex = uiState.selectedTabIndex,
                selectedIds = uiState.selectedIds,
                allSelected = uiState.allSelected,
                scrollState = scrollState,
                style = DocumentsListStyle,
                onTabSelected = onTabSelected,
                onToggleAll = onToggleAll,
                onSelect = onSelect,
                onOpenDetail = onOpenDetail,
            )
        }
    }
}
