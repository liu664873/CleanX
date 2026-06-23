package com.quickcleanpro.phonecleaner.presentation.screen.files.screenshots

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quickcleanpro.phonecleaner.R
import com.quickcleanpro.phonecleaner.domain.repository.FileRepository
import com.quickcleanpro.phonecleaner.presentation.common.fileRepositoryOrPreview
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.FILE_DELETE_ANIMATION_MIN_MILLIS
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.FileOperationPhase
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.FileOperationRunner
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.appString
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.deletionFailedMessage
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.fileScanFailedMessage
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.openDetailIndex
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.toggleAllVisible
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.toggleId
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

internal class ScreenshotsManagerViewModel(
    private val repository: FileRepository = fileRepositoryOrPreview(),
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val testLoader: (((suspend () -> Unit)) -> Unit)? = null,
    private val scanDelayMillis: Long = 900L,
    private val deleteDelayMillis: Long = FILE_DELETE_ANIMATION_MIN_MILLIS,
    private val completeDelayMillis: Long = 700L
) : ViewModel() {
    private val _uiState = MutableStateFlow(ScreenshotsManagerUiState())
    val uiState: StateFlow<ScreenshotsManagerUiState> = _uiState.asStateFlow()
    private val operationRunner = FileOperationRunner(viewModelScope, ioDispatcher, testLoader)

    fun startIfNeeded() {
        if (_uiState.value.phase != FileOperationPhase.Scanning || _uiState.value.items.isNotEmpty()) return
        refresh()
    }

    fun refresh() {
        _uiState.value = ScreenshotsManagerUiState(phase = FileOperationPhase.Scanning)
        operationRunner.launch {
            runCatching { mapScreenshots(repository.loadScreenshots()) }
                .onSuccess { items ->
                    operationRunner.delayIfNeeded(scanDelayMillis)
                    _uiState.value = ScreenshotsManagerUiState(
                        phase = if (items.isEmpty()) FileOperationPhase.NoResults else FileOperationPhase.Browsing,
                        items = items,
                    )
                }
                .onFailure { error ->
                    if (error is CancellationException) throw error
                    _uiState.update {
                        it.copy(phase = FileOperationPhase.NoResults, errorMessage = error.message ?: fileScanFailedMessage())
                    }
                }
        }
    }

    fun toggleSelection(id: Int) {
        _uiState.update { it.copy(selectedIds = toggleId(it.selectedIds, id)) }
    }

    fun toggleVisibleItems() {
        _uiState.update { state ->
            state.copy(selectedIds = toggleAllVisible(state.selectedIds, state.visibleIds))
        }
    }

    fun openDetail(index: Int?) {
        _uiState.update { it.copy(detailStartIndex = openDetailIndex(index)) }
    }

    fun closeDetail() {
        _uiState.update { it.copy(detailStartIndex = null) }
    }

    fun requestDelete() {
        _uiState.update { if (it.selectedIds.isNotEmpty()) it.copy(phase = FileOperationPhase.ConfirmDelete) else it }
    }

    fun cancelDelete() {
        _uiState.update { it.copy(phase = FileOperationPhase.Browsing) }
    }

    fun rejectSystemDelete() {
        cancelDelete()
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun cancelActiveOperation() {
        operationRunner.cancelActiveOperation()
    }

    fun deleteSelectedFiles() {
        val selectedFiles = _uiState.value.selectedFiles
        if (selectedFiles.isEmpty()) {
            _uiState.update { it.copy(phase = FileOperationPhase.Browsing) }
            return
        }
        _uiState.update { it.copy(phase = FileOperationPhase.Deleting, detailStartIndex = null) }
        operationRunner.launch {
            runCatching {
                val freedBytes = repository.deleteFiles(selectedFiles)
                if (freedBytes <= 0L) error(deletionFailedMessage())
                operationRunner.delayIfNeeded(deleteDelayMillis)
                val rebuilt = mapScreenshots(repository.loadScreenshots())
                _uiState.update {
                    it.copy(
                        phase = FileOperationPhase.CompleteAnimation,
                        items = rebuilt,
                        selectedIds = emptySet(),
                        deletedBytes = freedBytes
                    )
                }
                operationRunner.delayIfNeeded(completeDelayMillis)
                _uiState.update { it.copy(phase = FileOperationPhase.Result) }
            }.onFailure { error ->
                if (error is CancellationException) throw error
                _uiState.update {
                    it.copy(phase = FileOperationPhase.Browsing, errorMessage = error.message ?: appString(R.string.deletion_failed))
                }
            }
        }
    }

    fun continueManaging() {
        _uiState.update { it.copy(phase = FileOperationPhase.Browsing, selectedIds = emptySet(), detailStartIndex = null) }
    }
}
