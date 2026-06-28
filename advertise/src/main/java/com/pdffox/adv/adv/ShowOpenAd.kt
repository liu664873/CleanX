package com.pdffox.adv.adv

import android.app.Activity
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.OnPaidEventListener
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.firebase.analytics.FirebaseAnalytics
import com.pdffox.adv.Config
import com.pdffox.adv.adv.policy.AdPolicyManager
import com.pdffox.adv.log.LogAdData
import com.pdffox.adv.log.LogAdParam
import com.pdffox.adv.log.LogUtil

/**
 * 主动展示 App Open 广告的入口。
 *
 * 和 [AppOpenHelper] 的自动前后台展示不同，这里由宿主主动调用，并支持加载、关闭和收入回调。
 */
object ShowOpenAd {

	/** App Open 广告关闭回调。 */
	fun interface OpenAdCloseListener {
		fun onClose()
	}

	/** App Open 广告加载成功回调。 */
	fun interface OpenAdLoadedListener {
		fun onLoaded()
	}

	/** App Open 广告产生收入回调，value 为 AdMob 微单位收入。 */
	fun interface OpenAdPaidListener {
		fun onPaid(value: Long)
	}

	private const val TAG = "ShowOpenAd"

	/** 根据限频、广告策略和缓存状态展示 App Open 广告。 */
	fun showOpenAd(
		activity: Activity,
		areaKey: String,
		onCloseListener: OpenAdCloseListener?,
		onLoadedListener: OpenAdLoadedListener?,
		onPaidListener: OpenAdPaidListener?,
	) {
		if (activity.isUnavailable()) {
			// 宿主页面已经结束时不能再展示广告，直接走关闭回调让上层兜底处理。
			onCloseListener?.onClose()
			return
		}
		if (!AdPolicyManager.checkAdUnit(areaKey)) {
			// openPageAdv 未通过广告策略、频控或概率校验时，跳过广告并继续启动流程。
			onCloseListener?.onClose()
			return
		}
		if (ShowInterAd.isShowing) {
			// 避免 App Open 和插屏全屏广告同时展示，已有全屏广告时直接放行。
			onCloseListener?.onClose()
			return
		}
		if (Config.activeAdPlatform() != LogAdParam.ad_platform_admob) {
			// 当前只实现 AdMob App Open；平台不是 AdMob 时按无广告处理。
			onCloseListener?.onClose()
			return
		}
		// 优先消费预加载缓存，缓存不可用时再实时请求。
		val admobOpenAd = AdPool.getAdmobOpen(areaKey, {
			onCloseListener?.onClose()
		}) {
			if (AdConfig.canLoadOpen(AdConfig.LOAD_TIME_PLAY_FINISH)) {
				AdLoader.loadOpen(activity)
			}
		}
		if (admobOpenAd != null) {
			// 命中预加载缓存时，直接展示缓存广告，减少启动页等待。
			showAdmobOpenAdFromCache(admobOpenAd, activity, areaKey, onCloseListener, onLoadedListener, onPaidListener)
		} else {
			// 没有缓存时实时请求广告；加载成功立即展示，加载失败则回调关闭监听。
			showAdmobOpenAd(activity, areaKey, onCloseListener, onLoadedListener, onPaidListener)
		}
	}

	/** 展示预加载池中的 App Open 广告，并重新绑定回调到当前 areaKey。 */
	fun hasCachedOpenAd(): Boolean = AdPool.hasAvailableAdmobOpen()

	fun showCachedOpenAd(
		activity: Activity,
		areaKey: String,
		onCloseListener: OpenAdCloseListener?,
		onLoadedListener: OpenAdLoadedListener?,
		onPaidListener: OpenAdPaidListener?,
	): Boolean {
		if (activity.isUnavailable()) {
			onCloseListener?.onClose()
			return false
		}
		if (!AdPolicyManager.checkAdUnit(areaKey)) {
			return false
		}
		if (ShowInterAd.isShowing) {
			return false
		}
		if (Config.activeAdPlatform() != LogAdParam.ad_platform_admob) {
			return false
		}
		val admobOpenAd = AdPool.getAdmobOpen(areaKey, {
			onCloseListener?.onClose()
		}) {
			if (AdConfig.canLoadOpen(AdConfig.LOAD_TIME_PLAY_FINISH)) {
				AdLoader.loadOpen(activity)
			}
		} ?: return false
		showAdmobOpenAdFromCache(admobOpenAd, activity, areaKey, onCloseListener, onLoadedListener, onPaidListener)
		return true
	}

