package com.quickcleanpro.phonecleaner.presentation.screen.files

import com.quickcleanpro.phonecleaner.domain.repository.FileRepository
import kotlinx.coroutines.CoroutineDispatcher

internal abstract class FeatureFileCollectionViewModel(
    repository: FileRepository,
    ioDispatcher: CoroutineDispatcher,
    val kind: FileCollectionKind
) : FileCollectionViewModel(
    repository = repository,
    ioDispatcher = ioDispatcher
)

internal class PhotosManagerViewModel(
    repository: FileRepository,
    ioDispatcher: CoroutineDispatcher
) : FeatureFileCollectionViewModel(repository, ioDispatcher, FileCollectionKind.Photos)

internal class ScreenshotsManagerViewModel(
    repository: FileRepository,
    ioDispatcher: CoroutineDispatcher
) : FeatureFileCollectionViewModel(repository, ioDispatcher, FileCollectionKind.Screenshots)

internal class VideosManagerViewModel(
    repository: FileRepository,
    ioDispatcher: CoroutineDispatcher
) : FeatureFileCollectionViewModel(repository, ioDispatcher, FileCollectionKind.Videos)

internal class AudiosManagerViewModel(
    repository: FileRepository,
    ioDispatcher: CoroutineDispatcher
) : FeatureFileCollectionViewModel(repository, ioDispatcher, FileCollectionKind.Audios)

internal class SimilarPhotosManagerViewModel(
    repository: FileRepository,
    ioDispatcher: CoroutineDispatcher
) : FeatureFileCollectionViewModel(repository, ioDispatcher, FileCollectionKind.SimilarPhotos)

internal class PhotoPrivacyManagerViewModel(
    repository: FileRepository,
    ioDispatcher: CoroutineDispatcher
) : FeatureFileCollectionViewModel(repository, ioDispatcher, FileCollectionKind.PhotoPrivacy)

internal class LargeFilesManagerViewModel(
    repository: FileRepository,
    ioDispatcher: CoroutineDispatcher
) : FeatureFileCollectionViewModel(repository, ioDispatcher, FileCollectionKind.LargeFiles)

internal class DocumentsManagerViewModel(
    repository: FileRepository,
    ioDispatcher: CoroutineDispatcher
) : FeatureFileCollectionViewModel(repository, ioDispatcher, FileCollectionKind.Documents)
