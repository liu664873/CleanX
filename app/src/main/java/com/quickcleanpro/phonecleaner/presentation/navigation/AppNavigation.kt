package com.quickcleanpro.phonecleaner.presentation.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost

@Composable
fun AppNavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Splash.route,
    splashPaused: Boolean = false,
    onboardingScanCompleted: Boolean = false,
    onOnboardingScanCompleted: () -> Unit = {},
) {
    CompositionLocalProvider(LocalNavController provides navController) {
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.fillMaxSize(),
        ) {
            registerStartupRoutes(
                navController = navController,
                splashPaused = splashPaused,
                onboardingScanCompleted = onboardingScanCompleted,
                onOnboardingScanCompleted = onOnboardingScanCompleted,
            )
            registerHomeRoutes(navController)
            registerCleanRoutes(navController)
            registerAntiVirusRoutes(navController)
            registerAppLockRoutes(navController)
            registerToolboxRoutes(navController)
            registerFileManagerRoutes(navController)
            registerSettingsRoutes(navController)
        }
    }
}
