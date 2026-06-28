package com.pdffox.adv.notification

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannelGroup
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.RemoteViews
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.pdffox.adv.BuildConfig
import com.pdffox.adv.Config
import com.pdffox.adv.R
import com.pdffox.adv.log.LogUtil
import com.pdffox.adv.push.PushIntegration
import com.pdffox.adv.util.PreferenceUtil
import java.util.Locale
import java.util.concurrent.atomic.AtomicInteger

/**
 * 临时通知调度和展示管理器。
 *
 * 负责应用通知远程配置、监听系统/生命周期触发器、执行通知发送频控，并构造自定义 RemoteViews 通知。
 */
@SuppressLint("StaticFieldLeak")
object NotificationManager {
	private const val TAG = "NotificationManager"
	private const val CHANNEL_ID_PREFIX = "default_channel_id_"
	private const val CHANNEL_GROUP_ID_PREFIX = "GROUP_ID_"
	private const val NOTIFICATION_GROUP_KEY_PREFIX = "notification_group_"
	private const val DEFAULT_NMAX = 5

	var notificationConfig: NotificationConfig? = null
	var notificationContents: List<Notice>? = null
	private var appContext: Context? = null
	private var observerContext: Context? = null
	private var screenUnlockReceiver: BroadcastReceiver? = null
	private var powerReceiver: BroadcastReceiver? = null
	private var packageReceiver: BroadcastReceiver? = null
	private var screenOnReceiver: BroadcastReceiver? = null
	private var homeButtonReceiver: BroadcastReceiver? = null
	private var volumeReceiver: BroadcastReceiver? = null
	private var processLifecycleObserver: DefaultLifecycleObserver? = null
	private var isObserving = false
	@Volatile
	private var isAppForeground = false

	// 通知通道、通道组和通知 group key 均按通知上限数量分组创建。
	private fun getChannelId(index: Int) = "$CHANNEL_ID_PREFIX$index"
	private fun getChannelGroupId(index: Int) = "$CHANNEL_GROUP_ID_PREFIX$index"
	private fun getNotificationGroupKey(index: Int) = "$NOTIFICATION_GROUP_KEY_PREFIX$index"
	private fun getChannelCount() = (notificationConfig?.NMax ?: DEFAULT_NMAX).coerceAtLeast(1)

