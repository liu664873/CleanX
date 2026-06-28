package com.pdffox.adv

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.os.Process
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import com.facebook.FacebookSdk
import com.facebook.appevents.AppEventsLogger
import com.pdffox.adv.log.ThinkingAttr
import com.pdffox.adv.safe.SafeChecker
import com.pdffox.adv.update.AppUpdateHelper
import com.pdffox.adv.util.PreferenceUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference
import kotlin.system.exitProcess

/**
 * 广告库运行时上下文。
 *
 * 负责保存 Application/current Activity、监听前后台切换、初始化 Facebook/TikTok、
 * 调度安全校验和根据远程版本号触发强制更新。
 */
object AdvRuntime {
	private const val TAG = "AdvRuntime"

	/** SDK 当前绑定的宿主 Application。 */
	lateinit var application: Application
		private set

	// 用弱引用保存当前 Activity，避免 SDK 长期持有页面导致内存泄漏。
	private var currentActivityRef: WeakReference<Activity>? = null

	/** 当前前台 Activity；没有前台页面时返回 null。 */
	val currentActivity: Activity?
		get() = currentActivityRef?.get()

	/** 最近一次 App 从后台进入前台的时间戳。 */
	var startAppTime = 0L
		private set

	// activityCount 用于判断 App 是否从后台回到前台。
	private var activityCount = 0

	// 安全检测放在 IO 协程执行，避免阻塞主线程。
	private val detectionScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
	private val mainHandler by lazy { Handler(Looper.getMainLooper()) }
	private var registeredApplication: Application? = null
	private var facebookInitialized = false
	private var tiktokInitialized = false
	private var safetyCheckScheduled = false

	@Volatile
	private var isForceUpdateRetryPending = false

	/** 是否已经完成 Application 绑定。 */
	val isInitialized: Boolean
		get() = ::application.isInitialized

	/** 绑定宿主 Application，并确保生命周期监听只注册一次。 */
	fun init(application: Application) {
		this.application = application
		Config.packageName = application.packageName
		if (registeredApplication !== application) {
			registeredApplication = application
			addListener(application)
		}
	}

