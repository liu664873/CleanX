package com.pdffox.adv

import android.Manifest
import android.app.Activity
import android.app.Application
import android.content.Context
import android.view.ViewGroup
import androidx.annotation.RequiresPermission
import cn.thinkingdata.analytics.TDAnalytics
import com.pdffox.adv.adv.ShowOpenAd
import com.google.android.gms.ads.AdView
import org.json.JSONObject

/**
 * 广告库对宿主 App 暴露的统一入口。
 *
 * 该对象只负责提供稳定的 SDK API，并把实际初始化、广告展示、埋点和通知能力转交给内部的 [Ads]。
 */
object AdvertiseSdk {
	// 广告预加载触发时机标识，宿主传入这些 key 后由 AdConfig 判断是否允许预加载。
	const val LOAD_TIME_OPEN_APP = "open_app"
	const val LOAD_TIME_PLAY_FINISH = "play_finish"
	const val LOAD_TIME_ENTER_BACKGROUND = "enter_background"
	const val LOAD_TIME_RECEIVE_NOTIFICATION = "receive_notification"
	const val LOAD_TIME_ENTER_FEATURE = "enter_features"

	/** App Open 广告关闭回调，宿主通常在这里继续启动流程。 */
	fun interface OpenAdCloseListener {
		fun onClose()
	}

	/** App Open 广告加载成功回调，用于通知宿主广告已可展示。 */
	fun interface OpenAdLoadedListener {
		fun onLoaded()
	}

	/** App Open 广告产生付费回调，value 为广告平台返回的收入值。 */
	fun interface OpenAdPaidListener {
		fun onPaid(value: Long)
	}

	/**
	 * 初始化广告 SDK 和已开启的三方能力。
	 *
	 * @param context 宿主 Application，SDK 会保存为全局运行时上下文。
	 * @param isTest 是否测试环境，影响测试广告位、日志、接口 Host 和安全校验。
	 * @param sdkConfig 宿主传入的完整配置，默认使用库内兜底配置并补齐包名。
	 */
	@RequiresPermission(allOf = [Manifest.permission.INTERNET, Manifest.permission.ACCESS_NETWORK_STATE, Manifest.permission.WAKE_LOCK])
	suspend fun init(
		context: Application,
		isTest: Boolean,
		sdkConfig: AdvertiseSdkConfig = Config.sdkConfig.copy(packageName = Config.sdkConfig.packageName ?: context.packageName),
	) {
		Ads.init(context, isTest, sdkConfig)
	}

	/** 展示插屏广告；广告网络关闭时会直接触发关闭回调。 */
	fun showInterstitialAd(activity: Activity, areaKey: String, onClosed: () -> Unit) {
		Ads.showInterstitialAd(activity, areaKey, onClosed)
	}

	/** 获取 Banner 广告 View；无可用广告网络或当前用户需抑制广告时返回 null。 */
	@RequiresPermission(Manifest.permission.INTERNET)
	fun getBannerAd(context: Context, areaKey: String): ViewGroup? {
		return Ads.getBannerAd(context, areaKey)
	}

	/** 销毁由 [getBannerAd] 返回的 Banner View。 */
	fun destroyBannerAd(adView: ViewGroup?) {
		(adView as? AdView)?.destroy()
	}

	/** 展示 App Open 广告，并把内部回调适配为对外的监听接口。 */
	fun showOpenAd(
		activity: Activity,
		areaKey: String,
		onCloseListener: OpenAdCloseListener?,
		onLoadedListener: OpenAdLoadedListener?,
		onPaidListener: OpenAdPaidListener?,
	) {
		// 宿主启动页只关心 SDK 对外回调；这里把对外监听器转换成内部 ShowOpenAd 使用的接口。
		Ads.showOpenAd(
			activity = activity,
			areaKey = areaKey,
			onCloseListener = onCloseListener?.let { listener ->
				ShowOpenAd.OpenAdCloseListener { listener.onClose() }
			},
			onLoadedListener = onLoadedListener?.let { listener ->
				ShowOpenAd.OpenAdLoadedListener { listener.onLoaded() }
			},
			onPaidListener = onPaidListener?.let { listener ->
				ShowOpenAd.OpenAdPaidListener { value -> listener.onPaid(value) }
			},
		)
	}

	/** 新手引导页自动切换间隔，来自 NativeConfig/远程策略。 */
	fun hasCachedOpenAd(): Boolean = Ads.hasCachedOpenAd()

	fun showCachedOpenAd(
		activity: Activity,
		areaKey: String,
		onCloseListener: OpenAdCloseListener?,
		onLoadedListener: OpenAdLoadedListener?,
		onPaidListener: OpenAdPaidListener?,
	): Boolean {
		return Ads.showCachedOpenAd(
			activity = activity,
			areaKey = areaKey,
			onCloseListener = onCloseListener?.let { listener ->
				ShowOpenAd.OpenAdCloseListener { listener.onClose() }
			},
			onLoadedListener = onLoadedListener?.let { listener ->
				ShowOpenAd.OpenAdLoadedListener { listener.onLoaded() }
			},
			onPaidListener = onPaidListener?.let { listener ->
				ShowOpenAd.OpenAdPaidListener { value -> listener.onPaid(value) }
			},
		)
	}

	val guidePageSwapTime: Long
		get() = Ads.guidePageSwapTime

	/** 是否允许 AppOpenHelper 自动在前后台切换时展示开屏广告。 */
	var isAppOpenAdEnabled: Boolean
		get() = Ads.isAppOpenAdEnabled
		set(value) {
			Ads.isAppOpenAdEnabled = value
		}

