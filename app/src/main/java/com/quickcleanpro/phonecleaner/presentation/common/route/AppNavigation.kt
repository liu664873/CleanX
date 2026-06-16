package com.quickcleanpro.phonecleaner.presentation.common.route

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost

@Composable
fun AppNavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Splash.route,
    splashPaused: Boolean = false,
    interceptors: List<NavigationInterceptor> = emptyList(),
    adManager: AdManager = NoOpAdManager,
) {
    val routeManager =
        remember {
            RouteManager(navController, adManager = adManager).apply {
                interceptors.forEach { addInterceptor(it) }
            }
        }

    CompositionLocalProvider(
        LocalRouter provides routeManager,
    ) {
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.fillMaxSize(),
        ) {
            registerStartupRoutes(
                splashPaused = splashPaused,
            )
            registerHomeRoutes()
            registerCleanRoutes()
            registerAntiVirusRoutes()
            registerAppLockRoutes()
            registerToolboxRoutes()
            registerFileManagerRoutes()
            registerSettingsRoutes()
        }
    }
}
