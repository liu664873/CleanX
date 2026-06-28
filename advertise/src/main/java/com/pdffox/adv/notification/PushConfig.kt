package com.pdffox.adv.notification

import kotlinx.serialization.Serializable

/**
 * 上下文化 Push 配置，对应 Remote Config 的 Contextualized_Push 或本地 push.json。
 *
 * @property first_trigger_time 首次触发前的等待时间，单位秒。
 * @property scene 场景配置映射，key 必须与 PushSceneKeyConfig 保持一致。
 */
@Serializable
data class PushConfig(
	var first_trigger_time: Long = 0L,
	var scene: Map<String, PushScene> = emptyMap(),
)

/**
 * 单个 Push 场景配置。
 *
 * @property enabled 该场景是否启用。
 * @property messages 该场景的消息列表；当前 SDK 只使用第一条消息。
 * @property trigger_interval 同一场景两次触发的最小间隔，单位秒。
 */
@Serializable
data class PushScene(
	var enabled: Boolean = false,
	var messages: List<Message> = emptyList(),
	var trigger_interval: Long = 0L
)

/**
 * Push 消息内容。
 *
 * @property content 默认通知正文。
 * @property keys 多语言内容列表。
 * @property route 通知点击原始路由。
 * @property title 默认通知标题。
 */
@Serializable
data class Message(
	var content: String = "",
	var keys: List<Key> = emptyList(),
	var route: String = "",
	var title: String = ""
)

/**
 * Push 多语言内容。
 *
 * @property content 对应语言正文。
 * @property language 语言码，例如 zh、en、ja、ko。
 * @property title 对应语言标题。
 */
@Serializable
data class Key(
	var content: String = "",
	var language: String = "",
	var title: String = ""
)
