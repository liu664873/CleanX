package com.pdffox.adv.adv

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.pdffox.adv.Config
import com.pdffox.adv.NativeAdContent
import com.pdffox.adv.log.LogAdData
import com.pdffox.adv.log.LogAdParam
import com.pdffox.adv.log.LogUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlin.coroutines.resume

/**
 * AdMob 广告预加载器。
 *
 * 负责根据当前广告平台和池容量加载 App Open、插屏、Native 广告，并把成功加载的广告放入 [AdPool]。
 */
object AdLoader {

	private const val TAG = "AdLoader"

	/** 根据当前广告平台预加载 App Open 广告。 */
	fun loadOpen(context: Context) {
		if (Config.activeAdPlatform() == LogAdParam.ad_platform_admob) {
			fillAdmobOpenPool(context)
		}
	}

	/** 按池容量补齐 App Open 广告缓存。 */
	private fun fillAdmobOpenPool(context: Context) {
		// 启动页展示首个开屏广告前会走到这里；先清理过期缓存，再按池容量补齐。
		AdChecker.checkExpiredAdmobOpen()
		val loadSize = AdConfig.adload_poolsize_open - AdPool.admobOpenPool.size - AdPool.admobOpenIsLoadingNum
		if (loadSize > 0) {
			repeat(loadSize) {
				loadAdmobOpen(context)
			}
		}
	}

	/** 根据当前广告平台预加载插屏广告。 */
	fun loadInter(context: Context) {
		if (Config.activeAdPlatform() == LogAdParam.ad_platform_admob) {
			fillAdmobInterPool(context)
		}
	}

	/** 按池容量补齐插屏广告缓存。 */
	private fun fillAdmobInterPool(context: Context) {
		AdChecker.checkExpiredAdmobInter()
		val loadSize = AdConfig.adload_poolsize_inter - AdPool.admobInterPool.size - AdPool.admobInterIsLoadingNum
		if (loadSize > 0) {
			repeat(loadSize) {
				Log.e(TAG, "loadInter: preload admob interstitial")
				loadAdmobInter(context)
			}
		}
	}

	/**
	 * 填充 Native 广告池。
	 *
	 * 每一组 Native 广告会同时请求高价、中价、兜底三个广告位，至少一个成功才入池。
	 */
	fun fillNativePool(context: Context, onAdGroupLoaded: (() -> Unit)? = null) {
		if (!Config.sdkConfig.adMob.enabled) {
			return
		}
		CoroutineScope(Dispatchers.Main).launch {
			val ids = AdvIDs.getNextNativeAdId() ?: return@launch
			if (AdPool.admobNativePool.size > AdConfig.adload_poolsize_native) return@launch
			if (AdPool.admobNativeIsLoadingNum > 3) return@launch

			AdPool.admobNativeIsLoadingNum++
			val hDeferred = async(Dispatchers.IO) { loadNativeAdSuspended(context, ids.hId) }
			val mDeferred = async(Dispatchers.IO) { loadNativeAdSuspended(context, ids.mId) }
			val lDeferred = async(Dispatchers.IO) { loadNativeAdSuspended(context, ids.aId) }

			val nativeContent = NativeAdContent(
				ids.index,
				hDeferred.await(),
				mDeferred.await(),
				lDeferred.await()
			)
			AdPool.admobNativeIsLoadingNum--

			if (nativeContent.hAd != null || nativeContent.mAd != null || nativeContent.lAd != null) {
				AdPool.putAdmobNative(nativeContent)
				onAdGroupLoaded?.invoke()
			}
		}
	}

	/** 加载单个 App Open 广告，并在超时或失败时按配置重试。 */
	private fun loadAdmobOpen(context: Context, retryCount: Int = 0) {
		if (AdPool.admobOpenPool.size + AdPool.admobOpenIsLoadingNum >= AdConfig.adload_poolsize_open) return
		// 这里预加载的是通用 App Open 缓存，真正展示时会绑定到 openPageAdv 等具体广告位。
		LogUtil.log(
			LogAdData.ad_start_loading,
			mapOf(
				LogAdParam.ad_platform to LogAdParam.ad_platform_admob,
				LogAdParam.ad_areakey to "PreLoad",
				LogAdParam.ad_format to LogAdParam.ad_format_open,
				LogAdParam.ad_unit_name to AdvIDs.getAdmobOpenId(),
				LogAdParam.ad_preload to true,
			)
		)
		val startLoadingTime = System.currentTimeMillis()
		var isLoaded = false
		AdPool.admobOpenIsLoadingNum++
		AppOpenAd.load(
			context,
			AdvIDs.getAdmobOpenId(),
			AdRequest.Builder().build(),
			object : AppOpenAd.AppOpenAdLoadCallback() {
				override fun onAdLoaded(admobAppOpenAd: AppOpenAd) {
					isLoaded = true
					AdPool.admobOpenIsLoadingNum--
					// 预加载成功后放入缓存池，启动页展示时优先消费该缓存。
					LogUtil.log(
						LogAdData.ad_finish_loading,
						mapOf(
							LogAdParam.ad_platform to LogAdParam.ad_platform_admob,
							LogAdParam.duration to (System.currentTimeMillis() - startLoadingTime),
							LogAdParam.ad_areakey to "PreLoad",
							LogAdParam.ad_format to LogAdParam.ad_format_open,
							LogAdParam.ad_source to (admobAppOpenAd.responseInfo.loadedAdapterResponseInfo?.adSourceName ?: LogAdParam.unknow),
							LogAdParam.ad_unit_name to AdvIDs.getAdmobOpenId(),
							LogAdParam.ad_preload to true,
						)
					)
					AdPool.putAdmobOpen(admobAppOpenAd)
				}

				override fun onAdFailedToLoad(loadAdError: LoadAdError) {
					AdPool.admobOpenIsLoadingNum--
					// 预加载失败不影响启动页；启动页后续还可以实时加载或直接跳过广告。
					retryAdmobOpen(context, retryCount, isLoaded)
				}
			},
		)
		if (retryCount == 0) {
			// 首次请求超过最大加载时长还未成功时，按重试策略再发起一次加载。
			Handler(context.mainLooper).postDelayed({
				retryAdmobOpen(context, retryCount, isLoaded)
			}, AdConfig.adload_max_time)
		}
	}

