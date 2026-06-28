package com.pdffox.adv.log

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import cn.thinkingdata.analytics.TDAnalytics
import com.google.android.gms.ads.AdValue
import com.google.firebase.Firebase
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.remoteconfig.remoteConfig
import com.pdffox.adv.Ads
import com.pdffox.adv.BuildConfig
import com.pdffox.adv.Config
import com.pdffox.adv.adv.AdvCheckManager
import com.pdffox.adv.adv.policy.AdPlayRecordManager
import com.pdffox.adv.adv.policy.NativeAdPlayRecordManager
import com.pdffox.adv.adv.policy.data.AdRecord
import com.pdffox.adv.push.LogPushData
import com.pdffox.adv.remoteconfig.RemoteConfigRouting
import com.singular.sdk.Singular
import com.singular.sdk.SingularAdData
import org.json.JSONObject

/**
 * 广告库统一埋点工具。
 *
 * 负责把事件分发到 Firebase、ThinkingData 和 Singular，并在广告曝光时维护播放记录和 paid_0 路由状态。
 */
object LogUtil {
	private const val TAG = "LogUtil"

	/** 统一事件入口，根据事件类型决定是否上报 Firebase、ThinkingData、Singular 或写入播放记录。 */
	fun log(eventName: String, params: Map<String, Any>) {
		if (com.pdffox.adv.Config.isTest) {
			Log.e(TAG, "log: $eventName $params")
		}
		if (eventName == "notification_app_shown") {
			// 通知展示事件只在安装后指定小时窗口内上报，避免长期后台通知污染早期归因指标。
			val firstOpenTime = AdvCheckManager.params.installTime
			if (com.pdffox.adv.Config.isTest) {
				Log.e(TAG, "log: firstOpenTime = $firstOpenTime")
			}
			if (firstOpenTime > 0 && System.currentTimeMillis() - firstOpenTime > Config.log_time * 3600 * 1000) {
				if (com.pdffox.adv.Config.isTest) {
					Log.e(TAG, "log: skip notification_app_shown after log window")
				}
				return
			}
		}
		if (eventName != LogAdData.ad_impression) {
			logFirebase(eventName, params)
		}
		if (eventName == LogAdData.ad_impression) {
			// 广告曝光同时作为本地频控记录来源，Native 和非 Native 使用不同记录管理器。
			val adRecord = AdRecord(
				areakey = params[LogAdParam.ad_areakey] as String,
				adFormat = params[FirebaseAnalytics.Param.AD_FORMAT] as String,
				showAdPlatform = params[FirebaseAnalytics.Param.AD_PLATFORM] as String,
				timestamp = System.currentTimeMillis(),
			)
			if (params[FirebaseAnalytics.Param.AD_FORMAT] == LogAdParam.ad_format_native) {
				NativeAdPlayRecordManager.addRecord(adRecord)
			} else {
				AdPlayRecordManager.addRecord(adRecord)
			}
			if (!Config.paid0HasResult) {
				// 首次广告收入为 0 时按 paid_0 用户处理，并触发远程配置路由重算。
				Config.paid0HasResult = true
				Config.paid_0 = (params[FirebaseAnalytics.Param.VALUE] as? Number)?.toDouble() == 0.0
				if (Config.sdkConfig.remoteConfig.enabled && Config.remoteConfigHasResult) {
					val remoteConfig = Firebase.remoteConfig
					val adMapping = remoteConfig.getString("ad_mapping")
					RemoteConfigRouting.apply(
						remoteConfig = remoteConfig,
						adMapping = adMapping,
						source = "LogUtil.adImpression"
					)
				} else if (com.pdffox.adv.Config.isTest) {
					Log.e(TAG, "log: RemoteConfig not ready")
				}
			}
		}
		if (eventName != LogPushData.notification_shown && eventName != LogAdData.adv_sdk_initcomplete) {
			logThinking(eventName, params)
		}
	}

