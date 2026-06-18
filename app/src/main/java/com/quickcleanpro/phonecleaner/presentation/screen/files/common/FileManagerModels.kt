package com.quickcleanpro.phonecleaner.presentation.screen.files.common

import androidx.compose.ui.graphics.Color
import com.quickcleanpro.phonecleaner.domain.model.file.ManagedFileItem

internal enum class FileManagerPhase {
    Scanning,
    Browsing,
    ConfirmDelete,
    Deleting,
    CompleteAnimation,
    Result,
    NoResults
}

internal data class FileManagerMediaItem(
    val id: Int,
    val sizeLabel: String,
    val colors: List<Color>,
    val bestPhoto: Boolean = false,
    val realFile: ManagedFileItem? = null
)

internal data class FileManagerMediaTab(
    val title: String,
    val sizeLabel: String?,
    val items: List<FileManagerMediaItem>
)

internal data class LoadState<T>(
    val items: List<T> = emptyList(),
    val loaded: Boolean = false
)

internal enum class FileManagerLayout {
    Screenshots,
    SimilarPhotos,
    PhotoPrivacy,
    MediaGrid,
    AudioList
}

internal data class FileManagerMediaGroup(
    val count: Int,
    val items: List<FileManagerMediaItem>
)

internal data class FileManagerMediaConfig(
    val title: String,
    val scanText: String,
    val actionText: String,
    val processingText: String,
    val resultAmount: String,
    val resultUnit: String,
    val resultCaption: String,
    val layout: FileManagerLayout,
    val items: List<FileManagerMediaItem>,
    val groups: List<FileManagerMediaGroup> = emptyList(),
    val defaultSelectedIds: Set<Int> = emptySet(),
    val tabs: List<FileManagerTab> = emptyList(),
    val loaded: Boolean = true
)
internal enum class FileManagerItemKind {
    LargeVideo,
    Document
}

internal data class FileManagerListItem(
    val id: Int,
    val name: String,
    val meta: String,
    val sizeLabel: String,
    val kind: FileManagerItemKind,
    val realFile: ManagedFileItem? = null
)

internal data class FileManagerTab(
    val title: String,
    val sizeLabel: String
)

internal enum class FileManagerListStyle {
    Default,
    Documents
}

internal data class FileManagerListConfig(
    val title: String,
    val scanText: String,
    val tabs: List<FileManagerTab>,
    val items: List<FileManagerListItem>,
    val resultAmount: String,
    val resultUnit: String,
    val style: FileManagerListStyle = FileManagerListStyle.Default,
    val loaded: Boolean = true
)
internal data class DuplicateFileEntry(
    val id: Int,
    val path: String,
    val date: String,
    val sizeLabel: String,
    val selected: Boolean,
    val note: String? = null,
    val realFile: ManagedFileItem? = null
)

internal data class DuplicateGroupItem(
    val id: Int,
    val name: String,
    val sizeLabel: String,
    val duplicateCount: Int,
    val files: List<DuplicateFileEntry>
)

internal fun duplicateFileKey(file: DuplicateFileEntry): String =
    file.realFile?.uri?.toString() ?: "${file.path}#${file.date}#${file.sizeLabel}"
