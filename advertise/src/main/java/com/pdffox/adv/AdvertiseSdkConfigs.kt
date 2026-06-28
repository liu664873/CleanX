package com.pdffox.adv

import android.content.Context

object AdvertiseSdkConfigs {
	/**
	 * 使用当前 Context 包名创建 SDK 配置。
	 *
	 * @param context 宿主 Context，用于读取 packageName。
	 * @param isDebug 当前是否 debug 环境；会影响默认 topic 和安全校验强度。
	 * @param configure 配置 DSL。
	 */
	fun create(
		context: Context,
		isDebug: Boolean,
		configure: AdvertiseSdkConfigBuilder.() -> Unit,
	): AdvertiseSdkConfig {
		return create(context.packageName, isDebug, configure)
	}

	/**
	 * 使用指定包名创建 SDK 配置。
	 *
	 * @param packageName 宿主包名；用于策略包名匹配、安全校验和第三方后台一致性校验。
	 * @param isDebug 当前是否 debug 环境。
	 * @param configure 配置 DSL。
	 */
	fun create(
		packageName: String,
		isDebug: Boolean,
		configure: AdvertiseSdkConfigBuilder.() -> Unit,
	): AdvertiseSdkConfig {
		return AdvertiseSdkConfigBuilder(packageName, isDebug)
			.apply(configure)
			.build()
	}
}

