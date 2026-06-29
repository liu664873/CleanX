package com.quickcleanpro.phonecleaner.variant

import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder
import com.quickcleanpro.phonecleaner.config.VariantProfile
import com.quickcleanpro.phonecleaner.presentation.app.AppLaunchCoordinator
import com.quickcleanpro.phonecleaner.presentation.common.route.registerAntiVirusRoutes
import com.quickcleanpro.phonecleaner.presentation.common.route.registerAppLockRoutes
import com.quickcleanpro.phonecleaner.presentation.common.route.registerCleanRoutes
import com.quickcleanpro.phonecleaner.presentation.common.route.registerFileManagerRoutes
import com.quickcleanpro.phonecleaner.presentation.common.route.registerHomeRoutes
import com.quickcleanpro.phonecleaner.presentation.common.route.registerSettingsRoutes
import com.quickcleanpro.phonecleaner.presentation.common.route.registerStartupRoutes
import com.quickcleanpro.phonecleaner.presentation.common.route.registerToolboxRoutes

fun currentVariantUiRegistry(): VariantUiRegistry = StorageCleanerVariantUiRegistry

object StorageCleanerVariantUiRegistry : VariantUiRegistry {
    override val permissionUi: VariantPermissionUi = StorageCleanerPermissionUi

    override fun registerStartupRoutes(
        builder: NavGraphBuilder,
        splashPaused: Boolean,
        launchCoordinator: AppLaunchCoordinator?,
        splashNotificationPermissionPrompt: @Composable () -> Unit,
    ) {
        with(builder) {
            registerStartupRoutes(
                splashPaused = splashPaused,
                launchCoordinator = launchCoordinator,
                splashNotificationPermissionPrompt = splashNotificationPermissionPrompt,
            )
        }
    }

    override fun registerHomeRoutes(
        builder: NavGraphBuilder,
        externalBlockingPromptActive: Boolean,
        homeNotificationPermissionPrompt: @Composable () -> Unit,
    ) {
        with(builder) {
            registerHomeRoutes(
                externalBlockingPromptActive = externalBlockingPromptActive,
                homeNotificationPermissionPrompt = homeNotificationPermissionPrompt,
            )
        }
    }

    override fun registerFeatureRoutes(
        builder: NavGraphBuilder,
        profile: VariantProfile,
    ) {
        with(builder) {
            registerCleanRoutes()
            registerAntiVirusRoutes()
            registerAppLockRoutes()
            registerToolboxRoutes()
            registerFileManagerRoutes()
        }
    }

    override fun registerSettingsRoutes(builder: NavGraphBuilder) {
        with(builder) {
            registerSettingsRoutes()
        }
    }
}
