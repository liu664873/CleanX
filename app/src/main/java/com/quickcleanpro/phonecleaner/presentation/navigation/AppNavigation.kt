package com.quickcleanpro.phonecleaner.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.navigation.NavHostController


@Composable
fun AppNavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Splash.route,
    splashPaused: Boolean = false,
    onboardingScanCompleted: Boolean = false,
    onOnboardingScanCompleted: () -> Unit = {}
){
    CompositionLocalProvider(LocalNavController provides navController) {

    }
}