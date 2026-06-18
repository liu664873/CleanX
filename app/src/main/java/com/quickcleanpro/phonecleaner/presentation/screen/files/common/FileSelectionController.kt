package com.quickcleanpro.phonecleaner.presentation.screen.files.common

internal class FileSelectionController {
    fun selectTab(state: FileManagerUiState, index: Int): FileManagerUiState =
        state.copy(
            selectedTabIndex = index.coerceAtLeast(0),
            selectedIds = if (state.isGalleryFeature) emptySet() else state.selectedIds,
            detailStartIndex = null
        )

    fun selectMediaTab(state: FileManagerUiState, index: Int): FileManagerUiState =
        state.copy(selectedMediaTabIndex = index.coerceAtLeast(0), detailStartIndex = null)

    fun toggleSelection(state: FileManagerUiState, id: Int): FileManagerUiState =
        state.copy(selectedIds = if (id in state.selectedIds) state.selectedIds - id else state.selectedIds + id)

    fun toggleIds(state: FileManagerUiState, ids: Set<Int>): FileManagerUiState =
        state.copy(
            selectedIds = if (state.selectedIds.containsAll(ids)) {
                state.selectedIds - ids
            } else {
                state.selectedIds + ids
            }
        )

    fun openDetail(state: FileManagerUiState, index: Int?): FileManagerUiState =
        state.copy(detailStartIndex = index?.takeIf { it >= 0 })

    fun closeDetail(state: FileManagerUiState): FileManagerUiState =
        state.copy(detailStartIndex = null)
}
