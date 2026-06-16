package com.quickcleanpro.phonecleaner.presentation.screen.files

import androidx.compose.runtime.Composable
import com.quickcleanpro.phonecleaner.presentation.common.permission.PermissionGateConfig
import org.koin.androidx.compose.koinViewModel

@Composable
internal fun ScreenshotsManagerFeatureRoute(
    permissionGateConfig: PermissionGateConfig? = null
) {
    val viewModel: ScreenshotsManagerViewModel = koinViewModel()
    FileCollectionRoute(
        kind = FileCollectionKind.Screenshots,
        viewModel = viewModel,
        permissionGateConfig = permissionGateConfig
    )
}

@Composable
internal fun VideosManagerFeatureRoute(
    permissionGateConfig: PermissionGateConfig? = null
) {
    val viewModel: VideosManagerViewModel = koinViewModel()
    FileCollectionRoute(
        kind = FileCollectionKind.Videos,
        viewModel = viewModel,
        permissionGateConfig = permissionGateConfig
    )
}

@Composable
internal fun AudiosManagerFeatureRoute(
    permissionGateConfig: PermissionGateConfig? = null
) {
    val viewModel: AudiosManagerViewModel = koinViewModel()
    FileCollectionRoute(
        kind = FileCollectionKind.Audios,
        viewModel = viewModel,
        permissionGateConfig = permissionGateConfig
    )
}

@Composable
internal fun SimilarPhotosManagerFeatureRoute(
    permissionGateConfig: PermissionGateConfig? = null
) {
    val viewModel: SimilarPhotosManagerViewModel = koinViewModel()
    FileCollectionRoute(
        kind = FileCollectionKind.SimilarPhotos,
        viewModel = viewModel,
        permissionGateConfig = permissionGateConfig
    )
}

@Composable
internal fun PhotoPrivacyManagerFeatureRoute(
    permissionGateConfig: PermissionGateConfig? = null
) {
    val viewModel: PhotoPrivacyManagerViewModel = koinViewModel()
    FileCollectionRoute(
        kind = FileCollectionKind.PhotoPrivacy,
        viewModel = viewModel,
        permissionGateConfig = permissionGateConfig
    )
}

@Composable
internal fun LargeFilesManagerFeatureRoute(
    permissionGateConfig: PermissionGateConfig? = null
) {
    val viewModel: LargeFilesManagerViewModel = koinViewModel()
    ManagedFileRoute(
        kind = FileCollectionKind.LargeFiles,
        refreshOnResume = true,
        viewModel = viewModel,
        permissionGateConfig = permissionGateConfig
    )
}

@Composable
internal fun DocumentsManagerFeatureRoute(
    permissionGateConfig: PermissionGateConfig? = null
) {
    val viewModel: DocumentsManagerViewModel = koinViewModel()
    ManagedFileRoute(
        kind = FileCollectionKind.Documents,
        refreshOnResume = true,
        viewModel = viewModel,
        permissionGateConfig = permissionGateConfig
    )
}
