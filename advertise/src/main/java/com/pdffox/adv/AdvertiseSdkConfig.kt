package com.pdffox.adv

/**
 * 广告 SDK 总配置，由宿主 App 在初始化时一次性传入。
 *
 * @property packageName 宿主包名；用于安全校验、广告策略包名匹配和三方后台包名一致性检查。
 * @property privacyUrl 隐私政策 URL；用于 UMP/合规入口展示。
 * @property termsUrl 用户协议 URL；用于合规入口展示。
 * @property defaultTopic 默认 FCM topic；为空时按 debug/release 自动回退。
 * @property resources SDK 需要读取的 raw 资源配置。
 * @property server 服务端接口配置。
 * @property firebase Firebase Analytics/FCM 能力开关。
 * @property remoteConfig Firebase Remote Config 能力开关。
 * @property thinking ThinkingData 埋点配置。
 * @property singular Singular 归因配置。
 * @property adMob AdMob 广告配置。
 * @property facebook Facebook SDK/归因配置。
 * @property tiktok TikTok Business SDK 配置。
 * @property safe 安全校验配置。
 * @property push Push/后台组件配置。
 * @property notifications 通知展示和路由配置。
 * @property playIntegrity Play Integrity 校验配置。
 */
data class AdvertiseSdkConfig(
	val packageName: String? = "com.cleantool.cleanpartner",
	val privacyUrl: String = "https://sites.google.com/view/clean-partner-privacy-policy/language",
	val termsUrl: String = "https://sites.google.com/view/clean-partner-terms-conditions/language",
	val defaultTopic: String? = null,
	val resources: AdvertiseResourcesConfig = AdvertiseResourcesConfig(),
	val server: AdvertiseServerConfig = AdvertiseServerConfig(),
	val firebase: FirebaseConfig = FirebaseConfig(),
	val remoteConfig: RemoteConfigFeatureConfig = RemoteConfigFeatureConfig(),
	val thinking: ThinkingConfig = ThinkingConfig(),
	val singular: SingularConfigValues = SingularConfigValues(),
	val adMob: AdMobConfig = AdMobConfig(),
	val facebook: FacebookConfig = FacebookConfig(),
	val tiktok: TikTokConfig = TikTokConfig(),
	val safe: SafeConfig = SafeConfig(),
	val push: PushConfig = PushConfig(),
	val notifications: NotificationFeatureConfig = NotificationFeatureConfig(),
	val playIntegrity: PlayIntegrityConfig = PlayIntegrityConfig(),
)

/**
 * SDK 本地兜底资源配置。
 *
 * @property adPolicyRawResId 本地广告策略 JSON 资源；Remote Config 为空或不可用时兜底使用。
 * @property adLoadConfigRawResId 本地广告预加载策略 JSON 资源；0 表示不加载。
 * @property nativeAdPolicyRawResId 本地 Native 广告策略 JSON 资源；0 表示不加载。
 * @property nativeAdIdsRawResId 本地 Native 广告位 ID 队列 JSON 资源；0 表示不加载。
 * @property cloudCidrsRawResId Google Cloud CIDR 列表资源；用于 IP/环境识别。
 * @property googleCidrsRawResId Google CIDR 列表资源；用于 Google IP 判断。
 * @property pushConfigRawResId 本地上下文化 Push 配置资源；0 表示不加载本地 Push 配置。
 */
data class AdvertiseResourcesConfig(
	val adPolicyRawResId: Int = R.raw.ad_policy,
	val adLoadConfigRawResId: Int = 0,
	val nativeAdPolicyRawResId: Int = 0,
	val nativeAdIdsRawResId: Int = 0,
	val cloudCidrsRawResId: Int = R.raw.cloud,
	val googleCidrsRawResId: Int = R.raw.google,
	val pushConfigRawResId: Int = 0,
)

