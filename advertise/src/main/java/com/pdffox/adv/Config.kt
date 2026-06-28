package com.pdffox.adv

import com.pdffox.adv.log.LogAdParam
import com.pdffox.adv.util.PreferenceDelegate

/**
 * 广告库全局运行配置。
 *
 * 这里保存初始化后生效的 SDK 配置、远程配置结果、归因状态和服务端 URL 派生值。
 */
object Config {

	// 当前生效的 SDK 总配置，由 AdvertiseSdk.init/applySdkConfig 写入。
	var sdkConfig: AdvertiseSdkConfig = AdvertiseSdkConfig()
		private set

	// 是否测试环境；影响 Host、topic、安全校验和日志行为。
	var isTest = false

	// Remote Config 下发的配置版本号。
	var update_version: Long = 0

	// review/自然量相关路由开关。
	var openReview = false

	// 当前国家/地区代码，默认 US。
	var country = "US"

	// 当前 FCM topic。
	var topic = defaultTopic()

	// Singular 归因是否已有结果。
	var singularHasResult : Boolean by PreferenceDelegate("singularHasResult", false)

	// 当前用户是否自然量。
	var isNature: Boolean by PreferenceDelegate("isNature", false)

	// Remote Config 是否已有拉取/应用结果。
	var remoteConfigHasResult = false

	// paid_0 归因结果状态。
	var paid0HasResult = false
	// paid_0 用户标记，true 时通常需要抑制部分广告。
	var paid_0 = true

	// IP 检查结果状态。
	var ipCheckHasResult : Boolean by PreferenceDelegate("ipCheckHasResult", false)
	// 当前 IP 是否属于 Google。
	var isGoogleIP: Boolean by PreferenceDelegate("isGoogleIP", false)

	// 是否启用 AdMob mediation adapter 初始化。
	var openAdmobMediation = true

	// 日志/归因等待窗口，单位小时。
	var log_time: Long = 48


	/** 兼容旧入口：单独控制 AdMob mediation adapter 是否初始化。 */
	fun setConfig(openAdmobMediation: Boolean) {
		this.openAdmobMediation = openAdmobMediation
	}

	// 当前宿主包名。
	var packageName = sdkConfig.packageName.orEmpty()

	// 当前 SDK 本地资源配置。
	val resourceConfig: AdvertiseResourcesConfig
		get() = sdkConfig.resources

	/**
	 * 写入宿主传入的 SDK 配置，并刷新包名与默认 topic。
	 *
	 * @param config 新的完整配置。
	 * @param fallbackPackageName 当配置未指定包名时使用的宿主包名。
	 */
	fun applySdkConfig(config: AdvertiseSdkConfig, fallbackPackageName: String? = null) {
		sdkConfig = config
		packageName = config.packageName ?: fallbackPackageName ?: packageName
		val requestedDefaultTopic = config.defaultTopic?.takeIf { it.isNotBlank() }
		if (requestedDefaultTopic != null) {
			topic = requestedDefaultTopic
		} else if (topic.isBlank() || topic == "all" || topic == "debug-all") {
			topic = defaultTopic()
		}
	}

	/** 当前是否至少有一个广告网络开启。 */
	fun hasEnabledAdNetwork(): Boolean {
		return sdkConfig.adMob.enabled
	}

	/** 当前激活的广告平台名称，用于广告埋点参数。 */
	fun activeAdPlatform(): String? {
		return if (sdkConfig.adMob.enabled) LogAdParam.ad_platform_admob else null
	}

	/** 根据测试/正式环境返回默认 FCM topic。 */
	private fun defaultTopic(): String {
		return if (isTest) "debug-all" else "all"
	}

	// 以下属性为旧代码保留 PascalCase 命名，实际值都来自 sdkConfig。
	val PrivacyUrl: String
		get() = sdkConfig.privacyUrl
	val TermsUrl: String
		get() = sdkConfig.termsUrl

	val ThinkingKey: String
		get() = sdkConfig.thinking.appKey
	val ThinkkingUrl: String
		get() = sdkConfig.thinking.serverUrl

	val Singular_Api_Key: String
		get() = sdkConfig.singular.apiKey
	val Singular_Secret: String
		get() = sdkConfig.singular.secret

	val TestADVHost: String
		get() = sdkConfig.server.testHost
	val ADVHost: String
		get() = sdkConfig.server.releaseHost

	// 当前使用的服务端 Host：测试环境优先 testHost，否则使用 releaseHost。
	val host: String
		get() = if (isTest && TestADVHost.isNotBlank()) TestADVHost else ADVHost
//		get() =  ADVHost

	// 服务端能力是否可用。
	val isServerEnabled: Boolean
		get() = sdkConfig.server.enabled && host.isNotBlank()

	// 服务端接口完整 URL；当服务端能力关闭时 endpoint 会统一返回空字符串。
	val IPInfoV2Url: String
		get() = endpoint(sdkConfig.server.ipInfoV2Path)

	val PushUrl: String
		get() = endpoint(sdkConfig.server.pushPath)

	val ParseTokenUrl: String
		get() = endpoint(sdkConfig.server.parseTokenPath)

	/** 拼接服务端 Host 和接口 path，并统一处理斜杠。 */
	private fun endpoint(path: String): String {
		if (!isServerEnabled) {
			return ""
		}
		val normalizedPath = if (path.startsWith("/")) path else "/$path"
		return host.trimEnd('/') + normalizedPath
	}

}
