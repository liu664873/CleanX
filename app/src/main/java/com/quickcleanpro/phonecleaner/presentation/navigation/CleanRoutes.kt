package com.quickcleanpro.phonecleaner.presentation.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.quickcleanpro.phonecleaner.presentation.navigation.AppNavigationEvent
import com.quickcleanpro.phonecleaner.presentation.screen.deviceinfo.JunkCleanScreen

internal fun NavGraphBuilder.registerCleanRoutes(navController: NavHostController) {
    val onBack: () -> Unit = { navController.handleNavigationEvent(AppNavigationEvent.Back) }
    val onNavigate: (AppNavigationEvent) -> Unit = navController::handleNavigationEvent

    composable(Screen.Scan.route) {
        JunkCleanScreen(onBack = onBack, onNavigate = onNavigate)
    }
    composable(Screen.Result.route) {
        JunkCleanScreen(onBack = onBack, onNavigate = onNavigate)
    }
    composable(Screen.CleanResult.route) {
        JunkCleanScreen(onBack = onBack, onNavigate = onNavigate)
    }
}