/**
 * 服务端接口配置。
 *
 * @property enabled 是否启用服务端接口能力；关闭后接口 URL 会返回空字符串。
 * @property releaseHost 生产环境服务端 Host。
 * @property testHost 测试环境服务端 Host；debug/isTest 时优先使用。
 * @property ipInfoV2Path 新版 IP 信息接口路径。
 * @property pushPath 推送/上报接口路径。
 * @property parseTokenPath Play Integrity token 解析接口路径。
 * @property parseTokenKey Play Integrity token 解析密钥；应由服务端/安全负责人提供。
 */
data class AdvertiseServerConfig(
	val enabled: Boolean = false,
	val releaseHost: String = "",
	val testHost: String = "",
	val ipInfoV2Path: String = "/getIpInfoV2",
	val pushPath: String = "/publish",
	val parseTokenPath: String = "/parseToken",
	val parseTokenKey: String = "",
)

/**
 * Firebase 能力开关。
 *
 * @property analyticsEnabled 是否初始化 Firebase Analytics 并允许事件上报。
 * @property messagingEnabled 是否启用 Firebase Messaging/FCM。
 * @property subscribeDefaultTopic 是否订阅默认 FCM topic。
 */
data class FirebaseConfig(
	val analyticsEnabled: Boolean = true,
	val messagingEnabled: Boolean = true,
	val subscribeDefaultTopic: Boolean = true,
)

/**
 * Firebase Remote Config 开关。
 *
 * @property enabled 是否拉取并应用远程配置。
 */
data class RemoteConfigFeatureConfig(
	val enabled: Boolean = true,
)

/**
 * ThinkingData 埋点配置。
 *
 * @property enabled 是否初始化 ThinkingData 并上报事件/用户属性。
 * @property appKey ThinkingData 项目 App Key。
 * @property serverUrl ThinkingData 数据上报地址。
 */
data class ThinkingConfig(
	val enabled: Boolean = true,
	val appKey: String = "5c31f896847a46bab8d04059d18db105",
	val serverUrl: String = "https://mar2.top",
)

/**
 * Singular 归因配置。
 *
 * @property enabled 是否启用 Singular 初始化、自然量判断和广告收入上报。
 * @property apiKey Singular API Key。
 * @property secret Singular Secret；敏感信息，生产环境不要硬编码在公开仓库。
 */
data class SingularConfigValues(
	val enabled: Boolean = true,
	val apiKey: String = "mar2game_f7b9272a",
	val secret: String = "72b3df2ee5d0a64a6c404ce01937c3d6",
)

/**
 * AdMob 广告位配置。
 *
 * @property enabled 是否启用 AdMob 广告网络。
 * @property appId AdMob App ID；必须与 Manifest 中的 APPLICATION_ID meta-data 一致。
 * @property bannerId Banner 广告位 ID。
 * @property interstitialId 插屏广告位 ID。
 * @property nativeId Native 广告位 ID。
 * @property openId App Open 开屏广告位 ID。
 * @property testBannerId Google 官方 Banner 测试广告位 ID。
 * @property testInterstitialId Google 官方插屏测试广告位 ID。
 * @property testNativeId Google 官方 Native 测试广告位 ID。
 * @property testOpenId Google 官方 App Open 测试广告位 ID。
 * @property debugNativeIdsJson debug 模式 Native 高/中/低价广告位队列 JSON。
 */
data class AdMobConfig(
	val enabled: Boolean = true,
	val appId: String = "ca-app-pub-3615322193850391~9049230058",
	val bannerId: String = "ca-app-pub-3615322193850391/5414071526",
	val interstitialId: String = "ca-app-pub-3615322193850391/5399643550",
	val nativeId: String = "ca-app-pub-3615322193850391/7773949435",
	val openId: String = "ca-app-pub-3615322193850391/5042536433",
	val testBannerId: String = "ca-app-pub-3940256099942544/9214589741",
	val testInterstitialId: String = "ca-app-pub-3940256099942544/1033173712",
	val testNativeId: String = "ca-app-pub-3940256099942544/2247696110",
	val testOpenId: String = "ca-app-pub-3940256099942544/9257395921",
	val nativeIdsJson: String = DEFAULT_NATIVE_IDS_JSON,
	val debugNativeIdsJson: String = DEFAULT_DEBUG_NATIVE_IDS_JSON,
)

