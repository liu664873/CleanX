package com.quickcleanpro.phonecleaner.presentation.common.route

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.quickcleanpro.phonecleaner.presentation.screen.JunkClean.JunkCleanScreen
import com.quickcleanpro.phonecleaner.presentation.screen.JunkClean.JunkCleanViewModel
import org.koin.androidx.compose.koinViewModel

internal fun NavGraphBuilder.registerCleanRoutes() {
    composable(Screen.Scan.route) {
        val router = LocalRouter.current
        val viewModel: JunkCleanViewModel = koinViewModel()
        JunkCleanScreen(
            viewModel = viewModel,
            onNavigateBack = { router.goBack() },
            onNavigateHome = { router.goHome() },
            onNavigateHomeAfterComplete = { router.goHome() },
        )
    }
}
