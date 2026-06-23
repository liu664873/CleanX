package com.quickcleanpro.phonecleaner.presentation.screen.files.similarphotos

import android.net.Uri
import com.quickcleanpro.phonecleaner.domain.model.file.ManagedFileItem
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.FileImageDisplayItem
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.FileImageGroupDisplayItem
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.FileOperationPhase
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.managedFileToImageDisplayItem
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.splitFileSizeLabel
import com.quickcleanpro.phonecleaner.utils.FileSizeFormatter

internal data class SimilarPhotoItem(
    val id: Int,
    val file: ManagedFileItem,
    val bestPhoto: Boolean = false,
)

internal data class SimilarPhotoGroup(
    val count: Int,
    val items: List<SimilarPhotoItem>,
)

internal data class SimilarPhotosManagerUiState(
    val phase: FileOperationPhase = FileOperationPhase.Scanning,
    val groups: List<SimilarPhotoGroup> = emptyList(),
    val selectedIds: Set<Int> = emptySet(),
    val detailStartIndex: Int? = null,
    val deletedBytes: Long = 0L,
    val errorMessage: String? = null,
) {
    val items: List<SimilarPhotoItem> get() = groups.flatMap { it.items }
    val displayItems: List<FileImageDisplayItem> get() = items.map { it.toDisplayItem() }
    val displayGroups: List<FileImageGroupDisplayItem>
        get() = groups.map { group -> FileImageGroupDisplayItem(group.count, group.items.map { it.toDisplayItem() }) }
    val visibleIds: Set<Int> get() = items.map { it.id }.toSet()
    val selectedFiles: List<ManagedFileItem> get() = items.filter { it.id in selectedIds }.map { it.file }
    val selectedSizeBytes: Long get() = selectedFiles.sumOf { it.sizeBytes }
    val selectedUris: List<Uri> get() = selectedFiles.map { it.uri }
    val resultSize: Pair<String, String> get() = FileSizeFormatter.format(deletedBytes).splitFileSizeLabel()
}

internal fun buildSimilarPhotoGroups(files: List<ManagedFileItem>, limit: Int = 60): List<SimilarPhotoGroup> {
    val source = files.take(limit)
    fun key(file: ManagedFileItem): String {
        val day = file.modifiedSeconds / 86_400L
        val sizeBucket = (file.sizeBytes / (512L * 1024L)).coerceAtLeast(0L)
        val bucket = file.bucketName.orEmpty().lowercase()
        return "$bucket#$day#$sizeBucket"
    }

    var nextId = 1
    return source
        .groupBy(::key)
        .values
        .filter { it.size > 1 }
        .take(12)
        .map { group ->
            val sorted = group.sortedByDescending { it.sizeBytes }
            val items = sorted.mapIndexed { index, file ->
                SimilarPhotoItem(
                    id = nextId++,
                    file = file,
                    bestPhoto = index == 0,
                )
            }
            SimilarPhotoGroup(count = items.size, items = items)
        }
}

internal fun SimilarPhotoItem.toDisplayItem(): FileImageDisplayItem =
    managedFileToImageDisplayItem(
        id = id,
        file = file,
        bestPhoto = bestPhoto,
    )
