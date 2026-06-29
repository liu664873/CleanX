package com.quickcleanpro.phonecleaner.presentation.ads

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

fun AdController.runWithInterstitial(
    placement: AdPlacement,
    onComplete: () -> Unit
) {
    showInterstitialIfAvailable(placement, onComplete)
}

@Composable
fun rememberInterstitialGate(
    placement: AdPlacement,
    adController: AdController = LocalAdController.current
): (() -> Unit) -> Unit =
    remember(placement, adController) {
        { onComplete -> adController.runWithInterstitial(placement, onComplete) }
    }
