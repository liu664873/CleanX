package com.quickcleanpro.phonecleaner.presentation.ads

interface AdController {
    fun showSplashIfAvailable(placement: AdPlacement = AdPlacement.SplashLaunch, onComplete: () -> Unit)

    fun showInterstitialIfAvailable(placement: AdPlacement, onComplete: () -> Unit)

    fun nativeAdState(placement: AdPlacement): NativeAdState
}

object NoOpAdController : AdController {
    override fun showSplashIfAvailable(placement: AdPlacement, onComplete: () -> Unit) {
        onComplete()
    }

    override fun showInterstitialIfAvailable(placement: AdPlacement, onComplete: () -> Unit) {
        onComplete()
    }

    override fun nativeAdState(placement: AdPlacement): NativeAdState = NativeAdState.Unavailable
}

sealed interface NativeAdState {
    data object Unavailable : NativeAdState
}
