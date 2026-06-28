package com.pdffox.adv.adv

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.OnPaidEventListener
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.firebase.analytics.FirebaseAnalytics
import com.pdffox.adv.Ads
import com.pdffox.adv.Config
import com.pdffox.adv.R
import com.pdffox.adv.adv.policy.AdPolicyManager
import com.pdffox.adv.log.LogAdData
import com.pdffox.adv.log.LogAdParam
import com.pdffox.adv.log.LogUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * 插屏广告直接展示器。
 *
 * 与 PhotoRecovery 的 ShowInterAd 流程保持一致：不再通过独立承载 Activity 展示插屏；
 * 同一时间只允许一个插屏流程运行；优先播放预加载缓存，缓存不可用时再实时请求 AdMob。
 */
@SuppressLint("StaticFieldLeak")
object ShowInterAd {
	/** 日志标签，用于区分插屏展示流程日志。 */
	private const val TAG = "ShowInterAd"

	/** 实时加载 AdMob 插屏失败后的最大重试次数。 */
	private const val MAX_RETRY_COUNT = 3

	/** 单次插屏展示流程的最长等待时间，避免广告加载卡住宿主业务流程。 */
	private const val TIMEOUT_MILLIS = 6000L

	/** 当前是否已有插屏流程正在进行，用于防止重复打开多个插屏。 */
	@Volatile
	var isShowing = false
		private set

	/** 当前插屏展示使用的广告区域 Key，供埋点和缓存池回调使用。 */
	var areaKey: String = ""
		private set

	/** 主线程 Handler，用于调度超时和重试回调。 */
	private val mainHandler = Handler(Looper.getMainLooper())

	/** 宿主传入的关闭回调，广告关闭、失败或超时时都会通过它继续业务流程。 */
	private var onClosed: () -> Unit = {}

	/** 关闭回调是否已经分发，防止加载失败、展示失败和销毁流程重复回调。 */
	private var callbackDelivered = false

	/** 当前实时 AdMob 插屏加载已经重试的次数。 */
	private var admobRetryCount = 0

	/** 插屏流程超时任务，广告展示成功或流程结束时需要取消。 */
	private var timeoutRunnable: Runnable? = null

	/** 实时加载插屏时覆盖在宿主 Activity 上的加载视图。 */
	private var progressView: View? = null

	/**
	 * 展示插屏广告的统一入口。
	 *
	 * 会先做并发保护和宿主 Activity 状态检查，再异步校验广告策略；
	 * 策略允许后进入缓存优先、实时加载兜底的展示流程。
	 */
	fun showIntAd(activity: Activity, areaKey: String, onClosed: () -> Unit) {
		// 已有插屏流程时直接回调宿主，避免重复展示或重复跳转。
		if (isShowing) {
			if (com.pdffox.adv.Config.isTest) {
				Log.e(TAG, "showIntAd: isShowing", )
			}
			onClosed()
			return
		}
		// 宿主 Activity 已不可用时不再发起广告流程，业务继续向后执行。
		if (activity.isUnavailable()) {
			if (com.pdffox.adv.Config.isTest) {
				Log.e(TAG, "showIntAd: activity.isUnavailable()", )
			}
			onClosed()
			return
		}

		// 先占用展示状态，避免策略检查期间被并发请求再次进入。
		isShowing = true
		this.areaKey = areaKey
		this.onClosed = onClosed
		callbackDelivered = false
		admobRetryCount = 0

		// 广告策略检查可能读取本地/远程策略，放到 IO 线程，结果回到主线程继续展示。
		CoroutineScope(Dispatchers.IO).launch {
			val canPlay = AdPolicyManager.checkInterstitialAdUnit(areaKey)
			withContext(Dispatchers.Main) {
				// 策略不允许、宿主不可用或当前平台不是 AdMob 时，都按未展示处理并回调关闭。
				if (!canPlay || activity.isUnavailable() || Config.activeAdPlatform() != LogAdParam.ad_platform_admob) {
					logInterstitialTimeout(areaKey)
					finishFlow()
					return@withContext
				}
				// 开始展示阶段的兜底超时，防止缓存/实时请求都没有可靠回调。
				startTimeout()
				showAd(activity)
			}
		}
	}

