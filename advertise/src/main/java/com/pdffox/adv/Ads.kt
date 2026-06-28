package com.pdffox.adv

import android.Manifest
import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresPermission
import cn.thinkingdata.analytics.TDAnalytics
import cn.thinkingdata.analytics.TDConfig
import com.google.firebase.Firebase
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.remoteconfig.remoteConfig
import com.google.gson.Gson
import com.pdffox.adv.adv.AdChecker
import com.pdffox.adv.adv.AdConfig
import com.pdffox.adv.adv.AdLoader
import com.pdffox.adv.adv.AdvIDs
import com.pdffox.adv.adv.AdvCheckManager
import com.pdffox.adv.adv.AdvInit
import com.pdffox.adv.adv.AppOpenHelper
import com.pdffox.adv.adv.NativeConfig
import com.pdffox.adv.adv.ShowBannerAd
import com.pdffox.adv.adv.ShowInterAd
import com.pdffox.adv.adv.ShowNativeAd
import com.pdffox.adv.adv.ShowOpenAd
import com.pdffox.adv.adv.UMPUtil
import com.pdffox.adv.adv.policy.AdPolicyManager
import com.pdffox.adv.adv.policy.NativePolicyManager
import com.pdffox.adv.log.LogConfig
import com.pdffox.adv.log.LogParams
import com.pdffox.adv.log.LogUtil
import com.pdffox.adv.log.ThinkingAttr
import com.pdffox.adv.notification.CommonService
import com.pdffox.adv.notification.NotificationManager
import com.pdffox.adv.remoteconfig.RemoteConfig
import com.pdffox.adv.remoteconfig.RemoteConfigManager
import com.pdffox.adv.remoteconfig.RemoteConfigRouting
import com.pdffox.adv.util.PreferenceUtil
import com.singular.sdk.Singular
import com.singular.sdk.SingularConfig
import com.singular.sdk.SingularDeviceAttributionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject
import java.util.Locale

/**
 * 广告库内部编排层。
 *
 * 对外 API 由 [AdvertiseSdk] 暴露，这里负责串联配置写入、三方 SDK 初始化、广告预加载/展示、
 * 归因结果处理、通知启动和合规能力。
 */
internal object Ads {
	/** Ads 内部日志标签。 */
	private const val TAG = "Ads"

	/** 宿主 Manifest 中声明 AdMob App ID 的 meta-data key。 */
	private const val ADMOB_APPLICATION_ID_META_DATA = "com.google.android.gms.ads.APPLICATION_ID"

	/** 期望最终生效的 FCM topic；异步订阅回调回来后会用它过滤过期结果。 */
	@Volatile
	private var desiredTopic: String = Config.topic

	/** IP 信息检查是否已经启动，避免重复请求服务端环境接口。 */
	@Volatile
	private var ipInfoCheckStarted = false

	/** Firebase Analytics 是否已经初始化，避免重复开启采集。 */
	@Volatile
	private var firebaseAnalyticsInitialized = false

	/** Firebase Messaging 是否已经初始化，避免重复订阅默认 topic。 */
	@Volatile
	private var firebaseMessagingInitialized = false

	/** Singular 归因 SDK 是否已经初始化，避免重复注册归因回调。 */
	@Volatile
	private var singularInitialized = false

	/** ThinkingData 埋点 SDK 是否已经初始化，避免重复安装自动采集配置。 */
	@Volatile
	private var thinkingInitialized = false

	/** Play Integrity 是否已经请求过，保证单进程内只生成并上报一次 token。 */
	@Volatile
	private var playIntegrityRequested = false

	/** SDK 持有的宿主 Application，用于内部模块获取全局 Context。 */
	lateinit var application: Application

