package com.pdffox.adv.adv

import android.Manifest
import android.content.Context
import android.util.Log
import androidx.annotation.RequiresPermission
import com.google.android.gms.ads.nativead.NativeAd
import com.google.firebase.analytics.FirebaseAnalytics
import com.pdffox.adv.BuildConfig
import com.pdffox.adv.Config
import com.pdffox.adv.NativeAdContent
import com.pdffox.adv.adv.policy.NativePolicyManager
import com.pdffox.adv.log.LogAdData
import com.pdffox.adv.log.LogAdParam
import com.pdffox.adv.log.LogUtil

/**
 * Native 广告获取入口。
 *
 * 负责按用户归因/IP、Native 策略和广告池状态筛选广告，并为取出的 NativeAd 绑定收入和点击回调。
 */
object ShowNativeAd {

	private const val TAG = "ShowNativeAd"

	/** 获取一组 Native 广告；没有缓存时会触发异步填池并返回 null。 */
	@RequiresPermission(Manifest.permission.INTERNET)
	fun getNativeAd(context: Context, areaKey: String, onAdGroupLoaded: () -> Unit): NativeAdContent? {
		if (!Config.sdkConfig.adMob.enabled) {
			return null
		}
		// 原生广告屏蔽掉自然量
		if ( (!com.pdffox.adv.Config.isTest) && (Config.paid_0 || Config.isGoogleIP)) {
			Log.e(TAG, "getNativeAd: 屏蔽掉自然量" )
			return null
		}

		// Native 广告额外走 NativePolicyManager，便于和全屏广告使用不同策略。
		val canPlay = NativePolicyManager.checkAdUnit(areaKey)
		if (!canPlay) {
			Log.e(TAG, "getNativeAd: 后台配置筛掉 $areaKey" )
			return null
		}

		Log.e(TAG, "getNativeAd: 加载原生广告" )

		val adContent = AdPool.getAdmobNative()
		if (adContent != null) {
			// 按高价、中价、兜底广告位顺序选择本组里第一个可用广告。
			if (adContent.hAd != null) {
				val resultAd = adContent.hAd
				resultAd?.setOnPaidEventListener { adValue ->
					val micros = adValue.valueMicros         // 广告价值（微元单位，需除以1,000,000得到实际金额）
					val currency = adValue.currencyCode     // ISO 4217货币代码（如："USD"）
					val precision = adValue.precisionType    // 金额精度类型（0=估算，1=发布商定义，2=精确计算）
					// 收入跟踪（示例：转换为美元）
					val revenue = micros / 1_000_000.0
					LogUtil.logSingularAdRevenue(LogAdParam.BannerAd, LogAdParam.adMob, revenue)
					LogUtil.log(
						LogAdData.ad_impression,
						mapOf(
							LogAdParam.ad_areakey to areaKey,
							FirebaseAnalytics.Param.AD_PLATFORM to LogAdParam.ad_platform_admob,
							FirebaseAnalytics.Param.AD_UNIT_NAME to AdvIDs.getAdmobBannerId(),
							FirebaseAnalytics.Param.AD_FORMAT to LogAdParam.ad_format_native,
							FirebaseAnalytics.Param.AD_SOURCE to LogAdParam.adMob,
							FirebaseAnalytics.Param.CURRENCY to currency,
							FirebaseAnalytics.Param.VALUE to revenue,
							LogAdParam.ad_preload to false,
						)
					)
					LogUtil.log(
						LogAdData.ad_revenue,
						mapOf(
							LogAdParam.ad_areakey to areaKey,
							FirebaseAnalytics.Param.AD_PLATFORM to LogAdParam.ad_platform_admob,
							FirebaseAnalytics.Param.AD_UNIT_NAME to AdvIDs.getAdmobBannerId(),
							FirebaseAnalytics.Param.AD_FORMAT to LogAdParam.ad_format_native,
							FirebaseAnalytics.Param.AD_SOURCE to LogAdParam.adMob,
							FirebaseAnalytics.Param.CURRENCY to currency,
							FirebaseAnalytics.Param.VALUE to revenue,
							LogAdParam.ad_preload to false,
						)
					)
					LogUtil.logTaiChiAdmob(adValue)
					if (com.pdffox.adv.Config.isTest || ((!Config.paid_0) && (!Config.isGoogleIP) && AdConfig.canLoadNative(AdConfig.LOAD_TIME_PLAY_FINISH))) {
						// 展示产生收入后按策略补充下一组 Native 预加载。
						AdLoader.fillNativePool(context)
					}
				}
				// Native 高级点击回调用于区分确认点击和取消点击，统一落到 ad_click 埋点。
				resultAd?.setUnconfirmedClickListener(object : NativeAd.UnconfirmedClickListener{
					override fun onUnconfirmedClickReceived(p0: String) {
						LogUtil.log(
							LogAdData.ad_click,
							mapOf(
								LogAdParam.ad_platform to LogAdParam.ad_platform_admob,
								LogAdParam.ad_areakey to areaKey,
								LogAdParam.ad_format to LogAdParam.ad_format_native,
								LogAdParam.ad_preload to true,
								"onUnconfirmedClickReceived" to true
							)
						)
					}

					override fun onUnconfirmedClickCancelled() {
						LogUtil.log(
							LogAdData.ad_click,
							mapOf(
								LogAdParam.ad_platform to LogAdParam.ad_platform_admob,
								LogAdParam.ad_areakey to areaKey,
								LogAdParam.ad_format to LogAdParam.ad_format_native,
								LogAdParam.ad_preload to true,
								"onUnconfirmedClickReceived" to false
							)
						)
					}
				})
			} else if (adContent.mAd != null) {
				val resultAd = adContent.mAd
				resultAd?.setOnPaidEventListener { adValue ->
					val micros = adValue.valueMicros         // 广告价值（微元单位，需除以1,000,000得到实际金额）
					val currency = adValue.currencyCode     // ISO 4217货币代码（如："USD"）
					val precision = adValue.precisionType    // 金额精度类型（0=估算，1=发布商定义，2=精确计算）
					// 收入跟踪（示例：转换为美元）
					val revenue = micros / 1_000_000.0
					LogUtil.logSingularAdRevenue(LogAdParam.BannerAd, LogAdParam.adMob, revenue)
					LogUtil.log(
						LogAdData.ad_impression,
						mapOf(
							LogAdParam.ad_areakey to areaKey,
							FirebaseAnalytics.Param.AD_PLATFORM to LogAdParam.ad_platform_admob,
							FirebaseAnalytics.Param.AD_UNIT_NAME to AdvIDs.getAdmobBannerId(),
							FirebaseAnalytics.Param.AD_FORMAT to LogAdParam.ad_format_native,
							FirebaseAnalytics.Param.AD_SOURCE to LogAdParam.adMob,
							FirebaseAnalytics.Param.CURRENCY to currency,
							FirebaseAnalytics.Param.VALUE to revenue,
							LogAdParam.ad_preload to false,
						)
					)
					LogUtil.log(
						LogAdData.ad_revenue,
						mapOf(
							LogAdParam.ad_areakey to areaKey,
							FirebaseAnalytics.Param.AD_PLATFORM to LogAdParam.ad_platform_admob,
							FirebaseAnalytics.Param.AD_UNIT_NAME to AdvIDs.getAdmobBannerId(),
							FirebaseAnalytics.Param.AD_FORMAT to LogAdParam.ad_format_native,
							FirebaseAnalytics.Param.AD_SOURCE to LogAdParam.adMob,
							FirebaseAnalytics.Param.CURRENCY to currency,
							FirebaseAnalytics.Param.VALUE to revenue,
							LogAdParam.ad_preload to false,
						)
					)
					LogUtil.logTaiChiAdmob(adValue)
					if (com.pdffox.adv.Config.isTest || ((!Config.paid_0) && (!Config.isGoogleIP) && AdConfig.canLoadNative(AdConfig.LOAD_TIME_PLAY_FINISH))) {
						// 展示产生收入后按策略补充下一组 Native 预加载。
						AdLoader.fillNativePool(context)
					}
				}
				// Native 高级点击回调用于区分确认点击和取消点击，统一落到 ad_click 埋点。
				resultAd?.setUnconfirmedClickListener(object : NativeAd.UnconfirmedClickListener{
					override fun onUnconfirmedClickReceived(p0: String) {
						LogUtil.log(
							LogAdData.ad_click,
							mapOf(
								LogAdParam.ad_platform to LogAdParam.ad_platform_admob,
								LogAdParam.ad_areakey to areaKey,
								LogAdParam.ad_format to LogAdParam.ad_format_native,
								LogAdParam.ad_preload to true,
								"onUnconfirmedClickReceived" to true
							)
						)
					}

					override fun onUnconfirmedClickCancelled() {
						LogUtil.log(
							LogAdData.ad_click,
							mapOf(
								LogAdParam.ad_platform to LogAdParam.ad_platform_admob,
								LogAdParam.ad_areakey to areaKey,
								LogAdParam.ad_format to LogAdParam.ad_format_native,
								LogAdParam.ad_preload to true,
								"onUnconfirmedClickReceived" to false
							)
						)
					}
				})
			} else if (adContent.lAd != null) {
				val resultAd = adContent.lAd
				resultAd?.setOnPaidEventListener { adValue ->
					val micros = adValue.valueMicros         // 广告价值（微元单位，需除以1,000,000得到实际金额）
					val currency = adValue.currencyCode     // ISO 4217货币代码（如："USD"）
					val precision = adValue.precisionType    // 金额精度类型（0=估算，1=发布商定义，2=精确计算）
					// 收入跟踪（示例：转换为美元）
					val revenue = micros / 1_000_000.0
					LogUtil.logSingularAdRevenue(LogAdParam.BannerAd, LogAdParam.adMob, revenue)
					LogUtil.log(
						LogAdData.ad_impression,
						mapOf(
							LogAdParam.ad_areakey to areaKey,
							FirebaseAnalytics.Param.AD_PLATFORM to LogAdParam.ad_platform_admob,
							FirebaseAnalytics.Param.AD_UNIT_NAME to AdvIDs.getAdmobBannerId(),
							FirebaseAnalytics.Param.AD_FORMAT to LogAdParam.ad_format_native,
							FirebaseAnalytics.Param.AD_SOURCE to LogAdParam.adMob,
							FirebaseAnalytics.Param.CURRENCY to currency,
							FirebaseAnalytics.Param.VALUE to revenue,
							LogAdParam.ad_preload to false,
						)
					)
					LogUtil.log(
						LogAdData.ad_revenue,
						mapOf(
							LogAdParam.ad_areakey to areaKey,
							FirebaseAnalytics.Param.AD_PLATFORM to LogAdParam.ad_platform_admob,
							FirebaseAnalytics.Param.AD_UNIT_NAME to AdvIDs.getAdmobBannerId(),
							FirebaseAnalytics.Param.AD_FORMAT to LogAdParam.ad_format_native,
							FirebaseAnalytics.Param.AD_SOURCE to LogAdParam.adMob,
							FirebaseAnalytics.Param.CURRENCY to currency,
							FirebaseAnalytics.Param.VALUE to revenue,
							LogAdParam.ad_preload to false,
						)
					)
					LogUtil.logTaiChiAdmob(adValue)
					if (com.pdffox.adv.Config.isTest || ((!Config.paid_0) && (!Config.isGoogleIP) && AdConfig.canLoadNative(AdConfig.LOAD_TIME_PLAY_FINISH))) {
						// 展示产生收入后按策略补充下一组 Native 预加载。
						AdLoader.fillNativePool(context)
					}
				}
				// Native 高级点击回调用于区分确认点击和取消点击，统一落到 ad_click 埋点。
				resultAd?.setUnconfirmedClickListener(object : NativeAd.UnconfirmedClickListener{
					override fun onUnconfirmedClickReceived(p0: String) {
						LogUtil.log(
							LogAdData.ad_click,
							mapOf(
								LogAdParam.ad_platform to LogAdParam.ad_platform_admob,
								LogAdParam.ad_areakey to areaKey,
								LogAdParam.ad_format to LogAdParam.ad_format_native,
								LogAdParam.ad_preload to true,
								"onUnconfirmedClickReceived" to true
							)
						)
					}

					override fun onUnconfirmedClickCancelled() {
						LogUtil.log(
							LogAdData.ad_click,
							mapOf(
								LogAdParam.ad_platform to LogAdParam.ad_platform_admob,
								LogAdParam.ad_areakey to areaKey,
								LogAdParam.ad_format to LogAdParam.ad_format_native,
								LogAdParam.ad_preload to true,
								"onUnconfirmedClickReceived" to false
							)
						)
					}
				})
			}
		}

		if (adContent == null) {
			// 当前没有可展示缓存时，只触发填池；调用方等 onAdGroupLoaded 后再刷新 UI。
			Log.e(TAG, "getNativeAd: fillNativePool 开始填充原生广告" )
			AdLoader.fillNativePool(context, onAdGroupLoaded)
		}

		return adContent
	}

}