	/** App Open 广告加载失败或超时后的重试入口。 */
	private fun retryAdmobOpen(context: Context, retryCount: Int, isLoaded: Boolean) {
		if (!isLoaded && retryCount < AdConfig.adload_retry_num) {
			// 只在未加载成功且未超过重试次数时继续补池。
			loadAdmobOpen(context, retryCount + 1)
		}
	}

	/** 加载单个插屏广告，并在超时或失败时按配置重试。 */
	private fun loadAdmobInter(context: Context, retryCount: Int = 0) {
		if (AdPool.admobInterPool.size + AdPool.admobInterIsLoadingNum >= AdConfig.adload_poolsize_inter) return
		LogUtil.log(
			LogAdData.ad_start_loading,
			mapOf(
				LogAdParam.ad_platform to LogAdParam.ad_platform_admob,
				LogAdParam.ad_areakey to "PreLoad",
				LogAdParam.ad_format to LogAdParam.ad_format_interstitial,
				LogAdParam.ad_unit_name to AdvIDs.getAdmobInterstitialId(),
				LogAdParam.ad_preload to true,
			)
		)
		val startLoadingTime = System.currentTimeMillis()
		var isLoaded = false
		AdPool.admobInterIsLoadingNum++
		InterstitialAd.load(
			context,
			AdvIDs.getAdmobInterstitialId(),
			AdRequest.Builder().build(),
			object : InterstitialAdLoadCallback() {
				override fun onAdLoaded(adInter: InterstitialAd) {
					isLoaded = true
					AdPool.admobInterIsLoadingNum--
					LogUtil.log(
						LogAdData.ad_finish_loading,
						mapOf(
							LogAdParam.ad_platform to LogAdParam.ad_platform_admob,
							LogAdParam.duration to (System.currentTimeMillis() - startLoadingTime),
							LogAdParam.ad_areakey to "PreLoad",
							LogAdParam.ad_format to LogAdParam.ad_format_interstitial,
							LogAdParam.ad_source to (adInter.responseInfo.loadedAdapterResponseInfo?.adSourceName ?: LogAdParam.unknow),
							LogAdParam.ad_unit_name to AdvIDs.getAdmobInterstitialId(),
							LogAdParam.ad_preload to true,
						)
					)
					AdPool.putAdmobInter(adInter)
				}

				override fun onAdFailedToLoad(adError: LoadAdError) {
					AdPool.admobInterIsLoadingNum--
					retryAdmobInter(context, retryCount, isLoaded)
				}
			},
		)
		if (retryCount == 0) {
			Handler(context.mainLooper).postDelayed({
				retryAdmobInter(context, retryCount, isLoaded)
			}, AdConfig.adload_max_time)
		}
	}

	/** 插屏广告加载失败或超时后的重试入口。 */
	private fun retryAdmobInter(context: Context, retryCount: Int, isLoaded: Boolean) {
		if (!isLoaded && retryCount < AdConfig.adload_retry_num) {
			loadAdmobInter(context, retryCount + 1)
		}
	}

	/** 加载单个 Native 广告位，并把结果切回主线程回调。 */
	@SuppressLint("MissingPermission")
	fun loadNativeAD(context: Context, nativeAdId: String, onAdLoaded: (NativeAd?) -> Unit) {
		if (!Config.sdkConfig.adMob.enabled) {
			Handler(Looper.getMainLooper()).post {
				onAdLoaded(null)
			}
			return
		}
		CoroutineScope(Dispatchers.IO).launch {
			LogUtil.log(
				LogAdData.ad_start_loading,
				mapOf(
					LogAdParam.ad_platform to LogAdParam.ad_platform_admob,
					LogAdParam.ad_areakey to "PreLoad",
					LogAdParam.ad_format to LogAdParam.ad_format_native,
					LogAdParam.ad_unit_name to nativeAdId,
					LogAdParam.ad_preload to true,
				)
			)
			val adLoader = com.google.android.gms.ads.AdLoader.Builder(context, nativeAdId)
				.forNativeAd { nativeAd ->
					Handler(Looper.getMainLooper()).post {
						onAdLoaded(nativeAd)
					}
				}
				.withAdListener(
					object : AdListener() {
						override fun onAdFailedToLoad(adError: LoadAdError) {
							Log.e(TAG, "loadNativeAD failed: $nativeAdId $adError")
							Handler(Looper.getMainLooper()).post {
								onAdLoaded(null)
							}
						}
					}
				)
				.withNativeAdOptions(NativeAdOptions.Builder().build())
				.build()
			adLoader.loadAd(AdRequest.Builder().build())
		}
	}

	/** 将回调式 Native 广告加载封装为可挂起函数，便于并发加载一组广告位。 */
	suspend fun loadNativeAdSuspended(context: Context, adId: String): NativeAd? =
		kotlinx.coroutines.suspendCancellableCoroutine { continuation ->
			loadNativeAD(context, adId) { ad ->
				if (continuation.isActive) {
					continuation.resume(ad)
				} else {
					ad?.destroy()
				}
			}
		}
}
