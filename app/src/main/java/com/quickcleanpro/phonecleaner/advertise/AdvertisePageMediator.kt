package com.quickcleanpro.phonecleaner.advertise

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.pdffox.adv.AdvertiseSdk
import com.quickcleanpro.phonecleaner.config.FeatureCatalog
import com.quickcleanpro.phonecleaner.config.FeatureKey
import com.quickcleanpro.phonecleaner.config.VariantConfigs

object AdvertisePageMediator : AdvertiseMediator {
    private const val TAG = "AdvertisePageMediator"
    private const val APP_OPEN_TIMEOUT_MS = 6_500L

    fun showSplashConsentThenOpenAd(
        activity: Activity?,
        onContinue: () -> Unit,
    ) {
        if (!AdvertiseRuntimeCapabilities.ADVERTISE_SDK_ENABLED) {
            onContinue()
            return
        }
        if (activity == null || activity.isUnavailable()) {
            onContinue()
            return
        }
        runCatching {
            AdvertiseSdkBridge.showSplashConsent(activity) {
                showLaunchOpenAd(activity, onContinue)
            }
        }.onFailure { throwable ->
            Log.w(TAG, "showSplashConsent failed; continuing with app open", throwable)
            showLaunchOpenAd(activity, onContinue)
        }
    }

    fun initSplashConsent(
        activity: Activity?,
        onComplete: (Boolean) -> Unit,
    ) {
        if (!AdvertiseRuntimeCapabilities.ADVERTISE_SDK_ENABLED) {
            onComplete(false)
            return
        }
        if (activity == null || activity.isUnavailable()) {
            onComplete(false)
            return
        }
        runCatching {
            val hasCachedConsentState = AdvertiseSdkBridge.initConsent(activity) { success ->
                onComplete(success)
            }
            if (hasCachedConsentState) {
                onComplete(true)
            }
        }.onFailure { throwable ->
            Log.w(TAG, "initSplashConsent failed", throwable)
            onComplete(false)
        }
    }

    fun showSplashConsent(
        activity: Activity?,
        onComplete: () -> Unit,
    ) {
        if (!AdvertiseRuntimeCapabilities.ADVERTISE_SDK_ENABLED) {
            onComplete()
            return
        }
        if (activity == null || activity.isUnavailable()) {
            onComplete()
            return
        }
        runCatching {
            AdvertiseSdkBridge.showSplashConsent(activity) {
                onComplete()
            }
        }.onFailure { throwable ->
            Log.w(TAG, "showSplashConsent failed", throwable)
            onComplete()
        }
    }

    fun showFileAccessCancelInterstitial(
        activity: Activity?,
        onClosed: () -> Unit = {},
    ) {
        showInterstitial(activity, AdAreaKeys.Interstitial.FILE_ACCESS_CANCEL, onClosed)
    }

    fun showReturnHomeInterstitial(
        activity: Activity?,
        onClosed: () -> Unit,
    ) {
        showInterstitial(activity, AdAreaKeys.Interstitial.RETURN_HOME_PAGE, onClosed)
    }

    override fun showEntryAd(
        activity: Activity?,
        feature: FeatureKey,
        onClosed: () -> Unit,
    ) {
        val route = FeatureCatalog.routeFor(feature)?.value
        val areaKey = route?.let(VariantConfigs.current.adProfile.placements::entryFor)
        showInterstitial(activity, areaKey, onClosed)
    }

    override fun showFinishAd(
        activity: Activity?,
        feature: FeatureKey,
        onClosed: () -> Unit,
    ) {
        val route = FeatureCatalog.routeFor(feature)?.value
        val areaKey = route?.let(VariantConfigs.current.adProfile.placements::completionFor)
        showInterstitial(activity, areaKey, onClosed)
    }

    override fun showReturnHomeAd(
        activity: Activity?,
        onClosed: () -> Unit,
    ) {
        showReturnHomeInterstitial(activity, onClosed)
    }

    override fun showOperationAd(
        activity: Activity?,
        areaKey: String?,
        onClosed: () -> Unit,
    ) {
        showInterstitial(activity, areaKey, onClosed)
    }

