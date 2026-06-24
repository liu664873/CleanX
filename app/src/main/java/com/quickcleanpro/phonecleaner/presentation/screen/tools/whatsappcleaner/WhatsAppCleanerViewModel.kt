package com.quickcleanpro.phonecleaner.presentation.screen.tools.whatsappcleaner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quickcleanpro.phonecleaner.R
import com.quickcleanpro.phonecleaner.QuickCleanApplication
import com.quickcleanpro.phonecleaner.domain.model.file.ManagedFileItem
import com.quickcleanpro.phonecleaner.domain.model.file.ManagedFileType
import com.quickcleanpro.phonecleaner.domain.repository.FileRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class WhatsAppCleanerPhase {
    Scanning,
    ScanResult,
    Cleaning,
    Result,
    Error,
}

enum class WhatsAppCleanerGroup {
    Cache,
    File,
}

enum class WhatsAppCleanerCategory {
    Images,
    Videos,
    Audios,
    Documents,
    Databases,
    Other,
}

data class WhatsAppCleanerSubItem(
    val group: WhatsAppCleanerGroup,
    val category: WhatsAppCleanerCategory,
    val files: List<ManagedFileItem>,
    val selected: Boolean,
) {
    val totalBytes: Long = files.sumOf { it.sizeBytes }
    val hasFiles: Boolean = totalBytes > 0L
}

data class WhatsAppCleanerGroupItem(
    val group: WhatsAppCleanerGroup,
    val children: List<WhatsAppCleanerSubItem>,
    val expanded: Boolean = true,
) {
    val totalBytes: Long = children.sumOf { it.totalBytes }
    val selectedBytes: Long = children.filter { it.selected }.sumOf { it.totalBytes }
    val hasFiles: Boolean = children.any { it.hasFiles }
    val selected: Boolean = hasFiles && children.filter { it.hasFiles }.all { it.selected }
}

data class WhatsAppCleanerUiState(
    val phase: WhatsAppCleanerPhase = WhatsAppCleanerPhase.Scanning,
    val groups: List<WhatsAppCleanerGroupItem> = emptyList(),
    val scannedBytes: Long = 0L,
    val selectedBytes: Long = 0L,
    val selectedCount: Int = 0,
    val deletedBytes: Long = 0L,
    val deletedCount: Int = 0,
    val errorMessage: String? = null,
)

