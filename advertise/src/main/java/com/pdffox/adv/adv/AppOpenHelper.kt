package com.pdffox.adv.adv

import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.OnPaidEventListener
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.firebase.analytics.FirebaseAnalytics
import com.pdffox.adv.AdvRuntime
import com.pdffox.adv.Config
import com.pdffox.adv.adv.policy.AdPolicyManager
import com.pdffox.adv.log.LogAdData
import com.pdffox.adv.log.LogAdParam
import com.pdffox.adv.log.LogUtil
import com.pdffox.adv.util.PreferenceDelegate

/**
 * App Open 自动展示辅助器。
 *
 * 监听进程生命周期，在 App 从后台回到前台时按限频和策略自动展示开屏广告，并按策略补充预加载。
 */
object AppOpenHelper : DefaultLifecycleObserver {

	private const val TAG = "AppOpenHelper"

	/** 临时抑制开关，通常由广告点击触发，避免回流时再次弹 App Open。 */
	var spSwitch = false

	/** 标记 App 是否经历过后台状态，用于只在后台回前台时触发广告。 */
	private var isAppInBackground = false

	/** 最近一次展示开屏广告的时间，保留给旧逻辑和持久化限频使用。 */
	var showOpenAdvTime: Long by PreferenceDelegate("showOpenAdvTime", 0L)

	/** AdMob 是否已初始化完成。 */
	var hasInitAdmob = false

	/** 自动前后台开屏使用的广告区域 key。 */
	var areaKey = LogAdParam.foregroundKey

	/** 当前是否正在展示 App Open 广告。 */
	var isShowing = false
	@Volatile
	private var isObserving = false
	private var isLoadingAdmobAd = false
	var isShowingAdmobAd = false

	/** App 回到前台时尝试展示 App Open 广告。 */
	override fun onStart(owner: LifecycleOwner) {
		super.onStart(owner)
		if (Config.isTest && spSwitch) {
			Log.e(TAG, "onStart: spSwitch = $spSwitch")
			Toast.makeText(AdvRuntime.application, "onStart: spSwitch = $spSwitch", Toast.LENGTH_SHORT).show()
			spSwitch = false
			return
		}
		if (!Config.isTest && (Config.isGoogleIP || Config.paid_0)) {
			return
		}
		if (!isAppInBackground) {
			return
		}
		isAppInBackground = false
		if (isShowing || ShowInterAd.isShowing) {
			return
		}
		showAdIfReady()
	}

	/** App 进入后台时按策略预加载下一次前台可用的广告。 */
	override fun onStop(owner: LifecycleOwner) {
		super.onStop(owner)
		if (com.pdffox.adv.Config.isTest && spSwitch) {
			Log.e(TAG, "onStop: spSwitch = $spSwitch")
			Toast.makeText(AdvRuntime.application, "onStop: spSwitch = $spSwitch", Toast.LENGTH_SHORT).show()
			return
		}
		if (!com.pdffox.adv.Config.isTest && (Config.isGoogleIP || Config.paid_0)) {
			return
		}
		isAppInBackground = true
		if (AdConfig.canLoadOpen(AdConfig.LOAD_TIME_ENTER_BACKGROUND)) {
			AdLoader.loadOpen(AdvRuntime.application)
		}
		if (AdConfig.canLoadInter(AdConfig.LOAD_TIME_ENTER_BACKGROUND)) {
			AdLoader.loadInter(AdvRuntime.application)
		}
	}

	/** 注册进程生命周期监听；重复调用不会重复注册。 */
	fun startObserve() {
		if (Looper.myLooper() == Looper.getMainLooper()) {
			registerProcessLifecycleObserver()
		} else {
			Handler(Looper.getMainLooper()).post { registerProcessLifecycleObserver() }
		}
	}

	private fun registerProcessLifecycleObserver() {
		if (isObserving) {
			return
		}
		isObserving = true
		ProcessLifecycleOwner.get().lifecycle.addObserver(this)
	}

	/** 按限频、广告策略和缓存状态展示 App Open 广告。 */
	fun showAdIfReady() {
		if (!AdPolicyManager.checkAdUnit(areaKey)) {
			return
		}
		if (Config.activeAdPlatform() != LogAdParam.ad_platform_admob) {
			return
		}
		// 优先展示预加载广告，缓存为空时再实时加载。
		val admobOpenAd = AdPool.getAdmobOpen(areaKey, {
			isShowing = false
		}) {
			isShowing = true
			if (AdConfig.canLoadOpen(AdConfig.LOAD_TIME_PLAY_FINISH)) {
				AdLoader.loadOpen(AdvRuntime.application)
			}
		}
		if (admobOpenAd != null) {
			AdvRuntime.currentActivity?.let {
				admobOpenAd.show(it)
			}
		} else {
			showAdmob()
		}
	}

