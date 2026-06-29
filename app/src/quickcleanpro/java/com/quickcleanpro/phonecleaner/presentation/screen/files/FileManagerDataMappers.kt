package com.quickcleanpro.phonecleaner.presentation.screen.files

import androidx.compose.ui.graphics.Color
import com.quickcleanpro.phonecleaner.domain.model.file.ManagedFileItem
import com.quickcleanpro.phonecleaner.domain.model.file.ManagedFileType
import com.quickcleanpro.phonecleaner.utils.FileSizeFormatter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

internal fun mapPhotoItems(files: List<ManagedFileItem>, limit: Int = 60): List<PhotoItem> =
    files.take(limit).mapIndexed { index, file ->
        PhotoItem(
            id = index + 1,
            sizeLabel = file.formattedSize,
            colors = realFileColors(index),
            realFile = file
        )
    }

internal fun mapManagedFileUiItems(files: List<ManagedFileItem>): List<ManagedFileUiItem> =
    files.sortedByDescending { it.sizeBytes }
        .mapIndexed { index, file ->
            ManagedFileUiItem(
                id = index + 1,
                name = file.name,
                meta = "${formatFileDate(file.modifiedSeconds)} ${file.formattedSize}",
                sizeLabel = file.formattedSize,
                kind = if (file.type == ManagedFileType.Video) {
                    ManagedFileKind.LargeVideo
                } else {
                    ManagedFileKind.Document
                },
                realFile = file
            )
        }

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

internal fun buildPhotoTabs(allPhotos: List<PhotoItem>): List<PhotoTabInfo> {
    fun matchesFolder(item: PhotoItem, folder: String): Boolean {
        val file = item.realFile
        return file?.bucketName?.contains(folder, ignoreCase = true) == true ||
            file?.path?.contains("/$folder/", ignoreCase = true) == true
    }

    val pictures = allPhotos.filter { matchesFolder(it, "Pictures") }
    val dcim = allPhotos.filter { matchesFolder(it, "DCIM") || matchesFolder(it, "Camera") }
    val used = (pictures + dcim).map { it.id }.toSet()
    val other = allPhotos.filter { it.id !in used }

    return listOf(
        PhotoTabInfo("Photo", allPhotos.totalPhotoSizeLabel(), allPhotos),
        PhotoTabInfo("Pictures", pictures.totalPhotoSizeLabel(), pictures),
        PhotoTabInfo("DCIM", dcim.totalPhotoSizeLabel(), dcim),
        PhotoTabInfo("Other", other.totalPhotoSizeLabel(), other)
    )
}

internal fun buildSimilarPhotoGroups(photos: List<PhotoItem>): List<PhotoGroup> {
    fun key(item: PhotoItem): String {
        val file = item.realFile
        val day = (file?.modifiedSeconds ?: 0L) / 86_400L
        val sizeBucket = ((file?.sizeBytes ?: 0L) / (512L * 1024L)).coerceAtLeast(0L)
        val bucket = file?.bucketName.orEmpty().lowercase()
        return "$bucket#$day#$sizeBucket"
    }

    var nextId = 1
    return photos
        .groupBy(::key)
        .values
        .filter { it.size > 1 }
        .take(12)
        .map { group ->
            val sorted = group.sortedByDescending { it.realFile?.sizeBytes ?: 0L }
            val items = sorted.mapIndexed { index, item ->
                item.copy(id = nextId++, bestPhoto = index == 0)
            }
            PhotoGroup(count = items.size, items = items)
        }
}

internal fun List<PhotoItem>.totalPhotoSizeLabel(): String? {
    val total = sumOf { it.realFile?.sizeBytes ?: 0L }
    return total.takeIf { it > 0L }?.let { FileSizeFormatter.format(it) }
}

internal fun buildMediaTabs(items: List<PhotoItem>, titles: List<String>): List<ManagedFileTab> =
    titles.map { title ->
        val tabItems = filterMediaGridItems(title, items)
        ManagedFileTab(
            title = title,
            sizeLabel = FileSizeFormatter.format(tabItems.sumOf { it.realFile?.sizeBytes ?: 0L })
        )
    }

internal fun buildManagedFileTabs(items: List<ManagedFileUiItem>, titles: List<String>): List<ManagedFileTab> =
    titles.map { title ->
        val tabItems = filterManagedFileUiItems(title, items)
        ManagedFileTab(
            title = title,
            sizeLabel = FileSizeFormatter.format(tabItems.sumOf { it.realFile?.sizeBytes ?: 0L })
        )
    }

internal fun List<ManagedFileUiItem>.totalManagedSizeLabel(): String =
    FileSizeFormatter.format(sumOf { it.realFile?.sizeBytes ?: 0L })

internal fun String.splitSizeLabel(): Pair<String, String> =
    substringBefore(" ") to substringAfter(" ", "B")
