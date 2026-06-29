package com.quickcleanpro.phonecleaner.presentation.screen.files

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import org.koin.androidx.compose.koinViewModel
import com.quickcleanpro.phonecleaner.R
import com.quickcleanpro.phonecleaner.presentation.common.CleanXBackground
import com.quickcleanpro.phonecleaner.presentation.common.CleanXBottomActionBar
import com.quickcleanpro.phonecleaner.presentation.common.permission.CleanXProtectedAction
import com.quickcleanpro.phonecleaner.presentation.common.permission.LocalCleanXPermissionCoordinator
import com.quickcleanpro.phonecleaner.utils.FileSizeFormatter

@Composable
fun PhotosManagerScreen(
    onBack: () -> Unit,
    onResultBack: () -> Unit = {},
    onNavigateTool: (String) -> Unit
) {
    PhotosManagerScreenState(
        onBack = onBack,
        onResultBack = onResultBack,
        onNavigateTool = onNavigateTool,
        viewModel = viewModel()
    )
}

@Composable
internal fun PhotosManagerRoute(
    onBack: () -> Unit,
    onResultBack: () -> Unit,
    onNavigateTool: (String) -> Unit
) {
    val viewModel: PhotosManagerViewModel = koinViewModel()
    PhotosManagerScreenState(
        onBack = onBack,
        onResultBack = onResultBack,
        onNavigateTool = onNavigateTool,
        viewModel = viewModel
    )
}

@Composable
private fun PhotosManagerScreenState(
    onBack: () -> Unit,
    onResultBack: () -> Unit,
    onNavigateTool: (String) -> Unit,
    viewModel: FileCollectionViewModel
) {
    val context = LocalContext.current
    val permissionCoordinator = LocalCleanXPermissionCoordinator.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showStopDialog by remember { mutableStateOf(false) }
    val scrollStates = remember { mutableMapOf<Int, ScrollState>() }
    fun scrollStateForTab(index: Int): ScrollState =
        scrollStates.getOrPut(index) { ScrollState(0) }

    LaunchedEffect(viewModel) {
        permissionCoordinator.guard(
            action = CleanXProtectedAction.FileManagerLoadFiles,
            onGranted = { viewModel.load(FileCollectionKind.Photos) },
            onRejected = onBack,
        )
    }

    val photos = uiState.currentPhotos
    val selectedIds = uiState.selectedIds
    val selectedSize = uiState.selectedSizeBytes
    val detailStartIndex = uiState.detailStartIndex
    val deleteLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            permissionCoordinator.guard(
                action = CleanXProtectedAction.FileManagerDeleteFiles,
                onGranted = viewModel::deleteSelectedFiles,
            )
        } else {
            viewModel.rejectSystemDelete()
        }
    }

    BackHandler {
        when {
            detailStartIndex != null -> viewModel.closeDetail()
            uiState.phase == PhotosState.Scanning -> showStopDialog = true
            uiState.phase == PhotosState.Result -> onResultBack()
            else -> onBack()
        }
    }

    if (uiState.phase == PhotosState.Deleting) {
        DeletingAnimationContent(stringResource(R.string.file_deleting_photos))
        return
    }

    Scaffold(
        containerColor = CleanXBackground,
        topBar = {
            FileCollectionTopBar(
                title = stringResource(R.string.nav_photos),
                onBack = {
                    when {
                        detailStartIndex != null -> viewModel.closeDetail()
                        uiState.phase == PhotosState.Scanning -> showStopDialog = true
                        uiState.phase == PhotosState.Result -> onResultBack()
                        else -> onBack()
                    }
                },
                actionText = when {
                    detailStartIndex != null -> stringResource(R.string.file_delete_count, selectedIds.size)
                    uiState.phase == PhotosState.Browsing || uiState.phase == PhotosState.ConfirmDelete -> {
                        if (uiState.allSelected) {
                            stringResource(R.string.file_unselect_all)
                        } else {
                            stringResource(R.string.file_select_all)
                        }
                    }
                    else -> null
                },
                actionEnabled = detailStartIndex == null || selectedIds.isNotEmpty(),
                onAction = {
                    if (detailStartIndex != null) {
                        viewModel.requestDelete()
                    } else {
                        viewModel.toggleAllVisible()
                    }
                }
            )
        },
        bottomBar = {
            if ((uiState.phase == PhotosState.Browsing || uiState.phase == PhotosState.ConfirmDelete) && detailStartIndex == null) {
                CleanXBottomActionBar(
                    enabled = selectedIds.isNotEmpty(),
                    text = if (selectedIds.isEmpty()) {
                        stringResource(R.string.file_delete)
                    } else {
                        stringResource(R.string.file_delete_size, FileSizeFormatter.format(selectedSize))
                    },
                    onClick = viewModel::requestDelete
                )
            }
        }
    ) { paddingValues ->
        val visibleIds = uiState.visibleIds
        val allSelected = uiState.allSelected
        val startIndex = detailStartIndex

        if ((uiState.phase == PhotosState.Browsing || uiState.phase == PhotosState.ConfirmDelete) && startIndex != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                PhotoDetailScreen(
                    items = photos,
                    initialIndex = startIndex,
                    selectedIds = selectedIds,
                    selectedSize = selectedSize,
                    onToggleSelection = viewModel::toggleSelection
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(CleanXBackground)
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
            ) {
                when (uiState.phase) {
                    PhotosState.Scanning -> ScanningContent(text = stringResource(R.string.file_scanning_photos))
                    PhotosState.Browsing, PhotosState.ConfirmDelete -> {
                        PhotoBrowserContent(
                            tabs = uiState.photoTabs,
                            selectedTabIndex = uiState.selectedTabIndex,
                            photos = photos,
                            selectedIds = selectedIds,
                            scrollState = scrollStateForTab(uiState.selectedTabIndex),
                            onTabSelected = viewModel::selectTab,
                            onSelect = viewModel::toggleSelection,
                            onSelectAll = { viewModel.toggleIds(visibleIds) },
                            onOpenDetail = { item ->
                                viewModel.openDetail(photos.indexOfFirst { it.id == item.id })
                            }
                        )
                    }
                    PhotosState.Deleting -> Unit
                    PhotosState.CompleteAnimation -> CompleteAnimationContent()
                    PhotosState.Result -> PhotosResultContent(
                        onContinue = viewModel::continueManaging,
                        onNavigateTool = onNavigateTool,
                        deletedSize = uiState.deletedBytes
                    )
                    PhotosState.NoResults -> Unit
                }
            }
        }
    }

    if (showStopDialog) {
        StopScanDialog(
            onQuit = {
                showStopDialog = false
                onBack()
            },
            onResume = { showStopDialog = false }
        )
    }

    if (uiState.phase == PhotosState.ConfirmDelete) {
        DeleteConfirmDialog(
            onCancel = viewModel::cancelDelete,
            onDelete = {
                viewModel.closeDetail()
                requestMediaStoreDeleteOrDeleteDirectly(
                    context = context,
                    uris = uiState.selectedUris,
                    launchRequest = deleteLauncher::launch,
                    deleteDirectly = {
                        permissionCoordinator.guard(
                            action = CleanXProtectedAction.FileManagerDeleteFiles,
                            onGranted = viewModel::deleteSelectedFiles,
                        )
                    }
                )
            }
        )
    }

    if (uiState.phase == PhotosState.NoResults) {
        NoResultsDialog(onBack = onBack)
    }
}