	/**
	 * 执行缓存优先的插屏展示。
	 *
	 * 先从 [AdPool] 取预加载 AdMob 插屏，取不到时再进入实时加载流程。
	 */
	private fun showAd(activity: Activity) {
		// 从缓存池取出可用插屏，并把关闭、展示成功回调绑定到当前流程。
		val cachedAd = AdPool.getAdmobInter(
			areaKey = areaKey,
			onClosed = { finishFlow() },
			onDisplayed = {
				// 缓存广告已经展示成功，取消兜底超时并按策略补充下一条插屏缓存。
				clearTimeout()
				if (AdConfig.canLoadInter(AdConfig.LOAD_TIME_PLAY_FINISH)) {
					AdLoader.loadInter(activity)
				}
			}
		)
		// 与 PhotoRecovery 保持一致：播放流程开始后按 play_finish 策略补池。
		if (AdConfig.canLoadInter(AdConfig.LOAD_TIME_PLAY_FINISH)) {
			AdLoader.loadInter(activity)
		}
		if (cachedAd != null) {
			// 缓存命中时直接在宿主 Activity 上展示，不再打开独立 Activity。
			if (isShowing && !activity.isUnavailable()) {
				runCatching {
					cachedAd.show(activity)
				}.onFailure { error ->
					// show() 同步抛错时兜底结束流程，避免业务卡住。
					Log.e(TAG, "cached interstitial show failed", error)
					finishFlow()
				}
			} else {
				// 展示前宿主失效或流程已结束时直接分发关闭回调。
				finishFlow()
			}
		} else {
			// 缓存未命中时实时请求 AdMob 插屏。
			showAdmobAdv(activity)
		}
	}

	/**
	 * 实时加载并展示 AdMob 插屏。
	 *
	 * 该路径只在缓存池没有可用插屏时进入，并带有加载视图、失败重试和收入埋点。
	 */
	@SuppressLint("MissingPermission")
	private fun showAdmobAdv(activity: Activity) {
		// 流程已经结束或宿主不可用时，直接结束并回调宿主。
		if (!isShowing || activity.isUnavailable()) {
			finishFlow()
			return
		}
		// 实时请求可能耗时，显示一个覆盖层提示当前正在加载广告。
		showProgress(activity)
		// 记录非预加载插屏的开始加载事件。
		LogUtil.log(
			LogAdData.ad_start_loading,
			mapOf(
				LogAdParam.ad_platform to LogAdParam.ad_platform_admob,
				LogAdParam.ad_areakey to areaKey,
				LogAdParam.ad_format to LogAdParam.ad_format_interstitial,
				LogAdParam.ad_unit_name to AdvIDs.getAdmobInterstitialId(),
				LogAdParam.ad_preload to false,
			)
		)
		val startLoadingTime = System.currentTimeMillis()
		// 使用当前生效的 AdMob 插屏广告位发起实时加载。
		InterstitialAd.load(
			activity,
			AdvIDs.getAdmobInterstitialId(),
			AdRequest.Builder().build(),
			object : InterstitialAdLoadCallback() {
				/** 实时插屏加载成功后绑定展示回调并立即展示。 */
				override fun onAdLoaded(ad: InterstitialAd) {
					// 记录加载完成耗时和广告来源，便于排查广告链路质量。
					LogUtil.log(
						LogAdData.ad_finish_loading,
						mapOf(
							LogAdParam.ad_platform to LogAdParam.ad_platform_admob,
							LogAdParam.duration to (System.currentTimeMillis() - startLoadingTime),
							LogAdParam.ad_areakey to areaKey,
							LogAdParam.ad_format to LogAdParam.ad_format_interstitial,
							LogAdParam.ad_source to (ad.responseInfo.loadedAdapterResponseInfo?.adSourceName ?: LogAdParam.unknow),
							LogAdParam.ad_unit_name to AdvIDs.getAdmobInterstitialId(),
							LogAdParam.ad_preload to false,
						)
					)
					bindAdmobCallbacks(activity, ad, startLoadingTime)
					clearTimeout()
					// 加载成功后再次确认流程和宿主仍然有效，再展示广告。
					if (isShowing && !activity.isUnavailable()) {
						ad.show(activity)
						hideProgress()
					}
				}

				/** 实时插屏加载失败后进入短延迟重试流程。 */
				override fun onAdFailedToLoad(adError: LoadAdError) {
					Log.e(TAG, adError.message)
					retryShowAdmob(activity)
				}
			},
		)
	}

