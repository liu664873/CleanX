package com.pdffox.adv.adv.policy.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

/**
 * 广告策略配置，对应 Remote Config 的 ad_policy 或本地 ad_policy.json。
 *
 * @property package_name 策略适用包名；为空时不校验，非空时必须等于宿主 applicationId。
 * @property global_ad_switch 广告全局总开关。
 * @property limited 全局广告展示/加载频控次数。
 * @property limited_loadtime_seconds 全局频控窗口，单位秒。
 * @property first_open_enabled 是否启用首次开屏广告。
 * @property ad_units 页面广告位策略列表。
 */
@Serializable
data class AdPolicy(
	val package_name: String,
	val global_ad_switch: Boolean,
	val limited: Int,
	val limited_loadtime_seconds: Int,
	val first_open_enabled: Boolean,
	var ad_units: List<AdUnit>
)

/**
 * 单个广告位策略。
 *
 * @property areakey 页面广告位 key，必须与宿主调用传入的 areaKey 一致。
 * @property rate 广告展示/加载概率，取值通常为 0.0-1.0。
 * @property frequency_caps 单广告位频控配置。
 */
@Serializable
data class AdUnit(
	val areakey: String,
	val rate: Double,
	val frequency_caps: FrequencyCaps
)

/**
 * 单广告位频控配置。
 *
 * @property max_per_hour 每小时最多展示次数。
 * @property max_per_day 每天最多展示次数。
 * @property interval_seconds 两次展示之间的最小间隔，单位秒。
 */
@Serializable
data class FrequencyCaps(
	val max_per_hour: Int,
	val max_per_day: Int,
	val interval_seconds: Long
)

internal val adPolicyJson = Json {
	ignoreUnknownKeys = true
	explicitNulls = false
	coerceInputValues = true
}

/** 解析全屏/Banner 广告策略 JSON。 */
fun parseAdPolicy(jsonString: String): AdPolicy {
	return adPolicyJson.decodeFromString(jsonString)
}