    fun showInterstitial(
        activity: Activity?,
        areaKey: String?,
        onClosed: () -> Unit,
    ) {
        if (!AdvertiseRuntimeCapabilities.ADVERTISE_SDK_ENABLED) {
            onClosed()
            return
        }
        if (activity == null || activity.isUnavailable()) {
            AdEventLogger.showSkipped("activity unavailable")
            onClosed()
            return
        }
        if (areaKey.isNullOrBlank()) {
            AdEventLogger.showSkipped("blank areaKey")
            onClosed()
            return
        }
        AdEventLogger.showRequested(areaKey)

        var continued = false
        fun continueOnce() {
            if (continued) return
            continued = true
            if (!activity.isUnavailable()) {
                AdEventLogger.closed(areaKey)
                onClosed()
            }
        }

        runCatching {
            AdvertiseSdkBridge.showInterstitial(activity, areaKey) {
                continueOnce()
            }
        }.onFailure { throwable ->
            AdEventLogger.showFailed(areaKey, throwable)
            continueOnce()
        }
    }

    @SuppressLint("MissingPermission")
    fun getBannerAd(
        context: Context,
        areaKey: String,
    ): ViewGroup? {
        if (!AdvertiseRuntimeCapabilities.ADVERTISE_SDK_ENABLED) {
            return null
        }
        return runCatching {
            AdvertiseSdkBridge.getBannerAd(context, areaKey)
        }.onFailure { throwable ->
            Log.w(TAG, "getBannerAd failed", throwable)
        }.getOrNull()
    }

    @SuppressLint("MissingPermission")
    fun bindBanner(
        context: Context,
        container: FrameLayout,
        areaKey: String,
    ) {
        clearBanner(container)
        val banner = getBannerAd(context, areaKey)
        if (banner == null) {
            container.visibility = View.GONE
            return
        }
        container.visibility = View.VISIBLE
        (banner.parent as? ViewGroup)?.removeView(banner)
        container.addView(
            banner,
            FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
            ),
        )
    }

    fun clearBanner(container: FrameLayout) {
        for (index in 0 until container.childCount) {
            if (AdvertiseRuntimeCapabilities.ADVERTISE_SDK_ENABLED) {
                (container.getChildAt(index) as? ViewGroup)?.let(AdvertiseSdkBridge::destroyBannerAd)
            }
        }
        container.removeAllViews()
        container.visibility = View.GONE
    }

    fun preloadLaunchAds(context: Context) {
        if (!AdvertiseRuntimeCapabilities.ADVERTISE_SDK_ENABLED) {
            return
        }
        runCatching {
            AdvertiseSdkBridge.preloadLaunchAds(context)
        }.onFailure { throwable ->
            Log.w(TAG, "preloadLaunchAds failed", throwable)
        }
    }

    fun preloadLaunchOpenAd(context: Context) {
        if (!AdvertiseRuntimeCapabilities.ADVERTISE_SDK_ENABLED) {
            return
        }
        runCatching {
            AdvertiseSdkBridge.preloadOpen(context)
        }.onFailure { throwable ->
            Log.w(TAG, "preloadLaunchOpenAd failed", throwable)
        }
    }

    fun hasCachedLaunchOpenAd(): Boolean = false

    fun showCachedLaunchOpenAd(
        activity: Activity?,
        onContinue: () -> Unit,
    ): Boolean {
        onContinue()
        return false
    }

    fun preloadMainPageAds(context: Context) {
        if (!AdvertiseRuntimeCapabilities.ADVERTISE_SDK_ENABLED) {
            return
        }
        runCatching {
            AdvertiseSdkBridge.preloadMainPageAds(context)
        }.onFailure { throwable ->
            Log.w(TAG, "preloadMainPageAds failed", throwable)
        }
    }

    override fun suppressNextAppOpenAd() {
        if (!AdvertiseRuntimeCapabilities.ADVERTISE_SDK_ENABLED) {
            return
        }
        runCatching {
            AdvertiseSdkBridge.suppressNextAppOpenAd()
        }.onFailure { throwable ->
            Log.w(TAG, "suppressNextAppOpenAd failed", throwable)
        }
    }

    private fun showLaunchOpenAd(
        activity: Activity,
        onContinue: () -> Unit,
    ) {
        if (!AdvertiseRuntimeCapabilities.ADVERTISE_SDK_ENABLED) {
            onContinue()
            return
        }
        if (activity.isUnavailable()) {
            onContinue()
            return
        }
        preloadLaunchAds(activity)
        val handler = Handler(Looper.getMainLooper())
        var continued = false
        var openAdLoaded = false

        fun continueOnce() {
            if (continued) return
            continued = true
            handler.removeCallbacksAndMessages(null)
            if (!activity.isUnavailable()) {
                onContinue()
            }
        }

        handler.postDelayed({
            if (!openAdLoaded) {
                continueOnce()
            }
        }, APP_OPEN_TIMEOUT_MS)

        runCatching {
            AdvertiseSdkBridge.showOpenAd(
                activity = activity,
                areaKey = AdAreaKeys.Open.OPEN_PAGE,
                onClosed = {
                    continueOnce()
                },
                onLoaded = {
                    openAdLoaded = true
                    handler.removeCallbacksAndMessages(null)
                },
            )
        }.onFailure { throwable ->
            Log.w(TAG, "showOpenAd failed; continuing flow", throwable)
            continueOnce()
        }
    }

    private fun Activity.isUnavailable(): Boolean = isFinishing || isDestroyed
}

