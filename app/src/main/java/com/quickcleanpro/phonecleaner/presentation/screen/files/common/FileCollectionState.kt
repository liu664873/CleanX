package com.quickcleanpro.phonecleaner.presentation.screen.files.common

import android.net.Uri
import com.quickcleanpro.phonecleaner.domain.model.file.ManagedFileItem
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.components.filterMediaGridItems
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.components.filterManagedFileUiItems
import com.quickcleanpro.phonecleaner.utils.FileSizeFormatter

internal enum class FileCollectionKind {
    Photos,
    Screenshots,
    Videos,
    Audios,
    SimilarPhotos,
    PhotoPrivacy,
    LargeFiles,
    Documents
}

internal data class FileCollectionUiState(
    val kind: FileCollectionKind? = null,
    val phase: PhotosState = PhotosState.Scanning,
    val photoTabs: List<PhotoTabInfo> = emptyList(),
    val photoConfig: FileCollectionConfig? = null,
    val managedConfig: ManagedFileListConfig? = null,
    val selectedIds: Set<Int> = emptySet(),
    val selectedTabIndex: Int = 0,
    val selectedMediaTabIndex: Int = 0,
    val detailStartIndex: Int? = null,
    val deletedBytes: Long = 0L,
    val removedLocationCount: Int = 0,
    val errorMessage: String? = null
) {
    val isPhotos: Boolean get() = kind == FileCollectionKind.Photos

    val isPhotoPrivacy: Boolean get() = kind == FileCollectionKind.PhotoPrivacy

    val currentPhotos: List<PhotoItem>
        get() = photoTabs.getOrNull(selectedTabIndex)?.items.orEmpty()

    val collectionDetailItems: List<PhotoItem>
        get() {
            val config = photoConfig ?: return emptyList()
            return when (config.layout) {
                CollectionLayout.Screenshots -> config.items
                CollectionLayout.SimilarPhotos -> config.groups.flatMap { it.items }
                CollectionLayout.MediaGrid,
                CollectionLayout.AudioList -> filterMediaGridItems(
                    config.tabs.getOrNull(selectedMediaTabIndex)?.title.orEmpty(),
                    config.items
                )
                CollectionLayout.PhotoPrivacy -> emptyList()
            }
        }

    val visibleManagedItems: List<ManagedFileUiItem>
        get() {
            val config = managedConfig ?: return emptyList()
            return filterManagedFileUiItems(
                config.tabs.getOrNull(selectedTabIndex)?.title.orEmpty(),
                config.items
            )
        }

    val visibleIds: Set<Int>
        get() = when {
            isPhotos -> currentPhotos.map { it.id }.toSet()
            managedConfig != null -> visibleManagedItems.map { it.id }.toSet()
            photoConfig?.layout == CollectionLayout.MediaGrid ||
                photoConfig?.layout == CollectionLayout.AudioList -> collectionDetailItems.map { it.id }.toSet()
            else -> photoConfig?.items.orEmpty().map { it.id }.toSet()
        }

    val allSelected: Boolean
        get() = visibleIds.isNotEmpty() && selectedIds.containsAll(visibleIds)

    val selectedFiles: List<ManagedFileItem>
        get() = when {
            isPhotos -> currentPhotos.filter { it.id in selectedIds }.mapNotNull { it.realFile }
            managedConfig != null -> managedConfig.items.filter { it.id in selectedIds }.mapNotNull { it.realFile }
            else -> photoConfig?.items.orEmpty().filter { it.id in selectedIds }.mapNotNull { it.realFile }
        }

    val selectedSizeBytes: Long get() = selectedFiles.sumOf { it.sizeBytes }

    val selectedUris: List<Uri> get() = selectedFiles.map { it.uri }

    val resultSize: Pair<String, String>
        get() = if (isPhotoPrivacy) {
            removedLocationCount.toString() to "Photos"
        } else {
            FileSizeFormatter.format(deletedBytes).splitSizeLabel()
        }
}
