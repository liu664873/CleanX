package com.pdffox.adv.push

/** 宿主 Push 组件命名约定。 */
object PushHostContract {
	private const val DEFAULT_FILE_PROVIDER_SUFFIX = ".pdffox.adv.fileprovider"
	/** SDK 默认持久服务类名。 */
	const val DEFAULT_COMMON_SERVICE_CLASS_NAME = "com.pdffox.adv.notification.CommonService"

	/** 通知删除广播 action；宿主未配置时使用包名派生值。 */
	fun notificationDeletedAction(packageName: String, configuredAction: String?): String {
		return configuredAction?.takeIf { it.isNotBlank() }
			?: "$packageName.ACTION_NOTIFICATION_DELETED"
	}

	/** FileProvider authority；宿主未配置时使用包名加固定后缀。 */
	fun fileProviderAuthority(packageName: String, configuredAuthority: String?): String {
		return configuredAuthority?.takeIf { it.isNotBlank() }
			?: packageName + DEFAULT_FILE_PROVIDER_SUFFIX
	}
}
