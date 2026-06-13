package com.quickcleanpro.phonecleaner.presentation.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController

internal fun NavGraphBuilder.registerAntiVirusRoutes(navController: NavHostController) {
    registerPlaceholderRoutes(
        navController = navController,
        screens =
            listOf(
                Screen.AntiVirus,
                Screen.VirusQuickScan,
                Screen.VirusDeepScan,
                Screen.VirusResult,
                Screen.NoVirusResult,
            ),
    )
}
