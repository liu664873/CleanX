package com.quickcleanpro.phonecleaner.presentation.screen.files.common

import android.net.Uri
import com.quickcleanpro.phonecleaner.domain.model.file.ManagedFileItem
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.components.filterMediaGridItems
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.components.filterFileManagerListItems
import com.quickcleanpro.phonecleaner.utils.FileSizeFormatter

internal enum class FileManagerFeature {
    Photos,
    Screenshots,
    Videos,
    Audios,
    SimilarPhotos,
    PhotoPrivacy,
    LargeFiles,
    Documents
}

internal data class FileManagerUiState(
    val kind: FileManagerFeature? = null,
    val phase: FileManagerPhase = FileManagerPhase.Scanning,
    val galleryTabs: List<FileManagerMediaTab> = emptyList(),
    val mediaConfig: FileManagerMediaConfig? = null,
    val managedConfig: FileManagerListConfig? = null,
    val selectedIds: Set<Int> = emptySet(),
    val selectedTabIndex: Int = 0,
    val selectedMediaTabIndex: Int = 0,
    val detailStartIndex: Int? = null,
    val deletedBytes: Long = 0L,
    val removedLocationCount: Int = 0,
    val errorMessage: String? = null
) {
    val isGalleryFeature: Boolean get() = kind == FileManagerFeature.Photos

    val isPhotoPrivacy: Boolean get() = kind == FileManagerFeature.PhotoPrivacy

    val isBrowsingOrConfirming: Boolean
        get() = phase == FileManagerPhase.Browsing || phase == FileManagerPhase.ConfirmDelete

    val currentGalleryItems: List<FileManagerMediaItem>
        get() = galleryTabs.getOrNull(selectedTabIndex)?.items.orEmpty()

    val collectionDetailItems: List<FileManagerMediaItem>
        get() {
            val config = mediaConfig ?: return emptyList()
            return when (config.layout) {
                FileManagerLayout.Screenshots -> config.items
                FileManagerLayout.SimilarPhotos -> config.groups.flatMap { it.items }
                FileManagerLayout.MediaGrid,
                FileManagerLayout.AudioList -> filterMediaGridItems(
                    config.tabs.getOrNull(selectedMediaTabIndex)?.title.orEmpty(),
                    config.items
                )
                FileManagerLayout.PhotoPrivacy -> emptyList()
            }
        }

    val visibleManagedItems: List<FileManagerListItem>
        get() {
            val config = managedConfig ?: return emptyList()
            return filterFileManagerListItems(
                config.tabs.getOrNull(selectedTabIndex)?.title.orEmpty(),
                config.items
            )
        }

    val visibleIds: Set<Int>
        get() = when {
            isGalleryFeature -> currentGalleryItems.map { it.id }.toSet()
            managedConfig != null -> visibleManagedItems.map { it.id }.toSet()
            mediaConfig?.layout == FileManagerLayout.MediaGrid ||
                mediaConfig?.layout == FileManagerLayout.AudioList -> collectionDetailItems.map { it.id }.toSet()
            else -> mediaConfig?.items.orEmpty().map { it.id }.toSet()
        }

    val allSelected: Boolean
        get() = visibleIds.isNotEmpty() && selectedIds.containsAll(visibleIds)

    val selectedFiles: List<ManagedFileItem>
        get() = when {
            isGalleryFeature -> currentGalleryItems.filter { it.id in selectedIds }.mapNotNull { it.realFile }
            managedConfig != null -> managedConfig.items.filter { it.id in selectedIds }.mapNotNull { it.realFile }
            else -> mediaConfig?.items.orEmpty().filter { it.id in selectedIds }.mapNotNull { it.realFile }
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
