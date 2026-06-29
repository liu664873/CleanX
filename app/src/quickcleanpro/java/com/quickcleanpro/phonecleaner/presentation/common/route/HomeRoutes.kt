package com.quickcleanpro.phonecleaner.presentation.common.route

import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.quickcleanpro.phonecleaner.data.source.notification.ToolNotificationIntentFactory
import com.quickcleanpro.phonecleaner.presentation.screen.home.HomeScreen
import com.quickcleanpro.phonecleaner.presentation.screen.home.HomeViewModel
import org.koin.androidx.compose.koinViewModel

internal fun NavGraphBuilder.registerHomeRoutes(
    externalBlockingPromptActive: Boolean = false,
    homeNotificationPermissionPrompt: @Composable () -> Unit = {},
) {
    composable(Screen.Home.route) {
        val router = LocalRouter.current
        val viewModel: HomeViewModel = koinViewModel()
        HomeScreen(
            viewModel = viewModel,
            externalBlockingPromptActive = externalBlockingPromptActive,
            onNavigate = { event -> router.routeMotherUiEvent(event) },
        )
        homeNotificationPermissionPrompt()
    }
    composable(ToolNotificationIntentFactory.ROUTE_HOME_FILE_MANAGER) {
        val router = LocalRouter.current
        val viewModel: HomeViewModel = koinViewModel()
        HomeScreen(
            viewModel = viewModel,
            externalBlockingPromptActive = externalBlockingPromptActive,
            initialTabIndex = 1,
            onNavigate = { event -> router.routeMotherUiEvent(event) },
        )
        homeNotificationPermissionPrompt()
    }
    composable(ToolNotificationIntentFactory.ROUTE_HOME_TOOLBOX) {
        val router = LocalRouter.current
        val viewModel: HomeViewModel = koinViewModel()
        HomeScreen(
            viewModel = viewModel,
            externalBlockingPromptActive = externalBlockingPromptActive,
            initialTabIndex = 2,
            onNavigate = { event -> router.routeMotherUiEvent(event) },
        )
        homeNotificationPermissionPrompt()
    }
}

private fun RouteManager.routeMotherUiEvent(event: AppNavigationEvent) {
    when (event) {
        AppNavigationEvent.Back -> goBack()
        AppNavigationEvent.Home -> goHome()
        is AppNavigationEvent.AdDestination -> navigate(event.route)
        is AppNavigationEvent.Destination -> navigate(event)
        is AppNavigationEvent.ReplaceCurrent -> replaceCurrent(event.route)
        is AppNavigationEvent.ReplaceStack -> navigateAndClearStack(event.route)
    }
}
