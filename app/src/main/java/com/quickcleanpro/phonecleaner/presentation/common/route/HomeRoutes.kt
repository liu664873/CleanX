package com.quickcleanpro.phonecleaner.presentation.common.route

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.quickcleanpro.phonecleaner.presentation.screen.home.HomeScreen

internal fun NavGraphBuilder.registerHomeRoutes(
    externalBlockingPromptActive: Boolean = false,
) {
    composable(Screen.Home.route) {
        HomeScreen(
            externalBlockingPromptActive = externalBlockingPromptActive,
        )
    }
}
