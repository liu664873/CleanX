package com.quickcleanpro.phonecleaner.presentation.screen.files.audios

import com.quickcleanpro.phonecleaner.domain.repository.FileRepository
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.FeatureFileManagerViewModel
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.FileManagerFeature
import kotlinx.coroutines.CoroutineDispatcher

internal class AudiosManagerViewModel(
    repository: FileRepository,
    ioDispatcher: CoroutineDispatcher
) : FeatureFileManagerViewModel(repository, ioDispatcher, FileManagerFeature.Audios)
