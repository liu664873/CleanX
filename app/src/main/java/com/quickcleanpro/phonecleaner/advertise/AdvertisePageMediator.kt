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
        if (activity == null || activity.isUnavailable()) {
            onContinue()
            return
        }
        runCatching {
            AdvertiseSdk.showSplashConsent(activity) {
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
        if (activity == null || activity.isUnavailable()) {
            onComplete(false)
            return
        }
        runCatching {
            val hasCachedConsentState = AdvertiseSdk.initConsent(activity) { success ->
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
        if (activity == null || activity.isUnavailable()) {
            onComplete()
            return
        }
        runCatching {
            AdvertiseSdk.showSplashConsent(activity) {
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
            AdvertiseSdk.showInterstitialAd(activity, areaKey) {
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
    ): ViewGroup? =
        runCatching {
            AdvertiseSdk.getBannerAd(context, areaKey)
        }.onFailure { throwable ->
            Log.w(TAG, "getBannerAd failed", throwable)
        }.getOrNull()

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
            (container.getChildAt(index) as? ViewGroup)?.let(AdvertiseSdk::destroyBannerAd)
        }
        container.removeAllViews()
        container.visibility = View.GONE
    }

    fun preloadLaunchAds(context: Context) {
        runCatching {
            if (AdvertiseSdk.canPreloadOpen(AdvertiseSdk.LOAD_TIME_OPEN_APP)) {
                AdvertiseSdk.preloadOpen(context)
            }
            if (AdvertiseSdk.canPreloadInterstitial(AdvertiseSdk.LOAD_TIME_OPEN_APP)) {
                AdvertiseSdk.preloadInterstitial(context)
            }
            if (AdvertiseSdk.canPreloadNative(AdvertiseSdk.LOAD_TIME_OPEN_APP)) {
                AdvertiseSdk.preloadNative(context)
            }
        }.onFailure { throwable ->
            Log.w(TAG, "preloadLaunchAds failed", throwable)
        }
    }

    fun preloadLaunchOpenAd(context: Context) {
        runCatching {
            AdvertiseSdk.preloadOpen(context)
        }.onFailure { throwable ->
            Log.w(TAG, "preloadLaunchOpenAd failed", throwable)
        }
    }

    fun hasCachedLaunchOpenAd(): Boolean =
        runCatching {
            AdvertiseSdk.hasCachedOpenAd()
        }.getOrElse { throwable ->
            Log.w(TAG, "hasCachedLaunchOpenAd failed", throwable)
            false
        }

    fun showCachedLaunchOpenAd(
        activity: Activity?,
        onContinue: () -> Unit,
    ): Boolean {
        if (activity == null || activity.isUnavailable()) {
            onContinue()
            return false
        }
        var callbackHandled = false
        fun continueOnce() {
            if (callbackHandled) return
            callbackHandled = true
            if (!activity.isUnavailable()) {
                onContinue()
            }
        }
        return runCatching {
            AdvertiseSdk.showCachedOpenAd(
                activity = activity,
                areaKey = AdAreaKeys.Open.OPEN_PAGE,
                onCloseListener = AdvertiseSdk.OpenAdCloseListener {
                    continueOnce()
                },
                onLoadedListener = AdvertiseSdk.OpenAdLoadedListener { },
                onPaidListener = AdvertiseSdk.OpenAdPaidListener { _ -> },
            )
        }.onFailure { throwable ->
            Log.w(TAG, "showCachedLaunchOpenAd failed", throwable)
            continueOnce()
        }.getOrDefault(false)
    }

    fun preloadMainPageAds(context: Context) {
        runCatching {
            if (AdvertiseSdk.canPreloadInterstitial(AdvertiseSdk.LOAD_TIME_ENTER_FEATURE)) {
                AdvertiseSdk.preloadInterstitial(context)
            }
            if (AdvertiseSdk.canPreloadNative(AdvertiseSdk.LOAD_TIME_ENTER_FEATURE)) {
                AdvertiseSdk.preloadNative(context)
            }
        }.onFailure { throwable ->
            Log.w(TAG, "preloadMainPageAds failed", throwable)
        }
    }

    override fun suppressNextAppOpenAd() {
        runCatching {
            AdvertiseSdk.suppressNextAppOpenAd = true
        }.onFailure { throwable ->
            Log.w(TAG, "suppressNextAppOpenAd failed", throwable)
        }
    }

    private fun showLaunchOpenAd(
        activity: Activity,
        onContinue: () -> Unit,
    ) {
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
            AdvertiseSdk.showOpenAd(
                activity = activity,
                areaKey = AdAreaKeys.Open.OPEN_PAGE,
                onCloseListener = AdvertiseSdk.OpenAdCloseListener {
                    continueOnce()
                },
                onLoadedListener = AdvertiseSdk.OpenAdLoadedListener {
                    openAdLoaded = true
                    handler.removeCallbacksAndMessages(null)
                },
                onPaidListener = AdvertiseSdk.OpenAdPaidListener { _ -> },
            )
        }.onFailure { throwable ->
            Log.w(TAG, "showOpenAd failed; continuing flow", throwable)
            continueOnce()
        }
    }

    private fun Activity.isUnavailable(): Boolean = isFinishing || isDestroyed
}
