package com.pdffox.adv.remoteconfig

import android.util.Log
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.pdffox.adv.Config
import com.pdffox.adv.adv.AdvIDs
import com.pdffox.adv.adv.NativeConfig
import com.pdffox.adv.adv.policy.NativePolicyManager
import com.pdffox.adv.adv.policy.data.parseAdMapping
import com.pdffox.adv.log.ThinkingAttr
import com.pdffox.adv.notification.NotificationManager
import com.pdffox.adv.util.PreferenceUtil

/**
 * Firebase Remote Config 参数应用器。
 *
 * 负责把远程参数写入广告位、广告策略、通知策略、Native 策略、topic 路由和埋点用户属性。
 */
object RemoteConfig {
	private const val TAG = "RemoteConfig"

	// ABTestName：实验名称，会写入 ThinkingData 用户属性 ab_test。
	var ABTestName: String = ""

	// adload_config：最近一次下发的广告预加载策略 JSON。
	var str_adLoad_config : String = ""
	// ad_policy：最近一次下发的广告策略 JSON。
	var str_ad_policy : String = ""
	// notification_config：最近一次下发的通知触发策略 JSON。
	var str_notification_config : String = ""

	// 当前已订阅或待处理的 FCM topic 集合。
	val topicSet = HashSet<String>()

	/** 将 FirebaseRemoteConfig 中的最新参数应用到 SDK 全局配置。 */
	fun update(remoteConfig: FirebaseRemoteConfig) {

		if (com.pdffox.adv.Config.isTest) {
			Log.e(TAG, "update: RemoteConfig singularHasResult = ${Config.singularHasResult}")
			Log.e(TAG, "update: RemoteConfig isNature = ${Config.isNature}")
		}

		// ABTestName：实验名称，用于后续埋点分组。
		ABTestName = remoteConfig.getString("ABTestName")
		ThinkingAttr.setUserAttr(ThinkingAttr.ab_test, ABTestName)

		// update_version：远程配置版本号。
		Config.update_version = remoteConfig.getLong("update_version")


		Config.remoteConfigHasResult = true

		// OpenAdmobMediation：是否启用 AdMob mediation adapter 初始化。
		val openAdmobMediation = remoteConfig.getBoolean("OpenAdmobMediation")
		Config.setConfig(openAdmobMediation)

		// Admob_*：Remote Config 覆盖的 AdMob 广告位 ID。
		val admobBanner = remoteConfig.getString("Admob_Banner")
		val admobInterset = remoteConfig.getString("Admob_Interset")
		val admobNative = remoteConfig.getString("Admob_Native")
		val admobOpen = remoteConfig.getString("Admob_Open")
		AdvIDs.setAdmobIDs(admobBanner, admobInterset, admobNative, admobOpen)
		// Contextualized_Push：上下文化 Push 配置 JSON。
		var config= remoteConfig.getString("Contextualized_Push")
		PreferenceUtil.commitString("Contextualized_Push", config)

		// openReview：参与自然量/Google IP/paid_0 路由选择的开关。
		Config.openReview = remoteConfig.getBoolean("openReview")
		PreferenceUtil.commitBoolean("openReview", Config.openReview)
		if (com.pdffox.adv.Config.isTest) {
			Log.e(TAG, "update: openReview = ${Config.openReview}")
		}

		// ad_mapping：按自然量、国家、品牌切换广告/通知/预加载配置和 FCM topic。
		val adMapping = remoteConfig.getString("ad_mapping")
		PreferenceUtil.commitString("ad_mapping", adMapping)
		if (adMapping != "") {
			val adMappingObj = parseAdMapping(adMapping)
			if (com.pdffox.adv.Config.isTest) {
				Log.e(TAG, "update: adMappingObj = $adMappingObj")
			}
		}
		if (com.pdffox.adv.Config.isTest) {
			Log.e(TAG, "update: ad_mapping=$adMapping")
		}

		// adload_config：广告预加载策略 JSON。
		val adload_config = remoteConfig.getString("adload_config")
		str_adLoad_config = adload_config

		// ad_policy：广告展示策略 JSON。
		val ad_policy = remoteConfig.getString("ad_policy")
		str_ad_policy = ad_policy
		if (com.pdffox.adv.Config.isTest) {
			Log.e(TAG, "update: ad_policy=$ad_policy")
		}

		// notification_config：通知触发策略 JSON。
		val notification_config = remoteConfig.getString("notification_config")
		str_notification_config = notification_config
		if (com.pdffox.adv.Config.isTest) {
			Log.e(TAG, "update: notification_config = $notification_config")
		}

		// notification_content：通知展示内容 JSON。
		val notification_content = remoteConfig.getString("notification_content")
		if (com.pdffox.adv.Config.isTest) {
			Log.e(TAG, "update: notification_content = $notification_content")
		}
		if (Config.sdkConfig.notifications.enabled) {
			NotificationManager.updateNotificationContent(notification_content)
		}

		val allowTargetedSelection = Config.singularHasResult
		if (com.pdffox.adv.Config.isTest && !allowTargetedSelection) {
			Log.e(TAG, "update: Singular result unavailable, keep default routing")
		}
		RemoteConfigRouting.apply(
			remoteConfig = remoteConfig,
			adMapping = adMapping,
			source = "RemoteConfig.update",
			// Singular 未返回前不做国家/品牌/自然量定向选择，避免启动早期选错路由。
			allowTargetedSelection = allowTargetedSelection
		)

		// guide_page_swap_time：引导页自动切换时间，单位毫秒。
		NativeConfig.guide_page_swap_time = remoteConfig.getLong("guide_page_swap_time")
		if(com.pdffox.adv.Config.isTest) {
			Log.e(TAG, "update: guide_page_swap_time = ${NativeConfig.guide_page_swap_time}")
		}

		// native_refresh_time：Native 刷新间隔，下发单位秒，内部使用毫秒。
		val native_refresh_time = remoteConfig.getLong("native_refresh_time")
		if (native_refresh_time > 0) {
			NativeConfig.native_refresh_time = native_refresh_time * 1000
		}
		if(com.pdffox.adv.Config.isTest) {
			Log.e(TAG, "update: native_refresh_time = ${NativeConfig.native_refresh_time}")
		}

		// native_ad_ids：Native 高/中/低价广告位 ID 队列 JSON。
		val str_native_ad_ids = remoteConfig.getString("native_ad_ids")
		if(com.pdffox.adv.Config.isTest) {
			Log.e(TAG, "update: str_native_ad_ids = $str_native_ad_ids")
		}
		AdvIDs.setNativeIDs(str_native_ad_ids)

		// native_ad_policy：Native 广告策略 JSON。
		val str_native_ad_policy = remoteConfig.getString("native_ad_policy")
		if(com.pdffox.adv.Config.isTest) {
			Log.e(TAG, "update: str_native_ad_policy = $str_native_ad_policy")
		}
		NativePolicyManager.setPolicyFromJson(str_native_ad_policy)

		// log_time：日志/归因等待窗口，单位小时；0 时回退 48。
		Config.log_time = remoteConfig.getLong("log_time")
		if (Config.log_time == 0L) {
			Config.log_time = 48
		}
		if(com.pdffox.adv.Config.isTest) {
			Log.e(TAG, "update: log_time = ${Config.log_time}")
		}
	}

}