	fun showAdmobOpenAdFromCache(
		admobOpenAd: AppOpenAd,
		activity: Activity,
		areaKey: String,
		onCloseListener: OpenAdCloseListener?,
		onLoadedListener: OpenAdLoadedListener?,
		onPaidListener: OpenAdPaidListener?,
	) {
		if (activity.isUnavailable()) {
			onCloseListener?.onClose()
			return
		}
		// 告诉宿主广告已准备展示；启动页目前不使用该状态，但保留完整生命周期。
		onLoadedListener?.onLoaded()
		LogUtil.log(
			LogAdData.ad_occur,
			mapOf(
				LogAdParam.ad_platform to LogAdParam.ad_platform_admob,
				LogAdParam.ad_areakey to areaKey,
				LogAdParam.ad_format to LogAdParam.ad_format_open,
				LogAdParam.ad_unit_name to AdvIDs.getAdmobOpenId(),
				LogAdParam.ad_preload to true,
			)
		)
		val startShowAd = System.currentTimeMillis()
		admobOpenAd.fullScreenContentCallback = object : FullScreenContentCallback() {
			override fun onAdDismissedFullScreenContent() {
				// 用户关闭缓存广告后，上报关闭事件并通知启动页进入后续导航。
				LogUtil.log(
					LogAdData.ad_close,
					mapOf(
						LogAdParam.ad_platform to LogAdParam.ad_platform_admob,
						LogAdParam.duration to (System.currentTimeMillis() - startShowAd),
						LogAdParam.ad_areakey to areaKey,
						LogAdParam.ad_format to LogAdParam.ad_format_open,
						LogAdParam.ad_source to (admobOpenAd.responseInfo.loadedAdapterResponseInfo?.adSourceName ?: LogAdParam.unknow),
						LogAdParam.ad_unit_name to AdvIDs.getAdmobOpenId(),
						LogAdParam.ad_preload to true,
					)
				)
				onCloseListener?.onClose()
			}

			override fun onAdFailedToShowFullScreenContent(adError: AdError) {
				Log.e(TAG, adError.message)
				// 缓存广告展示失败时也必须回调，避免启动页一直等待。
				onCloseListener?.onClose()
			}

			override fun onAdClicked() {
				LogUtil.log(
					LogAdData.ad_click,
					mapOf(
						LogAdParam.ad_platform to LogAdParam.ad_platform_admob,
						LogAdParam.duration to (System.currentTimeMillis() - startShowAd),
						LogAdParam.ad_areakey to areaKey,
						LogAdParam.ad_format to LogAdParam.ad_format_open,
						LogAdParam.ad_source to (admobOpenAd.responseInfo.loadedAdapterResponseInfo?.adSourceName ?: LogAdParam.unknow),
						LogAdParam.ad_unit_name to AdvIDs.getAdmobOpenId(),
						LogAdParam.ad_preload to true,
					)
				)
				// 点击广告后用户可能跳转出 App，回流时抑制自动 App Open。
				AppOpenHelper.spSwitch = true
			}
		}
		admobOpenAd.onPaidEventListener = paidListener(
			ad = admobOpenAd,
			areaKey = areaKey,
			preload = true,
			onPaidListener = onPaidListener
		)
		if (activity.isUnavailable()) {
			onCloseListener?.onClose()
			return
		}
		// 真正展示缓存中的首个开屏广告，用户关闭后会触发上面的 onAdDismissedFullScreenContent。
		admobOpenAd.show(activity)
	}

