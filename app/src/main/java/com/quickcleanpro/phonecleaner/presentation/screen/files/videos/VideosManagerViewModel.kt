package com.quickcleanpro.phonecleaner.presentation.screen.files.videos

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
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.openDetailIndex
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.toggleId
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.toggleIds
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

internal class VideosManagerViewModel(
    private val repository: FileRepository = fileRepositoryOrPreview(),
    ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    testLoader: (((suspend () -> Unit)) -> Unit)? = null,
    private val scanDelayMillis: Long = 900L,
    private val deleteDelayMillis: Long = FILE_DELETE_ANIMATION_MIN_MILLIS,
    private val completeDelayMillis: Long = 700L
) : BaseFileManagerViewModel(FeatureKey.VIDEOS, ioDispatcher, testLoader) {

    private val _uiState = MutableStateFlow(VideosManagerUiState())
    val uiState: StateFlow<VideosManagerUiState> = _uiState.asStateFlow()

    fun startIfNeeded() {
        if (_uiState.value.phase != FileOperationPhase.Scanning || _uiState.value.items.isNotEmpty()) return
        refresh()
    }

    fun refresh() {
        trackScanStarted()
        _uiState.value = VideosManagerUiState(phase = FileOperationPhase.Scanning)
        operationRunner.launch {
            runCatching { mapVideos(repository.loadVideos()) }
                .onSuccess { videos ->
                    operationRunner.delayIfNeeded(scanDelayMillis)
                    trackScanFinished(hasResult = videos.isNotEmpty())
                    _uiState.value = VideosManagerUiState(
                        phase = if (videos.isEmpty()) FileOperationPhase.NoResults else FileOperationPhase.Browsing,
                        items = videos,
                        tabs = buildVideoTabs(videos),
                    )
                }
                .onFailure { error ->
                    if (error is CancellationException) throw error
                    trackScanFinished(hasResult = false)
                    _uiState.update { it.copy(phase = FileOperationPhase.NoResults, errorMessage = error.message ?: fileScanFailedMessage()) }
                }
        }
    }

    fun selectTab(index: Int) {
        _uiState.update { it.copy(selectedTabIndex = index.coerceAtLeast(0), detailStartIndex = null) }
    }

    fun toggleSelection(id: Int) {
        _uiState.update { it.copy(selectedIds = toggleId(it.selectedIds, id)) }
    }

    fun toggleVisibleItems(ids: Set<Int>) {
        _uiState.update { it.copy(selectedIds = toggleIds(it.selectedIds, ids)) }
    }

    fun openDetail(index: Int?) {
        _uiState.update { it.copy(detailStartIndex = openDetailIndex(index)) }
    }

    fun closeDetail() {
        _uiState.update { it.copy(detailStartIndex = null) }
    }

    fun requestDelete() {
        _uiState.update {
            if (it.selectedIds.isNotEmpty()) {
                trackActionRequested(OperationAction.DELETE)
                it.copy(phase = FileOperationPhase.ConfirmDelete)
            } else {
                it
            }
        }
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

    override fun onCancelDeletingPhase() {
        _uiState.update { current ->
            if (current.phase == FileOperationPhase.Deleting) {
                current.copy(phase = FileOperationPhase.Browsing)
            } else {
                current
            }
        }
    }

    fun deleteSelectedFiles() {
        val selectedFiles = _uiState.value.selectedFiles
        runFileOperation(
            selectedFiles = selectedFiles,
            action = OperationAction.DELETE,
            onEmptySelection = { _uiState.update { it.copy(phase = FileOperationPhase.Browsing) } },
            onStart = { _uiState.update { it.copy(phase = FileOperationPhase.Deleting, detailStartIndex = null) } },
            operationDelayMillis = deleteDelayMillis,
            completeDelayMillis = completeDelayMillis,
            operation = { FileOperationOutcome(freedBytes = repository.deleteFiles(selectedFiles)) },
            isSuccessful = { it.freedBytes > 0L },
            onCompleteAnimation = { outcome ->
                val rebuilt = mapVideos(repository.loadVideos())
                _uiState.update {
                    it.copy(
                        phase = FileOperationPhase.CompleteAnimation,
                        items = rebuilt,
                        tabs = buildVideoTabs(rebuilt),
                        selectedIds = emptySet(),
                        deletedBytes = outcome.freedBytes,
                    )
                }
            },
            onResult = { _uiState.update { it.copy(phase = FileOperationPhase.Result) } },
            onFailure = { error ->
                _uiState.update { it.copy(phase = FileOperationPhase.Browsing, errorMessage = error.message ?: appString(R.string.deletion_failed)) }
            },
        )
    }

    fun continueManaging() {
        _uiState.update { it.copy(phase = FileOperationPhase.Browsing, selectedIds = emptySet(), detailStartIndex = null) }
    }
}
