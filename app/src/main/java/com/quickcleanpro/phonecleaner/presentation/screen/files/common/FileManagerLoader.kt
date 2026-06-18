package com.quickcleanpro.phonecleaner.presentation.screen.files.common

import com.quickcleanpro.phonecleaner.domain.model.file.ManagedFileItem
import com.quickcleanpro.phonecleaner.domain.repository.FileRepository

internal class FileManagerLoader(
    private val repository: FileRepository
) {
    fun hasAllFilesAccess(): Boolean =
        runCatching { repository.hasAllFilesAccess() }.getOrDefault(false)

    suspend fun buildState(kind: FileManagerFeature): FileManagerUiState =
        if (!hasAllFilesAccess()) {
            FileManagerUiState(
                kind = kind,
                errorMessage = fileScanFailedMessage()
            )
        } else when (kind) {
            FileManagerFeature.Photos -> {
                val items = mapFileManagerMediaItems(repository.loadImages())
                FileManagerUiState(
                    kind = kind,
                    galleryTabs = buildFileManagerGalleryTabs(items)
                )
            }
            FileManagerFeature.Screenshots -> {
                val items = mapFileManagerMediaItems(repository.loadScreenshots())
                val resultSize = items.totalFileManagerMediaSizeLabel()?.splitSizeLabel() ?: ("0" to "B")
                FileManagerUiState(
                    kind = kind,
                    mediaConfig = FileManagerMediaConfig(
                        title = "Screenshots",
                        scanText = "Scanning screenshots...",
                        actionText = "Delete",
                        processingText = "Cleanup Completed...",
                        resultAmount = resultSize.first,
                        resultUnit = resultSize.second,
                        resultCaption = "Deleted in this cleanup",
                        layout = FileManagerLayout.Screenshots,
                        items = items
                    )
                )
            }
            FileManagerFeature.Videos -> mediaState(
                kind = kind,
                title = "Videos",
                scanText = "Scanning videos...",
                files = repository.loadVideos(),
                tabTitles = listOf("All", "DCIM", "Download")
            )
            FileManagerFeature.Audios -> mediaState(
                kind = kind,
                title = "Audios",
                scanText = "Scanning audios...",
                files = repository.loadAudios(),
                tabTitles = listOf("All", "Music"),
                layout = FileManagerLayout.AudioList
            )
            FileManagerFeature.SimilarPhotos -> {
                val source = mapFileManagerMediaItems(repository.loadImages())
                val groups = buildSimilarFileManagerMediaGroups(source)
                val resultSize = source.totalFileManagerMediaSizeLabel()?.splitSizeLabel() ?: ("0" to "B")
                FileManagerUiState(
                    kind = kind,
                    mediaConfig = FileManagerMediaConfig(
                        title = "Similar Photos",
                        scanText = "Scanning similar photos...",
                        actionText = "Delete",
                        processingText = "Cleanup Completed...",
                        resultAmount = resultSize.first,
                        resultUnit = resultSize.second,
                        resultCaption = "Deleted in this cleanup",
                        layout = FileManagerLayout.SimilarPhotos,
                        items = groups.flatMap { it.items },
                        groups = groups
                    )
                )
            }
            FileManagerFeature.PhotoPrivacy -> {
                val items = mapFileManagerMediaItems(repository.loadPrivacyImages())
                val selectedIds = items.map { it.id }.toSet()
                FileManagerUiState(
                    kind = kind,
                    mediaConfig = FileManagerMediaConfig(
                        title = "Photo Privacy",
                        scanText = "Scanning photo privacy...",
                        actionText = "Remove Location Data",
                        processingText = "Removing Location Data...",
                        resultAmount = items.size.toString(),
                        resultUnit = "Photos",
                        resultCaption = "Location data removed",
                        layout = FileManagerLayout.PhotoPrivacy,
                        items = items,
                        defaultSelectedIds = selectedIds
                    ),
                    selectedIds = selectedIds
                )
            }
            FileManagerFeature.LargeFiles -> managedState(
                kind = kind,
                title = "Large Files",
                scanText = "Scanning large files...",
                files = repository.loadLargeFiles(),
                style = FileManagerListStyle.Default
            )
            FileManagerFeature.Documents -> managedState(
                kind = kind,
                title = "Documents",
                scanText = "Scanning documents...",
                files = repository.loadDocuments(),
                style = FileManagerListStyle.Documents
            )
        }

    private fun mediaState(
        kind: FileManagerFeature,
        title: String,
        scanText: String,
        files: List<ManagedFileItem>,
        tabTitles: List<String>,
        layout: FileManagerLayout = FileManagerLayout.MediaGrid
    ): FileManagerUiState {
        val items = mapFileManagerMediaItems(files)
        val resultSize = items.totalFileManagerMediaSizeLabel()?.splitSizeLabel() ?: ("0" to "B")
        return FileManagerUiState(
            kind = kind,
            mediaConfig = FileManagerMediaConfig(
                title = title,
                scanText = scanText,
                actionText = "Delete",
                processingText = "Cleanup Completed...",
                resultAmount = resultSize.first,
                resultUnit = resultSize.second,
                resultCaption = "Deleted in this cleanup",
                layout = layout,
                items = items,
                tabs = buildFileManagerMediaTabs(items, tabTitles)
            )
        )
    }

    private fun managedState(
        kind: FileManagerFeature,
        title: String,
        scanText: String,
        files: List<ManagedFileItem>,
        style: FileManagerListStyle
    ): FileManagerUiState {
        val items = mapFileManagerListItems(files)
        val resultSize = items.totalFileManagerListSizeLabel().splitSizeLabel()
        return FileManagerUiState(
            kind = kind,
            managedConfig = FileManagerListConfig(
                title = title,
                scanText = scanText,
                tabs = buildFileManagerTabs(items, listOf("All", "Download", "Other")),
                items = items,
                resultAmount = resultSize.first,
                resultUnit = resultSize.second,
                style = style
            )
        )
    }
}
