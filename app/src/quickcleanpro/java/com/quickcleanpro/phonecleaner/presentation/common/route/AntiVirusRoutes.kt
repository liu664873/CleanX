package com.quickcleanpro.phonecleaner.presentation.common.route

import androidx.compose.runtime.remember
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.quickcleanpro.phonecleaner.presentation.common.permission.CleanXProtectedAction
import com.quickcleanpro.phonecleaner.presentation.common.permission.LocalCleanXPermissionCoordinator
import com.quickcleanpro.phonecleaner.presentation.screen.antivirus.AntiVirusScreen
import com.quickcleanpro.phonecleaner.presentation.screen.antivirus.DeepScanVirusScreen
import com.quickcleanpro.phonecleaner.presentation.screen.antivirus.NoVirusResultScreen
import com.quickcleanpro.phonecleaner.presentation.screen.antivirus.QuickScanVirusScreen
import com.quickcleanpro.phonecleaner.presentation.screen.antivirus.ScanVirusResultScreen
import com.quickcleanpro.phonecleaner.presentation.screen.antivirus.VirusScanViewModel
import org.koin.androidx.compose.koinViewModel

internal fun NavGraphBuilder.registerAntiVirusRoutes() {
    composable(Screen.AntiVirus.route) {
        val router = LocalRouter.current
        val permissionCoordinator = LocalCleanXPermissionCoordinator.current
        AntiVirusScreen(
            onBack = { router.goBack() },
            onQuickScan = { router.navigate(Screen.VirusQuickScan) },
            onDeepScan = {
                permissionCoordinator.guard(
                    action = CleanXProtectedAction.VirusDeepScanStart,
                    onGranted = { router.navigate(Screen.VirusDeepScan) },
                )
            },
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
            onBack = { router.goBack() },
            onThreatsFound = { router.navigate(Screen.VirusResult) },
            onNoThreats = { router.navigate(Screen.NoVirusResult) },
        )
    }

    composable(Screen.VirusDeepScan.route) { backStackEntry ->
        val router = LocalRouter.current
        val parentEntry = remember(backStackEntry, router) {
            router.navController.antiVirusViewModelOwnerOr(backStackEntry)
        }
        val viewModel: VirusScanViewModel = koinViewModel(viewModelStoreOwner = parentEntry)
        DeepScanVirusScreen(
            viewModel = viewModel,
            onBack = { router.goBack() },
            onThreatsFound = { router.navigate(Screen.VirusResult) },
            onNoThreats = { router.navigate(Screen.NoVirusResult) },
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
            onBack = { router.goBack() },
            onNoThreats = { router.navigate(Screen.NoVirusResult) },
        )
    }

    composable(Screen.NoVirusResult.route) {
        val router = LocalRouter.current
        NoVirusResultScreen(
            onBack = { router.goHome() },
            onNavigateTool = { route -> router.navigate(route) },
        )
    }
}

private fun NavHostController.antiVirusViewModelOwnerOr(
    fallback: NavBackStackEntry,
): NavBackStackEntry =
    runCatching { getBackStackEntry(Screen.AntiVirus.route) }.getOrDefault(fallback)
