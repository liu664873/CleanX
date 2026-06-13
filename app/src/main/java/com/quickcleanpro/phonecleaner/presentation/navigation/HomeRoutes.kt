package com.quickcleanpro.phonecleaner.presentation.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.quickcleanpro.phonecleaner.presentation.screen.home.HomeScreen
import com.quickcleanpro.phonecleaner.presentation.screen.splash.SplashScreen

internal fun NavGraphBuilder.registerHomeRoutes(
    navController: NavHostController,
    externalBlockingPromptActive: Boolean = false,
    onNavigate:(AppNavigationEvent) -> Unit = { event -> navController.handleNavigationEvent(event) }
) {
    composable(Screen.Splash.route) {
        HomeScreen(
            externalBlockingPromptActive,
            onNavigate
        )
    }
}
