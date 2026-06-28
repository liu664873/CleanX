package com.quickcleanpro.phonecleaner.presentation.common.route

import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.quickcleanpro.phonecleaner.config.FeatureKey
import com.quickcleanpro.phonecleaner.presentation.screen.files.audios.AudiosManagerScreen
import com.quickcleanpro.phonecleaner.presentation.screen.files.duplicates.DuplicateFilesManagerScreen
import com.quickcleanpro.phonecleaner.presentation.screen.files.documents.DocumentsManagerScreen
import com.quickcleanpro.phonecleaner.presentation.screen.files.largefiles.LargeFilesManagerScreen
import com.quickcleanpro.phonecleaner.presentation.screen.files.photoprivacy.PhotoPrivacyManagerScreen
import com.quickcleanpro.phonecleaner.presentation.screen.files.photos.PhotosManagerScreen
import com.quickcleanpro.phonecleaner.presentation.screen.files.screenshots.ScreenshotsManagerScreen
import com.quickcleanpro.phonecleaner.presentation.screen.files.similarphotos.SimilarPhotosManagerScreen
import com.quickcleanpro.phonecleaner.presentation.screen.files.videos.VideosManagerScreen

internal fun NavGraphBuilder.registerFileManagerRoutes(registry: FeatureRegistry) {
    if (registry.isEnabled(FeatureKey.PHOTOS)) {
        composable(Screen.PhotosManager.route) {
            PhotosManagerScreen()
        }
    }
    if (registry.isEnabled(FeatureKey.SIMILAR_PHOTOS)) {
        composable(Screen.SimilarPhotosManager.route) {
            SimilarPhotosManagerScreen()
        }
    }
    if (registry.isEnabled(FeatureKey.PHOTO_PRIVACY)) {
        composable(Screen.PhotoPrivacyManager.route) {
            PhotoPrivacyManagerScreen()
        }
    }
    if (registry.isEnabled(FeatureKey.SCREENSHOTS)) {
        composable(Screen.ScreenshotsManager.route) {
            ScreenshotsManagerScreen()
        }
    }
    if (registry.isEnabled(FeatureKey.VIDEOS)) {
        composable(Screen.VideosManager.route) {
            VideosManagerScreen()
        }
    }
    if (registry.isEnabled(FeatureKey.AUDIOS)) {
        composable(Screen.AudiosManager.route) {
            AudiosManagerScreen()
        }
    }
    if (registry.isEnabled(FeatureKey.LARGE_FILES)) {
        composable(Screen.LargeFilesManager.route) {
            LargeFilesManagerScreen()
        }
    }
    if (registry.isEnabled(FeatureKey.DUPLICATE_FILES)) {
        composable(Screen.DuplicateFilesManager.route) {
            DuplicateFilesManagerScreen()
        }
    }
    if (registry.isEnabled(FeatureKey.DOCUMENTS)) {
        composable(Screen.DocumentsManager.route) {
            DocumentsManagerScreen()
        }
    }
}
