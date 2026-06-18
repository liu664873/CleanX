package com.quickcleanpro.phonecleaner.presentation.screen.files.common

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.quickcleanpro.phonecleaner.R
import com.quickcleanpro.phonecleaner.presentation.common.components.CleanXBottomActionBar
import com.quickcleanpro.phonecleaner.presentation.common.components.CleanXScaffoldPage
import com.quickcleanpro.phonecleaner.presentation.common.components.popups.DeleteConfirmDialog
import com.quickcleanpro.phonecleaner.presentation.common.components.popups.NoResultsDialog
import com.quickcleanpro.phonecleaner.presentation.common.components.popups.StopScanDialog
import com.quickcleanpro.phonecleaner.presentation.common.permission.PermissionGateConfig
import com.quickcleanpro.phonecleaner.presentation.common.route.LocalRouter
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.components.FileManagerPageBrush
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.components.FileManagerTopAction
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.views.FileManagerContentView
import com.quickcleanpro.phonecleaner.utils.FileSizeFormatter

@Composable
internal fun FileManagerScreen(
    refreshOnResume: Boolean = false,
    viewModel: FileManagerViewModel,
    permissionGateConfig: PermissionGateConfig? = null
) {
    val router = LocalRouter.current
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showStopDialog by remember { mutableStateOf(false) }
    val kind = uiState.kind
    val isListFeature = kind == FileManagerFeature.LargeFiles || kind == FileManagerFeature.Documents
    val scrollStates = remember(kind) { mutableMapOf<Int, ScrollState>() }
    fun scrollStateForTab(index: Int): ScrollState =
        scrollStates.getOrPut(index) { ScrollState(0) }

    DisposableEffect(lifecycleOwner, refreshOnResume, viewModel) {
        val observer = LifecycleEventObserver { _, event ->
            if (refreshOnResume && event == Lifecycle.Event.ON_RESUME) {
                viewModel.refresh()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val mediaConfig = uiState.mediaConfig ?: placeholderFileManagerMediaConfig(kind)
    val listConfig = uiState.managedConfig ?: placeholderFileManagerListConfig(kind)
    val title = localizedFileManagerTitle(
        kind = kind,
        fallback = if (isListFeature) listConfig.title else mediaConfig.title
    )
    val scanText = localizedFileManagerScanText(
        kind = kind,
        fallback = if (isListFeature) listConfig.scanText else mediaConfig.scanText
    )
    val actionText = localizedFileManagerActionText(kind, mediaConfig.actionText)
    val processingText = if (isListFeature) {
        stringResource(R.string.file_deleting_files)
    } else {
        localizedFileManagerProcessingText(kind, mediaConfig.processingText)
    }
    val resultCaption = if (isListFeature) {
        stringResource(R.string.file_deleted_in_cleanup)
    } else {
        localizedFileManagerResultCaption(kind, mediaConfig.resultCaption)
    }
    val mediaDetailItems = if (uiState.isGalleryFeature) uiState.currentGalleryItems else uiState.collectionDetailItems
    val listDetailItems = uiState.visibleManagedItems
    val showDetail = uiState.detailStartIndex != null &&
        if (isListFeature) listDetailItems.isNotEmpty() else mediaDetailItems.isNotEmpty()
    val showSelectionAction = uiState.isBrowsingOrConfirming && !showDetail
    val deleteLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            viewModel.deleteSelectedFiles()
        } else {
            viewModel.rejectSystemDelete()
        }
    }

    fun handleBack() {
        when {
            showDetail -> viewModel.closeDetail()
            uiState.phase == FileManagerPhase.Scanning -> showStopDialog = true
            uiState.phase == FileManagerPhase.Result -> router.goHome()
            else -> router.goBack()
        }
    }

    FileManagerErrorToastEffect(
        errorMessage = uiState.errorMessage,
        onConsumed = viewModel::clearError
    )

    BackHandler { handleBack() }

    CleanXScaffoldPage(
        title = title,
        scrollEnabled = false,
        contentPadding = PaddingValues(0.dp),
        backgroundBrush = FileManagerPageBrush,
        onBack = ::handleBack,
        permissionGateConfig = permissionGateConfig,
        actions = {
            FileManagerTopAction(
                actionText = when {
                    showDetail -> stringResource(R.string.file_delete_count, uiState.selectedIds.size)
                    !isListFeature && uiState.isGalleryFeature && uiState.isBrowsingOrConfirming -> {
                        if (uiState.allSelected) {
                            stringResource(R.string.file_unselect_all)
                        } else {
                            stringResource(R.string.file_select_all)
                        }
                    }
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
                    text = if (isListFeature) {
                        fileManagerDeleteButtonText(listConfig, uiState.selectedSizeBytes)
                    } else if (uiState.isGalleryFeature && uiState.selectedIds.isNotEmpty()) {
                        stringResource(R.string.file_delete_size, FileSizeFormatter.format(uiState.selectedSizeBytes))
                    } else {
                        actionText
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
        FileManagerContentView(
            uiState = uiState,
            scanText = scanText,
            processingText = processingText,
            resultCaption = resultCaption,
            mediaConfig = if (isListFeature) null else mediaConfig,
            managedConfig = if (isListFeature) listConfig else null,
            resultUnitOverride = if (kind == FileManagerFeature.PhotoPrivacy) stringResource(R.string.file_photos) else null,
            scrollStateForTab = ::scrollStateForTab,
            onTabSelected = viewModel::selectTab,
            onMediaTabSelected = viewModel::selectMediaTab,
            onToggleAllVisible = viewModel::toggleAllVisible,
            onToggleIds = viewModel::toggleIds,
            onToggleGroup = viewModel::toggleGroup,
            onSelect = viewModel::toggleSelection,
            onOpenDetail = viewModel::openDetail,
            onCloseDetail = viewModel::closeDetail,
            onRequestDelete = viewModel::requestDelete,
            onContinue = viewModel::continueManaging,
        )
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

    if (uiState.phase == FileManagerPhase.ConfirmDelete) {
        val isPhotoPrivacy = kind == FileManagerFeature.PhotoPrivacy || mediaConfig.layout == FileManagerLayout.PhotoPrivacy
        DeleteConfirmDialog(
            title = if (isPhotoPrivacy) stringResource(R.string.file_remove_location_title) else null,
            message = if (isPhotoPrivacy) stringResource(R.string.file_remove_location_message) else null,
            confirmText = if (isPhotoPrivacy) stringResource(R.string.remove) else null,
            onCancel = viewModel::cancelDelete,
            onDelete = {
                viewModel.closeDetail()
                if (isPhotoPrivacy) {
                    viewModel.deleteSelectedFiles()
                } else {
                    requestMediaStoreDeleteOrDeleteDirectly(
                        context = context,
                        uris = uiState.selectedUris,
                        launchRequest = deleteLauncher::launch,
                        deleteDirectly = viewModel::deleteSelectedFiles
                    )
                }
            }
        )
    }

    if (uiState.phase == FileManagerPhase.NoResults) {
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

private fun placeholderFileManagerMediaConfig(kind: FileManagerFeature?): FileManagerMediaConfig =
    FileManagerMediaConfig(
        title = when (kind) {
            FileManagerFeature.Screenshots -> "Screenshots"
            FileManagerFeature.Videos -> "Videos"
            FileManagerFeature.Audios -> "Audios"
            FileManagerFeature.SimilarPhotos -> "Similar Photos"
            FileManagerFeature.PhotoPrivacy -> "Photo Privacy"
            else -> "Files"
        },
        scanText = when (kind) {
            FileManagerFeature.Screenshots -> "Scanning screenshots..."
            FileManagerFeature.Videos -> "Scanning videos..."
            FileManagerFeature.Audios -> "Scanning audios..."
            FileManagerFeature.SimilarPhotos -> "Scanning similar photos..."
            FileManagerFeature.PhotoPrivacy -> "Scanning photo privacy..."
            else -> "Scanning files..."
        },
        actionText = if (kind == FileManagerFeature.PhotoPrivacy) "Remove Location Data" else "Delete",
        processingText = if (kind == FileManagerFeature.PhotoPrivacy) "Removing Location Data..." else "Cleanup Completed...",
        resultAmount = "0",
        resultUnit = if (kind == FileManagerFeature.PhotoPrivacy) "Photos" else "B",
        resultCaption = if (kind == FileManagerFeature.PhotoPrivacy) "Location data removed" else "Deleted in this cleanup",
        layout = when (kind) {
            FileManagerFeature.Screenshots -> FileManagerLayout.Screenshots
            FileManagerFeature.SimilarPhotos -> FileManagerLayout.SimilarPhotos
            FileManagerFeature.PhotoPrivacy -> FileManagerLayout.PhotoPrivacy
            else -> FileManagerLayout.MediaGrid
        },
        items = emptyList()
    )

private fun placeholderFileManagerListConfig(kind: FileManagerFeature?): FileManagerListConfig =
    FileManagerListConfig(
        title = if (kind == FileManagerFeature.Documents) "Documents" else "Large Files",
        scanText = if (kind == FileManagerFeature.Documents) "Scanning documents..." else "Scanning large files...",
        tabs = emptyList(),
        items = emptyList(),
        resultAmount = "0",
        resultUnit = "B",
        style = if (kind == FileManagerFeature.Documents) FileManagerListStyle.Documents else FileManagerListStyle.Default
    )

@Composable
private fun fileManagerDeleteButtonText(config: FileManagerListConfig, selectedSize: Long): String =
    if (selectedSize > 0L) {
        val sizeLabel = FileSizeFormatter.format(selectedSize)
        if (config.style == FileManagerListStyle.Documents) {
            stringResource(R.string.file_delete_size, sizeLabel.replace(" ", ""))
        } else {
            stringResource(R.string.file_delete_size, sizeLabel)
        }
    } else {
        stringResource(R.string.file_delete)
    }

@Composable
private fun localizedFileManagerTitle(kind: FileManagerFeature?, fallback: String): String =
    when (kind) {
        FileManagerFeature.Photos -> stringResource(R.string.nav_photos)
        FileManagerFeature.Screenshots -> stringResource(R.string.nav_screenshots)
        FileManagerFeature.Videos -> stringResource(R.string.nav_videos)
        FileManagerFeature.Audios -> stringResource(R.string.nav_audios)
        FileManagerFeature.SimilarPhotos -> stringResource(R.string.nav_similar_photos)
        FileManagerFeature.PhotoPrivacy -> stringResource(R.string.nav_photo_privacy)
        FileManagerFeature.LargeFiles -> stringResource(R.string.nav_large_files)
        FileManagerFeature.Documents -> stringResource(R.string.nav_documents)
        null -> fallback
    }.ifBlank { fallback }

@Composable
private fun localizedFileManagerScanText(kind: FileManagerFeature?, fallback: String): String =
    when (kind) {
        FileManagerFeature.Photos -> stringResource(R.string.file_scanning_photos)
        FileManagerFeature.Screenshots -> stringResource(R.string.file_scanning_screenshots)
        FileManagerFeature.Videos -> stringResource(R.string.file_scanning_videos)
        FileManagerFeature.Audios -> stringResource(R.string.file_scanning_audios)
        FileManagerFeature.SimilarPhotos -> stringResource(R.string.file_scanning_similar_photos)
        FileManagerFeature.PhotoPrivacy -> stringResource(R.string.file_scanning_photo_privacy)
        FileManagerFeature.LargeFiles -> stringResource(R.string.file_scanning_large_files)
        FileManagerFeature.Documents -> stringResource(R.string.file_scanning_documents)
        null -> fallback
    }.ifBlank { fallback }

@Composable
private fun localizedFileManagerActionText(kind: FileManagerFeature?, fallback: String): String =
    when (kind) {
        FileManagerFeature.PhotoPrivacy -> stringResource(R.string.file_remove_location_data)
        else -> stringResource(R.string.file_delete)
    }.ifBlank { fallback }

@Composable
private fun localizedFileManagerProcessingText(kind: FileManagerFeature?, fallback: String): String =
    when (kind) {
        FileManagerFeature.Photos -> stringResource(R.string.file_deleting_photos)
        FileManagerFeature.PhotoPrivacy -> stringResource(R.string.file_removing_location_data_progress)
        else -> stringResource(R.string.file_cleanup_completed)
    }.ifBlank { fallback }

@Composable
private fun localizedFileManagerResultCaption(kind: FileManagerFeature?, fallback: String): String =
    when (kind) {
        FileManagerFeature.PhotoPrivacy -> stringResource(R.string.file_location_data_removed)
        else -> stringResource(R.string.file_deleted_in_cleanup)
    }.ifBlank { fallback }