	/**
	 * 初始化广告库的完整运行时。
	 *
	 * 初始化顺序很重要：先写入全局配置和偏好存储，再初始化三方能力、远程配置、广告策略和通知观察器。
	 */
	@RequiresPermission(allOf = [Manifest.permission.INTERNET, Manifest.permission.ACCESS_NETWORK_STATE, Manifest.permission.WAKE_LOCK])
	suspend fun init(
		context: Application,
		isTest: Boolean,
		sdkConfig: AdvertiseSdkConfig = Config.sdkConfig.copy(packageName = Config.sdkConfig.packageName ?: context.packageName),
	) {
		// 初始化广告 SDK 的全局运行时上下文。
		AdvRuntime.init(context)
		// 保存 Application 实例，供后续内部模块复用。
		application = context
		// 记录当前是否为测试环境，影响日志、校验和部分三方 SDK 行为。
		Config.isTest = isTest
		// 应用宿主传入的 SDK 配置，并同步广告位、topic 等派生配置。
		configure(sdkConfig, context.packageName)
		// 广告能力开启时，先校验宿主 Manifest 中的 AdMob App ID。
		if (sdkConfig.adMob.enabled) {
			// 校验配置中的 AdMob App ID 是否和 Manifest 声明一致。
			validateAdMobAppId(context, sdkConfig)
		}
		// 测试环境下打印安装时间，便于排查首次打开相关逻辑。
		if (Config.isTest) {
			// 输出宿主 App 的首次安装时间。
			Log.e(TAG, "init: appInstallTime ${getAppInstallTime(context)}")
		}
		// 初始化 SDK 内部偏好存储工具。
		PreferenceUtil.init(context)
		// 初始化配置中启用的 Facebook、TikTok 等运行时集成。
		AdvRuntime.initConfiguredIntegrations()
		// IP 信息会影响 Google IP/投放环境判断，放到 IO 协程异步执行，避免阻塞 SDK 初始化。
		if (sdkConfig.server.enabled) {
			// 启动 IP 信息检查，结果会写入广告环境判断状态。
			startIpInfoCheck()
		}
		// 初始化基础配置状态，例如首次打开时间和国家码。
		initConfig()
		// 初始化 Firebase Analytics 和默认 FCM topic。
		initFireBase()
		// 先从本地资源加载广告策略，保证远程配置未返回前也有可用兜底。
		AdPolicyManager.loadPolicyFromLocal(context)
		AdConfig.loadConfigFromLocal(context)
		AdvIDs.loadNativeIDsFromLocal(context)
		NativePolicyManager.loadPolicyFromLocal(context)
		// 远程配置开启时，启动 Firebase Remote Config 拉取流程。
		if (sdkConfig.remoteConfig.enabled) {
			// 初始化远程配置管理器并触发配置加载。
			RemoteConfigManager.initRemoteConfig()
		}
		// Singular 归因开启时，初始化归因 SDK。
		if (sdkConfig.singular.enabled) {
			// 初始化 Singular 并处理自然量/投放来源结果。
			initSingular()
		}
		// ThinkingData 开启时，初始化埋点 SDK。
		if (sdkConfig.thinking.enabled) {
			// 初始化 ThinkingData，并启用自动事件采集。
			initThinking()
		}
		// 广告能力开启时，初始化广告 SDK 并启动自动检查。
		if (sdkConfig.adMob.enabled) {
			// 初始化 AdMob、广告策略和广告预加载相关能力。
			AdvInit.initAdv(context)
			// 启动广告状态自动检查任务。
			AdChecker.startAutoCheck()
		}
		// 通知能力开启时，启动通知配置和生命周期观察。
		if (sdkConfig.notifications.enabled) {
			// 注册通知相关观察器，负责普通通知和持久通知调度。
			NotificationManager.startObservers(context)
		}
		// Play Integrity 只请求一次，避免重复生成 token 和重复上报服务端。
		if (shouldRequestPlayIntegrity(sdkConfig)) {
			// 发起 Play Integrity 校验请求。
			PlayIntegrityHelper().requestPlayIntegrity()
		}
		// 读取首次打开埋点是否已经上报过。
		val isFirstOpen = PreferenceUtil.getBoolean(LogConfig.app_first_open, false)
		// 首次打开埋点未上报时，只上报一次并写入标记。
		if (!isFirstOpen) {
			// 上报首次打开事件和对应时间参数。
			LogUtil.log(LogConfig.app_first_open, mapOf(
				LogParams.timesmap to System.currentTimeMillis(),
				LogParams.time to ThinkingAttr.convertToCaliforniaTime(System.currentTimeMillis()),
			))
			// 写入首次打开已上报标记，避免重复上报。
			PreferenceUtil.commitBoolean(LogConfig.app_first_open, true)
		}
	}

	/** 判断当前配置是否允许发起 Play Integrity 请求，并用状态位保证单进程内只请求一次。 */
	private fun shouldRequestPlayIntegrity(sdkConfig: AdvertiseSdkConfig): Boolean {
		if (!sdkConfig.playIntegrity.enabled || sdkConfig.playIntegrity.cloudProjectNumber <= 0L) {
			return false
		}
		if (Config.isTest && !sdkConfig.playIntegrity.runInDebugBuilds) {
			return false
		}
		if (playIntegrityRequested) {
			return false
		}
		playIntegrityRequested = true
		return true
	}