	/** 实时加载并展示 AdMob App Open 广告。 */
	private fun showAdmob() {
		if (isLoadingAdmobAd || Config.activeAdPlatform() != LogAdParam.ad_platform_admob) {
			return
		}
		isLoadingAdmobAd = true
		LogUtil.log(
			LogAdData.ad_start_loading,
			mapOf(
				LogAdParam.ad_platform to LogAdParam.ad_platform_admob,
				LogAdParam.ad_areakey to areaKey,
				LogAdParam.ad_format to LogAdParam.ad_format_open,
				LogAdParam.ad_unit_name to AdvIDs.getAdmobOpenId(),
				LogAdParam.ad_preload to false,
			)
		)
		LogUtil.log(
			LogAdData.ad_occur,
			mapOf(
				LogAdParam.ad_platform to LogAdParam.ad_platform_admob,
				LogAdParam.ad_areakey to areaKey,
				LogAdParam.ad_format to LogAdParam.ad_format_open,
				LogAdParam.ad_unit_name to AdvIDs.getAdmobOpenId(),
				LogAdParam.ad_preload to false,
			)
		)
		val startLoadingTime = System.currentTimeMillis()
		AppOpenAd.load(
			AdvRuntime.application,
			AdvIDs.getAdmobOpenId(),
			AdRequest.Builder().build(),
			object : AppOpenAd.AppOpenAdLoadCallback() {
				override fun onAdLoaded(ad: AppOpenAd) {
					isLoadingAdmobAd = false
					LogUtil.log(
						LogAdData.ad_finish_loading,
						mapOf(
							LogAdParam.ad_platform to LogAdParam.ad_platform_admob,
							LogAdParam.duration to (System.currentTimeMillis() - startLoadingTime),
							LogAdParam.ad_areakey to areaKey,
							LogAdParam.ad_format to LogAdParam.ad_format_open,
							LogAdParam.ad_source to (ad.responseInfo.loadedAdapterResponseInfo?.adSourceName ?: LogAdParam.unknow),
							LogAdParam.ad_unit_name to AdvIDs.getAdmobOpenId(),
							LogAdParam.ad_preload to false,
						)
					)
					ad.fullScreenContentCallback = object : FullScreenContentCallback() {
						override fun onAdDismissedFullScreenContent() {
							LogUtil.log(
								LogAdData.ad_close,
								mapOf(
									LogAdParam.ad_platform to LogAdParam.ad_platform_admob,
									LogAdParam.duration to (System.currentTimeMillis() - startLoadingTime),
									LogAdParam.ad_areakey to areaKey,
									LogAdParam.ad_format to LogAdParam.ad_format_open,
									LogAdParam.ad_source to (ad.responseInfo.loadedAdapterResponseInfo?.adSourceName ?: LogAdParam.unknow),
									LogAdParam.ad_unit_name to AdvIDs.getAdmobOpenId(),
									LogAdParam.ad_preload to false,
								)
							)
							isShowingAdmobAd = false
							isShowing = false
						}

						override fun onAdFailedToShowFullScreenContent(adError: AdError) {
							Log.e(TAG, adError.message)
							isShowingAdmobAd = false
							isShowing = false
						}

						override fun onAdShowedFullScreenContent() {
							isShowing = true
							isShowingAdmobAd = true
						}

						override fun onAdImpression() {
							if (AdConfig.canLoadOpen(AdConfig.LOAD_TIME_PLAY_FINISH)) {
								AdLoader.loadOpen(AdvRuntime.application)
							}
						}

						override fun onAdClicked() {
							LogUtil.log(
								LogAdData.ad_click,
								mapOf(
									LogAdParam.ad_platform to LogAdParam.ad_platform_admob,
									LogAdParam.duration to (System.currentTimeMillis() - startLoadingTime),
									LogAdParam.ad_areakey to areaKey,
									LogAdParam.ad_format to LogAdParam.ad_format_open,
									LogAdParam.ad_source to (ad.responseInfo.loadedAdapterResponseInfo?.adSourceName ?: LogAdParam.unknow),
									LogAdParam.ad_unit_name to AdvIDs.getAdmobOpenId(),
									LogAdParam.ad_preload to false,
								)
							)
							// 点击广告后用户可能跳出 App，回流时先抑制自动开屏。
							spSwitch = true
						}
					}
					ad.onPaidEventListener = OnPaidEventListener { adValue ->
						val revenue = adValue.valueMicros / 1_000_000.0
						LogUtil.logSingularAdRevenue(LogAdParam.OpenAd, LogAdParam.adMob, revenue)
						logOpenRevenue(LogAdData.ad_impression, ad, adValue.currencyCode, revenue)
						logOpenRevenue(LogAdData.ad_revenue, ad, adValue.currencyCode, revenue)
						LogUtil.logTaiChiAdmob(adValue)
					}
					AdvRuntime.currentActivity?.let {
						ad.show(it)
					} ?: run {
						isShowing = false
					}
				}

				override fun onAdFailedToLoad(loadAdError: LoadAdError) {
					isLoadingAdmobAd = false
					isShowingAdmobAd = false
					isShowing = false
					Log.e(TAG, "onAdFailedToLoad: ${loadAdError.message}")
				}
			},
		)
	}

	/** 统一上报 App Open 收入类事件。 */
	private fun logOpenRevenue(eventName: String, ad: AppOpenAd, currency: String, revenue: Double) {
		LogUtil.log(
			eventName,
			mapOf(
				LogAdParam.ad_areakey to areaKey,
				FirebaseAnalytics.Param.AD_PLATFORM to LogAdParam.ad_platform_admob,
				FirebaseAnalytics.Param.AD_UNIT_NAME to AdvIDs.getAdmobOpenId(),
				FirebaseAnalytics.Param.AD_FORMAT to LogAdParam.ad_format_open,
				FirebaseAnalytics.Param.AD_SOURCE to (ad.responseInfo.loadedAdapterResponseInfo?.adSourceName ?: LogAdParam.unknow),
				FirebaseAnalytics.Param.CURRENCY to currency,
				FirebaseAnalytics.Param.VALUE to revenue,
				LogAdParam.ad_preload to false,
			)
		)
	}
}
