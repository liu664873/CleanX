package com.pdffox.adv.adv

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.pdffox.adv.Config
import com.pdffox.adv.util.PreferenceUtil
import org.json.JSONObject

/**
 * 广告加载策略配置。
 *
 * 这些值主要由 Remote Config 的广告预加载配置更新，控制缓存时间、重试次数、池大小和不同触发时机是否允许预加载。
 */
object AdConfig {
	private const val TAG = "AdConfig"
	// 是否允许 App Open 广告辅助器工作。
	var isOpenAppOpenHelper = true

	// 广告缓存有效期；本地单位毫秒，Remote Config 下发单位秒。
	var adload_cache_time: Long = 60 * 60 * 1000
	// 广告加载失败后的重试次数。
	var adload_retry_num: Int = 1
	// 广告最大加载时长；本地单位毫秒，Remote Config 下发单位秒。
	var adload_max_time: Long = 6 * 1000
	// 开屏广告预加载池大小。
	var adload_poolsize_open: Int = 1
	// 插屏广告预加载池大小。
	var adload_poolsize_inter: Int = 1
	// Native 广告预加载池大小。
	var adload_poolsize_native: Int = 3

	// 打开 App 时触发预加载。
	val LOAD_TIME_OPEN_APP = "open_app"
	// 广告播放结束时触发预加载。
	val LOAD_TIME_PLAY_FINISH = "play_finish"
	// App 进入后台时触发预加载。
	val LOAD_TIME_ENTER_BACKGROUND = "enter_background"
	// 收到通知时触发预加载。
	val LOAD_TIME_RECEIVE_NOTIFICATION = "receive_notification"

	// 进入功能页时触发预加载。
	val LOAD_TIME_ENTER_FEATURE = "enter_features"

	// 开屏广告在各触发时机下是否允许预加载。
	var adload_trigger_timing_open: Map<String, Boolean> = mapOf(
		LOAD_TIME_OPEN_APP to false,
		LOAD_TIME_PLAY_FINISH to true,
		LOAD_TIME_ENTER_BACKGROUND to false,
		LOAD_TIME_RECEIVE_NOTIFICATION to false
	)
	// 插屏广告在各触发时机下是否允许预加载。
	var adload_trigger_timing_inter: Map<String, Boolean> = mapOf(
		LOAD_TIME_OPEN_APP to false,
		LOAD_TIME_PLAY_FINISH to true,
		LOAD_TIME_ENTER_BACKGROUND to false,
		LOAD_TIME_RECEIVE_NOTIFICATION to false,
		LOAD_TIME_ENTER_FEATURE to false
	)

	// Native 广告在各触发时机下是否允许预加载。
	var adload_trigger_timing_native: Map<String, Boolean> = mapOf(
		LOAD_TIME_OPEN_APP to false,
		LOAD_TIME_PLAY_FINISH to true,
		LOAD_TIME_ENTER_BACKGROUND to false,
		LOAD_TIME_RECEIVE_NOTIFICATION to false,
		LOAD_TIME_ENTER_FEATURE to false
	)

	/** 从宿主配置的本地 raw 资源读取兜底广告预加载策略。 */
	fun loadConfigFromLocal(context: Context) {
		val resId = Config.resourceConfig.adLoadConfigRawResId
		if (resId == 0) {
			return
		}
		val strConfig = context.resources.openRawResource(resId).bufferedReader().use { it.readText() }
		updateConfigFromJson(strConfig)
	}

	/**
	 * 从 Remote Config JSON 更新广告加载策略。
	 *
	 * 远程配置中的时间字段单位为秒，写入本地时统一换算为毫秒。
	 */
	fun updateConfigFromJson(strJson: String) {
		if (com.pdffox.adv.Config.isTest) {
			Log.e(TAG, "updateConfigFromJson: $strJson")
			PreferenceUtil.commitString("updateConfigFromJson", strJson)
		}
		try {
			val jb = JSONObject(strJson)
			adload_cache_time = jb.optLong("adload_cache_time", adload_cache_time) * 1000
			adload_retry_num = jb.optInt("adload_retry_num", adload_retry_num)
			adload_max_time = jb.optLong("adload_max_time", adload_max_time) * 1000
			adload_poolsize_open = jb.optInt("adload_poolsize_open", adload_poolsize_open)
			adload_poolsize_inter = jb.optInt("adload_poolsize_inter", adload_poolsize_inter)
			adload_poolsize_native = jb.optInt("adload_poolsize_native",adload_poolsize_native)
			adload_trigger_timing_open = jb.optJSONObject("adload_trigger_timing_open")?.let {
				Gson().fromJson(it.toString(), Map::class.java) as Map<String, Boolean>
			} ?: emptyMap()
			adload_trigger_timing_inter = jb.optJSONObject("adload_trigger_timing_inter")?.let {
				Gson().fromJson(it.toString(), Map::class.java) as Map<String, Boolean>
			} ?: emptyMap()
			adload_trigger_timing_native = jb.optJSONObject("adload_trigger_timing_native")?.let {
				Gson().fromJson(it.toString(), Map::class.java) as Map<String, Boolean>
			} ?: emptyMap()

		} catch (e: Exception) {
			e.printStackTrace()
		}
	}

	/** 当前触发时机是否允许预加载 App Open 广告。 */
	fun canLoadOpen(loadTimeKey: String): Boolean {
		val canLoadOpen = adload_trigger_timing_open[loadTimeKey] ?: false
		Log.e(TAG, "canLoadOpen:$canLoadOpen  $loadTimeKey, ${adload_trigger_timing_open.keys}")
		return canLoadOpen
	}

	/** 当前触发时机是否允许预加载插屏广告。 */
	fun canLoadInter(loadTimeKey: String): Boolean {
		val canLoadInter = isInterstitialLoadAllowedByPolicy(loadTimeKey)
		Log.e(TAG, "canLoadInter:$canLoadInter  $loadTimeKey, ${adload_trigger_timing_inter.keys}")
		return canLoadInter
	}

	internal fun isInterstitialLoadAllowedByPolicy(loadTimeKey: String): Boolean {
		return Config.isTest || (adload_trigger_timing_inter[loadTimeKey] ?: false)
	}

	/** 当前触发时机是否允许预加载 Native 广告。 */
	fun canLoadNative(loadTimeKey: String): Boolean {
		val canLoadNative = adload_trigger_timing_native[loadTimeKey] ?: false
		Log.e(TAG, "canLoadNative:$canLoadNative  $loadTimeKey, ${adload_trigger_timing_native.keys}")
		return canLoadNative
	}
}