	/** 应用宿主配置，并同步广告位 ID、topic 等派生配置。 */
	fun configure(sdkConfig: AdvertiseSdkConfig, fallbackPackageName: String? = null) {
		Config.applySdkConfig(sdkConfig, fallbackPackageName)
		AdvIDs.configure(sdkConfig)
		desiredTopic = Config.topic
	}

	/** 校验宿主 Manifest 中声明的 AdMob App ID 是否与传入配置一致。 */
	private fun validateAdMobAppId(context: Context, sdkConfig: AdvertiseSdkConfig) {
		val configuredAppId = sdkConfig.adMob.appId
		val manifestAppId = readManifestMetaData(context, ADMOB_APPLICATION_ID_META_DATA)
		if (manifestAppId == configuredAppId) {
			return
		}
		val message = "Configured AdMob appId '$configuredAppId' does not match manifest value '${manifestAppId ?: "<missing>"}'. Declare com.google.android.gms.ads.APPLICATION_ID metadata in the host app manifest, directly or through manifestPlaceholders."
		if (Config.isTest) {
			error(message)
		} else {
			Log.w(TAG, message)
		}
	}

	/** 从宿主 Application meta-data 中读取配置项，读取失败时在测试环境打印日志。 */
	private fun readManifestMetaData(context: Context, key: String): String? {
		return runCatching {
			@Suppress("DEPRECATION")
			context.packageManager.getApplicationInfo(context.packageName, PackageManager.GET_META_DATA)
				.metaData
				?.getString(key)
		}.onFailure {
			if (com.pdffox.adv.Config.isTest) {
				Log.w(TAG, "readManifestMetaData: failed to read $key", it)
			}
		}.getOrNull()
	}

	/** 启动 IP 环境检查，结果会写入 [Config] 和相关持久化状态。 */
	private fun startIpInfoCheck() {
		if (ipInfoCheckStarted || !Config.isServerEnabled || Config.IPInfoV2Url.isBlank()) {
			return
		}
		ipInfoCheckStarted = true
		CoroutineScope(Dispatchers.IO).launch {
			val result = AdvCheckManager.getIpInfoV2()
			if (Config.isTest) {
				Log.e(TAG, "startIpInfoCheck: result=$result")
			}
		}
	}

	/** 获取宿主 App 首次安装时间，异常时返回 0 表示不可用。 */
	fun getAppInstallTime(context: Context): Long {
		return try {
			val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
			packageInfo.firstInstallTime
		} catch (e: Exception) {
			e.printStackTrace()
			0L
		}
	}

	/** 初始化基础运行参数，包括首次打开时间和系统 Locale 对应的国家码。 */
	fun initConfig() {
		if (AdvCheckManager.params.isFirstOpen) {
			AdvCheckManager.params.installTime = System.currentTimeMillis()
			AdvCheckManager.params.isFirstOpen = false
		}
		val localeCountry = Locale.getDefault().country
		Config.country = localeCountry
		if (com.pdffox.adv.Config.isTest) {
			Log.e(TAG, "initConfig: Config.country = ${Config.country}")
		}
	}

	/** 展示插屏广告；广告网络关闭时直接执行关闭回调，保持宿主流程不中断。 */
	fun showInterstitialAd(activity: Activity, areaKey: String, onClosed: () -> Unit) {
		if (!Config.hasEnabledAdNetwork()) {
			if (com.pdffox.adv.Config.isTest) {
				Log.e(TAG, "showInterstitialAd: !Config.hasEnabledAdNetwork()", )
			}
			onClosed()
			return
		}
		ShowInterAd.showIntAd(activity, areaKey, onClosed)
	}

	/** 获取 Native 广告组，加载完成后通过回调通知宿主刷新广告容器。 */
	@RequiresPermission(Manifest.permission.INTERNET)
	fun getNativeAd(context: Context, areaKey: String, onAdGroupLoaded: () -> Unit): NativeAdContent? {
		if (!Config.hasEnabledAdNetwork()) {
			return null
		}
		return ShowNativeAd.getNativeAd(context, areaKey, onAdGroupLoaded)
	}

	/** Native 广告自动刷新间隔。 */
	val nativeRefreshTime: Long
		get() = NativeConfig.native_refresh_time

