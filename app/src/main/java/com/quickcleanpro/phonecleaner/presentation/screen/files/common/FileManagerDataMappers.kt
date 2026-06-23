package com.quickcleanpro.phonecleaner.presentation.screen.files.common

import androidx.compose.ui.graphics.Color
import com.quickcleanpro.phonecleaner.domain.model.file.ManagedFileItem
import com.quickcleanpro.phonecleaner.domain.model.file.ManagedFileType
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.components.filterMediaGridItems
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.components.filterFileManagerListItems
import com.quickcleanpro.phonecleaner.utils.FileSizeFormatter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

internal fun mapFileManagerMediaItems(files: List<ManagedFileItem>, limit: Int = 60): List<FileManagerMediaItem> =
    files.take(limit).mapIndexed { index, file ->
        FileManagerMediaItem(
            id = index + 1,
            sizeLabel = file.formattedSize,
            colors = realFileColors(index),
            realFile = file
        )
    }

internal fun mapFileManagerListItems(files: List<ManagedFileItem>): List<FileManagerListItem> =
    files.sortedByDescending { it.sizeBytes }
        .mapIndexed { index, file ->
            FileManagerListItem(
                id = index + 1,
                name = file.name,
                meta = "${formatFileDate(file.modifiedSeconds)} ${file.formattedSize}",
                sizeLabel = file.formattedSize,
                kind = if (file.type == ManagedFileType.Video) {
                    FileManagerItemKind.LargeVideo
                } else {
                    FileManagerItemKind.Document
                },
                realFile = file
            )
        }

internal fun FileManagerMediaItem.toFileManagerDetailItem(): FileManagerDetailItem =
    FileManagerDetailItem(
        id = id,
        name = realFile?.name.orEmpty(),
        meta = realFile?.let { "${formatFileDate(it.modifiedSeconds)} ${it.formattedSize}" }.orEmpty(),
        sizeLabel = sizeLabel,
        preview = FileManagerDetailPreview.MediaPreview(this),
        realFile = realFile,
    )

internal fun FileManagerListItem.toFileManagerDetailItem(): FileManagerDetailItem =
    FileManagerDetailItem(
        id = id,
        name = name,
        meta = meta,
        sizeLabel = sizeLabel,
        preview = FileManagerDetailPreview.FileIconPreview(kind),
        realFile = realFile,
    )

internal fun mapDuplicateGroups(groups: List<List<ManagedFileItem>>): List<DuplicateGroupItem> =
    groups.filter { it.isNotEmpty() }
        .mapIndexed { groupIndex, files ->
            val first = files.first()
            DuplicateGroupItem(
                id = groupIndex + 1,
                name = first.name,
                sizeLabel = first.formattedSize,
                duplicateCount = files.size,
                files = files.mapIndexed { index, file ->
                    DuplicateFileEntry(
                        id = index + 1,
                        path = file.path ?: file.name,
                        date = formatFileDate(file.modifiedSeconds),
                        sizeLabel = file.formattedSize,
                        selected = index > 0,
                        note = if (index == 0) "Removal not recommended" else null,
                        realFile = file
                    )
                }
            )
        }

internal fun realFileColors(index: Int): List<Color> {
    val palettes = listOf(
        listOf(Color(0xFF36543B), Color(0xFFD5C7B9)),
        listOf(Color(0xFF1D2330), Color(0xFFC47D63)),
        listOf(Color(0xFF5F794A), Color(0xFFFFC4D6)),
        listOf(Color(0xFFA9745D), Color(0xFFF1D6CD)),
        listOf(Color(0xFFE3DFCB), Color(0xFF9DB58D)),
        listOf(Color(0xFF476941), Color(0xFFFFD6E8))
    )
    return palettes[index % palettes.size]
}

internal fun formatFileDate(modifiedSeconds: Long): String {
    val millis = if (modifiedSeconds > 9_999_999_999L) modifiedSeconds else modifiedSeconds * 1000L
    return SimpleDateFormat("yyyy.MM.dd HH:mm:ss", Locale.US).format(Date(millis))
}

internal fun buildFileManagerGalleryTabs(allItems: List<FileManagerMediaItem>): List<FileManagerMediaTab> {
    fun matchesFolder(item: FileManagerMediaItem, folder: String): Boolean {
        val file = item.realFile
        return file?.bucketName?.contains(folder, ignoreCase = true) == true ||
            file?.path?.contains("/$folder/", ignoreCase = true) == true
    }

    val pictures = allItems.filter { matchesFolder(it, "Pictures") }
    val dcim = allItems.filter { matchesFolder(it, "DCIM") || matchesFolder(it, "Camera") }
    val used = (pictures + dcim).map { it.id }.toSet()
    val other = allItems.filter { it.id !in used }

    return listOf(
        FileManagerMediaTab("Photo", allItems.totalFileManagerMediaSizeLabel(), allItems),
        FileManagerMediaTab("Pictures", pictures.totalFileManagerMediaSizeLabel(), pictures),
        FileManagerMediaTab("DCIM", dcim.totalFileManagerMediaSizeLabel(), dcim),
        FileManagerMediaTab("Other", other.totalFileManagerMediaSizeLabel(), other)
    )
}

internal fun buildSimilarFileManagerMediaGroups(items: List<FileManagerMediaItem>): List<FileManagerMediaGroup> {
    fun key(item: FileManagerMediaItem): String {
        val file = item.realFile
        val day = (file?.modifiedSeconds ?: 0L) / 86_400L
        val sizeBucket = ((file?.sizeBytes ?: 0L) / (512L * 1024L)).coerceAtLeast(0L)
        val bucket = file?.bucketName.orEmpty().lowercase()
        return "$bucket#$day#$sizeBucket"
    }

    var nextId = 1
    return items
        .groupBy(::key)
        .values
        .filter { it.size > 1 }
        .take(12)
        .map { group ->
            val sorted = group.sortedByDescending { it.realFile?.sizeBytes ?: 0L }
            val items = sorted.mapIndexed { index, item ->
                item.copy(id = nextId++, bestPhoto = index == 0)
            }
            FileManagerMediaGroup(count = items.size, items = items)
        }
}

internal fun List<FileManagerMediaItem>.totalFileManagerMediaSizeLabel(): String? {
    val total = sumOf { it.realFile?.sizeBytes ?: 0L }
    return total.takeIf { it > 0L }?.let { FileSizeFormatter.format(it) }
}

internal fun buildFileManagerMediaTabs(items: List<FileManagerMediaItem>, titles: List<String>): List<FileManagerTab> =
    titles.map { title ->
        val tabItems = filterMediaGridItems(title, items)
        FileManagerTab(
            title = title,
            sizeLabel = FileSizeFormatter.format(tabItems.sumOf { it.realFile?.sizeBytes ?: 0L })
        )
    }

internal fun buildFileManagerTabs(items: List<FileManagerListItem>, titles: List<String>): List<FileManagerTab> =
    titles.map { title ->
        val tabItems = filterFileManagerListItems(title, items)
        FileManagerTab(
            title = title,
            sizeLabel = FileSizeFormatter.format(tabItems.sumOf { it.realFile?.sizeBytes ?: 0L })
        )
    }

internal fun List<FileManagerListItem>.totalFileManagerListSizeLabel(): String =
    FileSizeFormatter.format(sumOf { it.realFile?.sizeBytes ?: 0L })

internal fun String.splitSizeLabel(): Pair<String, String> =
    substringBefore(" ") to substringAfter(" ", "B")
