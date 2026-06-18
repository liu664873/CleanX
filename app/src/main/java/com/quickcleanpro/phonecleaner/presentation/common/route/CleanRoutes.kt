package com.quickcleanpro.phonecleaner.presentation.common.route

import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.quickcleanpro.phonecleaner.R
import com.quickcleanpro.phonecleaner.domain.repository.SettingsRepository
import com.quickcleanpro.phonecleaner.presentation.common.permission.CleanXPermissionFeature
import com.quickcleanpro.phonecleaner.presentation.common.permission.CleanXPermissionType
import com.quickcleanpro.phonecleaner.presentation.common.permission.PermissionGateConfig
import com.quickcleanpro.phonecleaner.presentation.screen.cleanresult.CleanResultViewModel
import com.quickcleanpro.phonecleaner.presentation.screen.result.ResultViewModel
import com.quickcleanpro.phonecleaner.presentation.screen.scan.JunkRemovalScreen
import com.quickcleanpro.phonecleaner.presentation.screen.scan.ScanViewModel
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

internal fun NavGraphBuilder.registerCleanRoutes() {
    composable(Screen.Scan.route) {
        val router = LocalRouter.current
        val scanViewModel: ScanViewModel = koinViewModel()
        val resultViewModel: ResultViewModel = koinViewModel()
        val cleanResultViewModel: CleanResultViewModel = koinViewModel()
        JunkRemovalScreen(
            scanViewModel = scanViewModel,
            resultViewModel = resultViewModel,
            cleanResultViewModel = cleanResultViewModel,
            permissionGateConfig = cleanPermissionConfig(),
            onNavigateBack = { router.goBack() },
            onNavigateHome = { router.goHome() },
        )
    }
}

@Composable
private fun cleanPermissionConfig(): PermissionGateConfig {
    val router = LocalRouter.current
    val settingsRepository: SettingsRepository = koinInject()
    return PermissionGateConfig(
        permissionType = CleanXPermissionType.StorageFiles,
        feature = CleanXPermissionFeature.JunkRemoval,
        onDenied = { router.goBack() },
        settingsRepository = settingsRepository,
        deniedContent = { onRetry ->
            PermissionDeniedContent(
                titleRes = R.string.junk_removal,
                onBack = { router.goBack() },
                onRetry = onRetry,
            )
        },
    )
}
