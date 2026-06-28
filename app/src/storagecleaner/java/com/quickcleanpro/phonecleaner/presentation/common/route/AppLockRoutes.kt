package com.quickcleanpro.phonecleaner.presentation.common.route

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.quickcleanpro.phonecleaner.presentation.screen.applock.AppLockRoute

internal fun NavGraphBuilder.registerAppLockRoutes() {
    composable(Screen.AppLock.route) {
        AppLockRoute()
    }
}
