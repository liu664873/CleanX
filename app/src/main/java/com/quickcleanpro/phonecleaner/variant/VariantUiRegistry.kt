package com.quickcleanpro.phonecleaner.variant

import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder
import com.quickcleanpro.phonecleaner.config.VariantProfile
import com.quickcleanpro.phonecleaner.presentation.app.AppLaunchCoordinator

interface VariantUiRegistry {
    val permissionUi: VariantPermissionUi

    fun registerStartupRoutes(
        builder: NavGraphBuilder,
        splashPaused: Boolean,
        launchCoordinator: AppLaunchCoordinator?,
        splashNotificationPermissionPrompt: @Composable () -> Unit,
    )

    fun registerHomeRoutes(
        builder: NavGraphBuilder,
        externalBlockingPromptActive: Boolean,
        homeNotificationPermissionPrompt: @Composable () -> Unit,
    )

    fun registerFeatureRoutes(
        builder: NavGraphBuilder,
        profile: VariantProfile,
    )

    fun registerSettingsRoutes(builder: NavGraphBuilder)
}