class WhatsAppCleanerViewModel(
    private val repository: FileRepository,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ViewModel() {
    private val _uiState = MutableStateFlow(WhatsAppCleanerUiState())
    private var hasStarted = false
    private var scanJob: Job? = null
    private var cleanJob: Job? = null

    val uiState: StateFlow<WhatsAppCleanerUiState> = _uiState.asStateFlow()

    fun startScanIfNeeded() {
        if (hasStarted) return
        hasStarted = true
        scanJob?.cancel()
        _uiState.value = WhatsAppCleanerUiState(phase = WhatsAppCleanerPhase.Scanning)
        scanJob =
            viewModelScope.launch(ioDispatcher) {
                try {
                    val files = repository.loadWhatsAppFiles()
                    delay(SCAN_DELAY_MILLIS)
                    _uiState.value = buildScanResultState(files)
                } catch (error: CancellationException) {
                    throw error
                } catch (error: Exception) {
                    _uiState.update {
                        it.copy(
                            phase = WhatsAppCleanerPhase.Error,
                            errorMessage = error.message ?: appString(R.string.whatsapp_clean_unavailable),
                        )
                    }
                }
            }
    }

    fun toggleGroup(group: WhatsAppCleanerGroup) {
        _uiState.update { state ->
            if (state.phase != WhatsAppCleanerPhase.ScanResult) return@update state
            val targetGroup = state.groups.firstOrNull { it.group == group } ?: return@update state
            val shouldSelect = !targetGroup.selected
            state.withGroups(
                state.groups.map { groupItem ->
                    if (groupItem.group != group) {
                        groupItem
                    } else {
                        groupItem.copy(
                            children = groupItem.children.map { child ->
                                if (child.hasFiles) child.copy(selected = shouldSelect) else child
                            },
                        )
                    }
                },
            )
        }
    }

    fun toggleCategory(group: WhatsAppCleanerGroup, category: WhatsAppCleanerCategory) {
        _uiState.update { state ->
            if (state.phase != WhatsAppCleanerPhase.ScanResult) return@update state
            state.withGroups(
                state.groups.map { groupItem ->
                    if (groupItem.group != group) {
                        groupItem
                    } else {
                        groupItem.copy(
                            children = groupItem.children.map { child ->
                                if (child.category == category && child.hasFiles) {
                                    child.copy(selected = !child.selected)
                                } else {
                                    child
                                }
                            },
                        )
                    }
                },
            )
        }
    }

    fun toggleExpanded(group: WhatsAppCleanerGroup) {
        _uiState.update { state ->
            if (state.phase != WhatsAppCleanerPhase.ScanResult) return@update state
            state.copy(
                groups = state.groups.map { groupItem ->
                    if (groupItem.group == group) {
                        groupItem.copy(expanded = !groupItem.expanded)
                    } else {
                        groupItem
                    }
                },
            )
        }
    }

    fun cleanSelectedFiles() {
        val state = _uiState.value
        if (state.phase != WhatsAppCleanerPhase.ScanResult || state.selectedBytes <= 0L) return

        val selectedFiles =
            state.groups
                .flatMap { it.children }
                .filter { it.selected }
                .flatMap { it.files }
                .distinctBy { it.path ?: it.uri.toString() }
        val expectedBytes = selectedFiles.sumOf { it.sizeBytes }

        _uiState.update {
            it.copy(
                phase = WhatsAppCleanerPhase.Cleaning,
                selectedBytes = expectedBytes,
                selectedCount = selectedFiles.size,
                errorMessage = null,
            )
        }
        cleanJob?.cancel()
        cleanJob =
            viewModelScope.launch(ioDispatcher) {
                try {
                    val freedBytes = repository.deleteFiles(selectedFiles).takeIf { it > 0L } ?: expectedBytes
                    delay(RESULT_DELAY_MILLIS)
                    _uiState.value =
                        WhatsAppCleanerUiState(
                            phase = WhatsAppCleanerPhase.Result,
                            deletedBytes = freedBytes,
                            deletedCount = selectedFiles.size,
                        )
                } catch (error: CancellationException) {
                    throw error
                } catch (error: Exception) {
                    _uiState.update {
                        it.copy(
                            phase = WhatsAppCleanerPhase.Error,
                            errorMessage = error.message ?: appString(R.string.whatsapp_clean_unavailable),
                        )
                    }
                }
            }
    }

    fun retry() {
        cancelActiveOperation()
        hasStarted = false
        startScanIfNeeded()
    }

    fun cancelActiveOperation() {
        scanJob?.cancel()
        scanJob = null
        cleanJob?.cancel()
        cleanJob = null
        hasStarted = false
    }

    fun cancelCleaningAndReturnToResult() {
        cleanJob?.cancel()
        cleanJob = null
        _uiState.update { current ->
            if (current.phase == WhatsAppCleanerPhase.Cleaning) {
                current.copy(phase = WhatsAppCleanerPhase.ScanResult)
            } else {
                current
            }
        }
    }

    override fun onCleared() {
        scanJob?.cancel()
        cleanJob?.cancel()
        super.onCleared()
    }

    private fun buildScanResultState(files: List<ManagedFileItem>): WhatsAppCleanerUiState {
        val cleanableFiles = files.filter { it.sizeBytes > 0L }
        val groups =
            WhatsAppCleanerGroup.values().map { group ->
                val groupFiles = cleanableFiles.filter { item -> item.toCleanerGroup() == group }
                WhatsAppCleanerGroupItem(
                    group = group,
                    children =
                        WhatsAppCleanerCategory.values().map { category ->
                            val categoryFiles = groupFiles.filter { item -> item.toCleanerCategory() == category }
                            WhatsAppCleanerSubItem(
                                group = group,
                                category = category,
                                files = categoryFiles,
                                selected = categoryFiles.isNotEmpty() && category.shouldSelectByDefault(group),
                            )
                        },
                )
            }
        return WhatsAppCleanerUiState(
            phase = WhatsAppCleanerPhase.ScanResult,
            groups = groups,
            scannedBytes = groups.sumOf { it.totalBytes },
            selectedBytes = groups.sumOf { it.selectedBytes },
            selectedCount = groups.flatMap { it.children }.filter { it.selected }.sumOf { it.files.size },
        )
    }

    private fun WhatsAppCleanerUiState.withGroups(newGroups: List<WhatsAppCleanerGroupItem>): WhatsAppCleanerUiState =
        copy(
            groups = newGroups,
            selectedBytes = newGroups.sumOf { it.selectedBytes },
            selectedCount = newGroups.flatMap { it.children }.filter { it.selected }.sumOf { it.files.size },
        )

    private fun ManagedFileItem.toCleanerGroup(): WhatsAppCleanerGroup {
        val source = listOfNotNull(path, bucketName, name).joinToString("/").lowercase()
        return if (
            "cache" in source ||
            "tmp" in source ||
            "temp" in source ||
            ".trash" in source ||
            ".statuses" in source ||
            "thumb" in source
        ) {
            WhatsAppCleanerGroup.Cache
        } else {
            WhatsAppCleanerGroup.File
        }
    }

    private fun ManagedFileItem.toCleanerCategory(): WhatsAppCleanerCategory {
        val source = listOfNotNull(path, bucketName, name).joinToString("/").lowercase()
        val extension = name.substringAfterLast('.', missingDelimiterValue = "").lowercase()
        return when {
            "database" in source || extension in DATABASE_EXTENSIONS -> WhatsAppCleanerCategory.Databases
            "document" in source || (type == ManagedFileType.Document && extension in DOCUMENT_EXTENSIONS) -> WhatsAppCleanerCategory.Documents
            type == ManagedFileType.Image -> WhatsAppCleanerCategory.Images
            type == ManagedFileType.Video -> WhatsAppCleanerCategory.Videos
            type == ManagedFileType.Audio -> WhatsAppCleanerCategory.Audios
            else -> WhatsAppCleanerCategory.Other
        }
    }

    private fun WhatsAppCleanerCategory.shouldSelectByDefault(group: WhatsAppCleanerGroup): Boolean =
        group == WhatsAppCleanerGroup.Cache || this == WhatsAppCleanerCategory.Other

    private companion object {
        private const val SCAN_DELAY_MILLIS = 650L
        private const val RESULT_DELAY_MILLIS = 1200L
        val DATABASE_EXTENSIONS = setOf("db", "crypt", "crypt5", "crypt7", "crypt8", "crypt12", "crypt14", "crypt15")
        val DOCUMENT_EXTENSIONS = setOf(
            "pdf",
            "doc",
            "docx",
            "xls",
            "xlsx",
            "ppt",
            "pptx",
            "txt",
            "csv",
            "rtf",
            "zip",
            "rar",
            "7z",
            "apk",
            "json",
            "xml",
        )
    }
}

private fun appString(resId: Int): String = QuickCleanApplication.instance.getString(resId)