	/**
	 * 绑定 AdMob 插屏的展示生命周期和收入回调。
	 *
	 * 关闭、展示失败、点击和收入事件都会在这里统一埋点。
	 */
	private fun bindAdmobCallbacks(activity: Activity, ad: InterstitialAd, startLoadingTime: Long) {
		ad.fullScreenContentCallback = object : FullScreenContentCallback() {
			/** 用户点击插屏时记录点击事件，并抑制下一次自动 App Open。 */
			override fun onAdClicked() {
				LogUtil.log(
					LogAdData.ad_click,
					mapOf(
						LogAdParam.ad_platform to LogAdParam.ad_platform_admob,
						LogAdParam.duration to (System.currentTimeMillis() - startLoadingTime),
						LogAdParam.ad_areakey to areaKey,
						LogAdParam.ad_format to LogAdParam.ad_format_interstitial,
						LogAdParam.ad_source to (ad.responseInfo.loadedAdapterResponseInfo?.adSourceName ?: LogAdParam.unknow),
						LogAdParam.ad_unit_name to AdvIDs.getAdmobInterstitialId(),
						LogAdParam.ad_preload to false,
					)
				)
				// 广告点击可能跳出 App，回流时不应立即再弹开屏广告。
				AppOpenHelper.spSwitch = true
			}

			/** 用户关闭插屏时记录关闭事件，并恢复宿主业务流程。 */
			override fun onAdDismissedFullScreenContent() {
				LogUtil.log(
					LogAdData.ad_close,
					mapOf(
						LogAdParam.ad_platform to LogAdParam.ad_platform_admob,
						LogAdParam.duration to (System.currentTimeMillis() - startLoadingTime),
						LogAdParam.ad_areakey to areaKey,
						LogAdParam.ad_format to LogAdParam.ad_format_interstitial,
						LogAdParam.ad_source to (ad.responseInfo.loadedAdapterResponseInfo?.adSourceName ?: LogAdParam.unknow),
						LogAdParam.ad_unit_name to AdvIDs.getAdmobInterstitialId(),
						LogAdParam.ad_preload to false,
					)
				)
				finishFlow()
			}

			/** 插屏展示失败时记录失败原因，并恢复宿主业务流程。 */
			override fun onAdFailedToShowFullScreenContent(adError: AdError) {
				LogUtil.log(
					LogAdData.ad_show_fail,
					mapOf(
						"msg" to adError.message,
						LogAdParam.ad_platform to LogAdParam.ad_platform_admob,
						LogAdParam.duration to (System.currentTimeMillis() - startLoadingTime),
						LogAdParam.ad_areakey to areaKey,
						LogAdParam.ad_format to LogAdParam.ad_format_interstitial,
						LogAdParam.ad_source to (ad.responseInfo.loadedAdapterResponseInfo?.adSourceName ?: LogAdParam.unknow),
						LogAdParam.ad_unit_name to AdvIDs.getAdmobInterstitialId(),
						LogAdParam.ad_preload to false,
					)
				)
				finishFlow()
			}

			/** 插屏产生展示曝光后，按策略补充下一条插屏缓存。 */
			override fun onAdImpression() {
				if (AdConfig.canLoadInter(AdConfig.LOAD_TIME_PLAY_FINISH)) {
					AdLoader.loadInter(activity)
				}
			}
		}
		// 收入回调用于同步 Singular、Firebase/ThinkingData 和太极广告收入埋点。
		ad.onPaidEventListener = OnPaidEventListener { adValue ->
			val revenue = adValue.valueMicros.toDouble() / 1_000_000.0
			LogUtil.logSingularAdRevenue(LogAdParam.InterAd, LogAdParam.adMob, revenue)
			logInterstitialRevenue(LogAdData.ad_impression, ad, adValue.currencyCode, revenue)
			logInterstitialRevenue(LogAdData.ad_revenue, ad, adValue.currencyCode, revenue)
			LogUtil.logTaiChiAdmob(adValue)
		}
	}

