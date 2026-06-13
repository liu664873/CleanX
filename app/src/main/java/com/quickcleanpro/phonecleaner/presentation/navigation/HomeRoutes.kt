package com.quickcleanpro.phonecleaner.presentation.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController

internal fun NavGraphBuilder.registerHomeRoutes(navController: NavHostController) {
    registerPlaceholderRoutes(
        navController = navController,
        screens = listOf(Screen.Home),
    )
}
