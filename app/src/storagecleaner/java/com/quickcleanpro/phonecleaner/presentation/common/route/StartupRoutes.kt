package com.quickcleanpro.phonecleaner.presentation.common.route

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.quickcleanpro.phonecleaner.advertise.AdvertisePageMediator
import com.quickcleanpro.phonecleaner.presentation.app.AppLaunchCoordinator
import com.quickcleanpro.phonecleaner.presentation.app.AppLaunchRequest
import com.quickcleanpro.phonecleaner.presentation.app.navigateToNotificationTarget
import com.quickcleanpro.phonecleaner.presentation.screen.onboarding.OnboardingScanScreen
import com.quickcleanpro.phonecleaner.presentation.screen.splash.SplashScreen
import com.quickcleanpro.phonecleaner.presentation.screen.splash.SplashViewModel
import org.koin.androidx.compose.koinViewModel

internal fun NavGraphBuilder.registerStartupRoutes(
    splashPaused: Boolean,
    launchCoordinator: AppLaunchCoordinator?,
    splashNotificationPermissionPrompt: @Composable () -> Unit = {},
) {
    composable(Screen.Splash.route) {
        val context = LocalContext.current
        val router = LocalRouter.current
        val viewModel: SplashViewModel = koinViewModel()
        SplashScreen(paused = splashPaused) {
            AdvertisePageMediator.showSplashConsentThenOpenAd(context.findActivity()) {
                when (val request = launchCoordinator?.consumeRequest() ?: AppLaunchRequest.Normal) {
                    is AppLaunchRequest.NotificationTarget -> {
                        router.navController.navigateToNotificationTarget(request.route)
                    }
                    is AppLaunchRequest.ForegroundReturn -> {
                        Unit
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
        splashNotificationPermissionPrompt()
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

private tailrec fun Context.findActivity(): Activity? =
    when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }
