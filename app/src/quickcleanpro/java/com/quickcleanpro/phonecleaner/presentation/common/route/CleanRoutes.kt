package com.quickcleanpro.phonecleaner.presentation.common.route

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.quickcleanpro.phonecleaner.presentation.screen.JunkClean.JunkCleanViewModel
import com.quickcleanpro.phonecleaner.presentation.screen.junkclean.QuickCleanProJunkCleanScreen
import org.koin.androidx.compose.koinViewModel

internal fun NavGraphBuilder.registerCleanRoutes() {
    composable(Screen.Scan.route) {
        val router = LocalRouter.current
        val viewModel: JunkCleanViewModel = koinViewModel()
        QuickCleanProJunkCleanScreen(
            viewModel = viewModel,
            onNavigateBack = { router.goBack() },
            onNavigateHome = { router.goHome() },
            onNavigateTool = { route -> router.navigate(route) },
        )
    }
}
