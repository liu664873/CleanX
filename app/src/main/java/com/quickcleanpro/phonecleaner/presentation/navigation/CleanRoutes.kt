package com.quickcleanpro.phonecleaner.presentation.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController

internal fun NavGraphBuilder.registerCleanRoutes(navController: NavHostController) {
    registerPlaceholderRoutes(
        navController = navController,
        screens =
            listOf(
                Screen.Scan,
                Screen.Result,
                Screen.CleanResult,
            ),
    )
}
