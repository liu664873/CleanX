package com.quickcleanpro.phonecleaner.presentation.navigation

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.navigation.NavHostController

val LocalNavController = staticCompositionLocalOf<NavHostController> {
    error("NavController not provided. Wrap your root composable with CompositionLocalProvider.")
}