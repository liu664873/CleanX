package com.quickcleanpro.phonecleaner.presentation.common.route

import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.quickcleanpro.phonecleaner.R
import com.quickcleanpro.phonecleaner.domain.repository.SettingsRepository
import com.quickcleanpro.phonecleaner.presentation.common.permission.CleanXPermissionFeature
import com.quickcleanpro.phonecleaner.presentation.common.permission.CleanXPermissionType
import com.quickcleanpro.phonecleaner.presentation.common.permission.PermissionGateConfig
import com.quickcleanpro.phonecleaner.presentation.screen.files.AudiosManagerFeatureRoute
import com.quickcleanpro.phonecleaner.presentation.screen.files.DocumentsManagerFeatureRoute
import com.quickcleanpro.phonecleaner.presentation.screen.files.DuplicateFilesManagerRoute
import com.quickcleanpro.phonecleaner.presentation.screen.files.LargeFilesManagerFeatureRoute
import com.quickcleanpro.phonecleaner.presentation.screen.files.PhotoPrivacyManagerFeatureRoute
import com.quickcleanpro.phonecleaner.presentation.screen.files.PhotosManagerRoute
import com.quickcleanpro.phonecleaner.presentation.screen.files.ScreenshotsManagerFeatureRoute
import com.quickcleanpro.phonecleaner.presentation.screen.files.SimilarPhotosManagerFeatureRoute
import com.quickcleanpro.phonecleaner.presentation.screen.files.VideosManagerFeatureRoute
import org.koin.compose.koinInject

internal fun NavGraphBuilder.registerFileManagerRoutes() {
    composable(Screen.PhotosManager.route) {
        PhotosManagerRoute(permissionGateConfig = fileManagerPermissionConfig())
    }
    composable(Screen.SimilarPhotosManager.route) {
        SimilarPhotosManagerFeatureRoute(permissionGateConfig = fileManagerPermissionConfig())
    }
    composable(Screen.PhotoPrivacyManager.route) {
        PhotoPrivacyManagerFeatureRoute(permissionGateConfig = fileManagerPermissionConfig())
    }
    composable(Screen.ScreenshotsManager.route) {
        ScreenshotsManagerFeatureRoute(permissionGateConfig = fileManagerPermissionConfig())
    }
    composable(Screen.VideosManager.route) {
        VideosManagerFeatureRoute(permissionGateConfig = fileManagerPermissionConfig())
    }
    composable(Screen.AudiosManager.route) {
        AudiosManagerFeatureRoute(permissionGateConfig = fileManagerPermissionConfig())
    }
    composable(Screen.LargeFilesManager.route) {
        LargeFilesManagerFeatureRoute(permissionGateConfig = fileManagerPermissionConfig())
    }
    composable(Screen.DuplicateFilesManager.route) {
        DuplicateFilesManagerRoute(permissionGateConfig = fileManagerPermissionConfig())
    }
    composable(Screen.DocumentsManager.route) {
        DocumentsManagerFeatureRoute(permissionGateConfig = fileManagerPermissionConfig())
    }
}

@Composable
private fun fileManagerPermissionConfig(): PermissionGateConfig {
    val router = LocalRouter.current
    val settingsRepository: SettingsRepository = koinInject()
    return PermissionGateConfig(
        permissionType = CleanXPermissionType.StorageFiles,
        feature = CleanXPermissionFeature.FileManager,
        onDenied = { router.goBack() },
        settingsRepository = settingsRepository,
        deniedContent = { onRetry ->
            FilePermissionDeniedContent(
                titleRes = R.string.home_tab_file_manager,
                onBack = { router.goBack() },
                onRetry = onRetry,
            )
        },
    )
}
