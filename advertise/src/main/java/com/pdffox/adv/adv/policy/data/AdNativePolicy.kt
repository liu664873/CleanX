package com.pdffox.adv.adv.policy.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString

/**
 * Native 广告策略配置，对应 Remote Config 的 native_ad_policy。
 *
 * @property limited Native 广告全局频控次数。
 * @property limited_loadtime_seconds Native 广告全局频控窗口，单位秒。
 * @property ad_units Native 页面广告位策略列表。
 */
@Serializable
data class AdNativePolicy(
	val limited: Int,
	val limited_loadtime_seconds: Int,
	var ad_units: List<AdUnit>
)

/** 解析 Native 广告策略 JSON。 */
fun parseAdNativePolicy(jsonString: String): AdNativePolicy {
	return adPolicyJson.decodeFromString(jsonString)
}
