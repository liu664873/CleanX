












package com.quickcleanpro.phonecleaner.presentation.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.quickcleanpro.phonecleaner.presentation.navigation.AppNavigationEvent
import com.quickcleanpro.phonecleaner.presentation.screen.settings.ManagePermissionsScreen
import com.quickcleanpro.phonecleaner.presentation.screen.settings.SettingsScreen

internal fun NavGraphBuilder.registerSettingsRoutes(navController: NavHostController) {
    val onBack: () -> Unit = { navController.handleNavigationEvent(AppNavigationEvent.Back) }
    val onNavigate: (AppNavigationEvent) -> Unit = navController::handleNavigationEvent

    composable(Screen.Settings.route) {
        SettingsScreen(onBack = onBack, onNavigate = onNavigate)
    }
    composable(Screen.ManagePermissions.route) {
        ManagePermissionsScreen(onBack = onBack)
    }
}
