package com.pdffox.adv.adv

import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.OnPaidEventListener
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.firebase.analytics.FirebaseAnalytics
import com.pdffox.adv.NativeAdContent
import com.pdffox.adv.log.LogAdData
import com.pdffox.adv.log.LogAdParam
import com.pdffox.adv.log.LogUtil
import java.util.ArrayDeque

object AdPool {
	private const val TAG = "AdPool"

	val admobInterPool = mutableMapOf<InterstitialAd, Long>()
	val admobOpenPool = mutableMapOf<AppOpenAd, Long>()
	val admobNativePool = ArrayDeque<NativeAdContent>()

	var admobInterIsLoadingNum = 0
	var admobOpenIsLoadingNum = 0
	var admobNativeIsLoadingNum = 0

	fun putAdmobOpen(ad: AppOpenAd) {
		admobOpenPool[ad] = System.currentTimeMillis()
	}

	fun hasAvailableAdmobOpen(): Boolean {
		AdChecker.checkExpiredAdmobOpen()
		return admobOpenPool.entries.any { (_, time) ->
			System.currentTimeMillis() - time < AdConfig.adload_cache_time
		}
	}

	fun getAdmobOpen(
		areaKey: String,
		onClosed: () -> Unit,
		onDisplayed: () -> Unit,
	): AppOpenAd? {
		AdChecker.checkExpiredAdmobOpen()
		val admobAppOpenAd = admobOpenPool.entries.firstOrNull { (_, time) ->
			System.currentTimeMillis() - time < AdConfig.adload_cache_time
		}?.key

		LogUtil.log(
			LogAdData.ad_occur,
			mapOf(
				LogAdParam.ad_platform to LogAdParam.ad_platform_admob,
				LogAdParam.ad_areakey to areaKey,
				LogAdParam.ad_format to LogAdParam.ad_format_open,
				LogAdParam.ad_unit_name to AdvIDs.getAdmobOpenId(),
				LogAdParam.ad_preload to true,
			),
		)

		val occurTime = System.currentTimeMillis()
		admobAppOpenAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
			override fun onAdDismissedFullScreenContent() {
				LogUtil.log(
					LogAdData.ad_close,
					mapOf(
						LogAdParam.ad_platform to LogAdParam.ad_platform_admob,
						LogAdParam.duration to System.currentTimeMillis() - occurTime,
						LogAdParam.ad_areakey to areaKey,
						LogAdParam.ad_format to LogAdParam.ad_format_open,
						LogAdParam.ad_source to adSourceName(admobAppOpenAd),
						LogAdParam.ad_unit_name to AdvIDs.getAdmobOpenId(),
						LogAdParam.ad_preload to true,
					),
				)
				onClosed()
			}

			override fun onAdFailedToShowFullScreenContent(adError: AdError) {
				Log.e(TAG, adError.message)
				onClosed()
			}

			override fun onAdImpression() {
				onDisplayed()
			}

			override fun onAdClicked() {
				LogUtil.log(
					LogAdData.ad_click,
					mapOf(
						LogAdParam.ad_platform to LogAdParam.ad_platform_admob,
						LogAdParam.duration to 0L,
						LogAdParam.ad_areakey to areaKey,
						LogAdParam.ad_format to LogAdParam.ad_format_open,
						LogAdParam.ad_source to adSourceName(admobAppOpenAd),
						LogAdParam.ad_unit_name to AdvIDs.getAdmobOpenId(),
						LogAdParam.ad_preload to true,
					),
				)
				AppOpenHelper.spSwitch = true
			}
		}

		admobAppOpenAd?.onPaidEventListener = OnPaidEventListener { adValue ->
			val revenue = adValue.valueMicros / 1_000_000.0
			LogUtil.logSingularAdRevenue(LogAdParam.OpenAd, LogAdParam.adMob, revenue)
			logAdmobRevenue(
				eventName = LogAdData.ad_impression,
				areaKey = areaKey,
				adUnitId = AdvIDs.getAdmobOpenId(),
				adFormat = LogAdParam.ad_format_open,
				adSource = adSourceName(admobAppOpenAd),
				currency = adValue.currencyCode,
				revenue = revenue,
				preload = true,
			)
			logAdmobRevenue(
				eventName = LogAdData.ad_revenue,
				areaKey = areaKey,
				adUnitId = AdvIDs.getAdmobOpenId(),
				adFormat = LogAdParam.ad_format_open,
				adSource = adSourceName(admobAppOpenAd),
				currency = adValue.currencyCode,
				revenue = revenue,
				preload = true,
			)
			LogUtil.logTaiChiAdmob(adValue)
		}

