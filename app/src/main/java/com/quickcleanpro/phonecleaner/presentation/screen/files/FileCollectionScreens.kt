package com.quickcleanpro.phonecleaner.presentation.screen.files

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import org.koin.androidx.compose.koinViewModel
import com.quickcleanpro.phonecleaner.R
import com.quickcleanpro.phonecleaner.presentation.common.CleanXBottomActionBar
import com.quickcleanpro.phonecleaner.presentation.common.components.CleanXScaffoldPage
import com.quickcleanpro.phonecleaner.presentation.common.permission.PermissionGateConfig
import com.quickcleanpro.phonecleaner.presentation.common.route.LocalRouter
import com.quickcleanpro.phonecleaner.utils.FileSizeFormatter

@Composable
fun ScreenshotsManagerScreen() {
    FileCollectionRoute(
        kind = FileCollectionKind.Screenshots,
    )
}

@Composable
internal fun ScreenshotsManagerRoute() {
    val viewModel: ScreenshotsManagerViewModel = koinViewModel()
    FileCollectionRoute(
        kind = FileCollectionKind.Screenshots,
        viewModel = viewModel
    )
}

@Composable
fun VideosManagerScreen() {
    FileCollectionRoute(
        kind = FileCollectionKind.Videos,
    )
}

@Composable
internal fun VideosManagerRoute() {
    val viewModel: VideosManagerViewModel = koinViewModel()
    FileCollectionRoute(
        kind = FileCollectionKind.Videos,
        viewModel = viewModel
    )
}

@Composable
fun AudiosManagerScreen() {
    FileCollectionRoute(
        kind = FileCollectionKind.Audios,
    )
}

@Composable
internal fun AudiosManagerRoute() {
    val viewModel: AudiosManagerViewModel = koinViewModel()
    FileCollectionRoute(
        kind = FileCollectionKind.Audios,
        viewModel = viewModel
    )
}

@Composable
fun SimilarPhotosManagerScreen() {
    FileCollectionRoute(
        kind = FileCollectionKind.SimilarPhotos,
    )
}

@Composable
internal fun SimilarPhotosManagerRoute() {
    val viewModel: SimilarPhotosManagerViewModel = koinViewModel()
    FileCollectionRoute(
        kind = FileCollectionKind.SimilarPhotos,
        viewModel = viewModel
    )
}

@Composable
fun PhotoPrivacyManagerScreen() {
    FileCollectionRoute(
        kind = FileCollectionKind.PhotoPrivacy,
    )
}

@Composable
internal fun PhotoPrivacyManagerRoute() {
    val viewModel: PhotoPrivacyManagerViewModel = koinViewModel()
    FileCollectionRoute(
        kind = FileCollectionKind.PhotoPrivacy,
        viewModel = viewModel
    )
}

@Composable
fun LargeFilesManagerScreen() {
    ManagedFileRoute(
        kind = FileCollectionKind.LargeFiles,
        refreshOnResume = true
    )
}

@Composable
internal fun LargeFilesManagerRoute() {
    val viewModel: LargeFilesManagerViewModel = koinViewModel()
    ManagedFileRoute(
        kind = FileCollectionKind.LargeFiles,
        refreshOnResume = true,
        viewModel = viewModel
    )
}

@Composable
fun DocumentsManagerScreen() {
    ManagedFileRoute(
        kind = FileCollectionKind.Documents,
        refreshOnResume = true
    )
}

@Composable
internal fun DocumentsManagerRoute() {
    val viewModel: DocumentsManagerViewModel = koinViewModel()
    ManagedFileRoute(
        kind = FileCollectionKind.Documents,
        refreshOnResume = true,
        viewModel = viewModel
    )
}

