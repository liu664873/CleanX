package com.quickcleanpro.phonecleaner.presentation.screen.files

import androidx.compose.ui.graphics.Color
import com.quickcleanpro.phonecleaner.domain.model.file.ManagedFileItem

internal enum class PhotosState {
    Scanning,
    Browsing,
    ConfirmDelete,
    Deleting,
    CompleteAnimation,
    Result,
    NoResults
}

internal data class PhotoItem(
    val id: Int,
    val sizeLabel: String,
    val colors: List<Color>,
    val bestPhoto: Boolean = false,
    val realFile: ManagedFileItem? = null
)

internal data class PhotoTabInfo(
    val title: String,
    val sizeLabel: String?,
    val items: List<PhotoItem>
)

internal data class LoadState<T>(
    val items: List<T> = emptyList(),
    val loaded: Boolean = false
)

internal data class FileManagerActions(
    val onBack: () -> Unit,
    val onResultBack: () -> Unit,
    val onNavigateTool: (String) -> Unit
)

internal enum class CollectionLayout {
    Screenshots,
    SimilarPhotos,
    PhotoPrivacy,
    MediaGrid
}

internal data class PhotoGroup(
    val count: Int,
    val items: List<PhotoItem>
)

internal data class FileCollectionConfig(
    val title: String,
    val scanText: String,
    val actionText: String,
    val processingText: String,
    val resultAmount: String,
    val resultUnit: String,
    val resultCaption: String,
    val layout: CollectionLayout,
    val items: List<PhotoItem>,
    val groups: List<PhotoGroup> = emptyList(),
    val defaultSelectedIds: Set<Int> = emptySet(),
    val tabs: List<ManagedFileTab> = emptyList(),
    val loaded: Boolean = true
)
internal enum class ManagedFileKind {
    LargeVideo,
    Document
}

internal data class ManagedFileUiItem(
    val id: Int,
    val name: String,
    val meta: String,
    val sizeLabel: String,
    val kind: ManagedFileKind,
    val realFile: ManagedFileItem? = null
)

internal data class ManagedFileTab(
    val title: String,
    val sizeLabel: String
)

internal enum class ManagedFileListStyle {
    Default,
    Documents
}

internal data class ManagedFileListConfig(
    val title: String,
    val scanText: String,
    val tabs: List<ManagedFileTab>,
    val items: List<ManagedFileUiItem>,
    val resultAmount: String,
    val resultUnit: String,
    val style: ManagedFileListStyle = ManagedFileListStyle.Default,
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