		admobAppOpenAd?.let { admobOpenPool.remove(it) }
		return admobAppOpenAd
	}

	fun putAdmobInter(ad: InterstitialAd) {
		admobInterPool[ad] = System.currentTimeMillis()
	}

	fun getAdmobInter(
		areaKey: String,
		onClosed: () -> Unit,
		onDisplayed: () -> Unit,
	): InterstitialAd? {
		AdChecker.checkExpiredAdmobInter()
		val admobInter = admobInterPool.entries.firstOrNull { (_, time) ->
			System.currentTimeMillis() - time < AdConfig.adload_cache_time
		}?.key

		LogUtil.log(
			LogAdData.ad_occur,
			mapOf(
				LogAdParam.ad_platform to LogAdParam.ad_platform_admob,
				LogAdParam.ad_areakey to areaKey,
				LogAdParam.ad_format to LogAdParam.ad_format_interstitial,
				LogAdParam.ad_unit_name to AdvIDs.getAdmobInterstitialId(),
				LogAdParam.ad_preload to true,
			),
		)

		val occurTime = System.currentTimeMillis()
		admobInter?.fullScreenContentCallback = object : FullScreenContentCallback() {
			override fun onAdClicked() {
				LogUtil.log(
					LogAdData.ad_click,
					mapOf(
						LogAdParam.ad_platform to LogAdParam.ad_platform_admob,
						LogAdParam.duration to System.currentTimeMillis() - occurTime,
						LogAdParam.ad_areakey to areaKey,
						LogAdParam.ad_format to LogAdParam.ad_format_interstitial,
						LogAdParam.ad_source to adSourceName(admobInter),
						LogAdParam.ad_unit_name to AdvIDs.getAdmobInterstitialId(),
						LogAdParam.ad_preload to true,
					),
				)
				AppOpenHelper.spSwitch = true
			}

			override fun onAdDismissedFullScreenContent() {
				LogUtil.log(
					LogAdData.ad_close,
					mapOf(
						LogAdParam.ad_platform to LogAdParam.ad_platform_admob,
						LogAdParam.duration to 0L,
						LogAdParam.ad_areakey to areaKey,
						LogAdParam.ad_format to LogAdParam.ad_format_interstitial,
						LogAdParam.ad_source to adSourceName(admobInter),
						LogAdParam.ad_unit_name to AdvIDs.getAdmobInterstitialId(),
						LogAdParam.ad_preload to true,
					),
				)
				onClosed()
			}

			override fun onAdFailedToShowFullScreenContent(adError: AdError) {
				LogUtil.log(
					LogAdData.ad_show_fail,
					mapOf(
						"msg" to adError.message,
						LogAdParam.ad_platform to LogAdParam.ad_platform_admob,
						LogAdParam.duration to 0L,
						LogAdParam.ad_areakey to areaKey,
						LogAdParam.ad_format to LogAdParam.ad_format_interstitial,
						LogAdParam.ad_source to adSourceName(admobInter),
						LogAdParam.ad_unit_name to AdvIDs.getAdmobInterstitialId(),
						LogAdParam.ad_preload to true,
					),
				)
				onClosed()
			}

			override fun onAdImpression() {
				onDisplayed()
			}
		}

		admobInter?.onPaidEventListener = OnPaidEventListener { adValue ->
			val revenue = adValue.valueMicros.toDouble() / 1_000_000.0
			LogUtil.logSingularAdRevenue(LogAdParam.InterAd, LogAdParam.adMob, revenue)
			logAdmobRevenue(
				eventName = LogAdData.ad_impression,
				areaKey = areaKey,
				adUnitId = AdvIDs.getAdmobInterstitialId(),
				adFormat = LogAdParam.ad_format_interstitial,
				adSource = adSourceName(admobInter),
				currency = adValue.currencyCode,
				revenue = revenue,
				preload = true,
			)
			logAdmobRevenue(
				eventName = LogAdData.ad_revenue,
				areaKey = areaKey,
				adUnitId = AdvIDs.getAdmobInterstitialId(),
				adFormat = LogAdParam.ad_format_interstitial,
				adSource = adSourceName(admobInter),
				currency = adValue.currencyCode,
				revenue = revenue,
				preload = true,
			)
			LogUtil.logTaiChiAdmob(adValue)
		}

		admobInter?.let { admobInterPool.remove(it) }
		return admobInter
	}

	fun putAdmobNative(content: NativeAdContent) {
		admobNativePool.offer(content)
		Log.e(TAG, "NativeAdContent queued, pool size: ${admobNativePool.size}")
	}

	fun getAdmobNative(): NativeAdContent? {
		val content = admobNativePool.poll()
		if (content == null) {
			Log.e(TAG, "getAdmobNative: no cached native ad")
		}
		return content
	}

	private fun adSourceName(ad: AppOpenAd): String {
		return ad.responseInfo.loadedAdapterResponseInfo?.adSourceName ?: LogAdParam.unknow
	}

	private fun adSourceName(ad: InterstitialAd): String {
		return ad.responseInfo.loadedAdapterResponseInfo?.adSourceName ?: LogAdParam.unknow
	}

	private fun logAdmobRevenue(
		eventName: String,
		areaKey: String,
		adUnitId: String,
		adFormat: String,
		adSource: String,
		currency: String,
		revenue: Double,
		preload: Boolean,
	) {
		LogUtil.log(
			eventName,
			mapOf(
				LogAdParam.ad_areakey to areaKey,
				FirebaseAnalytics.Param.AD_PLATFORM to LogAdParam.ad_platform_admob,
				FirebaseAnalytics.Param.AD_UNIT_NAME to adUnitId,
				FirebaseAnalytics.Param.AD_FORMAT to adFormat,
				FirebaseAnalytics.Param.AD_SOURCE to adSource,
				FirebaseAnalytics.Param.CURRENCY to currency,
				FirebaseAnalytics.Param.VALUE to revenue,
				LogAdParam.ad_preload to preload,
			),
		)
	}
}
