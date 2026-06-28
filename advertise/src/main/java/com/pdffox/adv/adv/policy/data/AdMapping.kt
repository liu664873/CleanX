package com.pdffox.adv.adv.policy.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

/**
 * 远程配置路由规则，对应 Remote Config 的 ad_mapping。
 *
 * @property config 默认配置标签。
 * @property nature_config 自然量/Google IP/付费用户等特殊流量配置标签。
 * @property configs 按国家/品牌命中的定向配置列表。
 */
@Serializable
data class AdMapping(
	val config: Config = Config(),
	val nature_config: Config = Config(),
	val configs: List<ConfigItem> = emptyList()
)

/**
 * 一组远程配置标签。
 *
 * @property ad 广告策略 tag；为空时回退默认 ad_policy。
 * @property notification 通知策略 tag；为空时回退默认 notification_config。
 * @property fcm_topic FCM topic。
 * @property preload 预加载策略 tag；为空时回退默认 adload_config。
 */
@Serializable
data class Config(
	val ad: String = "",
	val notification: String = "",
	val fcm_topic: String = "all",
	val preload: String = ""
)

/**
 * 定向配置命中项。
 *
 * @property countrys 国家/地区命中条件；字段名沿用后端 countrys 拼写。
 * @property brands 设备品牌命中条件。
 * @property config 命中后的配置标签。
 */
@Serializable
data class ConfigItem(
	val countrys: List<String> = emptyList(),
	val brands: List<String> = emptyList(),
	val config: Config = Config()
)

private val adMappingJson = Json {
	ignoreUnknownKeys = true
	explicitNulls = false
	coerceInputValues = true
}

/** 解析 Remote Config 下发的 ad_mapping JSON，空字符串或解析失败时返回 null。 */
fun parseAdMapping(jsonString: String): AdMapping? {
	if (jsonString.isBlank()) {
		return null
	}
	return runCatching {
		adMappingJson.decodeFromString<AdMapping>(jsonString)
	}.getOrNull()
}
