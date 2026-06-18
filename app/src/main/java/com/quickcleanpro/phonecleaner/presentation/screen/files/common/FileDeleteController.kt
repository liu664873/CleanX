package com.quickcleanpro.phonecleaner.presentation.screen.files.common

internal class FileDeleteController {
    fun requestDelete(state: FileManagerUiState): FileManagerUiState =
        if (state.selectedIds.isNotEmpty()) state.copy(phase = FileManagerPhase.ConfirmDelete) else state

    fun cancelDelete(state: FileManagerUiState): FileManagerUiState =
        state.copy(phase = FileManagerPhase.Browsing)

    fun continueManaging(state: FileManagerUiState): FileManagerUiState =
        state.copy(
            phase = FileManagerPhase.Browsing,
            selectedIds = state.mediaConfig?.defaultSelectedIds ?: emptySet(),
            detailStartIndex = null
        )
}