class AdvertiseSdkConfigBuilder internal constructor(
	private val packageName: String,
	private val isDebug: Boolean,
) {
	private val defaults = AdvertiseSdkConfig(packageName = packageName)
	private var privacyUrl = defaults.privacyUrl
	private var termsUrl = defaults.termsUrl
	private var defaultTopic = defaults.defaultTopic.orEmpty()
	private var resources = defaults.resources
	private var server = defaults.server
	private var firebase = defaults.firebase
	private var remoteConfig = defaults.remoteConfig
	private var thinking = defaults.thinking
	private var singular = defaults.singular
	private var adMob = defaults.adMob
	private var facebook = defaults.facebook
	private var tiktok = defaults.tiktok
	private var safe = defaults.safe
	private var push = defaults.push
	private var notifications = defaults.notifications
	private var playIntegrity = defaults.playIntegrity

	/**
	 * 配置合规链接。
	 *
	 * @param privacyUrl 隐私政策 URL。
	 * @param termsUrl 用户协议 URL。
	 */
	fun legal(
		privacyUrl: String,
		termsUrl: String,
	) {
		this.privacyUrl = privacyUrl
		this.termsUrl = termsUrl
	}

	/**
	 * 配置默认 FCM topic；为空时 debug 使用 debug-all，release 使用 all。
	 */
	fun defaultTopic(value: String) {
		defaultTopic = value
	}

	/**
	 * 配置 SDK 本地兜底资源。
	 *
	 * @param adPolicyRawResId 本地广告策略 JSON。
	 * @param adLoadConfigRawResId 本地广告预加载策略 JSON。
	 * @param nativeAdPolicyRawResId 本地 Native 广告策略 JSON。
	 * @param nativeAdIdsRawResId 本地 Native 广告位 ID 队列 JSON。
	 * @param cloudCidrsRawResId Google Cloud CIDR 列表。
	 * @param googleCidrsRawResId Google CIDR 列表。
	 * @param pushConfigRawResId 本地上下文化 Push JSON；0 表示不加载。
	 */
	fun resources(
		adPolicyRawResId: Int = R.raw.ad_policy,
		adLoadConfigRawResId: Int = 0,
		nativeAdPolicyRawResId: Int = 0,
		nativeAdIdsRawResId: Int = 0,
		cloudCidrsRawResId: Int = R.raw.cloud,
		googleCidrsRawResId: Int = R.raw.google,
		pushConfigRawResId: Int = 0,
	) {
		resources = AdvertiseResourcesConfig(
			adPolicyRawResId = adPolicyRawResId,
			adLoadConfigRawResId = adLoadConfigRawResId,
			nativeAdPolicyRawResId = nativeAdPolicyRawResId,
			nativeAdIdsRawResId = nativeAdIdsRawResId,
			cloudCidrsRawResId = cloudCidrsRawResId,
			googleCidrsRawResId = googleCidrsRawResId,
			pushConfigRawResId = pushConfigRawResId,
		)
	}

	/**
	 * 配置服务端接口。
	 *
	 * @param enabled 是否启用服务端能力。
	 * @param releaseHost 生产服务端 Host。
	 * @param testHost 测试服务端 Host。
	 * @param parseTokenKey Play Integrity token 解析密钥。
	 * @param ipInfoV2Path 新版 IP 信息接口路径。
	 * @param pushPath 推送/上报接口路径。
	 * @param parseTokenPath Play Integrity token 解析接口路径。
	 */
	fun server(
		enabled: Boolean,
		releaseHost: String,
		testHost: String,
		parseTokenKey: String,
		ipInfoV2Path: String = "/getIpInfoV2",
		pushPath: String = "/publish",
		parseTokenPath: String = "/parseToken",
	) {
		server = AdvertiseServerConfig(
			enabled = enabled,
			releaseHost = releaseHost,
			testHost = testHost,
			ipInfoV2Path = ipInfoV2Path,
			pushPath = pushPath,
			parseTokenPath = parseTokenPath,
			parseTokenKey = parseTokenKey,
		)
	}

	/**
	 * 配置 Firebase 能力。
	 *
	 * @param analyticsEnabled 是否启用 Firebase Analytics。
	 * @param messagingEnabled 是否启用 Firebase Messaging/FCM。
	 * @param subscribeDefaultTopic 是否订阅默认 topic。
	 */
	fun firebase(
		analyticsEnabled: Boolean,
		messagingEnabled: Boolean,
		subscribeDefaultTopic: Boolean,
	) {
		firebase = FirebaseConfig(
			analyticsEnabled = analyticsEnabled,
			messagingEnabled = messagingEnabled,
			subscribeDefaultTopic = subscribeDefaultTopic,
		)
	}

	/**
	 * 配置 Firebase Remote Config 是否启用。
	 */
	fun remoteConfig(enabled: Boolean) {
		remoteConfig = RemoteConfigFeatureConfig(enabled = enabled)
	}

	/**
	 * 配置 ThinkingData。
	 *
	 * @param enabled 是否启用 ThinkingData。
	 * @param appKey ThinkingData App Key。
	 * @param serverUrl ThinkingData 上报地址。
	 */
	fun thinking(
		enabled: Boolean,
		appKey: String,
		serverUrl: String,
	) {
		thinking = ThinkingConfig(
			enabled = enabled,
			appKey = appKey,
			serverUrl = serverUrl,
		)
	}

	/**
	 * 配置 Singular。
	 *
	 * @param enabled 是否启用 Singular 归因。
	 * @param apiKey Singular API Key。
	 * @param secret Singular Secret；敏感信息。
	 */
	fun singular(
		enabled: Boolean,
		apiKey: String,
		secret: String,
	) {
		singular = SingularConfigValues(
			enabled = enabled,
			apiKey = apiKey,
			secret = secret,
		)
	}

	/**
	 * 配置 AdMob 广告位。
	 *
	 * @param enabled 是否启用 AdMob。
	 * @param appId AdMob App ID，必须与 Manifest meta-data 一致。
	 * @param bannerId Banner 广告位 ID。
	 * @param interstitialId 插屏广告位 ID。
	 * @param nativeId Native 广告位 ID。
	 * @param openId App Open 广告位 ID。
	 * @param debugNativeIdsJson debug 模式 Native 广告位队列 JSON。
	 */
	fun adMob(
		enabled: Boolean,
		appId: String,
		bannerId: String,
		interstitialId: String,
		nativeId: String,
		openId: String,
		nativeIdsJson: String = DEFAULT_NATIVE_IDS_JSON,
		debugNativeIdsJson: String = DEFAULT_DEBUG_NATIVE_IDS_JSON,
	) {
		adMob = AdMobConfig(
			enabled = enabled,
			appId = appId,
			bannerId = bannerId,
			interstitialId = interstitialId,
			nativeId = nativeId,
			openId = openId,
			nativeIdsJson = nativeIdsJson,
			debugNativeIdsJson = debugNativeIdsJson,
		)
	}

	/**
	 * 配置 Facebook SDK。
	 *
	 * @param enabled 是否启用 Facebook SDK/归因能力。
	 * @param appId Facebook App ID。
	 * @param clientToken Facebook Client Token；敏感信息。
	 * @param advertiserIdCollectionEnabled 是否允许收集广告 ID。
	 */
	fun facebook(
		enabled: Boolean,
		appId: String,
		clientToken: String,
		advertiserIdCollectionEnabled: Boolean = true,
	) {
		facebook = FacebookConfig(
			enabled = enabled,
			appId = appId,
			clientToken = clientToken,
			advertiserIdCollectionEnabled = advertiserIdCollectionEnabled,
		)
	}

	/**
	 * 配置 TikTok Business SDK。
	 *
	 * @param enabled 是否启用 TikTok。
	 * @param accessToken TikTok Access Token。
	 * @param ttAppId TikTok 后台 TT App ID。
	 * @param appId TikTok 应用 ID；通常与宿主包名一致。
	 * @param startTrackOnInit 初始化时是否立即开始追踪。
	 */
	fun tiktok(
		enabled: Boolean,
		accessToken: String,
		ttAppId: String,
		appId: String?,
		startTrackOnInit: Boolean = enabled,
	) {
		tiktok = TikTokConfig(
			enabled = enabled,
			accessToken = accessToken,
			ttAppId = ttAppId,
			appId = appId?.takeIf { it.isNotBlank() },
			startTrackOnInit = startTrackOnInit,
		)
	}

	/**
	 * 配置安全校验。
	 *
	 * @param enabled 是否启用安全校验。
	 * @param expectedSignatures 允许的签名 SHA-256，支持逗号、分号或换行分隔。
	 * @param rejectDebuggableBuilds 是否拒绝 debuggable 包。
	 * @param rejectDebuggerAttached 是否拒绝已连接调试器。
	 * @param killProcessOnFailure 校验失败时是否杀进程。
	 * @param expectedPackageName 期望包名；为空时使用宿主包名。
	 * @param enforceInDebugBuilds debug 包是否也执行强校验。
	 */
	fun safe(
		enabled: Boolean,
		expectedSignatures: String,
		rejectDebuggableBuilds: Boolean = true,
		rejectDebuggerAttached: Boolean = true,
		killProcessOnFailure: Boolean = true,
		expectedPackageName: String? = null,
		enforceInDebugBuilds: Boolean = false,
	) {
		val shouldEnforce = !isDebug || enforceInDebugBuilds
		safe = SafeConfig(
			enabled = enabled,
			expectedPackageName = expectedPackageName,
			expectedSignatures = if (shouldEnforce) parseSignatures(expectedSignatures) else emptySet(),
			rejectDebuggableBuilds = shouldEnforce && rejectDebuggableBuilds,
			rejectDebuggerAttached = shouldEnforce && rejectDebuggerAttached,
			killProcessOnFailure = shouldEnforce && killProcessOnFailure,
		)
	}

	/**
	 * 配置 Push 和后台组件。
	 *
	 * @param enabled Push 总开关。
	 * @param persistentServiceEnabled 是否启用持久前台服务。
	 * @param firebaseMessagingServiceEnabled 是否启用 FCM service。
	 * @param serviceStarterJobEnabled 是否启用 JobService 拉起服务。
	 * @param bootReceiverEnabled 是否启用开机广播接收器。
	 * @param notificationDeletedReceiverEnabled 是否启用通知删除广播接收器。
	 * @param fileProviderEnabled 是否启用 FileProvider。
	 * @param deletionObserverEnabled 是否启用删除观察能力。
	 * @param sceneKeys Push 场景 key 映射。
	 * @param commonServiceClassName 自定义持久服务类名。
	 * @param notificationDeletedAction 自定义通知删除 action。
	 * @param fileProviderAuthority 自定义 FileProvider authority。
	 */
	fun push(
		enabled: Boolean,
		persistentServiceEnabled: Boolean,
		firebaseMessagingServiceEnabled: Boolean,
		serviceStarterJobEnabled: Boolean,
		bootReceiverEnabled: Boolean,
		notificationDeletedReceiverEnabled: Boolean,
		fileProviderEnabled: Boolean,
		deletionObserverEnabled: Boolean,
		sceneKeys: PushSceneKeyConfig = PushSceneKeyConfig(),
		commonServiceClassName: String? = null,
		notificationDeletedAction: String? = null,
		fileProviderAuthority: String? = null,
	) {
		push = PushConfig(
			enabled = enabled,
			persistentServiceEnabled = persistentServiceEnabled,
			firebaseMessagingServiceEnabled = firebaseMessagingServiceEnabled,
			serviceStarterJobEnabled = serviceStarterJobEnabled,
			bootReceiverEnabled = bootReceiverEnabled,
			notificationDeletedReceiverEnabled = notificationDeletedReceiverEnabled,
			fileProviderEnabled = fileProviderEnabled,
			deletionObserverEnabled = deletionObserverEnabled,
			sceneKeys = sceneKeys,
			commonServiceClassName = commonServiceClassName,
			notificationDeletedAction = notificationDeletedAction,
			fileProviderAuthority = fileProviderAuthority,
		)
	}

	/**
	 * 配置通知展示、快捷入口和点击路由映射。
	 */
	fun notifications(config: NotificationFeatureConfig) {
		notifications = config
	}

	/**
	 * 配置 Play Integrity。
	 *
	 * @param enabled 是否启用 Play Integrity。
	 * @param cloudProjectNumber Google Cloud Project Number。
	 * @param runInDebugBuilds debug 包是否执行 Play Integrity。
	 */
	fun playIntegrity(
		enabled: Boolean,
		cloudProjectNumber: Long,
		runInDebugBuilds: Boolean = false,
	) {
		playIntegrity = PlayIntegrityConfig(
			enabled = enabled,
			cloudProjectNumber = cloudProjectNumber,
			runInDebugBuilds = runInDebugBuilds,
		)
	}

	fun build(): AdvertiseSdkConfig {
		return AdvertiseSdkConfig(
			packageName = packageName,
			privacyUrl = privacyUrl,
			termsUrl = termsUrl,
			defaultTopic = resolvedDefaultTopic(),
			resources = resources,
			server = server,
			firebase = firebase,
			remoteConfig = remoteConfig,
			thinking = thinking,
			singular = singular,
			adMob = adMob,
			facebook = facebook,
			tiktok = tiktok,
			safe = safe,
			push = push,
			notifications = notifications,
			playIntegrity = playIntegrity,
		)
	}

	private fun resolvedDefaultTopic(): String {
		return defaultTopic.takeIf { it.isNotBlank() }
			?: if (isDebug) "debug-all" else "all"
	}

	private fun parseSignatures(value: String): Set<String> {
		return value
			.split(',', ';', '\n')
			.map { it.trim() }
			.filter { it.isNotEmpty() }
			.toSet()
	}
}
