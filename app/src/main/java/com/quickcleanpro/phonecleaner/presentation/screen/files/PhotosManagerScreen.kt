package com.quickcleanpro.phonecleaner.presentation.screen.files

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import org.koin.androidx.compose.koinViewModel
import com.quickcleanpro.phonecleaner.R
import com.quickcleanpro.phonecleaner.presentation.common.CleanXBottomActionBar
import com.quickcleanpro.phonecleaner.presentation.common.components.CleanXEmptyScanResult
import com.quickcleanpro.phonecleaner.presentation.common.components.CleanXScaffoldPage
import com.quickcleanpro.phonecleaner.presentation.common.permission.PermissionGateConfig
import com.quickcleanpro.phonecleaner.presentation.common.route.LocalRouter
import com.quickcleanpro.phonecleaner.utils.FileSizeFormatter

private val PhotosPageBrush = Brush.verticalGradient(
    colors = listOf(Color(0xFFE3ECFD), Color(0xFFDFEBF5)),
)

@Composable
fun PhotosManagerScreen() {
    PhotosManagerScreenState(
        viewModel = viewModel()
    )
}

@Composable
internal fun PhotosManagerRoute(
    permissionGateConfig: PermissionGateConfig? = null
) {
    val viewModel: PhotosManagerViewModel = koinViewModel()
    PhotosManagerScreenState(
        viewModel = viewModel,
        permissionGateConfig = permissionGateConfig
    )
}

@Composable
private fun PhotosManagerScreenState(
    viewModel: FileCollectionViewModel,
    permissionGateConfig: PermissionGateConfig? = null
) {
    val router = LocalRouter.current
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showStopDialog by remember { mutableStateOf(false) }
    val scrollStates = remember { mutableMapOf<Int, ScrollState>() }
    fun scrollStateForTab(index: Int): ScrollState =
        scrollStates.getOrPut(index) { ScrollState(0) }

    LaunchedEffect(viewModel) {
        viewModel.load(FileCollectionKind.Photos)
    }

    val photos = uiState.currentPhotos
    val selectedIds = uiState.selectedIds
    val selectedSize = uiState.selectedSizeBytes
    val detailStartIndex = uiState.detailStartIndex
    val deleteLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            viewModel.deleteSelectedFiles()
        } else {
            viewModel.rejectSystemDelete()
        }
    }

    BackHandler {
        when {
            detailStartIndex != null -> viewModel.closeDetail()
            uiState.phase == PhotosState.Scanning -> showStopDialog = true
            uiState.phase == PhotosState.Result -> router.goHome()
            else -> router.goBack()
        }
    }

    if (uiState.phase == PhotosState.Deleting) {
        DeletingAnimationContent(stringResource(R.string.file_deleting_photos))
        return
    }

    CleanXScaffoldPage(
        title = stringResource(R.string.nav_photos),
        scrollEnabled = false,
        contentPadding = PaddingValues(0.dp),
        backgroundBrush = PhotosPageBrush,
        onBack = {
            when {
                detailStartIndex != null -> viewModel.closeDetail()
                uiState.phase == PhotosState.Scanning -> showStopDialog = true
                uiState.phase == PhotosState.Result -> router.goHome()
                else -> router.goBack()
            }
        },
        permissionGateConfig = permissionGateConfig,
        actions = {
            FileCollectionTopAction(
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
                    onClick = viewModel::requestDelete,
                    backgroundColor = Color.Transparent,
                    buttonModifier = Modifier.height(52.dp),
                    buttonCornerRadius = 10.dp,
                    buttonFontSize = 20.sp,
                )
            }
        }
    ) {
        val visibleIds = uiState.visibleIds
        val allSelected = uiState.allSelected
        val startIndex = detailStartIndex

        if ((uiState.phase == PhotosState.Browsing || uiState.phase == PhotosState.ConfirmDelete) && startIndex != null) {
            Box(
                modifier = Modifier.fillMaxSize()
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
                    .background(PhotosPageBrush)
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
                        deletedSize = uiState.deletedBytes
                    )
                    PhotosState.NoResults -> CleanXEmptyScanResult(
                        message = stringResource(R.string.file_scan_completed_no_results),
                    )
                }
            }
        }
    }

    if (showStopDialog) {
        StopScanDialog(
            onQuit = {
                showStopDialog = false
                router.goBack()
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
                    deleteDirectly = viewModel::deleteSelectedFiles
                )
            }
        )
    }

    if (uiState.phase == PhotosState.NoResults) {
        NoResultsDialog(onBack = { router.goBack() })
    }
}