/**
 * Facebook SDK 配置。
 *
 * @property enabled 是否启用 Facebook SDK/归因能力。
 * @property appId Facebook App ID；需与 Manifest placeholder 一致。
 * @property clientToken Facebook Client Token；敏感信息。
 * @property advertiserIdCollectionEnabled 是否允许 Facebook 收集广告 ID。
 */
data class FacebookConfig(
	val enabled: Boolean = false,
	val appId: String = "",
	val clientToken: String = "",
	val advertiserIdCollectionEnabled: Boolean = true,
)

/**
 * TikTok Business SDK 配置。
 *
 * @property enabled 是否启用 TikTok Business SDK。
 * @property accessToken TikTok Access Token；生产环境应替换为正式 token。
 * @property ttAppId TikTok 后台 TT App ID。
 * @property appId TikTok 侧应用 ID；通常应与宿主包名一致。
 * @property startTrackOnInit 初始化时是否立即开始追踪。
 */
data class TikTokConfig(
	val enabled: Boolean = false,
	val accessToken: String = "",
	val ttAppId: String = "",
	val appId: String? = null,
	val startTrackOnInit: Boolean = true,
)

/**
 * 安全校验配置。
 *
 * @property enabled 是否启用安全校验。
 * @property expectedPackageName 期望包名；非空时要求运行包名一致。
 * @property expectedSignatures 允许的签名 SHA-256 集合。
 * @property rejectDebuggableBuilds 是否拒绝 debuggable 包。
 * @property rejectDebuggerAttached 是否拒绝已连接调试器的运行环境。
 * @property killProcessOnFailure 安全校验失败时是否杀进程。
 */
data class SafeConfig(
	val enabled: Boolean = true,
	val expectedPackageName: String? = "com.cleantool.cleanpartner",
	val expectedSignatures: Set<String> = emptySet(),
	val rejectDebuggableBuilds: Boolean = true,
	val rejectDebuggerAttached: Boolean = true,
	val killProcessOnFailure: Boolean = true,
)

/**
 * Push 和后台组件配置。
 *
 * @property enabled Push 模块总开关。
 * @property persistentServiceEnabled 是否启用持久前台服务。
 * @property firebaseMessagingServiceEnabled 是否启用 FCM service 运行时能力。
 * @property serviceStarterJobEnabled 是否启用 JobService 拉起服务能力。
 * @property bootReceiverEnabled 是否启用开机广播接收能力。
 * @property notificationDeletedReceiverEnabled 是否启用通知删除广播接收能力。
 * @property fileProviderEnabled 是否启用 FileProvider。
 * @property deletionObserverEnabled 是否启用媒体/文件删除观察相关能力。
 * @property sceneKeys 业务场景和 Push 配置 scene key 的映射。
 * @property commonServiceClassName 自定义持久服务类名；为空时使用 SDK 默认服务。
 * @property notificationDeletedAction 自定义通知删除广播 action；为空时使用 SDK 默认 action。
 * @property fileProviderAuthority 自定义 FileProvider authority；为空时使用默认 authority。
 */
data class PushConfig(
	val enabled: Boolean = true,
	val persistentServiceEnabled: Boolean = true,
	val firebaseMessagingServiceEnabled: Boolean = true,
	val serviceStarterJobEnabled: Boolean = true,
	val bootReceiverEnabled: Boolean = true,
	val notificationDeletedReceiverEnabled: Boolean = true,
	val fileProviderEnabled: Boolean = true,
	val deletionObserverEnabled: Boolean = true,
	val sceneKeys: PushSceneKeyConfig = PushSceneKeyConfig(),
	val commonServiceClassName: String? = null,
	val notificationDeletedAction: String? = null,
	val fileProviderAuthority: String? = null,
)

/**
 * Push 场景 key 配置，必须与 Remote Config 的 Contextualized_Push/push.json scene key 对齐。
 *
 * @property imageDeleted 图片删除场景 key。
 * @property videoDeleted 视频删除场景 key。
 * @property fileDeleted 文件删除场景 key。
 */
