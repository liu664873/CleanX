package com.quickcleanpro.phonecleaner.presentation.app

import androidx.compose.runtime.staticCompositionLocalOf

data class ExternalActivityLaunchHandler(
    val markLaunch: () -> Unit = {},
    val cancelLaunch: () -> Unit = {},
)

val LocalExternalActivityLaunchHandler =
    staticCompositionLocalOf { ExternalActivityLaunchHandler() }
