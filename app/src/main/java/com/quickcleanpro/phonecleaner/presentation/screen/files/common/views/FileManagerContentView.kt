package com.quickcleanpro.phonecleaner.presentation.screen.files.common.views

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.quickcleanpro.phonecleaner.R
import com.quickcleanpro.phonecleaner.presentation.common.components.CleanXEmptyScanResult
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.FileManagerMediaConfig
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.FileManagerUiState
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.FileManagerPhase
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.FileManagerListConfig
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.FileManagerMediaGroup
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.components.FileManagerMediaBrowserView
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.components.FileManagerPageBrush
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.components.FileManagerListView
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.toFileManagerDetailItem
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.views.detail.FileManagerDetailView
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.views.list.FileManagerGalleryBrowserView
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.views.process.FileManagerScanningView

@Composable
internal fun FileManagerContentView(
    uiState: FileManagerUiState,
    scanText: String,
    processingText: String,
    resultCaption: String,
    mediaConfig: FileManagerMediaConfig? = null,
    managedConfig: FileManagerListConfig? = null,
    resultUnitOverride: String? = null,
    scrollStateForTab: (Int) -> ScrollState,
    onTabSelected: (Int) -> Unit,
    onMediaTabSelected: (Int) -> Unit,
    onToggleAllVisible: () -> Unit,
    onToggleIds: (Set<Int>) -> Unit,
    onToggleGroup: (FileManagerMediaGroup) -> Unit,
    onSelect: (Int) -> Unit,
    onOpenDetail: (Int?) -> Unit,
    onContinue: () -> Unit,
) {
    if (uiState.phase == FileManagerPhase.Deleting) {
        FileManagerDeletingView(processingText)
        return
    }

    val isBrowsing = uiState.phase == FileManagerPhase.Browsing || uiState.phase == FileManagerPhase.ConfirmDelete
    val startIndex = uiState.detailStartIndex
    val mediaDetailItems = if (uiState.isGalleryFeature) uiState.currentGalleryItems else uiState.collectionDetailItems
    val managedItems = uiState.visibleManagedItems
    val detailItems =
        if (managedConfig != null) {
            managedItems.map { it.toFileManagerDetailItem() }
        } else {
            mediaDetailItems.map { it.toFileManagerDetailItem() }
        }

    if (isBrowsing && startIndex != null && detailItems.isNotEmpty()) {
        FileManagerDetailView(
            items = detailItems,
            initialIndex = startIndex,
            selectedIds = uiState.selectedIds,
            selectedSize = uiState.selectedSizeBytes,
            onToggleSelection = onSelect,
        )
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FileManagerPageBrush)
            .padding(horizontal = 16.dp),
    ) {
        when (uiState.phase) {
            FileManagerPhase.Scanning -> FileManagerScanningView(text = scanText)
            FileManagerPhase.Browsing,
            FileManagerPhase.ConfirmDelete -> FileManagerBrowserContent(
                uiState = uiState,
                mediaConfig = mediaConfig,
                managedConfig = managedConfig,
                scrollStateForTab = scrollStateForTab,
                onTabSelected = onTabSelected,
                onMediaTabSelected = onMediaTabSelected,
                onToggleAllVisible = onToggleAllVisible,
                onToggleIds = onToggleIds,
                onToggleGroup = onToggleGroup,
                onSelect = onSelect,
                onOpenDetail = onOpenDetail,
            )
            FileManagerPhase.Deleting -> Unit
            FileManagerPhase.CompleteAnimation -> FileManagerCompleteView()
            FileManagerPhase.Result -> {
                val result = uiState.resultSize
                FileManagerResultView(
                    amount = result.first,
                    unit = resultUnitOverride ?: result.second,
                    caption = resultCaption,
                    onContinue = onContinue,
                )
            }
            FileManagerPhase.NoResults -> CleanXEmptyScanResult(
                message = stringResource(R.string.file_scan_completed_no_results),
            )
        }
    }
}

@Composable
private fun FileManagerBrowserContent(
    uiState: FileManagerUiState,
    mediaConfig: FileManagerMediaConfig?,
    managedConfig: FileManagerListConfig?,
    scrollStateForTab: (Int) -> ScrollState,
    onTabSelected: (Int) -> Unit,
    onMediaTabSelected: (Int) -> Unit,
    onToggleAllVisible: () -> Unit,
    onToggleIds: (Set<Int>) -> Unit,
    onToggleGroup: (FileManagerMediaGroup) -> Unit,
    onSelect: (Int) -> Unit,
    onOpenDetail: (Int?) -> Unit,
) {
    when {
        uiState.isGalleryFeature -> {
            val items = uiState.currentGalleryItems
            FileManagerGalleryBrowserView(
                tabs = uiState.galleryTabs,
                selectedTabIndex = uiState.selectedTabIndex,
                items = items,
                selectedIds = uiState.selectedIds,
                scrollState = scrollStateForTab(uiState.selectedTabIndex),
                onTabSelected = onTabSelected,
                onSelect = onSelect,
                onSelectAll = { onToggleIds(uiState.visibleIds) },
                onOpenDetail = { item ->
                    onOpenDetail(items.indexOfFirst { it.id == item.id })
                },
            )
        }
        managedConfig != null -> {
            val visibleItems = uiState.visibleManagedItems
            FileManagerListView(
                config = managedConfig,
                items = visibleItems,
                selectedTabIndex = uiState.selectedTabIndex,
                selectedIds = uiState.selectedIds,
                allSelected = uiState.allSelected,
                scrollState = scrollStateForTab(uiState.selectedTabIndex),
                onTabSelected = onTabSelected,
                onToggleAll = onToggleAllVisible,
                onSelect = onSelect,
                onOpenDetail = { item ->
                    onOpenDetail(visibleItems.indexOfFirst { it.id == item.id })
                },
            )
        }
        mediaConfig != null -> {
            val detailItems = uiState.collectionDetailItems
            FileManagerMediaBrowserView(
                config = mediaConfig,
                selectedIds = uiState.selectedIds,
                allSelected = uiState.allSelected,
                onToggleAll = onToggleAllVisible,
                onToggleIds = onToggleIds,
                onToggleGroup = onToggleGroup,
                onSelect = onSelect,
                onOpenDetail = { item ->
                    onOpenDetail(detailItems.indexOfFirst { it.id == item.id })
                },
                scrollState = scrollStateForTab(uiState.selectedMediaTabIndex),
                selectedMediaTabIndex = uiState.selectedMediaTabIndex,
                onMediaTabSelected = onMediaTabSelected,
            )
        }
    }
}