@Composable
internal fun FileCollectionRoute(
    kind: FileCollectionKind,
    viewModel: FileCollectionViewModel = viewModel(),
    permissionGateConfig: PermissionGateConfig? = null
) {
    val router = LocalRouter.current
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showStopDialog by remember { mutableStateOf(false) }
    val scrollStates = remember(kind) { mutableMapOf<Int, ScrollState>() }
    fun scrollStateForCollection(index: Int): ScrollState =
        scrollStates.getOrPut(index) { ScrollState(0) }

    LaunchedEffect(kind, viewModel) {
        viewModel.load(kind)
    }

    val config = uiState.photoConfig ?: placeholderCollectionConfig(kind)
    val title = localizedFileCollectionTitle(kind, config.title)
    val scanText = localizedFileCollectionScanText(kind, config.scanText)
    val actionText = localizedFileCollectionActionText(kind, config.actionText)
    val processingText = localizedFileCollectionProcessingText(kind, config.processingText)
    val resultCaption = localizedFileCollectionResultCaption(kind, config.resultCaption)
    val detailItems = uiState.collectionDetailItems
    val showDetail = uiState.detailStartIndex != null && detailItems.isNotEmpty()
    val showSelectionAction = (uiState.phase == PhotosState.Browsing || uiState.phase == PhotosState.ConfirmDelete) && !showDetail
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

    BackHandler {
        when {
            showDetail -> viewModel.closeDetail()
            uiState.phase == PhotosState.Scanning -> showStopDialog = true
            uiState.phase == PhotosState.Result -> router.goHome()
            else -> router.goBack()
        }
    }

    if (uiState.phase == PhotosState.Deleting) {
        DeletingAnimationContent(processingText)
        return
    }

    CleanXScaffoldPage(
        title = title,
        scrollEnabled = false,
        contentPadding = PaddingValues(0.dp),
        backgroundBrush = FileManagerPageBrush,
        onBack = {
            when {
                showDetail -> viewModel.closeDetail()
                uiState.phase == PhotosState.Scanning -> showStopDialog = true
                uiState.phase == PhotosState.Result -> router.goHome()
                else -> router.goBack()
            }
        },
        permissionGateConfig = permissionGateConfig,
        actions = {
            FileCollectionTopAction(
                actionText = when {
                    showDetail -> stringResource(R.string.file_delete_count, uiState.selectedIds.size)
                    else -> null
                },
                actionEnabled = !showDetail || uiState.selectedIds.isNotEmpty(),
                onAction = {
                    if (showDetail) {
                        viewModel.requestDelete()
                    } else {
                        viewModel.toggleAllVisible()
                    }
                }
            )
        },
        bottomBar = {
            if (showSelectionAction) {
                CleanXBottomActionBar(
                    enabled = uiState.selectedIds.isNotEmpty(),
                    text = actionText,
                    onClick = viewModel::requestDelete,
                    backgroundColor = Color.Transparent,
                    buttonModifier = Modifier.height(52.dp),
                    buttonCornerRadius = 10.dp,
                    buttonFontSize = 20.sp,
                )
            }
        }
    ) {
        val startIndex = uiState.detailStartIndex
        if ((uiState.phase == PhotosState.Browsing || uiState.phase == PhotosState.ConfirmDelete) && startIndex != null && detailItems.isNotEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                PhotoDetailScreen(
                    items = detailItems,
                    initialIndex = startIndex,
                    selectedIds = uiState.selectedIds,
                    selectedSize = uiState.selectedSizeBytes,
                    onToggleSelection = viewModel::toggleSelection
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(FileManagerPageBrush)
                    .padding(horizontal = 16.dp)
            ) {
                when (uiState.phase) {
                    PhotosState.Scanning -> ScanningContent(text = scanText)
                    PhotosState.Browsing, PhotosState.ConfirmDelete -> FileCollectionBrowserContent(
                        config = config,
                        selectedIds = uiState.selectedIds,
                        allSelected = uiState.allSelected,
                        onToggleAll = viewModel::toggleAllVisible,
                        onToggleIds = viewModel::toggleIds,
                        onToggleGroup = viewModel::toggleGroup,
                        onSelect = viewModel::toggleSelection,
                        onOpenDetail = { item ->
                            viewModel.openDetail(detailItems.indexOfFirst { it.id == item.id })
                        },
                        scrollState = scrollStateForCollection(uiState.selectedMediaTabIndex),
                        selectedMediaTabIndex = uiState.selectedMediaTabIndex,
                        onMediaTabSelected = viewModel::selectMediaTab
                    )
                    PhotosState.Deleting -> Unit
                    PhotosState.CompleteAnimation -> CompleteAnimationContent()
                    PhotosState.Result -> {
                        val result = uiState.resultSize
                        FileCollectionResultContent(
                            amount = result.first,
                            unit = if (kind == FileCollectionKind.PhotoPrivacy) {
                                stringResource(R.string.file_photos)
                            } else {
                                result.second
                            },
                            caption = resultCaption,
                            onContinue = viewModel::continueManaging,
                        )
                    }
                    PhotosState.NoResults -> Image(
                        painter = painterResource(R.drawable.files_blank),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize()
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
            title = if (config.layout == CollectionLayout.PhotoPrivacy) stringResource(R.string.file_remove_location_title) else null,
            message = if (config.layout == CollectionLayout.PhotoPrivacy) {
                stringResource(R.string.file_remove_location_message)
            } else {
                null
            },
            confirmText = if (config.layout == CollectionLayout.PhotoPrivacy) stringResource(R.string.remove) else null,
            onCancel = viewModel::cancelDelete,
            onDelete = {
                viewModel.closeDetail()
                if (config.layout != CollectionLayout.PhotoPrivacy) {
                    requestMediaStoreDeleteOrDeleteDirectly(
                        context = context,
                        uris = uiState.selectedUris,
                        launchRequest = deleteLauncher::launch,
                        deleteDirectly = viewModel::deleteSelectedFiles
                    )
                } else {
                    viewModel.deleteSelectedFiles()
                }
            }
        )
    }

    if (uiState.phase == PhotosState.NoResults) {
        NoResultsDialog(onBack = { router.goBack() })
    }
}

@Composable
internal fun ManagedFileRoute(
    kind: FileCollectionKind,
    refreshOnResume: Boolean = false,
    viewModel: FileCollectionViewModel = viewModel(),
    permissionGateConfig: PermissionGateConfig? = null
) {
    val router = LocalRouter.current
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showStopDialog by remember { mutableStateOf(false) }
    val scrollStates = remember(kind) { mutableMapOf<Int, ScrollState>() }
    fun scrollStateForTab(index: Int): ScrollState =
        scrollStates.getOrPut(index) { ScrollState(0) }

    LaunchedEffect(kind, viewModel) {
        viewModel.load(kind)
    }

    DisposableEffect(lifecycleOwner, refreshOnResume, viewModel) {
        val observer = LifecycleEventObserver { _, event ->
            if (refreshOnResume && event == Lifecycle.Event.ON_RESUME) {
                viewModel.refresh()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val config = uiState.managedConfig ?: placeholderManagedConfig(kind)
    val title = localizedFileCollectionTitle(kind, config.title)
    val scanText = localizedFileCollectionScanText(kind, config.scanText)
    val visibleItems = uiState.visibleManagedItems
    val showDetail = uiState.detailStartIndex != null && visibleItems.isNotEmpty()
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

    BackHandler {
        when {
            showDetail -> viewModel.closeDetail()
            uiState.phase == PhotosState.Scanning -> showStopDialog = true
            uiState.phase == PhotosState.Result -> router.goHome()
            else -> router.goBack()
        }
    }

    if (uiState.phase == PhotosState.Deleting) {
        DeletingAnimationContent(stringResource(R.string.file_deleting_files))
        return
    }

    CleanXScaffoldPage(
        title = title,
        scrollEnabled = false,
        contentPadding = PaddingValues(0.dp),
        backgroundBrush = FileManagerPageBrush,
        onBack = {
            when {
                showDetail -> viewModel.closeDetail()
                uiState.phase == PhotosState.Scanning -> showStopDialog = true
                uiState.phase == PhotosState.Result -> router.goHome()
                else -> router.goBack()
            }
        },
        permissionGateConfig = permissionGateConfig,
        actions = {
            FileCollectionTopAction(
                actionText = when {
                    showDetail -> stringResource(R.string.file_delete_count, uiState.selectedIds.size)
                    else -> null
                },
                actionEnabled = !showDetail || uiState.selectedIds.isNotEmpty(),
                onAction = {
                    if (showDetail) {
                        viewModel.requestDelete()
                    } else {
                        viewModel.toggleAllVisible()
                    }
                }
            )
        },
        bottomBar = {
            if ((uiState.phase == PhotosState.Browsing || uiState.phase == PhotosState.ConfirmDelete) && !showDetail) {
                CleanXBottomActionBar(
                    enabled = uiState.selectedIds.isNotEmpty(),
                    text = managedDeleteButtonText(config, uiState.selectedSizeBytes),
                    onClick = viewModel::requestDelete,
                    backgroundColor = Color.Transparent,
                    buttonModifier = Modifier.height(52.dp),
                    buttonCornerRadius = 10.dp,
                    buttonFontSize = 20.sp,
                )
            }
        }
    ) {
        val startIndex = uiState.detailStartIndex
        if ((uiState.phase == PhotosState.Browsing || uiState.phase == PhotosState.ConfirmDelete) && startIndex != null && visibleItems.isNotEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                ManagedFileDetailScreen(
                    items = visibleItems,
                    initialIndex = startIndex,
                    selectedIds = uiState.selectedIds,
                    selectedSize = uiState.selectedSizeBytes,
                    onToggleSelection = viewModel::toggleSelection
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(FileManagerPageBrush)
                    .padding(horizontal = 16.dp)
            ) {
                when (uiState.phase) {
                    PhotosState.Scanning -> ScanningContent(text = scanText)
                    PhotosState.Browsing, PhotosState.ConfirmDelete -> ManagedFileListContent(
                        config = config,
                        items = visibleItems,
                        selectedTabIndex = uiState.selectedTabIndex,
                        selectedIds = uiState.selectedIds,
                        allSelected = uiState.allSelected,
                        scrollState = scrollStateForTab(uiState.selectedTabIndex),
                        onTabSelected = viewModel::selectTab,
                        onToggleAll = viewModel::toggleAllVisible,
                        onSelect = viewModel::toggleSelection,
                        onOpenDetail = { item ->
                            viewModel.openDetail(visibleItems.indexOfFirst { it.id == item.id })
                        }
                    )
                    PhotosState.Deleting -> Unit
                    PhotosState.CompleteAnimation -> CompleteAnimationContent()
                    PhotosState.Result -> {
                        val result = uiState.resultSize
                        FileCollectionResultContent(
                            amount = result.first,
                            unit = result.second,
                            caption = stringResource(R.string.file_deleted_in_cleanup),
                            onContinue = viewModel::continueManaging,
                        )
                    }
                    PhotosState.NoResults -> Unit
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

@Composable
fun DuplicateFilesManagerScreen() {
    DuplicateFilesManagerScreenState(
        viewModel = viewModel()
    )
}

@Composable
internal fun DuplicateFilesManagerRoute(
    permissionGateConfig: PermissionGateConfig? = null
) {
    DuplicateFilesManagerScreenState(
        viewModel = koinViewModel(),
        permissionGateConfig = permissionGateConfig
    )
}

@Composable
private fun DuplicateFilesManagerScreenState(
    viewModel: DuplicateFilesViewModel,
    permissionGateConfig: PermissionGateConfig? = null
) {
    val router = LocalRouter.current
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showStopDialog by remember { mutableStateOf(false) }
    val groupListScrollState = remember { ScrollState(0) }
    val groupDetailScrollStates = remember { mutableMapOf<Int, ScrollState>() }
    fun scrollStateForGroup(groupId: Int): ScrollState =
        groupDetailScrollStates.getOrPut(groupId) { ScrollState(0) }
    val selectedGroup = uiState.selectedGroup
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

    BackHandler {
        when {
            uiState.phase == PhotosState.Scanning -> showStopDialog = true
            selectedGroup != null -> viewModel.closeGroup()
            uiState.phase == PhotosState.Result -> router.goHome()
            else -> router.goBack()
        }
    }

    if (uiState.phase == PhotosState.Deleting) {
        DeletingAnimationContent(stringResource(R.string.file_deleting_duplicate_files))
        return
    }

    CleanXScaffoldPage(
        title = stringResource(R.string.nav_duplicate_files),
        scrollEnabled = false,
        contentPadding = PaddingValues(0.dp),
        backgroundBrush = FileManagerPageBrush,
        onBack = {
            when {
                uiState.phase == PhotosState.Scanning -> showStopDialog = true
                selectedGroup != null -> viewModel.closeGroup()
                uiState.phase == PhotosState.Result -> router.goHome()
                else -> router.goBack()
            }
        },
        permissionGateConfig = permissionGateConfig,
        bottomBar = {
            if ((uiState.phase == PhotosState.Browsing || uiState.phase == PhotosState.ConfirmDelete) && selectedGroup == null) {
                CleanXBottomActionBar(
                    enabled = uiState.filesToDelete.isNotEmpty(),
                    text = stringResource(R.string.file_clean_up_size, formatDuplicateCleanupSize(uiState.selectedDeleteSize)),
                    onClick = viewModel::requestDelete,
                    backgroundColor = Color.Transparent,
                    buttonModifier = Modifier.height(52.dp),
                    buttonCornerRadius = 10.dp,
                    buttonFontSize = 20.sp,
                )
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(FileManagerPageBrush)
                .padding(horizontal = 16.dp)
        ) {
            when (uiState.phase) {
                PhotosState.Scanning -> ScanningContent(text = stringResource(R.string.file_scanning_duplicate_files))
                PhotosState.Browsing, PhotosState.ConfirmDelete -> {
                    val group = selectedGroup
                    if (group == null) {
                        DuplicateGroupsContent(
                            groups = uiState.groups,
                            selectedFileKeys = uiState.selectedFileKeys,
                            allSelected = uiState.allSelected,
                            scrollState = groupListScrollState,
                            onToggleAll = viewModel::toggleAll,
                            onOpenGroup = viewModel::openGroup
                        )
                    } else {
                        DuplicateGroupDetailContent(
                            group = group,
                            selectedFileKeys = uiState.selectedFileKeys,
                            scrollState = scrollStateForGroup(group.id),
                            onToggleFile = viewModel::toggleFile,
                            onAutoSelect = viewModel::autoSelectCurrentGroup,
                            onToggleGroupSelection = viewModel::toggleCurrentGroupSelection
                        )
                    }
                }
                PhotosState.Deleting -> Unit
                PhotosState.CompleteAnimation -> CompleteAnimationContent()
                PhotosState.Result -> {
                    val result = FileSizeFormatter.format(uiState.deletedBytes).splitSizeLabel()
                    FileCollectionResultContent(
                        amount = result.first,
                        unit = result.second,
                        caption = stringResource(R.string.file_deleted_in_cleanup),
                        onContinue = viewModel::continueManaging,
                    )
                }
                PhotosState.NoResults -> Unit
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

private fun placeholderCollectionConfig(kind: FileCollectionKind): FileCollectionConfig =
    FileCollectionConfig(
        title = when (kind) {
            FileCollectionKind.Screenshots -> "Screenshots"
            FileCollectionKind.Videos -> "Videos"
            FileCollectionKind.Audios -> "Audios"
            FileCollectionKind.SimilarPhotos -> "Similar Photos"
            FileCollectionKind.PhotoPrivacy -> "Photo Privacy"
            else -> "Files"
        },
        scanText = when (kind) {
            FileCollectionKind.Screenshots -> "Scanning screenshots..."
            FileCollectionKind.Videos -> "Scanning videos..."
            FileCollectionKind.Audios -> "Scanning audios..."
            FileCollectionKind.SimilarPhotos -> "Scanning similar photos..."
            FileCollectionKind.PhotoPrivacy -> "Scanning photo privacy..."
            else -> "Scanning files..."
        },
        actionText = if (kind == FileCollectionKind.PhotoPrivacy) "Remove Location Data" else "Delete",
        processingText = if (kind == FileCollectionKind.PhotoPrivacy) "Removing Location Data..." else "Cleanup Completed...",
        resultAmount = "0",
        resultUnit = if (kind == FileCollectionKind.PhotoPrivacy) "Photos" else "B",
        resultCaption = if (kind == FileCollectionKind.PhotoPrivacy) "Location data removed" else "Deleted in this cleanup",
        layout = when (kind) {
            FileCollectionKind.Screenshots -> CollectionLayout.Screenshots
            FileCollectionKind.SimilarPhotos -> CollectionLayout.SimilarPhotos
            FileCollectionKind.PhotoPrivacy -> CollectionLayout.PhotoPrivacy
            else -> CollectionLayout.MediaGrid
        },
        items = emptyList()
    )

private fun placeholderManagedConfig(kind: FileCollectionKind): ManagedFileListConfig =
    ManagedFileListConfig(
        title = if (kind == FileCollectionKind.Documents) "Documents" else "Large Files",
        scanText = if (kind == FileCollectionKind.Documents) "Scanning documents..." else "Scanning large files...",
        tabs = emptyList(),
        items = emptyList(),
        resultAmount = "0",
        resultUnit = "B",
        style = if (kind == FileCollectionKind.Documents) ManagedFileListStyle.Documents else ManagedFileListStyle.Default
    )

@Composable
private fun managedDeleteButtonText(config: ManagedFileListConfig, selectedSize: Long): String =
    if (selectedSize > 0L) {
        val sizeLabel = FileSizeFormatter.format(selectedSize)
        if (config.style == ManagedFileListStyle.Documents) {
            stringResource(R.string.file_delete_size, sizeLabel.replace(" ", ""))
        } else {
            stringResource(R.string.file_delete_size, sizeLabel)
        }
    } else {
        stringResource(R.string.file_delete)
    }

private fun formatDuplicateCleanupSize(bytes: Long): String =
    if (bytes == 0L) "0KB" else FileSizeFormatter.format(bytes)

@Composable
private fun localizedFileCollectionTitle(kind: FileCollectionKind, fallback: String): String =
    when (kind) {
        FileCollectionKind.Photos -> stringResource(R.string.nav_photos)
        FileCollectionKind.Screenshots -> stringResource(R.string.nav_screenshots)
        FileCollectionKind.Videos -> stringResource(R.string.nav_videos)
        FileCollectionKind.Audios -> stringResource(R.string.nav_audios)
        FileCollectionKind.SimilarPhotos -> stringResource(R.string.nav_similar_photos)
        FileCollectionKind.PhotoPrivacy -> stringResource(R.string.nav_photo_privacy)
        FileCollectionKind.LargeFiles -> stringResource(R.string.nav_large_files)
        FileCollectionKind.Documents -> stringResource(R.string.nav_documents)
    }.ifBlank { fallback }

@Composable
private fun localizedFileCollectionScanText(kind: FileCollectionKind, fallback: String): String =
    when (kind) {
        FileCollectionKind.Photos -> stringResource(R.string.file_scanning_photos)
        FileCollectionKind.Screenshots -> stringResource(R.string.file_scanning_screenshots)
        FileCollectionKind.Videos -> stringResource(R.string.file_scanning_videos)
        FileCollectionKind.Audios -> stringResource(R.string.file_scanning_audios)
        FileCollectionKind.SimilarPhotos -> stringResource(R.string.file_scanning_similar_photos)
        FileCollectionKind.PhotoPrivacy -> stringResource(R.string.file_scanning_photo_privacy)
        FileCollectionKind.LargeFiles -> stringResource(R.string.file_scanning_large_files)
        FileCollectionKind.Documents -> stringResource(R.string.file_scanning_documents)
    }.ifBlank { fallback }

@Composable
private fun localizedFileCollectionActionText(kind: FileCollectionKind, fallback: String): String =
    when (kind) {
        FileCollectionKind.PhotoPrivacy -> stringResource(R.string.file_remove_location_data)
        else -> stringResource(R.string.file_delete)
    }.ifBlank { fallback }

@Composable
private fun localizedFileCollectionProcessingText(kind: FileCollectionKind, fallback: String): String =
    when (kind) {
        FileCollectionKind.PhotoPrivacy -> stringResource(R.string.file_removing_location_data_progress)
        else -> stringResource(R.string.file_cleanup_completed)
    }.ifBlank { fallback }

@Composable
private fun localizedFileCollectionResultCaption(kind: FileCollectionKind, fallback: String): String =
    when (kind) {
        FileCollectionKind.PhotoPrivacy -> stringResource(R.string.file_location_data_removed)
        else -> stringResource(R.string.file_deleted_in_cleanup)
    }.ifBlank { fallback }
