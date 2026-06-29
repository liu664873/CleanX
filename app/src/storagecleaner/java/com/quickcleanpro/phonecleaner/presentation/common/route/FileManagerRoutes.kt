package com.quickcleanpro.phonecleaner.presentation.common.route

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
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
        PhotosManagerScreen()
    }
    composable(Screen.SimilarPhotosManager.route) {
        SimilarPhotosManagerScreen()
    }
    composable(Screen.PhotoPrivacyManager.route) {
        PhotoPrivacyManagerScreen()
    }
    composable(Screen.ScreenshotsManager.route) {
        ScreenshotsManagerScreen()
    }
    composable(Screen.VideosManager.route) {
        VideosManagerScreen()
    }
    composable(Screen.AudiosManager.route) {
        AudiosManagerScreen()
    }
    composable(Screen.LargeFilesManager.route) {
        LargeFilesManagerScreen()
    }
    composable(Screen.DuplicateFilesManager.route) {
        DuplicateFilesManagerScreen()
    }
    composable(Screen.DocumentsManager.route) {
        DocumentsManagerScreen()
    }
}