	/** 引导页自动翻页间隔。 */
	val guidePageSwapTime: Long
		get() = NativeConfig.guide_page_swap_time

	/** 控制 AppOpenHelper 是否参与前后台切换广告展示。 */
	var isAppOpenAdEnabled: Boolean
		get() = AdConfig.isOpenAppOpenHelper
		set(value) {
			AdConfig.isOpenAppOpenHelper = value
		}

	/** 控制下一次 App Open 广告是否被临时抑制。 */
	var suppressNextAppOpenAd: Boolean
		get() = AppOpenHelper.spSwitch
		set(value) {
			AppOpenHelper.spSwitch = value
		}

	/** 当前用户是否被识别为 Google IP 环境。 */
	val isGoogleIp: Boolean
		get() = Config.isGoogleIP

	/** 当前用户是否命中 paid_0 付费/投放抑制标记。 */
	val isPaidUser: Boolean
		get() = Config.paid_0

	/** 当前用户是否应抑制广告展示，付费标记或 Google IP 任一命中即抑制。 */
	val shouldSuppressAdsForCurrentUser: Boolean
		get() = Config.paid_0 || Config.isGoogleIP

	/** Singular 归因结果是否判定为自然量。 */
	val isNature: Boolean
		get() = Config.isNature

	/** 当前生效的 FCM topic。 */
	val topic: String
		get() = Config.topic

	/** 隐私政策页面地址。 */
	val privacyUrl: String
		get() = Config.PrivacyUrl

	/** 用户协议页面地址。 */
	val termsUrl: String
		get() = Config.TermsUrl

	/** 查询指定触发时机是否允许预加载 App Open 广告。 */
	fun canPreloadOpen(loadTimeKey: String): Boolean = AdConfig.canLoadOpen(loadTimeKey)

	/** 查询指定触发时机是否允许预加载插屏广告。 */
	fun canPreloadInterstitial(loadTimeKey: String): Boolean = AdConfig.canLoadInter(loadTimeKey)

	/** 查询指定触发时机是否允许预加载 Native 广告。 */
	fun canPreloadNative(loadTimeKey: String): Boolean = AdConfig.canLoadNative(loadTimeKey)

	/** 预加载 App Open 广告。 */
	fun preloadOpen(context: Context) {
		AdLoader.loadOpen(context)
	}

	/** 预加载插屏广告。 */
	fun preloadInterstitial(context: Context) {
		AdLoader.loadInter(context)
	}

	/** 预加载 Native 广告池。 */
	fun preloadNative(context: Context, onAdGroupLoaded: (() -> Unit)? = null) {
		AdLoader.fillNativePool(context, onAdGroupLoaded)
	}

	/** 获取 Banner 广告；付费用户或 Google IP 在非测试环境下会被抑制。 */
	@RequiresPermission(Manifest.permission.INTERNET)
	fun getBannerAd(context: Context, areaKey: String): ViewGroup? {
		if (!Config.hasEnabledAdNetwork()) {
			return null
		}
		if (!Config.isTest && (Config.paid_0 || Config.isGoogleIP)) {
			return null
		}
		return ShowBannerAd.getBannerAd(context, areaKey)
	}

	/** 展示 App Open 广告，内部广告网络关闭时直接回调关闭监听。 */
	fun showOpenAd(activity: Activity, areaKey: String, onCloseListener: ShowOpenAd.OpenAdCloseListener?, onLoadedListener: ShowOpenAd.OpenAdLoadedListener?, onPaidListener: ShowOpenAd.OpenAdPaidListener?) {
		if (!Config.hasEnabledAdNetwork()) {
			// 广告网络未开启时等同于“没有广告可展示”，直接通知宿主继续启动。
			onCloseListener?.onClose()
			return
		}
		// 广告网络可用时交给 App Open 展示模块处理策略校验、缓存命中和实时加载。
		ShowOpenAd.showOpenAd(activity, areaKey, onCloseListener, onLoadedListener, onPaidListener)
	}

	/** 初始化 UMP 同意流程。 */
	fun hasCachedOpenAd(): Boolean {
		return Config.hasEnabledAdNetwork() && ShowOpenAd.hasCachedOpenAd()
	}

