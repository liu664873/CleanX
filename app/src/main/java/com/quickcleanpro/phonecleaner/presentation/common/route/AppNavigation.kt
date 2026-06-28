package com.quickcleanpro.phonecleaner.presentation.common.route

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.quickcleanpro.phonecleaner.config.VariantConfigs
import com.quickcleanpro.phonecleaner.navigation.AppRoute
import com.quickcleanpro.phonecleaner.navigation.RouteAdPolicy
import com.quickcleanpro.phonecleaner.presentation.app.AppLaunchCoordinator
import com.quickcleanpro.phonecleaner.variant.currentVariantUiRegistry

@Composable
fun AppNavGraph(
    navController: NavHostController,
    startDestination: String = AppRoute.Splash.value,
    launchCoordinator: AppLaunchCoordinator? = null,
    splashPaused: Boolean = false,
    externalBlockingPromptActive: Boolean = false,
    splashNotificationPermissionPrompt: @Composable () -> Unit = {},
    homeNotificationPermissionPrompt: @Composable () -> Unit = {},
    interceptors: List<NavigationInterceptor> = emptyList(),
    adManager: AdManager = NoOpAdManager,
) {
    val profile = remember { VariantConfigs.current }
    val routeAdPolicy = remember(profile) { RouteAdPolicy(profile) }
    val variantUiRegistry = remember { currentVariantUiRegistry() }
    val routeManager =
        remember {
            RouteManager(navController, adManager = adManager).apply {
                addInterceptor(AdInterceptor(routeAdPolicy.featureEntryAdPlacements))
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
            variantUiRegistry.registerStartupRoutes(
                builder = this,
                splashPaused = splashPaused,
                launchCoordinator = launchCoordinator,
                splashNotificationPermissionPrompt = splashNotificationPermissionPrompt,
            )
            variantUiRegistry.registerHomeRoutes(
                builder = this,
                externalBlockingPromptActive = externalBlockingPromptActive,
                homeNotificationPermissionPrompt = homeNotificationPermissionPrompt,
            )
            variantUiRegistry.registerFeatureRoutes(this, profile)
            variantUiRegistry.registerSettingsRoutes(this)
        }
    }
}
