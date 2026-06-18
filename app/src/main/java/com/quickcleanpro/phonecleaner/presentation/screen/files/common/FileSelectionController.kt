package com.quickcleanpro.phonecleaner.presentation.screen.files.common

internal class FileSelectionController {
    fun selectTab(state: FileCollectionUiState, index: Int): FileCollectionUiState =
        state.copy(
            selectedTabIndex = index.coerceAtLeast(0),
            selectedIds = if (state.isPhotos) emptySet() else state.selectedIds,
            detailStartIndex = null
        )

    fun selectMediaTab(state: FileCollectionUiState, index: Int): FileCollectionUiState =
        state.copy(selectedMediaTabIndex = index.coerceAtLeast(0), detailStartIndex = null)

    fun toggleSelection(state: FileCollectionUiState, id: Int): FileCollectionUiState =
        state.copy(selectedIds = if (id in state.selectedIds) state.selectedIds - id else state.selectedIds + id)

    fun toggleIds(state: FileCollectionUiState, ids: Set<Int>): FileCollectionUiState =
        state.copy(
            selectedIds = if (state.selectedIds.containsAll(ids)) {
                state.selectedIds - ids
            } else {
                state.selectedIds + ids
            }
        )

    fun openDetail(state: FileCollectionUiState, index: Int?): FileCollectionUiState =
        state.copy(detailStartIndex = index?.takeIf { it >= 0 })

    fun closeDetail(state: FileCollectionUiState): FileCollectionUiState =
        state.copy(detailStartIndex = null)
}