	fun showCachedOpenAd(
		activity: Activity,
		areaKey: String,
		onCloseListener: ShowOpenAd.OpenAdCloseListener?,
		onLoadedListener: ShowOpenAd.OpenAdLoadedListener?,
		onPaidListener: ShowOpenAd.OpenAdPaidListener?,
	): Boolean {
		if (!Config.hasEnabledAdNetwork()) {
			return false
		}
		return ShowOpenAd.showCachedOpenAd(activity, areaKey, onCloseListener, onLoadedListener, onPaidListener)
	}

	fun initConsent(activity: Activity, onComplete: (success: Boolean) -> Unit): Boolean {
		return UMPUtil.initUMP(activity, onComplete)
	}

	/** 启动页场景展示 UMP 同意弹窗。 */
	fun showSplashConsent(activity: Activity, onComplete: () -> Unit) {
		UMPUtil.showSplashUMP(activity, onComplete)
	}

	/** 展示隐私选项弹窗。 */
	fun showPrivacyOptions(activity: Activity) {
		UMPUtil.showUMP(activity)
	}

	/** 当前是否需要向用户展示隐私选项入口。 */
	val isPrivacyOptionsRequired: Boolean
		get() = UMPUtil.isPrivacyOptionsRequired

	/** 上报 ThinkingData 事件。 */
	fun logEvent(eventName: String, params: Map<String, Any>) {
		LogUtil.log(eventName, params)
	}

	/** 读取 SDK 偏好存储字符串。 */
	fun getPreferenceString(key: String, defaultValue: String): String {
		return PreferenceUtil.getString(key, defaultValue) ?: defaultValue
	}

	/** 写入 SDK 偏好存储字符串。 */
	fun putPreferenceString(key: String, value: String) {
		PreferenceUtil.commitString(key, value)
	}

	/** 设置只写一次的用户属性。 */
	fun setUserOnceAttr(key: String, value: String) {
		ThinkingAttr.setUserOnceAttr(key, value)
	}

	/** 设置可覆盖的用户属性。 */
	fun setUserAttr(key: String, value: Any) {
		ThinkingAttr.setUserAttr(key, value)
	}

	/** 获取首次打开时间。 */
	fun getFirstOpenTime(): String = ThinkingAttr.getFirstOpenTime()

	/** 获取最近打开时间。 */
	fun getLatestOpenTime(): String = ThinkingAttr.getLatestOpenTime()

	/** ThinkingData 内置用户属性 key。 */
	object ThinkingKeys {
		/** 首次打开时间用户属性 key。 */
		const val firstOpenTime = "first_open_time"

		/** 最近打开时间用户属性 key。 */
		const val latestOpenTime = "latest_open_time"
	}

	/** Push 点击相关埋点 key。 */
	object PushLog {
		/** 通知点击事件名。 */
		const val notificationClicked = "notification_clicked"

		/** 通知消息 ID 参数名。 */
		const val msgId = "msg_id"

		/** 通知目标用户 ID 参数名。 */
		const val targetUserId = "target_user_id"
	}

	/** 发送指定类型和配置名的调试通知。 */
	fun sendDebugNotification(context: Context, notificationType: String, configName: String) {
		if (Config.sdkConfig.notifications.enabled) {
			NotificationManager.sendNotificationDetail(notificationType, configName)
		}
	}

	/** 确保持久通知前台服务启动。 */
	fun ensurePersistentNotificationServiceRunning(context: Context) {
		CommonService.start(context)
	}

