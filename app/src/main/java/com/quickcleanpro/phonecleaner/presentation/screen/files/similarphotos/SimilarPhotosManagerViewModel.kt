package com.quickcleanpro.phonecleaner.presentation.screen.files.similarphotos

import com.quickcleanpro.phonecleaner.domain.repository.FileRepository
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.FeatureFileManagerViewModel
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.FileManagerFeature
import kotlinx.coroutines.CoroutineDispatcher

internal class SimilarPhotosManagerViewModel(
    repository: FileRepository,
    ioDispatcher: CoroutineDispatcher
) : FeatureFileManagerViewModel(repository, ioDispatcher, FileManagerFeature.SimilarPhotos)
