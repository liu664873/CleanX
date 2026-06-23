package com.quickcleanpro.phonecleaner.presentation.screen.files.common

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quickcleanpro.phonecleaner.R
import com.quickcleanpro.phonecleaner.domain.repository.FileRepository
import com.quickcleanpro.phonecleaner.presentation.common.fileRepositoryOrPreview
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal open class FileManagerViewModel(
    private val initialKind: FileManagerFeature? = null,
    private val repository: FileRepository = fileRepositoryOrPreview(),
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val testLoader: (((suspend () -> Unit)) -> Unit)? = null,
    private val scanDelayMillis: Long = 900L,
    private val deleteDelayMillis: Long = FILE_DELETE_ANIMATION_MIN_MILLIS,
    private val completeDelayMillis: Long = 700L,
    private val collectionLoader: FileManagerLoader = FileManagerLoader(repository),
    private val selectionController: FileSelectionController = FileSelectionController(),
    private val deleteController: FileDeleteController = FileDeleteController()
) : ViewModel() {

    constructor(
        repository: FileRepository,
        ioDispatcher: CoroutineDispatcher,
        initialKind: FileManagerFeature? = null
    ) : this(
        initialKind = initialKind,
        repository = repository,
        ioDispatcher = ioDispatcher,
        testLoader = null,
        scanDelayMillis = 900L,
        deleteDelayMillis = FILE_DELETE_ANIMATION_MIN_MILLIS,
        completeDelayMillis = 700L
    )

    private val _uiState = MutableStateFlow(FileManagerUiState(kind = initialKind))
    val uiState: StateFlow<FileManagerUiState> = _uiState.asStateFlow()

    fun load(kind: FileManagerFeature) {
        if (_uiState.value.kind == kind && currentItemsLoaded()) return
        loadInternal(kind)
    }

    fun startIfPermitted() {
        initialKind?.let(::load)
    }

    fun refresh() {
        val kind = _uiState.value.kind ?: return
        loadInternal(kind)
    }

    fun selectTab(index: Int) {
        _uiState.update { selectionController.selectTab(it, index) }
    }

    fun selectMediaTab(index: Int) {
        _uiState.update { selectionController.selectMediaTab(it, index) }
    }

    fun toggleSelection(id: Int) {
        _uiState.update { selectionController.toggleSelection(it, id) }
    }

    fun toggleIds(ids: Set<Int>) {
        if (ids.isEmpty()) return
        _uiState.update { selectionController.toggleIds(it, ids) }
    }

    fun toggleAllVisible() {
        toggleIds(_uiState.value.visibleIds)
    }

    fun toggleGroup(group: FileManagerMediaGroup) {
        toggleIds(group.items.map { it.id }.toSet())
    }

    fun openDetail(index: Int?) {
        _uiState.update { selectionController.openDetail(it, index) }
    }

    fun closeDetail() {
        _uiState.update { selectionController.closeDetail(it) }
    }

    fun requestDelete() {
        _uiState.update { deleteController.requestDelete(it) }
    }

    fun cancelDelete() {
        _uiState.update { deleteController.cancelDelete(it) }
    }

    fun rejectSystemDelete() {
        cancelDelete()
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun deleteSelectedFiles() {
        val state = _uiState.value
        val selectedFiles = state.selectedFiles
        if (selectedFiles.isEmpty()) {
            _uiState.update { it.copy(phase = FileManagerPhase.Browsing) }
            return
        }

        val kind = state.kind ?: return
        _uiState.update { it.copy(phase = FileManagerPhase.Deleting, detailStartIndex = null) }
        launchLoad {
            runCatching {
                if (state.isPhotoPrivacy) {
                    val removed = repository.removeLocationData(selectedFiles)
                    delayIfNeeded(deleteDelayMillis)
                    rebuildAfterMutation(kind)
                    _uiState.update {
                        it.copy(
                            phase = FileManagerPhase.CompleteAnimation,
                            selectedIds = emptySet(),
                            removedLocationCount = removed
                        )
                    }
                } else {
                    val freedBytes = repository.deleteFiles(selectedFiles)
                    if (freedBytes <= 0L) {
                        error(deletionFailedMessage())
                    }
                    delayIfNeeded(deleteDelayMillis)
                    rebuildAfterMutation(kind)
                    _uiState.update {
                        it.copy(
                            phase = FileManagerPhase.CompleteAnimation,
                            selectedIds = emptySet(),
                            deletedBytes = freedBytes
                        )
                    }
                }
                delayIfNeeded(completeDelayMillis)
                _uiState.update { it.copy(phase = FileManagerPhase.Result) }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        phase = FileManagerPhase.Browsing,
                        errorMessage = error.message ?: appString(R.string.deletion_failed)
                    )
                }
            }
        }
    }

    fun continueManaging() {
        _uiState.update { deleteController.continueManaging(it) }
    }

    private fun loadInternal(kind: FileManagerFeature) {
        _uiState.value = FileManagerUiState(kind = kind, phase = FileManagerPhase.Scanning)
        launchLoad {
            runCatching { collectionLoader.buildState(kind) }
                .onSuccess { loaded ->
                    delayIfNeeded(scanDelayMillis)
                    _uiState.value = loaded.copy(
                        phase = if (loadedHasResults(loaded)) FileManagerPhase.Browsing else FileManagerPhase.NoResults
                    )
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            phase = FileManagerPhase.NoResults,
                            errorMessage = error.message ?: fileScanFailedMessage()
                        )
                    }
                }
        }
    }

    private fun currentItemsLoaded(): Boolean {
        val state = _uiState.value
        return state.galleryTabs.isNotEmpty() || state.mediaConfig != null || state.managedConfig != null
    }

    private suspend fun rebuildAfterMutation(kind: FileManagerFeature) {
        val rebuilt = collectionLoader.buildState(kind)
        _uiState.update {
            rebuilt.copy(
                phase = it.phase,
                deletedBytes = it.deletedBytes,
                removedLocationCount = it.removedLocationCount
            )
        }
    }

    private fun loadedHasResults(state: FileManagerUiState): Boolean =
        when {
            state.isGalleryFeature -> state.galleryTabs.firstOrNull()?.items.orEmpty().isNotEmpty()
            state.managedConfig != null -> state.managedConfig.items.isNotEmpty()
            else -> state.mediaConfig?.items.orEmpty().isNotEmpty()
        }

    private suspend fun delayIfNeeded(millis: Long) {
        if (millis > 0L) delay(millis)
    }

    private fun launchLoad(block: suspend () -> Unit) {
        val loader = testLoader
        if (loader != null) {
            loader(block)
        } else {
            viewModelScope.launch(ioDispatcher) { block() }
        }
    }
}