	/** 安全注册广播接收器，兼容 Android 13 的 RECEIVER_NOT_EXPORTED 要求。 */
	private fun registerReceiverSafely(receiver: BroadcastReceiver?, filter: IntentFilter) {
		val context = appContext ?: return
		if (receiver == null) return
		try {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
				context.registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED)
			} else {
				@Suppress("DEPRECATION")
				context.registerReceiver(receiver, filter)
			}
		} catch (e: Exception) {
			if (BuildConfig.DEBUG) {
				Log.e(TAG, "registerReceiver failed", e)
			}
		}
	}

	/** 安全注销广播接收器，忽略未注册导致的 IllegalArgumentException。 */
	private fun unregisterReceiverSafely(receiver: BroadcastReceiver?) {
		val context = appContext ?: return
		if (receiver == null) return
		try {
			context.unregisterReceiver(receiver)
		} catch (e: IllegalArgumentException) {
			if (BuildConfig.DEBUG) {
				Log.e(TAG, "unregisterReceiver skipped", e)
			}
		}
	}

	/** 包装广播处理逻辑，避免单个广播异常导致接收器崩溃。 */
	private fun handleNotificationBroadcast(intent: Intent?, block: () -> Unit) {
		try {
			block()
		} catch (e: Exception) {
			if (BuildConfig.DEBUG) {
				Log.e(TAG, "notification broadcast failed: ${intent?.action}", e)
			}
		}
	}

	/** 更新通知触发策略，并在观察器已启动时重建通道和定时通知。 */
	fun updateNotificationConfig(strConfig: String) {
		if (strConfig.isBlank()) return
		val config = try {
			parseNotificationConfig(strConfig)
		} catch (e: Exception) {
			Log.e(TAG, "parse notification config failed", e)
			return
		}
		if (BuildConfig.DEBUG) {
			Log.e(TAG, "updateNotificationConfig: $strConfig")
			PreferenceUtil.commitString("updateNotificationConfig", strConfig)
		}
		PreferenceUtil.commitString("notification_config", strConfig)
		notificationConfig = config
		observerContext?.let {
			createNotificationChannel(it)
			sendTimerNotification(it)
		}
	}

	/** 更新通知内容池，发送通知时会从该列表随机选择一条内容。 */
	fun updateNotificationContent(strContent: String) {
		if (strContent.isBlank()) return
		val notices = try {
			parseNotificationContents(strContent)
		} catch (e: Exception) {
			Log.e(TAG, "parse notification content failed", e)
			return
		}
		PreferenceUtil.commitString("notification_content", strContent)
		if (BuildConfig.DEBUG) {
			Log.e(TAG, "updateNotificationContent: $notices")
		}
		notificationContents = notices
	}

	/** 启动通知观察器，包括生命周期监听、系统广播监听和定时通知。 */
	fun startObservers(context: Context) {
		if (isObserving) return
		isObserving = true
		observerContext = context
		appContext = context.applicationContext

		if (notificationConfig == null) {
			val strConfig = PreferenceUtil.getString("notification_config", "")
			if (!strConfig.isNullOrBlank()) {
				notificationConfig = try {
					parseNotificationConfig(strConfig)
				} catch (e: Exception) {
					Log.e(TAG, "parse cached notification config failed", e)
					PreferenceUtil.removeByKey("notification_config")
					null
				}
			}
		}
		if (notificationContents == null) {
			val strContent = PreferenceUtil.getString("notification_content", "")
			if (!strContent.isNullOrBlank()) {
				notificationContents = try {
					parseNotificationContents(strContent)
				} catch (e: Exception) {
					Log.e(TAG, "parse cached notification content failed", e)
					null
				}
			}
		}

		createNotificationChannel(context)
		registerProcessLifecycleCallbacks(context)
		registerSystemReceivers()
		sendTimerNotification(context)
	}

	/** 注册进程生命周期监听，用于识别 App 前台/后台并触发回到桌面相关通知。 */
	private fun registerProcessLifecycleCallbacks(context: Context) {
		val registerObserver = {
			if (isObserving && processLifecycleObserver == null) {
				val lifecycle = ProcessLifecycleOwner.get().lifecycle
				isAppForeground = lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)
				processLifecycleObserver = object : DefaultLifecycleObserver {
					override fun onStart(owner: LifecycleOwner) {
						isAppForeground = true
						showDebugAppStateToast(context, "APP进入前台")
					}

					override fun onStop(owner: LifecycleOwner) {
						isAppForeground = false
						showDebugAppStateToast(context, "APP切到后台")
						sendNotification(context, "return_home")
					}
				}.also { lifecycle.addObserver(it) }
			}
		}
		if (Looper.myLooper() == Looper.getMainLooper()) {
			registerObserver()
		} else {
			Handler(Looper.getMainLooper()).post {
				registerObserver()
			}
		}
	}

	/** 注册系统广播触发器，例如解锁、充电变化、安装卸载、亮屏、Home 键和音量变化。 */
	private fun registerSystemReceivers() {
		screenUnlockReceiver = object : BroadcastReceiver() {
			override fun onReceive(context: Context?, intent: Intent?) = handleNotificationBroadcast(intent) {
				if (intent?.action == Intent.ACTION_USER_PRESENT) {
					context?.let { sendNotification(it, "screen_unlock") }
				}
			}
		}
		registerReceiverSafely(screenUnlockReceiver, IntentFilter(Intent.ACTION_USER_PRESENT))

		powerReceiver = object : BroadcastReceiver() {
			override fun onReceive(context: Context?, intent: Intent?) = handleNotificationBroadcast(intent) {
				when (intent?.action) {
					Intent.ACTION_POWER_CONNECTED,
					Intent.ACTION_POWER_DISCONNECTED -> context?.let { sendNotification(it, "battery_change") }
				}
			}
		}
		registerReceiverSafely(powerReceiver, IntentFilter().apply {
			addAction(Intent.ACTION_POWER_CONNECTED)
			addAction(Intent.ACTION_POWER_DISCONNECTED)
		})

		packageReceiver = object : BroadcastReceiver() {
			override fun onReceive(context: Context?, intent: Intent?) = handleNotificationBroadcast(intent) {
				when (intent?.action) {
					Intent.ACTION_PACKAGE_ADDED -> context?.let { sendNotification(it, "install_app") }
					Intent.ACTION_PACKAGE_REMOVED -> context?.let { sendNotification(it, "uninstall_app") }
				}
			}
		}
		registerReceiverSafely(packageReceiver, IntentFilter().apply {
			addAction(Intent.ACTION_PACKAGE_ADDED)
			addAction(Intent.ACTION_PACKAGE_REMOVED)
			addDataScheme("package")
		})

		screenOnReceiver = object : BroadcastReceiver() {
			override fun onReceive(context: Context?, intent: Intent?) = handleNotificationBroadcast(intent) {
				if (intent?.action == Intent.ACTION_SCREEN_ON) {
					context?.let { sendNotification(it, "screen_on") }
				}
			}
		}
		registerReceiverSafely(screenOnReceiver, IntentFilter(Intent.ACTION_SCREEN_ON))

		homeButtonReceiver = object : BroadcastReceiver() {
			override fun onReceive(context: Context?, intent: Intent?) = handleNotificationBroadcast(intent) {
				if (intent?.action == Intent.ACTION_CLOSE_SYSTEM_DIALOGS) {
					when (intent.getStringExtra("reason")) {
						"homekey" -> context?.let { sendNotification(it, "press_key_home") }
						"recentapps" -> context?.let { sendNotification(it, "recent_apps") }
					}
				}
			}
		}
		registerReceiverSafely(homeButtonReceiver, IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS))

		volumeReceiver = object : BroadcastReceiver() {
			override fun onReceive(context: Context?, intent: Intent?) = handleNotificationBroadcast(intent) {
				if (intent?.action == "android.media.VOLUME_CHANGED_ACTION") {
					context?.let { sendNotification(it, "volume_changed") }
				}
			}
		}
		registerReceiverSafely(volumeReceiver, IntentFilter("android.media.VOLUME_CHANGED_ACTION"))
	}

	/** 停止所有通知观察器并释放 Application/Context 引用。 */
	fun stopObservers() {
		isObserving = false
		unregisterReceiverSafely(screenUnlockReceiver)
		screenUnlockReceiver = null
		unregisterReceiverSafely(powerReceiver)
		powerReceiver = null
		unregisterReceiverSafely(screenOnReceiver)
		screenOnReceiver = null
		unregisterReceiverSafely(packageReceiver)
		packageReceiver = null
		unregisterReceiverSafely(homeButtonReceiver)
		homeButtonReceiver = null
		unregisterReceiverSafely(volumeReceiver)
		volumeReceiver = null
		processLifecycleObserver?.let { observer ->
			val removeObserver = {
				ProcessLifecycleOwner.get().lifecycle.removeObserver(observer)
			}
			if (Looper.myLooper() == Looper.getMainLooper()) {
				removeObserver()
			} else {
				Handler(Looper.getMainLooper()).post {
					removeObserver()
				}
			}
		}
		processLifecycleObserver = null
		isAppForeground = false
		appContext = null
		observerContext = null
	}

	/** 根据 timer 配置注册每日定时通知。 */
	fun sendTimerNotification(context: Context) {
		if (notificationConfig?.timer == null) return
		NotificationScheduler.cancelDailyNotification(context)
		for (timer in notificationConfig?.timer ?: emptyList()) {
			NotificationScheduler.scheduleDailyNotification(context, "daily_notification", timer.HH, timer.MM)
		}
	}

	/** 检查通知权限是否开启。 */
	private fun hasNotificationPermission(context: Context): Boolean {
		val hasPermission = NotificationManagerCompat.from(context).areNotificationsEnabled()
		if (!hasPermission && BuildConfig.DEBUG) {
			Log.e(TAG, "sendNotification: no notification permission")
		}
		return hasPermission
	}

	/** 检查全局通知发送间隔是否仍在冷却期。 */
	private fun reachGlobalIntervalLimit(): Boolean {
		val intervalSecond = notificationConfig?.interval_second ?: return false
		if (intervalSecond <= 0) return false
		val lastRecord = NotificationRecordManager.getLastRecord() ?: return false
		return System.currentTimeMillis() - lastRecord.timestamp < intervalSecond * 1000
	}

	/** 检查 1 小时和 24 小时通知配额是否会被本次批量发送超过。 */
	private fun reachQuotaLimit(sendCount: Int): Boolean {
		val config = notificationConfig ?: return true
		return NotificationRecordManager.get24HRecordsCount() + sendCount > (config.`24HMax` * sendCount) ||
			NotificationRecordManager.get1HRecordsCount() + sendCount > (config.`1HMax` * sendCount)
	}

	/** 通知实际展示后上报 notification_app_shown。 */
	private fun logNotificationShown(context: Context, notificationType: String) {
		if (NotificationManagerCompat.from(context).areNotificationsEnabled()) {
			LogUtil.log("notification_app_shown", mapOf("scene" to notificationType))
		}
	}

	/** 按前台开关、权限、配额和全局间隔判断是否允许批量发送通知。 */
	fun sendNotificationBatchIfAllowed(
		context: Context,
		notificationType: String,
		configName: String = notificationType,
		perNotificationDelayMillis: Long = 6_000L
	): Boolean {
		val config = notificationConfig ?: return false
		if (shouldBlockForegroundNotification(config)) return false
		if (!hasNotificationPermission(context)) return false
		val sendCount = config.each_trigger_sent
		if (sendCount <= 0) return false
		if (reachQuotaLimit(sendCount)) return false
		if (reachGlobalIntervalLimit()) return false

		repeat(sendCount) { index ->
			Handler(Looper.getMainLooper()).postDelayed({
				if (!shouldBlockForegroundNotification(notificationConfig)) {
					sendNotificationDetail(notificationType, configName)
				}
			}, index * perNotificationDelayMillis + 3_000L)
		}
		NotificationRecordManager.addRecord(NotificationRecord(configName, System.currentTimeMillis()))
		logNotificationShown(context, notificationType)
		return true
	}

	/** 处理单个触发器通知；按 PhotoRecovery 策略只应用触发器 delay，再进入统一批量频控。 */
	fun sendNotification(context: Context, notificationType : String) {
		val triggerConfig = notificationConfig?.triggers?.firstOrNull { it.name == notificationType } ?: return
		if (!hasNotificationPermission(context)) return
		val delayMillis = (triggerConfig.delay ?: 0).coerceAtLeast(0) * 1000L
		Handler(Looper.getMainLooper()).postDelayed({
			sendNotificationBatchIfAllowed(context, notificationType, notificationType)
		}, delayMillis)
	}

	/** 创建通知通道组和通知通道。 */
	fun createNotificationChannel(context: Context) {
		val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
		for (index in 1..getChannelCount()) {
			val groupId = getChannelGroupId(index)
			notificationManager.createNotificationChannelGroup(
				NotificationChannelGroup(groupId, "GROUP_NAME_$index")
			)
			val channel = android.app.NotificationChannel(
				getChannelId(index),
				"channelName_$index",
				NotificationManager.IMPORTANCE_HIGH
			).apply {
				description = "channelDescription_$index"
				setGroup(groupId)
				setShowBadge(true)
				enableVibration(true)
				vibrationPattern = longArrayOf(0, 100, 200, 300)
				lockscreenVisibility = Notification.VISIBILITY_PUBLIC
			}
			notificationManager.createNotificationChannel(channel)
		}
	}

	// 循环使用通知 ID，避免通知数量无限增长。
	val idQueue: ArrayDeque<Int> = ArrayDeque()

	/** 根据通知内容池随机选择一条内容，并解析多语言后发送临时通知。 */
	fun sendNotificationDetail(notificationType : String, configName : String) {
		val context = observerContext ?: appContext ?: return
		if (!hasNotificationPermission(context)) return
		if (shouldBlockForegroundNotification(notificationConfig)) return
		Handler(Looper.getMainLooper()).post {
			if (shouldBlockForegroundNotification(notificationConfig)) return@post
			val max = getChannelCount()
			if (idQueue.size >= max) {
				idQueue.removeFirst()
			}
			val id = (1..max).firstOrNull { !idQueue.contains(it) } ?: idQueue.removeFirst()
			val notice = notificationContents?.randomOrNull()
			if (notice != null) {
				var title = notice.Title
				var content = notice.Content
				var button = notice.Button
				var route = notice.Route
				val languagesObj = runCatching { parseLanguages(notice.Languages) }.getOrNull()
				val language = languagesObj?.keys?.firstOrNull {
					it.language.equals(Locale.getDefault().language, ignoreCase = true)
				}
				if (language != null) {
					title = language.title
					content = language.content
					button = language.button
				}
				sendTemporaryNotification(
					context = context,
					id = id,
					channelId = getChannelId(id),
					notificationGroupKey = getNotificationGroupKey(id),
					scene = notificationType,
					icon = getIcon(route),
					title = title,
					message = content,
					button = button,
					route = getRoute(route)
				)
			}
			idQueue.add(id)
		}
	}

	private fun shouldBlockForegroundNotification(config: NotificationConfig?): Boolean {
		return isAppForeground && config?.is_foreground_send == false
	}

	private fun showDebugAppStateToast(context: Context, message: String) {
		if (!BuildConfig.DEBUG) return
		Toast.makeText(context.applicationContext, message, Toast.LENGTH_SHORT).show()
	}

	/** 构造并发送自定义 RemoteViews 临时通知。 */
	@SuppressLint("RemoteViewLayout")
	fun sendTemporaryNotification(
		context: Context,
		id: Int,
		channelId: String,
		notificationGroupKey: String,
		scene: String = "",
		icon:Int,
		title: String,
		message: String,
		button: String,
		route: String
	) {
		try {
			val pendingIntent = getAppPendingIntent(context, route, scene)
			val remoteViews = RemoteViews(Config.packageName, R.layout.temp_notification)
			remoteViews.setImageViewResource(R.id.iv_push,icon)
			remoteViews.setTextViewText(R.id.tv_detail, title)
			remoteViews.setTextViewText(R.id.button, button)
			remoteViews.setOnClickPendingIntent(R.id.root, pendingIntent)
			val bigRemoteViews = RemoteViews(Config.packageName, R.layout.temp_notification_big)
			bigRemoteViews.setImageViewResource(R.id.iv_push,icon)
			bigRemoteViews.setTextViewText(R.id.tv_title, title)
			bigRemoteViews.setTextViewText(R.id.tv_message, message)
			bigRemoteViews.setTextViewText(R.id.button, button)
			bigRemoteViews.setOnClickPendingIntent(R.id.root, pendingIntent)
			val notification =
				NotificationCompat.Builder(context, channelId)
					.setContentTitle(title)
					.setSmallIcon(notificationSmallIconResId())
					.setColor(context.getColor(R.color.color_purple))
					.setCustomContentView(remoteViews)
					.setCustomHeadsUpContentView(remoteViews)
					.setCustomBigContentView(bigRemoteViews)
					.setContentIntent(pendingIntent)
					.setGroup(notificationGroupKey)
					.setAutoCancel(true)
					.setPriority(NotificationCompat.PRIORITY_HIGH)
					.setCategory(NotificationCompat.CATEGORY_MESSAGE)
					.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
					.setOngoing(false)
					.setOnlyAlertOnce(false)
					.setShowWhen(true)
					.setWhen(System.currentTimeMillis())
					.build()
			val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
			manager?.notify(id, notification)
		} catch (e: Exception) {
			Log.e(TAG, "sendTemporaryNotification: ", e)
		}
	}

	/** 将远程配置中的原始 route 映射为宿主实际路由。 */
	fun getRoute(route: String): String {
		return Config.sdkConfig.notifications.routeMappings
			.firstOrNull { it.rawRoute == route }
			?.route
			?: route
	}

	/** 根据 route 选择通知图标，优先使用 routeMapping，其次使用通知全局小图标。 */
	fun getIcon(route: String): Int {
		val configuredIcon = Config.sdkConfig.notifications.routeMappings
			.firstOrNull { it.rawRoute == route || it.route == route }
			?.let { mapping ->
				mapping.temporaryIconResId.takeIf { it != 0 }
					?: mapping.persistentIconResId.takeIf { it != 0 }
			}
		return configuredIcon
			?: Config.sdkConfig.notifications.smallIconResId.takeIf { it != 0 }
			?: R.drawable.nlogo
	}

	/** Android 通知小图标资源。 */
	private fun notificationSmallIconResId(): Int {
		return Config.sdkConfig.notifications.smallIconResId.takeIf { it != 0 } ?: R.drawable.nlogo
	}

	private val requestCodeGenerator = AtomicInteger(0)
	/** 创建通知点击后的启动 PendingIntent，携带 route 和 scene 参数给宿主导航层。 */
	fun getAppPendingIntent(context: Context ,route: String = "", scene: String = ""): PendingIntent {
		val launchIntent = PushIntegration.appLaunchIntent(context, "app_push", route, scene) ?: Intent()
		val requestCode = requestCodeGenerator.incrementAndGet()
		return PendingIntent.getActivity(
			context,
			requestCode,
			launchIntent,
			PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
		)
	}
}
