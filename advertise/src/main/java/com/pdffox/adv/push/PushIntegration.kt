package com.pdffox.adv.push

import android.content.Context
import android.content.Intent
import com.pdffox.adv.Ads
import com.pdffox.adv.Config
import com.pdffox.adv.notification.CommonService

/** Push 相关宿主集成适配器。 */
object PushIntegration {
	/** 构造持久服务启动 Intent，支持宿主通过配置替换服务类名。 */
	fun commonServiceIntent(context: Context): Intent? {
		if (!Config.sdkConfig.push.enabled || !Config.sdkConfig.push.persistentServiceEnabled) {
			return null
		}
		val className = Config.sdkConfig.push.commonServiceClassName?.takeIf { it.isNotBlank() }
		return if (className == null) {
			CommonService.intent(context)
		} else {
			Intent().apply {
				setClassName(context.packageName, className)
			}
		}
	}

	/** 获取通知删除广播 action。 */
	fun notificationDeletedAction(context: Context): String {
		return PushHostContract.notificationDeletedAction(
			packageName = context.packageName,
			configuredAction = Config.sdkConfig.push.notificationDeletedAction,
		)
	}

	/** 获取通知图片 FileProvider authority。 */
	fun fileProviderAuthority(context: Context): String {
		return PushHostContract.fileProviderAuthority(
			packageName = context.packageName,
			configuredAuthority = Config.sdkConfig.push.fileProviderAuthority,
		)
	}

	/** 构造回到宿主 App 的启动 Intent，并携带来源、route 和 scene。 */
	fun appLaunchIntent(
		context: Context,
		appOpenFrom: String,
		route: String = "",
		scene: String = "",
	): Intent? {
		return Ads.createLaunchIntent(context, appOpenFrom, route, scene)
	}
}
