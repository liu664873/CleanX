package com.quickcleanpro.phonecleaner.presentation.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController

internal fun NavGraphBuilder.registerSettingsRoutes(navController: NavHostController) {
    registerPlaceholderRoutes(
        navController = navController,
        screens =
            listOf(
                Screen.Settings,
                Screen.ManagePermissions,
            ),
    )
}
