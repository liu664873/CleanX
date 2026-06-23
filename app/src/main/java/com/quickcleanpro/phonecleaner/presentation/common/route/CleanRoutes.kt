package com.quickcleanpro.phonecleaner.presentation.common.route

import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.quickcleanpro.phonecleaner.R
import com.quickcleanpro.phonecleaner.presentation.common.permission.CleanXFeature
import com.quickcleanpro.phonecleaner.presentation.common.permission.PermissionGateConfig
import com.quickcleanpro.phonecleaner.presentation.screen.JunkClean.JunkCleanScreen
import com.quickcleanpro.phonecleaner.presentation.screen.JunkClean.JunkCleanViewModel
import org.koin.androidx.compose.koinViewModel

internal fun NavGraphBuilder.registerCleanRoutes() {
    composable(Screen.Scan.route) {
        val router = LocalRouter.current
        val viewModel: JunkCleanViewModel = koinViewModel()
        JunkCleanScreen(
            viewModel = viewModel,
            permissionGateConfig = cleanPermissionConfig(),
            onNavigateBack = { router.goBack() },
            onNavigateHome = { router.goHome() },
        )
    }
}

@Composable
private fun cleanPermissionConfig(): PermissionGateConfig {
    val router = LocalRouter.current
    return PermissionGateConfig(
        cleanXFeature = CleanXFeature.JunkRemoval,
        onDenied = { router.goBack() },
        deniedContent = { onRetry ->
            PermissionDeniedContent(
                titleRes = R.string.junk_removal,
                onBack = { router.goBack() },
                onRetry = onRetry,
            )
        },
    )
}
