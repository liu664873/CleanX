package com.quickcleanpro.phonecleaner.presentation.common.route

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.quickcleanpro.phonecleaner.presentation.screen.settings.ManagePermissionsScreen
import com.quickcleanpro.phonecleaner.presentation.screen.settings.SettingsScreen
import com.quickcleanpro.phonecleaner.presentation.screen.settings.SettingsViewModel
import org.koin.androidx.compose.koinViewModel

internal fun NavGraphBuilder.registerSettingsRoutes() {
    composable(Screen.Settings.route) {
        val router = LocalRouter.current
        val viewModel: SettingsViewModel = koinViewModel()
        SettingsScreen(
            onBack = { router.goBack() },
            onManagePermissions = { router.navigate(Screen.ManagePermissions) },
            viewModel = viewModel,
        )
    }
    composable(Screen.ManagePermissions.route) {
        val router = LocalRouter.current
        val viewModel: SettingsViewModel = koinViewModel()
        ManagePermissionsScreen(
            onBack = { router.goBack() },
            viewModel = viewModel,
        )
    }
}
