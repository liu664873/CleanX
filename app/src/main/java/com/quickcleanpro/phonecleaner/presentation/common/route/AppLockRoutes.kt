package com.quickcleanpro.phonecleaner.presentation.common.route

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.quickcleanpro.phonecleaner.R
import com.quickcleanpro.phonecleaner.domain.repository.SettingsRepository
import com.quickcleanpro.phonecleaner.presentation.common.permission.CleanXPermissionFeature
import com.quickcleanpro.phonecleaner.presentation.common.permission.CleanXPermissionType
import com.quickcleanpro.phonecleaner.presentation.common.permission.PermissionGateConfig
import com.quickcleanpro.phonecleaner.presentation.screen.applock.AppLockRoute
import org.koin.compose.koinInject

internal fun NavGraphBuilder.registerAppLockRoutes() {
    composable(Screen.AppLock.route) {
        val router = LocalRouter.current
        val settingsRepository: SettingsRepository = koinInject()
        AppLockRoute(
            permissionGateConfig = PermissionGateConfig(
                permissionType = CleanXPermissionType.UsageAccess,
                feature = CleanXPermissionFeature.AppLock,
                onDenied = { router.goBack() },
                settingsRepository = settingsRepository,
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