	/** 上报 Firebase Analytics 事件，并把 Map 参数转换为 Bundle。 */
	@SuppressLint("MissingPermission")
	fun logFirebase(eventName: String, params: Map<String, Any>) {
		if (!Config.sdkConfig.firebase.analyticsEnabled) {
			return
		}
		val firebaseAnalytics = FirebaseAnalytics.getInstance(Ads.application)
		val bundle = Bundle()
		for ((key, value) in params) {
			when (value) {
				is String -> bundle.putString(key, value)
				is Int -> bundle.putInt(key, value)
				is Long -> bundle.putLong(key, value)
				is Double -> bundle.putDouble(key, value)
				is Float -> bundle.putFloat(key, value)
				is Boolean -> bundle.putBoolean(key, value)
				else -> bundle.putString(key, value.toString())
			}
		}
		firebaseAnalytics.logEvent(eventName, bundle)
	}

	/** 上报 ThinkingData 事件。 */
	fun logThinking(eventName: String, params: Map<String, Any>) {
		if (!Config.sdkConfig.thinking.enabled) {
			return
		}
		try {
			val jsonObject = JSONObject()
			for ((key, value) in params) {
				jsonObject.put(key, value)
			}
			TDAnalytics.track(eventName, jsonObject)
		} catch (e: Exception) {
			Log.e(TAG, "logThinking error: ${e.message}")
		}
	}

	/** 上报 Singular 广告收入事件，并在收入大于 0 时同步调用 Singular adRevenue。 */
	fun logSingularAdRevenue(adType: String, adPlatform: String, revenue: Double, trackRevenue: Boolean = true) {
		if (!Config.sdkConfig.singular.enabled) {
			return
		}
		try {
			val att = JSONObject().apply {
				put(LogAdParam.revenue, revenue)
				put(LogAdParam.adType, adType)
			}
			Singular.eventJSON(LogAdData.ad_revenue, att)
			if (trackRevenue && revenue > 0) {
				Singular.adRevenue(
					SingularAdData(
						adPlatform,
						LogAdParam.USD,
						revenue
					)
				)
			}
		} catch (e: Exception) {
			Log.e(TAG, "logSingularAdRevenue error: ${e.message}")
		}
	}

	// 太极 ROAS 缓存：小额广告收入累计到阈值后再上报 total_Ads_Revenue_001。
	private val taichiAdmobPref by lazy {
		Ads.application.getSharedPreferences(LogAdParam.admobTaichiTroasCache, 0)
	}

	private val taichiAdmobSharedPreferencesEditor by lazy {
		taichiAdmobPref.edit()
	}

	/** 上报 AdMob 单次曝光收入，并累计到 0.01 USD 后触发总收入事件。 */
	@SuppressLint("MissingPermission")
	fun logTaiChiAdmob(adValue: AdValue) {
		if (!Config.sdkConfig.firebase.analyticsEnabled) {
			return
		}
		val firebaseAnalytics = FirebaseAnalytics.getInstance(Ads.application)
		val currentImpressionRevenue = adValue.valueMicros.toDouble() / 1_000_000.0
		val precisionType = when (adValue.precisionType) {
			0 -> "UNKNOWN"
			1 -> "ESTIMATED"
			2 -> "PUBLISHER_PROVIDED"
			3 -> "PRECISE"
			else -> "Invalid"
		}

		val params = Bundle().apply {
			putDouble(FirebaseAnalytics.Param.VALUE, currentImpressionRevenue)
			putString(FirebaseAnalytics.Param.CURRENCY, LogAdParam.USD)
			putString("precisionType", precisionType)
		}
		firebaseAnalytics.logEvent(LogAdData.ad_Impression_Revenue, params)

		val previousTaichiTroasCache = taichiAdmobPref.getFloat(LogAdParam.admobTaichiTroasCache, 0f)
		val currentTaichiTroasCache = previousTaichiTroasCache + currentImpressionRevenue.toFloat()
		if (currentTaichiTroasCache >= 0.01f) {
			val roasBundle = Bundle().apply {
				putDouble(FirebaseAnalytics.Param.VALUE, currentTaichiTroasCache.toDouble())
				putString(FirebaseAnalytics.Param.CURRENCY, LogAdParam.USD)
			}
			firebaseAnalytics.logEvent(LogAdData.total_Ads_Revenue_001, roasBundle)
			taichiAdmobSharedPreferencesEditor.putFloat(LogAdParam.admobTaichiTroasCache, 0f)
		} else {
			taichiAdmobSharedPreferencesEditor.putFloat(LogAdParam.admobTaichiTroasCache, currentTaichiTroasCache)
		}
		taichiAdmobSharedPreferencesEditor.commit()
	}

}
