package com.quickcleanpro.phonecleaner.presentation.common.route

import androidx.compose.runtime.remember
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.quickcleanpro.phonecleaner.R
import com.quickcleanpro.phonecleaner.domain.repository.SettingsRepository
import com.quickcleanpro.phonecleaner.presentation.common.permission.CleanXPermissionFeature
import com.quickcleanpro.phonecleaner.presentation.common.permission.CleanXPermissionType
import com.quickcleanpro.phonecleaner.presentation.common.permission.PermissionGateConfig
import com.quickcleanpro.phonecleaner.presentation.screen.antivirus.AntiVirusScreen
import com.quickcleanpro.phonecleaner.presentation.screen.antivirus.DeepScanVirusScreen
import com.quickcleanpro.phonecleaner.presentation.screen.antivirus.NoVirusResultScreen
import com.quickcleanpro.phonecleaner.presentation.screen.antivirus.QuickScanVirusScreen
import com.quickcleanpro.phonecleaner.presentation.screen.antivirus.ScanVirusResultScreen
import com.quickcleanpro.phonecleaner.presentation.screen.antivirus.VirusScanViewModel
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

internal fun NavGraphBuilder.registerAntiVirusRoutes() {
    composable(Screen.AntiVirus.route) {
        val viewModel: VirusScanViewModel = koinViewModel()
        AntiVirusScreen(
            viewModel = viewModel,
        )
    }

    composable(Screen.VirusQuickScan.route) { backStackEntry ->
        val router = LocalRouter.current
        val parentEntry = remember(backStackEntry) {
            router.navController.antiVirusViewModelOwnerOr(backStackEntry)
        }
        val viewModel: VirusScanViewModel = koinViewModel(viewModelStoreOwner = parentEntry)
        QuickScanVirusScreen(
            viewModel = viewModel,
        )
    }

    composable(Screen.VirusDeepScan.route) { backStackEntry ->
        val router = LocalRouter.current
        val settingsRepository: SettingsRepository = koinInject()
        val parentEntry = remember(backStackEntry) {
            router.navController.antiVirusViewModelOwnerOr(backStackEntry)
        }
        val viewModel: VirusScanViewModel = koinViewModel(viewModelStoreOwner = parentEntry)
        DeepScanVirusScreen(
            viewModel = viewModel,
            permissionGateConfig = PermissionGateConfig(
                permissionType = CleanXPermissionType.StorageFiles,
                feature = CleanXPermissionFeature.VirusDeepScan,
                onDenied = { router.goBack() },
                settingsRepository = settingsRepository,
                deniedContent = { onRetry ->
                    PermissionDeniedContent(
                        titleRes = R.string.anti_virus,
                        onBack = { router.goBack() },
                        onRetry = onRetry,
                    )
                },
            )
        )
    }

    composable(Screen.VirusResult.route) { backStackEntry ->
        val router = LocalRouter.current
        val parentEntry = remember(backStackEntry) {
            router.navController.antiVirusViewModelOwnerOr(backStackEntry)
        }
        val viewModel: VirusScanViewModel = koinViewModel(viewModelStoreOwner = parentEntry)
        ScanVirusResultScreen(
            viewModel = viewModel,
        )
    }

    composable(Screen.NoVirusResult.route) {
        NoVirusResultScreen()
    }
}

private fun NavHostController.antiVirusViewModelOwnerOr(
    fallback: NavBackStackEntry,
): NavBackStackEntry =
    runCatching { getBackStackEntry(Screen.AntiVirus.route) }.getOrDefault(fallback)