	/**
	 * 实时 AdMob 插屏加载失败后的重试入口。
	 *
	 * 未超过最大次数时延迟 1 秒重试；超过次数后结束流程并回调宿主。
	 */
	private fun retryShowAdmob(activity: Activity) {
		admobRetryCount++
		if (admobRetryCount <= MAX_RETRY_COUNT && isShowing) {
			// 延迟重试前后都检查流程和 Activity 状态，避免对已销毁页面继续请求广告。
			mainHandler.postDelayed({
				if (isShowing && !activity.isUnavailable()) {
					showAdmobAdv(activity)
				}
			}, 1000)
		} else {
			finishFlow()
		}
	}

	/** 启动插屏展示兜底超时任务。 */
	private fun startTimeout() {
		clearTimeout()
		timeoutRunnable = Runnable {
			// 超时说明没有进入可见展示或可靠失败回调，按失败路径结束流程。
			Log.e(TAG, "interstitial show timeout")
			logInterstitialTimeout(areaKey)
			finishFlow()
		}
		mainHandler.postDelayed(timeoutRunnable!!, TIMEOUT_MILLIS)
	}

	/** 取消当前插屏流程的兜底超时任务。 */
	private fun clearTimeout() {
		timeoutRunnable?.let(mainHandler::removeCallbacks)
		timeoutRunnable = null
	}

	/** 在宿主 Activity 上添加插屏加载覆盖层。 */
	private fun showProgress(activity: Activity) {
		if (progressView?.parent != null) {
			// 覆盖层已经存在时只恢复可见性，避免重复添加 View。
			progressView?.visibility = View.VISIBLE
			return
		}
		val content = activity.findViewById<ViewGroup>(android.R.id.content) ?: return
		// 复用旧插屏加载布局作为普通覆盖层，不再作为 Activity 的 content view。
		progressView = LayoutInflater.from(activity).inflate(R.layout.activity_show_interstitial_ad, content, false).apply {
			setOnClickListener {}
		}
		content.addView(
			progressView,
			ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
		)
	}

	/** 移除插屏加载覆盖层。 */
	private fun hideProgress() {
		val view = progressView ?: return
		(view.parent as? ViewGroup)?.removeView(view)
		progressView = null
	}

	/**
	 * 结束当前插屏流程。
	 *
	 * 负责清理超时、移除加载层、重置状态，并保证宿主关闭回调只执行一次。
	 */
	private fun finishFlow() {
		clearTimeout()
		hideProgress()
		isShowing = false
		areaKey = ""
		// 多个广告 SDK 回调可能先后到达，这里保证只分发一次关闭回调。
		if (callbackDelivered) {
			return
		}
		callbackDelivered = true
		val callback = onClosed
		onClosed = {}
		callback()
	}

	/** 判断宿主 Activity 是否已经不适合继续发起或展示广告。 */
	private fun Activity.isUnavailable(): Boolean = isFinishing || isDestroyed

	/** 记录插屏策略拒绝、平台不可用或展示超时事件。 */
	private fun logInterstitialTimeout(areaKey: String) {
		LogUtil.log(
			LogAdData.ad_show_timeout,
			mapOf(
				LogAdParam.ad_platform to LogAdParam.ad_platform_admob,
				LogAdParam.ad_areakey to areaKey,
				LogAdParam.ad_format to LogAdParam.ad_format_interstitial,
				LogAdParam.ad_unit_name to AdvIDs.getAdmobInterstitialId(),
				LogAdParam.ad_preload to false,
			)
		)
	}

	/**
	 * 记录插屏收入类事件。
	 *
	 * 字段名保持与 Firebase Analytics 标准广告参数兼容。
	 */
	private fun logInterstitialRevenue(
		eventName: String,
		ad: InterstitialAd,
		currency: String,
		revenue: Double,
	) {
		LogUtil.log(
			eventName,
			mapOf(
				LogAdParam.ad_areakey to areaKey,
				FirebaseAnalytics.Param.AD_PLATFORM to LogAdParam.ad_platform_admob,
				FirebaseAnalytics.Param.AD_UNIT_NAME to AdvIDs.getAdmobInterstitialId(),
				FirebaseAnalytics.Param.AD_FORMAT to LogAdParam.ad_format_interstitial,
				FirebaseAnalytics.Param.AD_SOURCE to (ad.responseInfo.loadedAdapterResponseInfo?.adSourceName ?: LogAdParam.unknow),
				FirebaseAnalytics.Param.CURRENCY to currency,
				FirebaseAnalytics.Param.VALUE to revenue,
				LogAdParam.ad_preload to false,
			)
		)
	}
}
