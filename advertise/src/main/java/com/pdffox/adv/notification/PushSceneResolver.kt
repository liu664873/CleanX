package com.pdffox.adv.notification

import java.util.Locale

/** 解析后的 Push 展示文本。 */
internal data class PushMessageText(
	val title: String,
	val content: String,
	val route: String,
)

/** 上下文化 Push 场景和多语言文案解析器。 */
internal object PushSceneResolver {
	/** 根据场景 key 获取对应 Push 场景配置。 */
	fun scene(config: PushConfig?, sceneKey: String?): PushScene? {
		val key = sceneKey?.takeIf { it.isNotBlank() } ?: return null
		return config?.scene?.get(key)
	}

	/** 获取场景下第一条消息，并按当前语言优先使用本地化标题和正文。 */
	fun firstMessageText(
		config: PushConfig?,
		sceneKey: String?,
		locale: Locale = Locale.getDefault(),
	): PushMessageText? {
		val message = scene(config, sceneKey)?.messages?.firstOrNull() ?: return null
		val localized = message.keys.firstOrNull { it.language.equals(locale.language, ignoreCase = true) }
		return PushMessageText(
			title = localized?.title?.takeIf { it.isNotBlank() } ?: message.title,
			content = localized?.content?.takeIf { it.isNotBlank() } ?: message.content,
			route = message.route,
		)
	}
}
