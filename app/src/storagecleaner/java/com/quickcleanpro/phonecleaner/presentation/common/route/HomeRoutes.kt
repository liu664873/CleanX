package com.quickcleanpro.phonecleaner.presentation.common.route

import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.quickcleanpro.phonecleaner.data.source.notification.ToolNotificationIntentFactory
import com.quickcleanpro.phonecleaner.presentation.screen.home.HomeScreen

internal fun NavGraphBuilder.registerHomeRoutes(
    externalBlockingPromptActive: Boolean = false,
    homeNotificationPermissionPrompt: @Composable () -> Unit = {},
) {
    composable(Screen.Home.route) {
        HomeScreen(
            externalBlockingPromptActive = externalBlockingPromptActive,
        )
        homeNotificationPermissionPrompt()
    }
    composable(ToolNotificationIntentFactory.ROUTE_HOME_FILE_MANAGER) {
        HomeScreen(
            externalBlockingPromptActive = externalBlockingPromptActive,
            initialTabIndex = 1,
        )
        homeNotificationPermissionPrompt()
    }
    composable(ToolNotificationIntentFactory.ROUTE_HOME_TOOLBOX) {
        HomeScreen(
            externalBlockingPromptActive = externalBlockingPromptActive,
            initialTabIndex = 2,
        )
        homeNotificationPermissionPrompt()
    }
}
