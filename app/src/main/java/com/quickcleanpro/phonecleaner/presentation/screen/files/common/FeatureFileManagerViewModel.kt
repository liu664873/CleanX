package com.quickcleanpro.phonecleaner.presentation.screen.files.common

import com.quickcleanpro.phonecleaner.domain.repository.FileRepository
import kotlinx.coroutines.CoroutineDispatcher

internal abstract class FeatureFileManagerViewModel(
    repository: FileRepository,
    ioDispatcher: CoroutineDispatcher,
    val kind: FileManagerFeature
) : FileManagerViewModel(
    initialKind = kind,
    repository = repository,
    ioDispatcher = ioDispatcher
)