	/** 实时加载并展示 AdMob App Open 广告。 */
	fun showAdmobOpenAd(
		context: Activity,
		areaKey: String,
		onCloseListener: OpenAdCloseListener?,
		onLoadedListener: OpenAdLoadedListener?,
		onPaidListener: OpenAdPaidListener?,
	) {
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
		val startLoadingTime = System.currentTimeMillis()
		// 缓存未命中时现场请求 AdMob App Open 广告；回调结果决定是否继续展示或直接放行。
		AppOpenAd.load(
			context,
			AdvIDs.getAdmobOpenId(),
			AdRequest.Builder().build(),
			object : AppOpenAd.AppOpenAdLoadCallback() {
				override fun onAdLoaded(admobAppOpenAd: AppOpenAd) {
					if (context.isUnavailable()) {
						onCloseListener?.onClose()
						return
					}
					// 实时加载成功后立即通知宿主并准备展示，不额外写入缓存池。
					onLoadedListener?.onLoaded()
					LogUtil.log(
						LogAdData.ad_finish_loading,
						mapOf(
							LogAdParam.ad_platform to LogAdParam.ad_platform_admob,
							LogAdParam.duration to (System.currentTimeMillis() - startLoadingTime),
							LogAdParam.ad_areakey to areaKey,
							LogAdParam.ad_format to LogAdParam.ad_format_open,
							LogAdParam.ad_source to (admobAppOpenAd.responseInfo.loadedAdapterResponseInfo?.adSourceName ?: LogAdParam.unknow),
							LogAdParam.ad_unit_name to AdvIDs.getAdmobOpenId(),
							LogAdParam.ad_preload to false,
						)
					)
					admobAppOpenAd.fullScreenContentCallback = object : FullScreenContentCallback() {
						override fun onAdDismissedFullScreenContent() {
							// 用户关闭实时加载的开屏广告后，启动页继续执行导航逻辑。
							LogUtil.log(
								LogAdData.ad_close,
								mapOf(
									LogAdParam.ad_platform to LogAdParam.ad_platform_admob,
									LogAdParam.duration to (System.currentTimeMillis() - startLoadingTime),
									LogAdParam.ad_areakey to areaKey,
									LogAdParam.ad_format to LogAdParam.ad_format_open,
									LogAdParam.ad_source to (admobAppOpenAd.responseInfo.loadedAdapterResponseInfo?.adSourceName ?: LogAdParam.unknow),
									LogAdParam.ad_unit_name to AdvIDs.getAdmobOpenId(),
									LogAdParam.ad_preload to false,
								)
							)
							onCloseListener?.onClose()
						}

						override fun onAdFailedToShowFullScreenContent(adError: AdError) {
							Log.e(TAG, adError.message)
							// 加载成功但展示失败时仍然回调关闭，保持启动链路可继续。
							onCloseListener?.onClose()
						}

						override fun onAdClicked() {
							LogUtil.log(
								LogAdData.ad_click,
								mapOf(
									LogAdParam.ad_platform to LogAdParam.ad_platform_admob,
									LogAdParam.duration to (System.currentTimeMillis() - startLoadingTime),
									LogAdParam.ad_areakey to areaKey,
									LogAdParam.ad_format to LogAdParam.ad_format_open,
									LogAdParam.ad_source to (admobAppOpenAd.responseInfo.loadedAdapterResponseInfo?.adSourceName ?: LogAdParam.unknow),
									LogAdParam.ad_unit_name to AdvIDs.getAdmobOpenId(),
									LogAdParam.ad_preload to false,
								)
							)
							// 点击广告后用户可能跳转出 App，回流时抑制自动 App Open。
							AppOpenHelper.spSwitch = true
						}
					}
					admobAppOpenAd.onPaidEventListener = paidListener(
						ad = admobAppOpenAd,
						areaKey = areaKey,
						preload = false,
						onPaidListener = onPaidListener
					)
					if (context.isUnavailable()) {
						onCloseListener?.onClose()
						return
					}
					// 实时加载成功后立刻展示首个开屏广告。
					admobAppOpenAd.show(context)
				}

				override fun onAdFailedToLoad(loadAdError: LoadAdError) {
					Log.e(TAG, "showAdmobOpenAd failed: ${loadAdError.message}")
					// 实时加载失败时不再阻塞启动页，直接通知上层进入引导页/首页。
					onCloseListener?.onClose()
				}
			},
		)
	}

	/** 构造 App Open 收入监听，并同时上报 Singular、Firebase/ThinkingData 和外部回调。 */
	private fun paidListener(
		ad: AppOpenAd,
		areaKey: String,
		preload: Boolean,
		onPaidListener: OpenAdPaidListener?,
	): OnPaidEventListener = OnPaidEventListener { adValue ->
		val revenue = adValue.valueMicros / 1_000_000.0
		LogUtil.logSingularAdRevenue(LogAdParam.OpenAd, LogAdParam.adMob, revenue)
		logOpenRevenue(LogAdData.ad_impression, areaKey, ad, adValue.currencyCode, revenue, preload)
		logOpenRevenue(LogAdData.ad_revenue, areaKey, ad, adValue.currencyCode, revenue, preload)
		LogUtil.logTaiChiAdmob(adValue)
		onPaidListener?.onPaid(adValue.valueMicros)
	}

	/** 统一上报 App Open 收入类事件，字段兼容 Firebase Analytics 标准参数。 */
	private fun logOpenRevenue(
		eventName: String,
		areaKey: String,
		ad: AppOpenAd,
		currency: String,
		revenue: Double,
		preload: Boolean,
	) {
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
				LogAdParam.ad_preload to preload,
			)
		)
	}

	/** 判断宿主 Activity 是否已经不适合继续发起或展示广告。 */
	private fun Activity.isUnavailable(): Boolean = isFinishing || isDestroyed
}
