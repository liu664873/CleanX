package com.quickcleanpro.phonecleaner.presentation.common.route

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.quickcleanpro.phonecleaner.presentation.screen.settings.ManagePermissionsScreen
import com.quickcleanpro.phonecleaner.presentation.screen.settings.SettingsScreen

internal fun NavGraphBuilder.registerSettingsRoutes() {
    composable(Screen.Settings.route) {
        SettingsScreen()
    }
    composable(Screen.ManagePermissions.route) {
        ManagePermissionsScreen()
    }
}
