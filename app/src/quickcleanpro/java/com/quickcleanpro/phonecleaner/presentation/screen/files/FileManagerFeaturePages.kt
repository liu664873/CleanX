package com.quickcleanpro.phonecleaner.presentation.screen.files

import androidx.compose.runtime.Composable
import org.koin.androidx.compose.koinViewModel

@Composable
internal fun ScreenshotsManagerFeatureRoute(
    onBack: () -> Unit,
    onResultBack: () -> Unit,
    onNavigateTool: (String) -> Unit
) {
    val viewModel: ScreenshotsManagerViewModel = koinViewModel()
    FileCollectionRoute(
        kind = FileCollectionKind.Screenshots,
        onBack = onBack,
        onResultBack = onResultBack,
        onNavigateTool = onNavigateTool,
        viewModel = viewModel
    )
}

@Composable
internal fun VideosManagerFeatureRoute(
    onBack: () -> Unit,
    onResultBack: () -> Unit,
    onNavigateTool: (String) -> Unit
) {
    val viewModel: VideosManagerViewModel = koinViewModel()
    FileCollectionRoute(
        kind = FileCollectionKind.Videos,
        onBack = onBack,
        onResultBack = onResultBack,
        onNavigateTool = onNavigateTool,
        viewModel = viewModel
    )
}

@Composable
internal fun AudiosManagerFeatureRoute(
    onBack: () -> Unit,
    onResultBack: () -> Unit,
    onNavigateTool: (String) -> Unit
) {
    val viewModel: AudiosManagerViewModel = koinViewModel()
    FileCollectionRoute(
        kind = FileCollectionKind.Audios,
        onBack = onBack,
        onResultBack = onResultBack,
        onNavigateTool = onNavigateTool,
        viewModel = viewModel
    )
}

@Composable
internal fun SimilarPhotosManagerFeatureRoute(
    onBack: () -> Unit,
    onResultBack: () -> Unit,
    onNavigateTool: (String) -> Unit
) {
    val viewModel: SimilarPhotosManagerViewModel = koinViewModel()
    FileCollectionRoute(
        kind = FileCollectionKind.SimilarPhotos,
        onBack = onBack,
        onResultBack = onResultBack,
        onNavigateTool = onNavigateTool,
        viewModel = viewModel
    )
}

@Composable
internal fun PhotoPrivacyManagerFeatureRoute(
    onBack: () -> Unit,
    onResultBack: () -> Unit,
    onNavigateTool: (String) -> Unit
) {
    val viewModel: PhotoPrivacyManagerViewModel = koinViewModel()
    FileCollectionRoute(
        kind = FileCollectionKind.PhotoPrivacy,
        onBack = onBack,
        onResultBack = onResultBack,
        onNavigateTool = onNavigateTool,
        viewModel = viewModel
    )
}

@Composable
internal fun LargeFilesManagerFeatureRoute(
    onBack: () -> Unit,
    onResultBack: () -> Unit,
    onNavigateTool: (String) -> Unit
) {
    val viewModel: LargeFilesManagerViewModel = koinViewModel()
    ManagedFileRoute(
        kind = FileCollectionKind.LargeFiles,
        onBack = onBack,
        onResultBack = onResultBack,
        onNavigateTool = onNavigateTool,
        refreshOnResume = true,
        viewModel = viewModel
    )
}

@Composable
internal fun DocumentsManagerFeatureRoute(
    onBack: () -> Unit,
    onResultBack: () -> Unit,
    onNavigateTool: (String) -> Unit
) {
    val viewModel: DocumentsManagerViewModel = koinViewModel()
    ManagedFileRoute(
        kind = FileCollectionKind.Documents,
        onBack = onBack,
        onResultBack = onResultBack,
        onNavigateTool = onNavigateTool,
        refreshOnResume = true,
        viewModel = viewModel
    )
}
