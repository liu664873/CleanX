package com.quickcleanpro.phonecleaner.presentation.common.route

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.quickcleanpro.phonecleaner.presentation.common.permission.CleanXProtectedAction
import com.quickcleanpro.phonecleaner.presentation.common.permission.LocalCleanXPermissionCoordinator
import com.quickcleanpro.phonecleaner.presentation.screen.applock.AppLockRoute

internal fun NavGraphBuilder.registerAppLockRoutes() {
    composable(Screen.AppLock.route) {
        val router = LocalRouter.current
        val permissionCoordinator = LocalCleanXPermissionCoordinator.current
        var permissionGranted by remember { mutableStateOf(false) }

        LaunchedEffect(permissionCoordinator) {
            permissionCoordinator.guard(
                action = CleanXProtectedAction.AppLockOpenProtectedArea,
                onGranted = { permissionGranted = true },
                onRejected = { router.goBack() },
            )
        }

        if (permissionGranted) {
            AppLockRoute(onBack = { router.goBack() })
        }
    }
}
