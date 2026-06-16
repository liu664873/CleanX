package com.quickcleanpro.phonecleaner.presentation.common.route

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.quickcleanpro.phonecleaner.R
import com.quickcleanpro.phonecleaner.domain.repository.SettingsRepository
import com.quickcleanpro.phonecleaner.presentation.common.permission.CleanXPermissionFeature
import com.quickcleanpro.phonecleaner.presentation.common.permission.CleanXPermissionType
import com.quickcleanpro.phonecleaner.presentation.common.permission.PermissionGateConfig
import com.quickcleanpro.phonecleaner.presentation.screen.cleanresult.CleanResultScreen
import com.quickcleanpro.phonecleaner.presentation.screen.cleanresult.CleanResultViewModel
import com.quickcleanpro.phonecleaner.presentation.screen.result.ResultScreen
import com.quickcleanpro.phonecleaner.presentation.screen.result.ResultViewModel
import com.quickcleanpro.phonecleaner.presentation.screen.scan.ScanScreen
import com.quickcleanpro.phonecleaner.presentation.screen.scan.ScanViewModel
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

internal fun NavGraphBuilder.registerCleanRoutes() {
    composable(Screen.Scan.route) {
        val router = LocalRouter.current
        val viewModel: ScanViewModel = koinViewModel()
        ScanScreen(
            viewModel = viewModel,
            onScanComplete = {
                router.navigate(Screen.Result)
            },
            permissionGateConfig = cleanPermissionConfig(),
        )
    }
    composable(Screen.Result.route) {
        val router = LocalRouter.current
        val viewModel: ResultViewModel = koinViewModel()
        LaunchedEffect(Unit) {
            viewModel.loadPreview()
        }
        ResultScreen(
            viewModel = viewModel,
            onCleanComplete = {
                router.navigate(Screen.CleanResult)
            },
        )
    }
    composable(Screen.CleanResult.route) {
        val router = LocalRouter.current
        val viewModel: CleanResultViewModel = koinViewModel()
        LaunchedEffect(Unit) {
            viewModel.loadResult()
        }
        CleanResultScreen(
            viewModel = viewModel,
            onNavigateHome = {
                viewModel.clearResult()
                router.goHome()
            },
            onNavigateTool = { route -> router.navigateAndClearStack(route) },
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