	/** 注册 Activity 生命周期回调，用于维护前台 Activity、前后台状态和权限用户属性。 */
	@SuppressLint("SuspiciousIndentation")
	private fun addListener(application: Application) {
		application.registerActivityLifecycleCallbacks(object : Application.ActivityLifecycleCallbacks {
			override fun onActivityResumed(activity: Activity) {
				Log.e(TAG, "onActivityResumed: ")
				setCurrentActivity(activity)
				val appRecognitionVerdict = PreferenceUtil.getBoolean("appRecognitionVerdict", true)
				if (!appRecognitionVerdict) {
					// 服务端或安全校验判定 App 不可信时，恢复前台立即结束进程。
					currentActivity?.finishAffinity()
					Process.killProcess(Process.myPid())
					exitProcess(0)
				}
			}

			override fun onActivityPaused(activity: Activity) {
				Log.e(TAG, "onActivityPaused: ")
				if (currentActivity == activity) {
					clearCurrentActivity()
				}
			}

			override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

			override fun onActivityStarted(activity: Activity) {
				Log.e(TAG, "onActivityStarted: ")
				setCurrentActivity(activity)
				val wasInBackground = activityCount == 0
				activityCount++
				// activityCount 从 0 到 1 代表 App 刚进入前台，记录本次启动时间。
				if (activityCount == 1) {
					startAppTime = System.currentTimeMillis()
				}
				if (wasInBackground) {
					maybeForceUpdate()
				}
			}

			override fun onActivityStopped(activity: Activity) {
				activityCount = (activityCount - 1).coerceAtLeast(0)
				if (activityCount == 0 && Config.sdkConfig.thinking.enabled) {
					// App 退到后台时刷新权限类用户属性，保证埋点侧看到最新授权状态。
					val hasNotificationPermission =
						NotificationManagerCompat.from(application).areNotificationsEnabled()
					ThinkingAttr.setUserSetAttr(
						ThinkingAttr.has_notification_permission,
						hasNotificationPermission,
					)
					val hasAllFilePermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
						Environment.isExternalStorageManager()
					} else {
						val readPermission =
							application.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
						val writePermission =
							application.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
						readPermission && writePermission
					}
					ThinkingAttr.setUserSetAttr(ThinkingAttr.has_allfile_permission, hasAllFilePermission)
				}
			}

			override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}

			override fun onActivityDestroyed(activity: Activity) {
				if (currentActivity == activity) {
					clearCurrentActivity()
				}
			}
		})
	}

	/** 更新当前前台 Activity 引用。 */
	private fun setCurrentActivity(activity: Activity) {
		currentActivityRef = WeakReference(activity)
	}

	/** 清理当前 Activity 引用。 */
	private fun clearCurrentActivity() {
		currentActivityRef = null
	}

	/** 初始化由 SDK 配置开启的三方集成和安全检查。 */
	fun initConfiguredIntegrations() {
		initFacebook()
		initTiktok()
		scheduleSafetyCheck()
	}

	/** 初始化 Facebook SDK，并按配置控制广告 ID 收集。 */
	private fun initFacebook() {
		val facebookConfig = Config.sdkConfig.facebook
		if (!facebookConfig.enabled || facebookConfig.appId.isBlank() || facebookConfig.clientToken.isBlank()) {
			return
		}
		if (facebookInitialized) {
			return
		}
		facebookInitialized = true
		FacebookSdk.setApplicationId(facebookConfig.appId)
		FacebookSdk.setClientToken(facebookConfig.clientToken)
		FacebookSdk.fullyInitialize()
		AppEventsLogger.activateApp(application)
		FacebookSdk.setAdvertiserIDCollectionEnabled(facebookConfig.advertiserIdCollectionEnabled)
	}

	/** 初始化 TikTok Business SDK，并按配置决定是否立即开始追踪。 */
	private fun initTiktok() {
		val tiktokConfig = Config.sdkConfig.tiktok
		if (!tiktokConfig.enabled || tiktokConfig.accessToken.isBlank() || tiktokConfig.ttAppId.isBlank()) {
			return
		}
		if (tiktokInitialized) {
			return
		}
		tiktokInitialized = true
		Log.w(TAG, "TikTok capability is configured but TikTok SDK dependency is not bundled in CleanX phase 1.")
	}

	/** 延迟调度安全校验，避免与 Application 启动阶段的初始化抢主线程。 */
	private fun scheduleSafetyCheck() {
		if (!Config.sdkConfig.safe.enabled || safetyCheckScheduled) {
			return
		}
		safetyCheckScheduled = true
		detectionScope.launch {
			delay(1000)
			SafeChecker.checkAndShutDown(application)
		}
	}

	/** 根据远程配置下发的目标版本号决定是否触发强制更新。 */
	private fun maybeForceUpdate() {
		val targetVersion = Config.update_version
		if (targetVersion <= 0L) {
			return
		}
		val currentVersion = getCurrentVersionCode()
		if (currentVersion == 0L || currentVersion >= targetVersion) {
			return
		}
		if (com.pdffox.adv.Config.isTest) {
			Log.d(
				TAG,
				"maybeForceUpdate: currentVersion=$currentVersion, target=$targetVersion",
			)
		}
		val activity = currentActivity
		if (activity != null) {
			AppUpdateHelper.forceImmediateUpdate(activity)
		} else {
			// 生命周期回调里偶发拿不到当前 Activity，投递到主线程下一轮再尝试。
			scheduleForceUpdateRetry()
		}
	}

	/** 防抖后的强制更新重试，避免前后台切换时重复弹更新流程。 */
	private fun scheduleForceUpdateRetry() {
		if (isForceUpdateRetryPending) {
			return
		}
		isForceUpdateRetryPending = true
		mainHandler.post {
			isForceUpdateRetryPending = false
			val activity = currentActivity ?: return@post
			val targetVersion = Config.update_version
			val currentVersion = getCurrentVersionCode()
			if (targetVersion > 0 && currentVersion > 0 && currentVersion < targetVersion) {
				AppUpdateHelper.forceImmediateUpdate(activity)
			}
		}
	}

	/** 读取宿主当前 versionCode，兼容 Android 13 之后的新 PackageInfoFlags API。 */
	private fun getCurrentVersionCode(): Long {
		return try {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
				application.packageManager.getPackageInfo(
					application.packageName,
					PackageManager.PackageInfoFlags.of(0),
				).longVersionCode
			} else {
				@Suppress("DEPRECATION")
				application.packageManager.getPackageInfo(application.packageName, 0).longVersionCode
			}
		} catch (throwable: Exception) {
			Log.e(TAG, "getCurrentVersionCode: failed to read package info", throwable)
			0L
		}
	}

	/** 当前包名；运行时未初始化时回退到 Config 中保存的包名。 */
	fun currentPackageName(): String {
		return if (isInitialized) application.packageName else Config.packageName
	}

	/** 结束当前前台 Activity 栈，用于安全校验失败等场景。 */
	fun finishCurrentActivity() {
		currentActivity?.finishAffinity()
	}
}
