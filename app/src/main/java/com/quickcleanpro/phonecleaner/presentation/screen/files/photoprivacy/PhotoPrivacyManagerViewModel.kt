package com.quickcleanpro.phonecleaner.presentation.screen.files.photoprivacy

import com.quickcleanpro.phonecleaner.R
import com.quickcleanpro.phonecleaner.config.FeatureKey
import com.quickcleanpro.phonecleaner.domain.repository.FileRepository
import com.quickcleanpro.phonecleaner.operation.OperationAction
import com.quickcleanpro.phonecleaner.presentation.common.fileRepositoryOrPreview
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.BaseFileManagerViewModel
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.FILE_DELETE_ANIMATION_MIN_MILLIS
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.FileOperationPhase
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.appString
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.fileScanFailedMessage
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.toggleAllVisible
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.toggleId
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

internal class PhotoPrivacyManagerViewModel(
    private val repository: FileRepository = fileRepositoryOrPreview(),
    ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    testLoader: (((suspend () -> Unit)) -> Unit)? = null,
    private val scanDelayMillis: Long = 900L,
    private val removeDelayMillis: Long = FILE_DELETE_ANIMATION_MIN_MILLIS,
    private val completeDelayMillis: Long = 700L
) : BaseFileManagerViewModel(FeatureKey.PHOTO_PRIVACY, ioDispatcher, testLoader) {

    private val _uiState = MutableStateFlow(PhotoPrivacyManagerUiState())
    val uiState: StateFlow<PhotoPrivacyManagerUiState> = _uiState.asStateFlow()

    fun startIfNeeded() {
        if (_uiState.value.phase != FileOperationPhase.Scanning || _uiState.value.items.isNotEmpty()) return
        refresh()
    }

    fun refresh() {
        trackScanStarted()
        _uiState.value = PhotoPrivacyManagerUiState(phase = FileOperationPhase.Scanning)
        operationRunner.launch {
            runCatching { mapPrivacyPhotos(repository.loadPrivacyImages()) }
                .onSuccess { items ->
                    operationRunner.delayIfNeeded(scanDelayMillis)
                    trackScanFinished(hasResult = items.isNotEmpty())
                    _uiState.value = PhotoPrivacyManagerUiState(
                        phase = if (items.isEmpty()) FileOperationPhase.NoResults else FileOperationPhase.Browsing,
                        items = items,
                        selectedIds = items.map { it.id }.toSet(),
                    )
                }
                .onFailure { error ->
                    if (error is CancellationException) throw error
                    trackScanFinished(hasResult = false)
                    _uiState.update { it.copy(phase = FileOperationPhase.NoResults, errorMessage = error.message ?: fileScanFailedMessage()) }
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

    fun requestRemoveLocation() {
        _uiState.update {
            if (it.selectedIds.isNotEmpty()) {
                trackActionRequested(OperationAction.REMOVE_LOCATION)
                it.copy(phase = FileOperationPhase.ConfirmDelete)
            } else {
                it
            }
        }
    }

    fun cancelRemoveLocation() {
        _uiState.update { it.copy(phase = FileOperationPhase.Browsing) }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    override fun onCancelDeletingPhase() {
        _uiState.update { current ->
            if (current.phase == FileOperationPhase.Deleting) {
                current.copy(phase = FileOperationPhase.Browsing)
            } else {
                current
            }
        }
    }

    fun removeLocationData() {
        val selectedFiles = _uiState.value.selectedFiles
        runFileOperation(
            selectedFiles = selectedFiles,
            action = OperationAction.REMOVE_LOCATION,
            onEmptySelection = { _uiState.update { it.copy(phase = FileOperationPhase.Browsing) } },
            onStart = { _uiState.update { it.copy(phase = FileOperationPhase.Deleting) } },
            operationDelayMillis = removeDelayMillis,
            completeDelayMillis = completeDelayMillis,
            operation = { FileOperationOutcome(changedCount = repository.removeLocationData(selectedFiles)) },
            onCompleteAnimation = { outcome ->
                val rebuilt = mapPrivacyPhotos(repository.loadPrivacyImages())
                _uiState.update {
                    it.copy(
                        phase = FileOperationPhase.CompleteAnimation,
                        items = rebuilt,
                        selectedIds = emptySet(),
                        removedLocationCount = outcome.changedCount,
                    )
                }
            },
            onResult = { _uiState.update { it.copy(phase = FileOperationPhase.Result) } },
            onFailure = { error ->
                _uiState.update {
                    it.copy(phase = FileOperationPhase.Browsing, errorMessage = error.message ?: appString(R.string.deletion_failed))
                }
            },
        )
    }

    fun continueManaging() {
        _uiState.update { it.copy(phase = FileOperationPhase.Browsing, selectedIds = it.items.map { item -> item.id }.toSet()) }
    }
}
