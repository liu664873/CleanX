package com.quickcleanpro.phonecleaner.presentation.common.route

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.quickcleanpro.phonecleaner.R
import com.quickcleanpro.phonecleaner.presentation.common.permission.CleanXFeature
import com.quickcleanpro.phonecleaner.presentation.common.permission.PermissionGateConfig
import com.quickcleanpro.phonecleaner.presentation.screen.applock.AppLockRoute

internal fun NavGraphBuilder.registerAppLockRoutes() {
    composable(Screen.AppLock.route) {
        val router = LocalRouter.current
        AppLockRoute(
            permissionGateConfig = PermissionGateConfig(
                cleanXFeature = CleanXFeature.AppLock,
                onDenied = { router.goBack() },
                deniedContent = { onRetry ->
                    PermissionDeniedContent(
                        titleRes = R.string.app_lock,
                        onBack = { router.goBack() },
                        onRetry = onRetry,
                    )
                },
            )
        )
    }
}
