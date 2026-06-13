package com.quickcleanpro.phonecleaner.presentation.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController

internal fun NavGraphBuilder.registerFileManagerRoutes(navController: NavHostController) {
    registerPlaceholderRoutes(
        navController = navController,
        screens =
            listOf(
                Screen.PhotosManager,
                Screen.SimilarPhotosManager,
                Screen.PhotoPrivacyManager,
                Screen.ScreenshotsManager,
                Screen.VideosManager,
                Screen.AudiosManager,
                Screen.LargeFilesManager,
                Screen.DuplicateFilesManager,
                Screen.DocumentsManager,
            ),
    )
}
