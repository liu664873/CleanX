package com.quickcleanpro.phonecleaner.presentation.common.route

import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.quickcleanpro.phonecleaner.R
import com.quickcleanpro.phonecleaner.presentation.common.permission.CleanXFeature
import com.quickcleanpro.phonecleaner.presentation.common.permission.PermissionGateConfig
import com.quickcleanpro.phonecleaner.presentation.screen.files.audios.AudiosManagerScreen
import com.quickcleanpro.phonecleaner.presentation.screen.files.duplicates.DuplicateFilesManagerScreen
import com.quickcleanpro.phonecleaner.presentation.screen.files.documents.DocumentsManagerScreen
import com.quickcleanpro.phonecleaner.presentation.screen.files.largefiles.LargeFilesManagerScreen
import com.quickcleanpro.phonecleaner.presentation.screen.files.photoprivacy.PhotoPrivacyManagerScreen
import com.quickcleanpro.phonecleaner.presentation.screen.files.photos.PhotosManagerScreen
import com.quickcleanpro.phonecleaner.presentation.screen.files.screenshots.ScreenshotsManagerScreen
import com.quickcleanpro.phonecleaner.presentation.screen.files.similarphotos.SimilarPhotosManagerScreen
import com.quickcleanpro.phonecleaner.presentation.screen.files.videos.VideosManagerScreen

internal fun NavGraphBuilder.registerFileManagerRoutes() {
    composable(Screen.PhotosManager.route) {
        PhotosManagerScreen(permissionGateConfig = fileManagerPermissionConfig())
    }
    composable(Screen.SimilarPhotosManager.route) {
        SimilarPhotosManagerScreen(permissionGateConfig = fileManagerPermissionConfig())
    }
    composable(Screen.PhotoPrivacyManager.route) {
        PhotoPrivacyManagerScreen(permissionGateConfig = fileManagerPermissionConfig())
    }
    composable(Screen.ScreenshotsManager.route) {
        ScreenshotsManagerScreen(permissionGateConfig = fileManagerPermissionConfig())
    }
    composable(Screen.VideosManager.route) {
        VideosManagerScreen(permissionGateConfig = fileManagerPermissionConfig())
    }
    composable(Screen.AudiosManager.route) {
        AudiosManagerScreen(permissionGateConfig = fileManagerPermissionConfig())
    }
    composable(Screen.LargeFilesManager.route) {
        LargeFilesManagerScreen(permissionGateConfig = fileManagerPermissionConfig())
    }
    composable(Screen.DuplicateFilesManager.route) {
        DuplicateFilesManagerScreen(permissionGateConfig = fileManagerPermissionConfig())
    }
    composable(Screen.DocumentsManager.route) {
        DocumentsManagerScreen(permissionGateConfig = fileManagerPermissionConfig())
    }
}

@Composable
private fun fileManagerPermissionConfig(): PermissionGateConfig {
    val router = LocalRouter.current
    return PermissionGateConfig(
        cleanXFeature = CleanXFeature.FileManager,
        onDenied = { router.goBack() },
        deniedContent = { onRetry ->
            FilePermissionDeniedContent(
                titleRes = R.string.home_tab_file_manager,
                onBack = { router.goBack() },
                onRetry = onRetry,
            )
        },
    )
}
