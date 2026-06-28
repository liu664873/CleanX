package com.pdffox.adv.notification

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

/**
 * 通知触发策略配置，对应 Remote Config 的 notification_config。
 *
 * @property 24HMax 24 小时内最多发送的通知数量。
 * @property each_trigger_sent 每个触发器允许发送的次数。
 * @property NMax 通知总发送上限。
 * @property is_foreground_send App 前台时是否允许发送通知。
 * @property triggers 系统/业务事件触发器列表。
 * @property timer 定时通知配置列表。
 */
@Serializable
data class NotificationConfig(
	val `24HMax`: Int,
	val `1HMax`: Int = Int.MAX_VALUE,
	val each_trigger_sent: Int,
	val NMax: Int,
	val is_foreground_send: Boolean,
	val interval_second: Long? = null,
	val triggers: List<Trigger>,
	val timer: List<Timer>
)

/**
 * 通知触发器配置。
 *
 * @property name 触发器名称，必须与 SDK 内部触发场景匹配。
 * @property delay 收到触发后延迟发送秒数。
 */
@Serializable
data class Trigger(
	val id: String = "",
	val name: String,
	val delay: Long? = null
)

/**
 * 定时通知配置。
 *
 * @property HH 定时发送小时，范围 0-23。
 * @property MM 定时发送分钟，范围 0-59。
 */
@Serializable
data class Timer(
	val id: String = "",
	val name: String = "",
	val HH: Int,
	val MM: Int
)

/**
 * 通知内容配置，对应 Remote Config 的 notification_content 数组元素。
 *
 * @property Title 默认通知标题。
 * @property Content 默认通知正文。
 * @property Button 默认按钮文案。
 * @property Languages 多语言 JSON 字符串，后续按 Languages 结构解析。
 * @property Route 通知点击原始路由。
 */
@Serializable
data class Notice(
	val Id: Int? = null,
	val AppName: String? = null,
	val AppPackage: String? = null,
	val Policy: Int? = null,
	val NoticeId: String? = null,
	val Title: String,
	val Content: String,
	val Button: String,
	val Icon: String? = null,
	val Img: String? = null,
	val Languages: String, // 这里先用 String，后续按需再解析成 Languages。
	val Route: String
)

/**
 * 通知多语言内容。
 *
 * @property language 语言码，例如 zh、en、ja、ko。
 * @property title 对应语言的通知标题。
 * @property content 对应语言的通知正文。
 * @property button 对应语言的按钮文案。
 */
@Serializable
data class LanguageKey(
	val language: String,
	val title: String,
	val content: String,
	val img: String = "",
	val button: String
)

/**
 * 多语言内容根结构。
 *
 * @property keys 多语言条目列表。
 */
@Serializable
data class Languages(
	val keys: List<LanguageKey>
)

private val notificationJson = Json {
	ignoreUnknownKeys = true
	explicitNulls = false
	coerceInputValues = true
}

/** 解析通知触发策略 JSON。 */
fun parseNotificationConfig(jsonString: String): NotificationConfig {
	return notificationJson.decodeFromString(jsonString)
}

/** 解析通知内容列表 JSON。 */
fun parseNotificationContents(jsonString: String): List<Notice> {
	return notificationJson.decodeFromString(jsonString)
}

/** 解析单条通知的多语言内容 JSON。 */
fun parseLanguages(jsonString: String): Languages {
	return notificationJson.decodeFromString(jsonString)
}
