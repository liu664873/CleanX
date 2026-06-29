package com.quickcleanpro.phonecleaner.presentation.ads

import androidx.compose.runtime.staticCompositionLocalOf

val LocalAdController = staticCompositionLocalOf<AdController> {
    NoOpAdController
}