data class PushSceneKeyConfig(
	val imageDeleted: String = "delete_photos",
	val videoDeleted: String = "delete_videos",
	val fileDeleted: String = "delete_files",
)

/**
 * 通知展示和路由配置。
 *
 * @property enabled 通知模块总开关。
 * @property smallIconResId Android 通知小图标资源 ID。
 * @property persistentContentText 持久通知正文文案。
 * @property persistentActions 持久通知快捷入口配置。
 * @property routeMappings Remote Config/Push 原始 route 到宿主页面 route 的映射。
 */
data class NotificationFeatureConfig(
	val enabled: Boolean = true,
	val smallIconResId: Int = 0,
	val persistentContentText: String = "",
	val persistentActions: List<NotificationActionConfig> = emptyList(),
	val routeMappings: List<NotificationRouteMapping> = emptyList(),
)

/**
 * 持久通知快捷入口配置。
 *
 * @property route 宿主内部页面 route。
 * @property label 快捷入口显示文案。
 * @property iconResId 快捷入口图标资源 ID。
 */
data class NotificationActionConfig(
	val route: String,
	val label: String = route,
	val iconResId: Int = 0,
)

/**
 * 通知点击路由映射。
 *
 * @property rawRoute 远程配置或 Push 下发的原始 Route。
 * @property route 宿主 App 实际可识别的页面 route。
 * @property temporaryIconResId 临时通知图标资源 ID。
 * @property persistentIconResId 持久通知图标资源 ID。
 */
data class NotificationRouteMapping(
	val rawRoute: String,
	val route: String,
	val temporaryIconResId: Int = 0,
	val persistentIconResId: Int = 0,
)

/**
 * Play Integrity 配置。
 *
 * @property enabled 是否请求 Play Integrity token。
 * @property cloudProjectNumber 启用 Play Integrity API 的 Google Cloud Project Number。
 * @property runInDebugBuilds debug 包是否也执行 Play Integrity；默认关闭避免本地调试受阻。
 */
data class PlayIntegrityConfig(
	val enabled: Boolean = true,
	val cloudProjectNumber: Long = 807524859286L,
	val runInDebugBuilds: Boolean = false,
)

/**
 * 默认 Native 正式广告位队列，和宿主 BuildConfig 的默认配置保持一致。
 */
const val DEFAULT_NATIVE_IDS_JSON = """
[
  {
    "highPriceID": "ca-app-pub-3615322193850391/7773949435",
    "midPriceID": "ca-app-pub-3615322193850391/8625160396",
    "lowPriceID": "ca-app-pub-3615322193850391/4896504652"
  },
  {
    "highPriceID": "ca-app-pub-3615322193850391/7331096303",
    "midPriceID": "ca-app-pub-3615322193850391/1529269007",
    "lowPriceID": "ca-app-pub-3615322193850391/2078769629"
  },
  {
    "highPriceID": "ca-app-pub-3615322193850391/9765687950",
    "midPriceID": "ca-app-pub-3615322193850391/5252891595",
    "lowPriceID": "ca-app-pub-3615322193850391/8452606288"
  }
]
"""

/**
 * debug 模式使用的 Native 测试广告位队列，避免本地调试误请求线上 Native ID。
 */
const val DEFAULT_DEBUG_NATIVE_IDS_JSON = """
[
  {
    "highPriceID": "ca-app-pub-3940256099942544/2247696110",
    "midPriceID": "ca-app-pub-3940256099942544/2247696110",
    "lowPriceID": "ca-app-pub-3940256099942544/2247696110"
  },
  {
    "highPriceID": "ca-app-pub-3940256099942544/2247696110",
    "midPriceID": "ca-app-pub-3940256099942544/2247696110",
    "lowPriceID": "ca-app-pub-3940256099942544/2247696110"
  },
  {
    "highPriceID": "ca-app-pub-3940256099942544/2247696110",
    "midPriceID": "ca-app-pub-3940256099942544/2247696110",
    "lowPriceID": "ca-app-pub-3940256099942544/2247696110"
  }
]
"""
