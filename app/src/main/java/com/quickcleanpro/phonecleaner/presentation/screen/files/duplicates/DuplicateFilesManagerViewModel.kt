package com.quickcleanpro.phonecleaner.presentation.screen.files.duplicates

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quickcleanpro.phonecleaner.R
import com.quickcleanpro.phonecleaner.domain.model.file.ManagedFileItem
import com.quickcleanpro.phonecleaner.domain.repository.FileRepository
import com.quickcleanpro.phonecleaner.presentation.common.fileRepositoryOrPreview
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.DuplicateFileEntry
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.DuplicateGroupItem
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.FILE_DELETE_ANIMATION_MIN_MILLIS
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.FileManagerPhase
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.appString
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.deletionFailedMessage
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.duplicateFileKey
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.duplicateScanFailedMessage
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.mapDuplicateGroups
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal data class DuplicateFilesManagerUiState(
    val phase: FileManagerPhase = FileManagerPhase.Scanning,
    val groups: List<DuplicateGroupItem> = emptyList(),
    val selectedGroupId: Int? = null,
    val selectedFileKeys: Set<String> = emptySet(),
    val deletedBytes: Long = 0L,
    val errorMessage: String? = null
) {
    val selectedGroup: DuplicateGroupItem?
        get() = groups.firstOrNull { it.id == selectedGroupId }

    val allDeleteFileKeys: Set<String>
        get() = groups.flatMap { group -> group.files.drop(1).map(::duplicateFileKey) }.toSet()

    val filesToDelete: List<ManagedFileItem>
        get() = groups.flatMap { group ->
            group.files
                .filter { duplicateFileKey(it) in selectedFileKeys }
                .mapNotNull { it.realFile }
        }

    val selectedDeleteSize: Long get() = filesToDelete.sumOf { it.sizeBytes }

    val allSelected: Boolean
        get() = allDeleteFileKeys.isNotEmpty() && selectedFileKeys.containsAll(allDeleteFileKeys)

    val selectedUris: List<Uri> get() = filesToDelete.map { it.uri }
}

internal class DuplicateFilesManagerViewModel(
    private val repository: FileRepository = fileRepositoryOrPreview(),
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val testLoader: (((suspend () -> Unit)) -> Unit)? = null,
    private val scanDelayMillis: Long = 900L,
    private val deleteDelayMillis: Long = FILE_DELETE_ANIMATION_MIN_MILLIS,
    private val completeDelayMillis: Long = 700L
) : ViewModel() {

    constructor(
        repository: FileRepository,
        ioDispatcher: CoroutineDispatcher
    ) : this(
        repository = repository,
        ioDispatcher = ioDispatcher,
        testLoader = null,
        scanDelayMillis = 900L,
        deleteDelayMillis = FILE_DELETE_ANIMATION_MIN_MILLIS,
        completeDelayMillis = 700L
    )

    private val _uiState = MutableStateFlow(DuplicateFilesManagerUiState())
    val uiState: StateFlow<DuplicateFilesManagerUiState> = _uiState.asStateFlow()

    fun refresh() {
        _uiState.value = DuplicateFilesManagerUiState(phase = FileManagerPhase.Scanning)
        launchLoad {
            runCatching { mapDuplicateGroups(repository.loadDuplicateFiles()) }
                .onSuccess { groups ->
                    delayIfNeeded(scanDelayMillis)
                    _uiState.value = DuplicateFilesManagerUiState(
                        phase = if (groups.isEmpty()) FileManagerPhase.NoResults else FileManagerPhase.Browsing,
                        groups = groups,
                        selectedFileKeys = groups.flatMap { it.files.drop(1).map(::duplicateFileKey) }.toSet()
                    )
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            phase = FileManagerPhase.NoResults,
                            errorMessage = error.message ?: duplicateScanFailedMessage()
                        )
                    }
                }
        }
    }

    fun openGroup(group: DuplicateGroupItem) {
        _uiState.update { it.copy(selectedGroupId = group.id) }
    }

    fun closeGroup() {
        _uiState.update { it.copy(selectedGroupId = null) }
    }

    fun toggleAll() {
        _uiState.update {
            it.copy(selectedFileKeys = if (it.allSelected) emptySet() else it.allDeleteFileKeys)
        }
    }

    fun toggleFile(file: DuplicateFileEntry) {
        val key = duplicateFileKey(file)
        _uiState.update {
            it.copy(
                selectedFileKeys = if (key in it.selectedFileKeys) {
                    it.selectedFileKeys - key
                } else {
                    it.selectedFileKeys + key
                }
            )
        }
    }

    fun autoSelectCurrentGroup() {
        val group = _uiState.value.selectedGroup ?: return
        val groupKeys = group.files.map(::duplicateFileKey).toSet()
        val deletableGroupKeys = group.files.drop(1).map(::duplicateFileKey).toSet()
        _uiState.update {
            it.copy(selectedFileKeys = (it.selectedFileKeys - groupKeys) + deletableGroupKeys)
        }
    }

    fun toggleCurrentGroupSelection() {
        val group = _uiState.value.selectedGroup ?: return
        val groupKeys = group.files.map(::duplicateFileKey).toSet()
        val deletableGroupKeys = group.files.drop(1).map(::duplicateFileKey).toSet()
        _uiState.update {
            val hasSelectedInGroup = it.selectedFileKeys.any { key -> key in groupKeys }
            it.copy(
                selectedFileKeys = if (hasSelectedInGroup) {
                    it.selectedFileKeys - groupKeys
                } else {
                    it.selectedFileKeys + deletableGroupKeys
                }
            )
        }
    }

    fun requestDelete() {
        if (_uiState.value.filesToDelete.isNotEmpty()) {
            _uiState.update { it.copy(phase = FileManagerPhase.ConfirmDelete) }
        }
    }

    fun cancelDelete() {
        _uiState.update { it.copy(phase = FileManagerPhase.Browsing) }
    }

    fun rejectSystemDelete() {
        cancelDelete()
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun deleteSelectedFiles() {
        val selectedFiles = _uiState.value.filesToDelete
        if (selectedFiles.isEmpty()) {
            _uiState.update { it.copy(phase = FileManagerPhase.Browsing) }
            return
        }

        _uiState.update { it.copy(phase = FileManagerPhase.Deleting, selectedGroupId = null) }
        launchLoad {
            runCatching {
                val freedBytes = repository.deleteFiles(selectedFiles)
                if (freedBytes <= 0L) {
                    error(deletionFailedMessage())
                }
                delayIfNeeded(deleteDelayMillis)
                val groups = mapDuplicateGroups(repository.loadDuplicateFiles())
                _uiState.update {
                    it.copy(
                        phase = FileManagerPhase.CompleteAnimation,
                        groups = groups,
                        selectedGroupId = null,
                        selectedFileKeys = emptySet(),
                        deletedBytes = freedBytes
                    )
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
        _uiState.update { it.copy(phase = FileManagerPhase.Browsing, selectedFileKeys = emptySet(), selectedGroupId = null) }
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
