package com.quickcleanpro.phonecleaner.presentation.screen.files.common

import com.quickcleanpro.phonecleaner.domain.model.file.ManagedFileItem
import com.quickcleanpro.phonecleaner.domain.repository.FileRepository

internal class FileCollectionLoader(
    private val repository: FileRepository
) {
    fun hasAllFilesAccess(): Boolean =
        runCatching { repository.hasAllFilesAccess() }.getOrDefault(false)

    suspend fun buildState(kind: FileCollectionKind): FileCollectionUiState =
        if (!hasAllFilesAccess()) {
            FileCollectionUiState(
                kind = kind,
                errorMessage = fileScanFailedMessage()
            )
        } else when (kind) {
            FileCollectionKind.Photos -> {
                val items = mapPhotoItems(repository.loadImages())
                FileCollectionUiState(
                    kind = kind,
                    photoTabs = buildPhotoTabs(items)
                )
            }
            FileCollectionKind.Screenshots -> {
                val items = mapPhotoItems(repository.loadScreenshots())
                val resultSize = items.totalPhotoSizeLabel()?.splitSizeLabel() ?: ("0" to "B")
                FileCollectionUiState(
                    kind = kind,
                    photoConfig = FileCollectionConfig(
                        title = "Screenshots",
                        scanText = "Scanning screenshots...",
                        actionText = "Delete",
                        processingText = "Cleanup Completed...",
                        resultAmount = resultSize.first,
                        resultUnit = resultSize.second,
                        resultCaption = "Deleted in this cleanup",
                        layout = CollectionLayout.Screenshots,
                        items = items
                    )
                )
            }
            FileCollectionKind.Videos -> mediaState(
                kind = kind,
                title = "Videos",
                scanText = "Scanning videos...",
                files = repository.loadVideos(),
                tabTitles = listOf("All", "DCIM", "Download")
            )
            FileCollectionKind.Audios -> mediaState(
                kind = kind,
                title = "Audios",
                scanText = "Scanning audios...",
                files = repository.loadAudios(),
                tabTitles = listOf("All", "Music"),
                layout = CollectionLayout.AudioList
            )
            FileCollectionKind.SimilarPhotos -> {
                val source = mapPhotoItems(repository.loadImages())
                val groups = buildSimilarPhotoGroups(source)
                val resultSize = source.totalPhotoSizeLabel()?.splitSizeLabel() ?: ("0" to "B")
                FileCollectionUiState(
                    kind = kind,
                    photoConfig = FileCollectionConfig(
                        title = "Similar Photos",
                        scanText = "Scanning similar photos...",
                        actionText = "Delete",
                        processingText = "Cleanup Completed...",
                        resultAmount = resultSize.first,
                        resultUnit = resultSize.second,
                        resultCaption = "Deleted in this cleanup",
                        layout = CollectionLayout.SimilarPhotos,
                        items = groups.flatMap { it.items },
                        groups = groups
                    )
                )
            }
            FileCollectionKind.PhotoPrivacy -> {
                val items = mapPhotoItems(repository.loadPrivacyImages())
                val selectedIds = items.map { it.id }.toSet()
                FileCollectionUiState(
                    kind = kind,
                    photoConfig = FileCollectionConfig(
                        title = "Photo Privacy",
                        scanText = "Scanning photo privacy...",
                        actionText = "Remove Location Data",
                        processingText = "Removing Location Data...",
                        resultAmount = items.size.toString(),
                        resultUnit = "Photos",
                        resultCaption = "Location data removed",
                        layout = CollectionLayout.PhotoPrivacy,
                        items = items,
                        defaultSelectedIds = selectedIds
                    ),
                    selectedIds = selectedIds
                )
            }
            FileCollectionKind.LargeFiles -> managedState(
                kind = kind,
                title = "Large Files",
                scanText = "Scanning large files...",
                files = repository.loadLargeFiles(),
                style = ManagedFileListStyle.Default
            )
            FileCollectionKind.Documents -> managedState(
                kind = kind,
                title = "Documents",
                scanText = "Scanning documents...",
                files = repository.loadDocuments(),
                style = ManagedFileListStyle.Documents
            )
        }

    private fun mediaState(
        kind: FileCollectionKind,
        title: String,
        scanText: String,
        files: List<ManagedFileItem>,
        tabTitles: List<String>,
        layout: CollectionLayout = CollectionLayout.MediaGrid
    ): FileCollectionUiState {
        val items = mapPhotoItems(files)
        val resultSize = items.totalPhotoSizeLabel()?.splitSizeLabel() ?: ("0" to "B")
        return FileCollectionUiState(
            kind = kind,
            photoConfig = FileCollectionConfig(
                title = title,
                scanText = scanText,
                actionText = "Delete",
                processingText = "Cleanup Completed...",
                resultAmount = resultSize.first,
                resultUnit = resultSize.second,
                resultCaption = "Deleted in this cleanup",
                layout = layout,
                items = items,
                tabs = buildMediaTabs(items, tabTitles)
            )
        )
    }

    private fun managedState(
        kind: FileCollectionKind,
        title: String,
        scanText: String,
        files: List<ManagedFileItem>,
        style: ManagedFileListStyle
    ): FileCollectionUiState {
        val items = mapManagedFileUiItems(files)
        val resultSize = items.totalManagedSizeLabel().splitSizeLabel()
        return FileCollectionUiState(
            kind = kind,
            managedConfig = ManagedFileListConfig(
                title = title,
                scanText = scanText,
                tabs = buildManagedFileTabs(items, listOf("All", "Download", "Other")),
                items = items,
                resultAmount = resultSize.first,
                resultUnit = resultSize.second,
                style = style
            )
        )
    }
}
