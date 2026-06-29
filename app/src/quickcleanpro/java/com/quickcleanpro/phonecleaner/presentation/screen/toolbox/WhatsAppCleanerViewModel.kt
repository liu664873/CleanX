package com.quickcleanpro.phonecleaner.presentation.screen.toolbox

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quickcleanpro.phonecleaner.R
import com.quickcleanpro.phonecleaner.domain.model.file.ManagedFileItem
import com.quickcleanpro.phonecleaner.domain.model.file.ManagedFileType
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
import com.quickcleanpro.phonecleaner.presentation.common.appString

enum class WhatsAppCleanerPhase {
    Scanning,
    ScanResult,
    Cleaning,
    Result,
    Error
}

enum class WhatsAppCleanerGroup {
    Cache,
    File
}

enum class WhatsAppCleanerCategory {
    Images,
    Videos,
    Audios,
    Documents,
    Databases,
    Other
}

data class WhatsAppCleanerSubItem(
    val group: WhatsAppCleanerGroup,
    val category: WhatsAppCleanerCategory,
    val files: List<ManagedFileItem>,
    val selected: Boolean
) {
    val totalBytes: Long = files.sumOf { it.sizeBytes }
    val hasFiles: Boolean = totalBytes > 0L
}

data class WhatsAppCleanerGroupItem(
    val group: WhatsAppCleanerGroup,
    val children: List<WhatsAppCleanerSubItem>,
    val expanded: Boolean = true
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
    val errorMessage: String? = null
)

class WhatsAppCleanerViewModel(
    private val repository: FileRepository = fileRepositoryOrPreview(),
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val testLoader: (((suspend () -> Unit)) -> Unit)? = null,
    private val scanDelayMillis: Long = 650L,
    private val resultDelayMillis: Long = 1_200L
) : ViewModel() {

    constructor(
        repository: FileRepository,
        ioDispatcher: CoroutineDispatcher
    ) : this(
        repository = repository,
        ioDispatcher = ioDispatcher,
        testLoader = null,
        scanDelayMillis = 650L,
        resultDelayMillis = 1_200L
    )

    private val _uiState = MutableStateFlow(WhatsAppCleanerUiState())
    private var hasStarted = false

    val uiState: StateFlow<WhatsAppCleanerUiState> = _uiState.asStateFlow()
    fun startScanIfNeeded() {
        if (hasStarted) return
        hasStarted = true
        _uiState.value = WhatsAppCleanerUiState(phase = WhatsAppCleanerPhase.Scanning)
        launchLoad {
            runCatching {
                val files = repository.loadWhatsAppFiles()
                delay(scanDelayMillis)
                buildScanResultState(files)
            }.onSuccess { state ->
                _uiState.value = state
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        phase = WhatsAppCleanerPhase.Error,
                        errorMessage = error.message ?: appString(R.string.whatsapp_clean_unavailable)
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
                            }
                        )
                    }
                }
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
                            }
                        )
                    }
                }
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
                }
            )
        }
    }

    fun cleanSelectedFiles() {
        val state = _uiState.value
        if (state.phase != WhatsAppCleanerPhase.ScanResult || state.selectedBytes <= 0L) return
        val selectedFiles = state.groups
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
                errorMessage = null
            )
        }
        launchLoad {
            runCatching {
                val freedBytes = repository.deleteFiles(selectedFiles).takeIf { it > 0L } ?: expectedBytes
                delay(resultDelayMillis)
                WhatsAppCleanerUiState(
                    phase = WhatsAppCleanerPhase.Result,
                    deletedBytes = freedBytes,
                    deletedCount = selectedFiles.size
                )
            }.onSuccess { resultState ->
                _uiState.value = resultState
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        phase = WhatsAppCleanerPhase.Error,
                        errorMessage = error.message ?: appString(R.string.whatsapp_clean_unavailable)
                    )
                }
            }
        }
    }

    fun retry() {
        hasStarted = false
        startScanIfNeeded()
    }

    private fun launchLoad(block: suspend () -> Unit) {
        val loader = testLoader
        if (loader != null) {
            loader(block)
        } else {
            viewModelScope.launch(ioDispatcher) { block() }
        }
    }

    private fun buildScanResultState(files: List<ManagedFileItem>): WhatsAppCleanerUiState {
        val cleanableFiles = files.filter { it.sizeBytes > 0L }
        val groups = WhatsAppCleanerGroup.values().map { group ->
            val groupFiles = cleanableFiles.filter { item -> item.toCleanerGroup() == group }
            WhatsAppCleanerGroupItem(
                group = group,
                children = WhatsAppCleanerCategory.values().map { category ->
                    val categoryFiles = groupFiles.filter { item -> item.toCleanerCategory() == category }
                    WhatsAppCleanerSubItem(
                        group = group,
                        category = category,
                        files = categoryFiles,
                        selected = categoryFiles.isNotEmpty() && category.shouldSelectByDefault(group)
                    )
                }
            )
        }
        return WhatsAppCleanerUiState(
            phase = WhatsAppCleanerPhase.ScanResult,
            groups = groups,
            scannedBytes = groups.sumOf { it.totalBytes },
            selectedBytes = groups.sumOf { it.selectedBytes },
            selectedCount = groups.flatMap { it.children }.filter { it.selected }.sumOf { it.files.size }
        )
    }

    private fun WhatsAppCleanerUiState.withGroups(newGroups: List<WhatsAppCleanerGroupItem>): WhatsAppCleanerUiState =
        copy(
            groups = newGroups,
            selectedBytes = newGroups.sumOf { it.selectedBytes },
            selectedCount = newGroups.flatMap { it.children }.filter { it.selected }.sumOf { it.files.size }
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
            "document" in source || type == ManagedFileType.Document && extension in DOCUMENT_EXTENSIONS -> WhatsAppCleanerCategory.Documents
            type == ManagedFileType.Image -> WhatsAppCleanerCategory.Images
            type == ManagedFileType.Video -> WhatsAppCleanerCategory.Videos
            type == ManagedFileType.Audio -> WhatsAppCleanerCategory.Audios
            else -> WhatsAppCleanerCategory.Other
        }
    }

    private fun WhatsAppCleanerCategory.shouldSelectByDefault(group: WhatsAppCleanerGroup): Boolean =
        group == WhatsAppCleanerGroup.Cache || this == WhatsAppCleanerCategory.Other

    private companion object {
        val DATABASE_EXTENSIONS = setOf("db", "crypt", "crypt5", "crypt7", "crypt8", "crypt12", "crypt14", "crypt15")
        val DOCUMENT_EXTENSIONS = setOf(
            "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt", "csv", "rtf",
            "zip", "rar", "7z", "apk", "json", "xml"
        )
    }
}