	/**
	 * 创建点击通知后的宿主启动 Intent。
	 *
	 * AppOpenFrom、Route、Scene 是宿主启动页/导航层识别来源和目标页面的关键参数。
	 */
	internal fun createLaunchIntent(
		context: Context,
		appOpenFrom: String,
		route: String = "",
		scene: String = "",
	): Intent? {
		val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName) ?: return null
		return launchIntent.apply {
			putExtra("AppOpenFrom", appOpenFrom)
			putExtra("Route", route)
			if (scene.isNotBlank()) {
				putExtra("Scene", scene)
			}
			addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
		}
	}

	/** 初始化 Firebase Analytics，并在需要时订阅默认 FCM topic。 */
	@RequiresPermission(allOf = [Manifest.permission.INTERNET, Manifest.permission.ACCESS_NETWORK_STATE, Manifest.permission.WAKE_LOCK])
	private fun initFireBase() {
		val firebaseConfig = Config.sdkConfig.firebase
		if (!firebaseConfig.analyticsEnabled && !firebaseConfig.messagingEnabled) {
			return
		}
		if (firebaseConfig.analyticsEnabled && !firebaseAnalyticsInitialized) {
			firebaseAnalyticsInitialized = true
			val mFirebaseAnalytics = FirebaseAnalytics.getInstance(application)
			mFirebaseAnalytics.setAnalyticsCollectionEnabled(true)
		}
		if (!firebaseConfig.messagingEnabled || !firebaseConfig.subscribeDefaultTopic) {
			return
		}
		if (firebaseMessagingInitialized) {
			return
		}
		firebaseMessagingInitialized = true
		// 默认 topic 订阅是异步的；回调时会校验 desiredTopic，避免覆盖后续 changeTopic 的结果。
		val defaultTopic = getDefaultTopic()
		RemoteConfig.topicSet.add(defaultTopic)
		FirebaseMessaging.getInstance().subscribeToTopic(defaultTopic)
			.addOnCompleteListener { task ->
				if (task.isSuccessful) {
					if (desiredTopic == defaultTopic || desiredTopic.isBlank()) {
						rememberActiveTopic(defaultTopic)
					} else {
						FirebaseMessaging.getInstance().unsubscribeFromTopic(defaultTopic)
						if (com.pdffox.adv.Config.isTest) {
							Log.e(TAG, "initFireBase: topic[$defaultTopic] expired, unsubscribed")
						}
					}
					if (com.pdffox.adv.Config.isTest) {
						Log.e(TAG, "initFireBase: subscribed topic[$defaultTopic] success")
					}
				} else {
					if (com.pdffox.adv.Config.isTest) {
						Log.e(TAG, "initFireBase: subscribed topic[$defaultTopic] failed")
					}
				}
			}
	}

	/** 根据当前环境返回兜底 topic。 */
	private fun getDefaultTopic(): String = if (com.pdffox.adv.Config.isTest) "debug-all" else "all"

	/** 记录当前真正生效的 topic，并同步 RemoteConfig 的 topic 集合。 */
	private fun rememberActiveTopic(topic: String) {
		Config.topic = topic
		RemoteConfig.topicSet.clear()
		RemoteConfig.topicSet.add(topic)
		if (com.pdffox.adv.Config.isTest) {
			PreferenceUtil.commitString("Config.topic", Config.topic)
		}
	}

	/** 切换 FCM topic，会先退订旧 topic，再订阅新的目标 topic。 */
	fun changeTopic(newTopic: String) {
		if (newTopic.isBlank() || !Config.sdkConfig.firebase.messagingEnabled) {
			return
		}
		desiredTopic = newTopic
		val topicsToUnsubscribe = linkedSetOf<String>().apply {
			addAll(RemoteConfig.topicSet)
			add(Config.topic)
			add(getDefaultTopic())
		}.filter { it.isNotBlank() && it != newTopic }
		if (topicsToUnsubscribe.isEmpty() && Config.topic == newTopic) {
			return
		}
		for (item in topicsToUnsubscribe) {
			FirebaseMessaging.getInstance().unsubscribeFromTopic(item)
		}
		FirebaseMessaging.getInstance().subscribeToTopic(newTopic)
			.addOnCompleteListener { task ->
				if (task.isSuccessful) {
					// 订阅回调可能晚于下一次切换，只有仍是目标 topic 时才写入全局状态。
					if (desiredTopic == newTopic) {
						rememberActiveTopic(newTopic)
						if (com.pdffox.adv.Config.isTest) {
							Log.e(TAG, "subscribed topic[$newTopic] success")
						}
					} else {
						FirebaseMessaging.getInstance().unsubscribeFromTopic(newTopic)
						if (com.pdffox.adv.Config.isTest) {
							Log.e(TAG, "subscribed topic[$newTopic] stale, unsubscribed")
						}
					}
				} else {
					if (com.pdffox.adv.Config.isTest) {
						Log.e(TAG, "subscribed topic[$newTopic] failed")
					}
				}
			}
	}

	/** 初始化 ThinkingData 埋点 SDK，并开启启动、退出、安装等自动事件采集。 */
	private fun initThinking() {
		if (thinkingInitialized || Config.ThinkingKey.isBlank() || Config.ThinkkingUrl.isBlank()) {
			return
		}
		thinkingInitialized = true
		// 获取 TDConfig 实例
		val config = TDConfig.getInstance(application, Config.ThinkingKey, Config.ThinkkingUrl)
		/*
		设置运行模式为 Debug 模式
		NORMAL模式:数据会存入缓存，并依据一定的缓存策略上报,默认为NORMAL模式；建议在线上环境使用
		Debug模式:数据逐条上报。当出现问题时会以日志和异常的方式提示用户；不建议在线上环境使用
		DebugOnly模式:只对数据做校验，不会入库；不建议在线上环境使用
		 */
		config.setMode(if(Config.isTest) TDConfig.TDMode.DEBUG else TDConfig.TDMode.NORMAL)
		// 初始化 SDK
		TDAnalytics.init(config)
		//开启自动采集事件
		TDAnalytics.enableAutoTrack(
			TDAnalytics.TDAutoTrackEventType.APP_START or
					TDAnalytics.TDAutoTrackEventType.APP_END or
					TDAnalytics.TDAutoTrackEventType.APP_INSTALL
//					TDAnalytics.TDAutoTrackEventType.APP_VIEW_SCREEN or
//					TDAnalytics.TDAutoTrackEventType.APP_CLICK or
//					TDAnalytics.TDAutoTrackEventType.APP_CRASH
		)
		//打印SDK日志
//		TDAnalytics.enableLog(Config.isTest);
		if (Config.isTest) {
			val deviceId = TDAnalytics.getDeviceId()
			Log.e(TAG, "initThinking: deviceId = $deviceId" )
		}
		if (com.pdffox.adv.Config.isTest) {
			val superProperties = TDAnalytics.getSuperProperties()
			val presetProperties = TDAnalytics.getPresetProperties()
			Log.e(TAG, "initThinking: superProperties = $superProperties" )
			Log.e(TAG, "initThinking: presetProperties = ${Gson().toJson(presetProperties)}" )
		}
	}

	/** 初始化 Singular 归因，并根据归因结果更新自然量状态和远程配置路由。 */
	private fun initSingular() {
		if (singularInitialized || Config.Singular_Api_Key.isBlank() || Config.Singular_Secret.isBlank()) {
			return
		}
		singularInitialized = true
		Log.e(TAG, "initSingular: 开始初始化" )
		val config = SingularConfig(Config.Singular_Api_Key, Config.Singular_Secret)
			.withLoggingEnabled()
			.withLogLevel(1)
			.withSingularDeviceAttribution { attributionData ->
				// TODO: 只有首次安装APP时该方法会被回调
				if (com.pdffox.adv.Config.isTest) {
					Log.e(TAG, "initSingular:  device attribution: $attributionData")
				}
				Config.singularHasResult = true
				val promoteParams = JSONObject()
				try {
					val network = attributionData["network"]?.toString().orEmpty()
					Log.e(TAG, "initSingular: $network")
					promoteParams.put("network", network)
					attributionData["campaign_name"]?.toString()?.let {
						if (it != "") {
							promoteParams.put("campaign_name", it)
						}
					}

					// network 为空或 organic 时按自然量处理；测试环境固定为非自然量，方便走广告分支。
					val isNatural = network.equals("organic", ignoreCase = true) || network.isEmpty()
					if (com.pdffox.adv.Config.isTest) {
						Config.isNature = false
					} else {
						Config.isNature = isNatural
					}
					promoteParams.put("fromNature", isNatural)
//					TDAnalytics.setSuperProperties(promoteParams)
					if (Config.sdkConfig.thinking.enabled) {
						TDAnalytics.userSet(promoteParams)
					}
					if (Config.sdkConfig.remoteConfig.enabled && Config.remoteConfigHasResult) {
						val remoteConfig = Firebase.remoteConfig
						val adMapping = remoteConfig.getString("ad_mapping")
						// Singular 和 Remote Config 都有结果后，重新根据自然量状态选择对应配置。
						RemoteConfigRouting.apply(
							remoteConfig = remoteConfig,
							adMapping = adMapping,
							source = "Ads.initSingular"
						)
					} else {
						if (com.pdffox.adv.Config.isTest) {
							Log.e(TAG, "initSingular: RemoteConfig还未更新先不处理" )
						}
					}
				} catch (e: JSONException) {
					// 处理异常
					Log.e(TAG, "onDeviceAttributionInfoReceived: ", e)
				}
			}
		Singular.init(application, config)
		if (com.pdffox.adv.Config.isTest) {
			val singularGlobalProperties = Singular.getGlobalProperties()
			Log.e(TAG, "initSingular: singularGlobalProperties = ${Gson().toJson(singularGlobalProperties)}" )
		}
	}

}