	/** 是否抑制下一次 App Open 广告，常用于业务流程中避免打断用户操作。 */
	var suppressNextAppOpenAd: Boolean
		get() = Ads.suppressNextAppOpenAd
		set(value) {
			Ads.suppressNextAppOpenAd = value
		}

	/** 当前 IP 是否被识别为 Google IP。 */
	val isGoogleIp: Boolean
		get() = Ads.isGoogleIp

	/** 当前用户是否被标记为 paid_0 用户。 */
	val isPaidUser: Boolean
		get() = Ads.isPaidUser

	/** 当前用户是否需要抑制广告，paid_0 或 Google IP 任一成立即抑制。 */
	val shouldSuppressAdsForCurrentUser: Boolean
		get() = Ads.shouldSuppressAdsForCurrentUser

	/** 当前归因是否自然量。 */
	val isNature: Boolean
		get() = Ads.isNature

	/** 当前订阅的 FCM topic。 */
	val topic: String
		get() = Ads.topic

	/** 隐私政策 URL。 */
	val privacyUrl: String
		get() = Ads.privacyUrl

	/** 用户协议 URL。 */
	val termsUrl: String
		get() = Ads.termsUrl

	/** 判断指定触发时机是否允许预加载 App Open 广告。 */
	fun canPreloadOpen(loadTimeKey: String): Boolean = Ads.canPreloadOpen(loadTimeKey)

	/** 判断指定触发时机是否允许预加载插屏广告。 */
	fun canPreloadInterstitial(loadTimeKey: String): Boolean = Ads.canPreloadInterstitial(loadTimeKey)

	/** 判断指定触发时机是否允许预加载 Native 广告。 */
	fun canPreloadNative(loadTimeKey: String): Boolean = Ads.canPreloadNative(loadTimeKey)

	/** 主动预加载 App Open 广告。 */
	fun preloadOpen(context: Context) {
		Ads.preloadOpen(context)
	}

	/** 主动预加载插屏广告。 */
	fun preloadInterstitial(context: Context) {
		Ads.preloadInterstitial(context)
	}

	/** 主动填充 Native 广告池，加载到广告组后可回调宿主刷新 UI。 */
	fun preloadNative(context: Context, onAdGroupLoaded: (() -> Unit)? = null) {
		Ads.preloadNative(context, onAdGroupLoaded)
	}

	/** 初始化 Google UMP 同意状态，返回值表示是否已发起或完成同意流程。 */
	fun initConsent(activity: Activity, onComplete: (success: Boolean) -> Unit): Boolean {
		return Ads.initConsent(activity, onComplete)
	}

	/** 在启动页展示 UMP 同意弹窗，结束后继续启动流程。 */
	fun showSplashConsent(activity: Activity, onComplete: () -> Unit) {
		Ads.showSplashConsent(activity, onComplete)
	}

	/** 展示隐私选项入口，供用户重新调整广告同意状态。 */
	fun showPrivacyOptions(activity: Activity) {
		Ads.showPrivacyOptions(activity)
	}

	/** UMP 是否要求在界面上提供隐私选项入口。 */
	val isPrivacyOptionsRequired: Boolean
		get() = Ads.isPrivacyOptionsRequired

	/** 通过广告库统一上报 ThinkingData 事件。 */
	fun logEvent(eventName: String, params: Map<String, Any>) {
		Ads.logEvent(eventName, params)
	}

	/** 读取广告库共享偏好中的字符串配置。 */
	fun getPreferenceString(key: String, defaultValue: String): String {
		return Ads.getPreferenceString(key, defaultValue)
	}

	/** 写入广告库共享偏好中的字符串配置。 */
	fun putPreferenceString(key: String, value: String) {
		Ads.putPreferenceString(key, value)
	}

	/** 设置只写一次的 ThinkingData 用户属性。 */
	fun setUserOnceAttr(key: String, value: String) {
		Ads.setUserOnceAttr(key, value)
	}

	/** 设置可覆盖的 ThinkingData 用户属性。 */
	fun setUserAttr(key: String, value: Any) {
		Ads.setUserAttr(key, value)
	}

	/** 获取 ThinkingData 记录的首次打开时间。 */
	fun getFirstOpenTime(): String = Ads.getFirstOpenTime()

	/** 获取 ThinkingData 记录的最近打开时间。 */
	fun getLatestOpenTime(): String = Ads.getLatestOpenTime()

	/** 批量设置 ThinkingData 公共事件属性。 */
	fun setSuperProperties(properties: Map<String, Any?>) {
		val json = JSONObject()
		properties.forEach { (key, value) ->
			json.put(key, value)
		}
		TDAnalytics.setSuperProperties(json)
	}

	/** 获取 ThinkingData 设备 ID。 */
	fun getThinkingDeviceId(): String = TDAnalytics.getDeviceId()

	/** ThinkingData 用户属性 key，供宿主保持字段名一致。 */
	object ThinkingKeys {
		const val firstOpenTime = "first_open_time"
		const val latestOpenTime = "latest_open_time"
	}

	/** Push 点击埋点字段，供宿主和 SDK 共用同一套参数名。 */
	object PushLog {
		const val notificationClicked = "notification_clicked"
		const val msgId = "msg_id"
		const val targetUserId = "target_user_id"
	}

	/** 发送调试通知，便于开发阶段验证远程配置中的通知模板。 */
	fun sendDebugNotification(context: Context, notificationType: String, configName: String) {
		Ads.sendDebugNotification(context, notificationType, configName)
	}

	/** 确保持久通知前台服务处于运行状态。 */
	fun ensurePersistentNotificationServiceRunning(context: Context) {
		Ads.ensurePersistentNotificationServiceRunning(context)
	}
}