private object AdvertiseSdkBridge {
    fun showSplashConsent(
        activity: Activity,
        onComplete: () -> Unit,
    ) {
        AdvertiseSdk.showSplashConsent(activity, onComplete)
    }

    fun initConsent(
        activity: Activity,
        onComplete: (Boolean) -> Unit,
    ): Boolean = AdvertiseSdk.initConsent(activity, onComplete)

    fun showInterstitial(
        activity: Activity,
        areaKey: String,
        onClosed: () -> Unit,
    ) {
        AdvertiseSdk.showInterstitialAd(activity, areaKey, onClosed)
    }

    fun getBannerAd(
        context: Context,
        areaKey: String,
    ): ViewGroup? = AdvertiseSdk.getBannerAd(context, areaKey)

    fun destroyBannerAd(adView: ViewGroup) {
        AdvertiseSdk.destroyBannerAd(adView)
    }

    fun preloadLaunchAds(context: Context) {
        if (AdvertiseSdk.canPreloadOpen(AdvertiseSdk.LOAD_TIME_OPEN_APP)) {
            AdvertiseSdk.preloadOpen(context)
        }
        if (AdvertiseSdk.canPreloadInterstitial(AdvertiseSdk.LOAD_TIME_OPEN_APP)) {
            AdvertiseSdk.preloadInterstitial(context)
        }
        if (AdvertiseSdk.canPreloadNative(AdvertiseSdk.LOAD_TIME_OPEN_APP)) {
            AdvertiseSdk.preloadNative(context)
        }
    }

    fun preloadOpen(context: Context) {
        AdvertiseSdk.preloadOpen(context)
    }

    fun preloadMainPageAds(context: Context) {
        if (AdvertiseSdk.canPreloadInterstitial(AdvertiseSdk.LOAD_TIME_ENTER_FEATURE)) {
            AdvertiseSdk.preloadInterstitial(context)
        }
        if (AdvertiseSdk.canPreloadNative(AdvertiseSdk.LOAD_TIME_ENTER_FEATURE)) {
            AdvertiseSdk.preloadNative(context)
        }
    }

    fun suppressNextAppOpenAd() {
        AdvertiseSdk.suppressNextAppOpenAd = true
    }

    fun showOpenAd(
        activity: Activity,
        areaKey: String,
        onClosed: () -> Unit,
        onLoaded: () -> Unit,
    ) {
        AdvertiseSdk.showOpenAd(
            activity = activity,
            areaKey = areaKey,
            onCloseListener = AdvertiseSdk.OpenAdCloseListener(onClosed),
            onLoadedListener = AdvertiseSdk.OpenAdLoadedListener(onLoaded),
            onPaidListener = AdvertiseSdk.OpenAdPaidListener { _ -> },
        )
    }
}
