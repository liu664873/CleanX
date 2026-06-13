package com.quickcleanpro.phonecleaner.presentation.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.quickcleanpro.phonecleaner.presentation.screen.onboarding.OnboardingScanScreen
import com.quickcleanpro.phonecleaner.presentation.screen.splash.SplashScreen

internal fun NavGraphBuilder.registerStartupRoutes(
    navController: NavHostController,
    splashPaused: Boolean,
    onboardingScanCompleted: Boolean,
    onOnboardingScanCompleted: () -> Unit,
) {
    composable(Screen.Splash.route) {
        SplashScreen(paused = splashPaused) {
            val targetRoute =
                if (onboardingScanCompleted) {
                    Screen.Home.route
                } else {
                    Screen.OnboardingScan.route
                }
            navController.navigate(targetRoute) {
                popUpTo(Screen.Splash.route) {
                    inclusive = true
                }
                launchSingleTop = true
            }
        }
    }

    composable(Screen.OnboardingScan.route) {
        OnboardingScanScreen(
            onContinueToHome = {
                onOnboardingScanCompleted()
                navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.OnboardingScan.route) {
                        inclusive = true
                    }
                    launchSingleTop = true
                }
            },
        )
    }
}
