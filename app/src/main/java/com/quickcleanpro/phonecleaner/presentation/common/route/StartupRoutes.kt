package com.quickcleanpro.phonecleaner.presentation.common.route

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.quickcleanpro.phonecleaner.presentation.app.AppLaunchCoordinator
import com.quickcleanpro.phonecleaner.presentation.app.AppLaunchRequest
import com.quickcleanpro.phonecleaner.presentation.app.navigateToNotificationTarget
import com.quickcleanpro.phonecleaner.presentation.app.returnFromForegroundSplash
import com.quickcleanpro.phonecleaner.presentation.screen.onboarding.OnboardingScanScreen
import com.quickcleanpro.phonecleaner.presentation.screen.splash.SplashScreen
import com.quickcleanpro.phonecleaner.presentation.screen.splash.SplashViewModel
import org.koin.androidx.compose.koinViewModel

internal fun NavGraphBuilder.registerStartupRoutes(
    splashPaused: Boolean,
    launchCoordinator: AppLaunchCoordinator?,
) {
    composable(Screen.Splash.route) {
        val router = LocalRouter.current
        val viewModel: SplashViewModel = koinViewModel()
        SplashScreen(paused = splashPaused) {
            when (val request = launchCoordinator?.consumeRequest() ?: AppLaunchRequest.Normal) {
                is AppLaunchRequest.NotificationTarget -> {
                    router.navController.navigateToNotificationTarget(request.route)
                }
                is AppLaunchRequest.ForegroundReturn -> {
                    router.navController.returnFromForegroundSplash(request.previousRoute)
                }
                AppLaunchRequest.Normal -> {
                    val targetScreen =
                        if (viewModel.shouldShowOnboardingScan()) {
                            Screen.OnboardingScan
                        } else {
                            Screen.Home
                        }
                    router.replaceCurrent(targetScreen)
                }
            }
        }
    }

    composable(Screen.OnboardingScan.route) {
        val router = LocalRouter.current
        OnboardingScanScreen(
            onContinueToHome = {
                router.replaceCurrent(Screen.Home)
            },
        )
    }
}
