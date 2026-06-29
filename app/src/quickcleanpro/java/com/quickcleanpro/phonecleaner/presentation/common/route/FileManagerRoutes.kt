package com.quickcleanpro.phonecleaner.presentation.common.route

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.quickcleanpro.phonecleaner.presentation.screen.files.AudiosManagerRoute
import com.quickcleanpro.phonecleaner.presentation.screen.files.DocumentsManagerRoute
import com.quickcleanpro.phonecleaner.presentation.screen.files.DuplicateFilesManagerRoute
import com.quickcleanpro.phonecleaner.presentation.screen.files.LargeFilesManagerRoute
import com.quickcleanpro.phonecleaner.presentation.screen.files.PhotoPrivacyManagerRoute
import com.quickcleanpro.phonecleaner.presentation.screen.files.PhotosManagerRoute
import com.quickcleanpro.phonecleaner.presentation.screen.files.ScreenshotsManagerRoute
import com.quickcleanpro.phonecleaner.presentation.screen.files.SimilarPhotosManagerRoute
import com.quickcleanpro.phonecleaner.presentation.screen.files.VideosManagerRoute

internal fun NavGraphBuilder.registerFileManagerRoutes() {
    composable(Screen.PhotosManager.route) {
        val router = LocalRouter.current
        PhotosManagerRoute(onBack = { router.goBack() }, onResultBack = { router.goBack() }, onNavigateTool = { router.navigate(it) })
    }
    composable(Screen.SimilarPhotosManager.route) {
        val router = LocalRouter.current
        SimilarPhotosManagerRoute(onBack = { router.goBack() }, onResultBack = { router.goBack() }, onNavigateTool = { router.navigate(it) })
    }
    composable(Screen.PhotoPrivacyManager.route) {
        val router = LocalRouter.current
        PhotoPrivacyManagerRoute(onBack = { router.goBack() }, onResultBack = { router.goBack() }, onNavigateTool = { router.navigate(it) })
    }
    composable(Screen.ScreenshotsManager.route) {
        val router = LocalRouter.current
        ScreenshotsManagerRoute(onBack = { router.goBack() }, onResultBack = { router.goBack() }, onNavigateTool = { router.navigate(it) })
    }
    composable(Screen.VideosManager.route) {
        val router = LocalRouter.current
        VideosManagerRoute(onBack = { router.goBack() }, onResultBack = { router.goBack() }, onNavigateTool = { router.navigate(it) })
    }
    composable(Screen.AudiosManager.route) {
        val router = LocalRouter.current
        AudiosManagerRoute(onBack = { router.goBack() }, onResultBack = { router.goBack() }, onNavigateTool = { router.navigate(it) })
    }
    composable(Screen.LargeFilesManager.route) {
        val router = LocalRouter.current
        LargeFilesManagerRoute(onBack = { router.goBack() }, onResultBack = { router.goBack() }, onNavigateTool = { router.navigate(it) })
    }
    composable(Screen.DuplicateFilesManager.route) {
        val router = LocalRouter.current
        DuplicateFilesManagerRoute(onBack = { router.goBack() }, onResultBack = { router.goBack() }, onNavigateTool = { router.navigate(it) })
    }
    composable(Screen.DocumentsManager.route) {
        val router = LocalRouter.current
        DocumentsManagerRoute(onBack = { router.goBack() }, onResultBack = { router.goBack() }, onNavigateTool = { router.navigate(it) })
    }
}
