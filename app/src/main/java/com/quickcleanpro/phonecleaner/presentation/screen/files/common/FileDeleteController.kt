package com.quickcleanpro.phonecleaner.presentation.screen.files.common

internal class FileDeleteController {
    fun requestDelete(state: FileCollectionUiState): FileCollectionUiState =
        if (state.selectedIds.isNotEmpty()) state.copy(phase = PhotosState.ConfirmDelete) else state

    fun cancelDelete(state: FileCollectionUiState): FileCollectionUiState =
        state.copy(phase = PhotosState.Browsing)

    fun continueManaging(state: FileCollectionUiState): FileCollectionUiState =
        state.copy(
            phase = PhotosState.Browsing,
            selectedIds = state.photoConfig?.defaultSelectedIds ?: emptySet(),
            detailStartIndex = null
        )
}
