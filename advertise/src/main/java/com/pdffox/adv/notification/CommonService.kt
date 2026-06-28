package com.pdffox.adv.notification

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.database.ContentObserver
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.PowerManager
import android.provider.MediaStore
import android.util.Log
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.google.gson.Gson
import com.pdffox.adv.Ads
import com.pdffox.adv.BuildConfig
import com.pdffox.adv.Config
import com.pdffox.adv.NotificationActionConfig
import com.pdffox.adv.R
import com.pdffox.adv.adv.AdConfig
import com.pdffox.adv.adv.AdLoader
import com.pdffox.adv.adv.AdvCheckManager
import com.pdffox.adv.log.LogUtil
import com.pdffox.adv.push.PushIntegration
import com.pdffox.adv.util.PreferenceDelegate
import com.pdffox.adv.util.PreferenceUtil
import java.util.concurrent.atomic.AtomicInteger

/**
 * 持久前台通知服务。
 *
 * 负责展示常驻通知、监听媒体/文件删除变化、根据上下文化 Push 配置发送删除提醒，
 * 并在通知点击时把 route/scene 透传给宿主启动页或导航层。
 */
class CommonService : Service() {

	/** 服务启动入口和共享 Push 配置缓存。 */
	companion object {
		private const val NOTIFICATION_ID = 2001
		private const val CHANNEL_ID = "channel_id_common_notify"
		private const val TAG = "CommonService"
		private const val MAX_PERSISTENT_ACTIONS = 4

		var pushConfig: PushConfig? = null

		private val requestCodeGenerator = AtomicInteger(0)

		/** 构造启动当前服务的显式 Intent。 */
		fun intent(context: Context): Intent {
			return Intent(context, CommonService::class.java).apply {
				setPackage(context.packageName)
			}
		}

		/** 按 Push 配置启动前台服务；配置关闭时直接忽略。 */
		fun start(context: Context) {
			if (!Config.sdkConfig.push.enabled || !Config.sdkConfig.push.persistentServiceEnabled) {
				return
			}
			runCatching {
				ContextCompat.startForegroundService(context, intent(context))
			}.onFailure {
				Log.e(TAG, "start: failed to launch foreground service", it)
			}
		}
	}

    private lateinit var notificationManager: NotificationManager

	lateinit var wakeLock: PowerManager.WakeLock

	/** 短时持有 WakeLock，降低服务启动后被系统立即挂起的概率。 */
	private fun acquireWakeLock() {
		val powerManager = getSystemService(POWER_SERVICE) as PowerManager
		wakeLock = powerManager.newWakeLock(
			PowerManager.PARTIAL_WAKE_LOCK,
			"KeepAliveService::WakeLock"
		)
		wakeLock.acquire(10 * 60 * 1000L /*10 minutes*/)
	}

	/** 释放服务启动阶段持有的 WakeLock。 */
	private fun releaseWakeLock() {
		if (::wakeLock.isInitialized && wakeLock.isHeld) {
			wakeLock.release()
		}
	}

	/** 初始化持久通知、删除观察器和前台服务。 */
	override fun onCreate() {
		super.onCreate()
		if (!Config.sdkConfig.push.enabled || !Config.sdkConfig.push.persistentServiceEnabled) {
			stopSelf()
			return
		}
		initPushConfig()
		notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
		registerContentObservers()
		acquireWakeLock()
		NotificationChannelManager.createNormalChannel(this)
		try {
			val notification = createNotificationChannel()
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
				startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
			} else {
				startForeground(NOTIFICATION_ID, notification)
			}
		} catch (e: Exception) {
			e.printStackTrace()
		}
	}

	/** 初始化上下文化 Push 配置，优先使用 Remote Config 缓存，失败时回退本地 raw。 */
	fun initPushConfig(){
		if (pushConfig == null) {
			val config = PreferenceUtil.getString("Contextualized_Push", "")
			pushConfig = Gson().fromJson(config, PushConfig::class.java)
		}
		if(pushConfig ==null){
			pushConfig =parsePushConfigFromRaw()
		}
	}

	/** 从本地 raw 资源读取上下文化 Push 兜底配置。 */
	fun parsePushConfigFromRaw(): PushConfig? {
		if (Config.resourceConfig.pushConfigRawResId == 0) {
			return null
		}
		return try {
			val inputStream = this.resources.openRawResource(Config.resourceConfig.pushConfigRawResId)
			val jsonString = inputStream.bufferedReader().use { it.readText() }
			val gson = Gson()
			gson.fromJson(jsonString, PushConfig::class.java)
		} catch (e: Exception) {
			e.printStackTrace()
			null
		}
	}

	/** 服务被重新拉起时刷新前台通知，保持 START_STICKY。 */
	override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
		if (!Config.sdkConfig.push.enabled || !Config.sdkConfig.push.persistentServiceEnabled) {
			stopSelf()
			return START_NOT_STICKY
		}
		try {
			val notification = createNotificationChannel()
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
				startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
			} else {
				startForeground(NOTIFICATION_ID, notification)
			}
		} catch (e: Exception) {
			e.printStackTrace()
		}
		return START_STICKY
	}

	/** 任务被移除时尝试重新启动持久服务。 */
	override fun onTaskRemoved(rootIntent: Intent?) {
		super.onTaskRemoved(rootIntent)
		if (Config.sdkConfig.push.enabled && Config.sdkConfig.push.persistentServiceEnabled) {
			start(applicationContext)
		}
	}

	/** 服务销毁时注销观察器、释放 WakeLock，并清理持久/临时通知。 */
	override fun onDestroy() {
		super.onDestroy()
		unregisterContentObservers()
		releaseWakeLock()
        try {
            notificationManager.cancel(NOTIFICATION_ID)
            for (i in temp_notificationIds) {
                notificationManager.cancel(i)
            }
        } catch (e: Exception) {
            // 忽略未注册接收器的异常
        }
	}

	/** 创建持久服务通知通道并返回前台通知。 */
	private fun createNotificationChannel() : Notification {
//		Log.e(TAG, "createNotificationChannel: ", )
		val channel = NotificationChannel(
			CHANNEL_ID,
			"Long show notify",
			NotificationManager.IMPORTANCE_DEFAULT
		)
		channel.description = "Long show notify desc"
		channel.setShowBadge(false)
		val manager = getSystemService(NotificationManager::class.java)
		manager.createNotificationChannel(channel)
		return buildPersistentNotification()
	}

	/** 构造持久通知的小视图、大视图、删除回调和点击入口。 */
	private fun buildPersistentNotification(): Notification {
		val remoteViews = RemoteViews(
			packageName,
			R.layout.mini_common_notification
		).apply {
			bindPersistentActions(this, miniPersistentActionSlots())
		}

		val bigRemoteViews = RemoteViews(
			packageName,
			R.layout.big_common_notification
		).apply {
			bindPersistentActions(this, bigPersistentActionSlots())
		}

		val deleteIntent = PendingIntent.getBroadcast(
			this,
			0,
			Intent(this, NotificationDeletedReceiver::class.java),
			PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
		)

		if (!Config.paid_0) {
			val oneYearLater = System.currentTimeMillis() + (365L * 24 * 60 * 60 * 1000L)
			// 非 paid_0 用户将通知时间设到未来，减少系统按时间排序时被挤下去的概率。
			return NotificationCompat.Builder(this, CHANNEL_ID)
				.setCustomContentView(remoteViews)
				.setSmallIcon(notificationSmallIconResId())
				.setCustomBigContentView(bigRemoteViews)
				.setContentText(persistentNotificationText())
				.setOngoing(true)
				.setShowWhen(true)
				.setWhen(oneYearLater)
//			.setLargeIcon(BitmapFactory.decodeResource(resources, R.drawable.nlogo))
				.setColor(getColor(R.color.color_purple))
				.setPriority(NotificationCompat.PRIORITY_HIGH)
//			.setFullScreenIntent(getPendingIntent(""), true)
				.setContentIntent(getPendingIntent(""))
				.setDeleteIntent(deleteIntent)
				.build()
		} else {
			// paid_0 用户保留普通持久通知时间。
			return NotificationCompat.Builder(this, CHANNEL_ID)
				.setCustomContentView(remoteViews)
				.setSmallIcon(notificationSmallIconResId())
				.setCustomBigContentView(bigRemoteViews)
				.setContentText(persistentNotificationText())
				.setOngoing(true)
//			.setLargeIcon(BitmapFactory.decodeResource(resources, R.drawable.nlogo))
				.setColor(getColor(R.color.color_purple))
				.setPriority(NotificationCompat.PRIORITY_HIGH)
//			.setFullScreenIntent(getPendingIntent(""), true)
				.setContentIntent(getPendingIntent(""))
				.setDeleteIntent(deleteIntent)
				.build()
		}


	}

	/** 持久通知小图标资源。 */
	private fun notificationSmallIconResId(): Int {
		return Config.sdkConfig.notifications.smallIconResId.takeIf { it != 0 } ?: R.drawable.nlogo
	}

	/** 持久通知正文，配置为空时使用应用名。 */
	private fun persistentNotificationText(): String {
		return Config.sdkConfig.notifications.persistentContentText.takeIf { it.isNotBlank() }
			?: packageManager.getApplicationLabel(applicationInfo).toString()
	}

	/** 持久通知快捷入口的 RemoteViews 槽位。 */
	private data class PersistentActionSlot(
		val rootId: Int,
		val iconId: Int,
		val labelId: Int,
	)

	/** 小持久通知布局中的快捷入口槽位。 */
	private fun miniPersistentActionSlots(): List<PersistentActionSlot> {
		return listOf(
			PersistentActionSlot(R.id.action_1, R.id.action_1_icon, R.id.action_1_label),
			PersistentActionSlot(R.id.action_2, R.id.action_2_icon, R.id.action_2_label),
			PersistentActionSlot(R.id.action_3, R.id.action_3_icon, R.id.action_3_label),
			PersistentActionSlot(R.id.action_4, R.id.action_4_icon, R.id.action_4_label),
		)
	}

	/** 大持久通知布局中的快捷入口槽位；当前和小布局保持一致。 */
	private fun bigPersistentActionSlots(): List<PersistentActionSlot> = miniPersistentActionSlots()

	/** 将宿主配置的快捷入口绑定到 RemoteViews，未配置的槽位隐藏。 */
	private fun bindPersistentActions(remoteViews: RemoteViews, slots: List<PersistentActionSlot>) {
		val actions = Config.sdkConfig.notifications.persistentActions.take(MAX_PERSISTENT_ACTIONS)
		slots.forEachIndexed { index, slot ->
			val action = actions.getOrNull(index)
			if (action == null) {
				remoteViews.setViewVisibility(slot.rootId, android.view.View.GONE)
				return@forEachIndexed
			}
			remoteViews.setViewVisibility(slot.rootId, android.view.View.VISIBLE)
			remoteViews.setTextViewText(slot.labelId, action.label.ifBlank { action.route })
			remoteViews.setImageViewResource(slot.iconId, persistentActionIcon(action))
			remoteViews.setOnClickPendingIntent(slot.rootId, getPendingIntent(action.route))
		}
	}

	/** 选择持久通知快捷入口图标，优先使用 action 自身图标，其次按 route 映射。 */
	private fun persistentActionIcon(action: NotificationActionConfig): Int {
		if (action.iconResId != 0) {
			return action.iconResId
		}
		return getNotificationIcon(action.route)
	}

	/** 创建临时通知点击启动 PendingIntent，携带 route 和 scene。 */
	fun getAppPendingIntent(route: String = "", scene: String = ""): PendingIntent {
		val intent = PushIntegration.appLaunchIntent(this@CommonService, "app_push", route, scene) ?: Intent()
		val requestCode = requestCodeGenerator.incrementAndGet()
		Log.e(TAG, "getAppPendingIntent: $route $requestCode")
		val pendingIntent = PendingIntent.getActivity(
			this@CommonService,
			requestCode,
			intent,
			PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
		)
		return pendingIntent
	}

	/** 创建持久通知点击启动 PendingIntent。 */
	private fun getPendingIntent(route: String = ""): PendingIntent {
		val intent = PushIntegration.appLaunchIntent(this, "persistent", route) ?: Intent()
		val requestCode = requestCodeGenerator.incrementAndGet()
//		Log.e(TAG, "getPendingIntent: $route $requestCode")
		val pendingIntent = PendingIntent.getActivity(
			this,
			requestCode,
			intent,
			PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
		)
		return pendingIntent
	}

	/** 前台服务不提供绑定能力。 */
	override fun onBind(intent: Intent?): IBinder? {
		return null
	}

	// 当前服务发出的临时通知 ID，服务销毁时统一清理。
	var temp_notificationIds: MutableList<Int> = mutableListOf()

	// 删除场景最近通知时间，按文件类型分别持久化限频。
	var deletePhotosTime: Long by PreferenceDelegate("deletePhotosTime", 0L)
	var deleteVideosTime: Long by PreferenceDelegate("deleteVideosTime", 0L)
	var deleteFilesTime: Long by PreferenceDelegate("deleteFilesTime", 0L)

	/** 发送由删除观察器触发的临时通知。 */
	@SuppressLint("RemoteViewLayout")
	fun sendTemporaryNotification(
		scene: String = "",
		res:Int,
		title: String,
		message: String,
		layoutId: Int,
		bigLayoutId: Int,
		route: String
	) {
		try {
			val id= System.currentTimeMillis().toInt()
			// 构建小视图
			val remoteViews = RemoteViews(Config.packageName, layoutId)
			remoteViews.setImageViewResource(R.id.iv_push,res)
			// 构建大视图
			val bigRemoteViews = RemoteViews(Config.packageName, bigLayoutId)
			bigRemoteViews.setImageViewResource(R.id.iv_push,res)
			remoteViews.setTextViewText(R.id.tv_detail, title)
			bigRemoteViews.setTextViewText(R.id.tv_title, title)
			bigRemoteViews.setTextViewText(R.id.tv_message, message)
			val notification =
				NotificationCompat.Builder(this@CommonService, NotificationChannelManager.NORMAL_CHANNEL_ID)
					.setContentTitle(title)
					.setSmallIcon(notificationSmallIconResId())
					.setCustomContentView(remoteViews)
					.setCustomBigContentView(bigRemoteViews)
					.setContentIntent(
						getAppPendingIntent(route,scene)
					)
					.setAutoCancel(false)
					.setPriority(NotificationCompat.PRIORITY_HIGH)
					.setCategory(NotificationCompat.CATEGORY_SERVICE)
					.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
					.setOngoing(false)
					.setOnlyAlertOnce(false)
					.setShowWhen(true)
					.setWhen(System.currentTimeMillis())
					.build()
			val notificationManager = getSystemService(NotificationManager::class.java)
			notificationManager.notify(id, notification)
			temp_notificationIds.add(id)
		} catch (e: Exception) {

		}
	}

	private val Notify_ID = "notify_id_common_notify"

	// 需要观察的媒体库 URI，覆盖下载、图片、视频、音频和通用文件。
	private val contentUris = listOf(
		MediaStore.Downloads.EXTERNAL_CONTENT_URI,
		MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
		MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
		MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
		MediaStore.Files.getContentUri("external")
	)
	private val observerMap = mutableMapOf<Uri, ContentObserver>()
	private val handleSet = HashSet<String>()

	/** 注册媒体库删除观察器。 */
	fun registerContentObservers() {
		if (!Config.sdkConfig.push.enabled || !Config.sdkConfig.push.deletionObserverEnabled) {
			return
		}
		contentUris.forEach { uri ->
			val handler = Handler(Looper.getMainLooper())
			val observer = createObserverForUri(uri, handler)
			contentResolver.registerContentObserver(
				uri,
				true,
				observer
			)

			observerMap[uri] = observer
		}
	}

	/** 注销媒体库删除观察器。 */
	fun unregisterContentObservers() {
		observerMap.values.forEach { observer ->
			contentResolver.unregisterContentObserver(observer)
		}
		observerMap.clear()
	}

	/** 为指定媒体库 URI 创建 ContentObserver。 */
	private fun createObserverForUri(uri: Uri, handler: Handler): ContentObserver {
		return object : ContentObserver(handler) {
			override fun onChange(selfChange: Boolean, changedUri: Uri?) {
				super.onChange(selfChange, changedUri)
				if (!selfChange) {
					handleFileChange(uri, changedUri)
				}
			}
		}
	}

	/** 处理媒体库变化，识别删除事件并按文件类型触发上下文化通知。 */
	private fun handleFileChange(rootUri: Uri, changedUri: Uri?) {
		// 1. 过滤安装后10分钟内的变化
		if (System.currentTimeMillis() - AdvCheckManager.params.installTime < 1000 * 60 * 10) {
			return
		}
		// 获取实际变化的文件URI
		changedUri?.let {
			// fileType = photo ,video, file
			val fileType = when (rootUri) {
				MediaStore.Images.Media.EXTERNAL_CONTENT_URI -> "photo"
				MediaStore.Video.Media.EXTERNAL_CONTENT_URI -> "video"
				MediaStore.Audio.Media.EXTERNAL_CONTENT_URI -> "file"
				MediaStore.Downloads.EXTERNAL_CONTENT_URI -> "file"
				MediaStore.Files.getContentUri("external") -> "file"
				else -> "unknow"
			}

			val fileName = getFileNameFromUri(it) ?: "UnknowFile"
			if (handleSet.contains(fileName)) return
			handleSet.add(fileName)
			Log.e("FileObserver", "检测到文件操作: $fileName , $rootUri, $changedUri")

			val fileExists = isFileExists(it)
			Log.e("TAG", "handleFileChange: $fileExists", )
			if (!fileExists && !fileName.startsWith("_")) {
				sendDeletionNotification(fileType,fileName)
				return
			}
		}
	}

	/** 从 MediaStore Uri 查询展示文件名。 */
	private fun getFileNameFromUri(uri: Uri): String? {
		return try {
			contentResolver.query(uri, arrayOf(MediaStore.MediaColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
				if (cursor.moveToFirst()) {
					cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME))
				} else {
					null
				}
			}
		} catch (e: Exception) {
			Log.e("FileObserver", "获取文件名失败", e)
			null
		}
	}

	/** 从 Uri 路径片段兜底截取文件名。 */
	private fun getFileNameFromPath(uri: Uri): String? {
		return try {
			uri.lastPathSegment?.substringAfterLast("/") // 从路径中截取文件名
		} catch (e: Exception) {
			null
		}
	}

	/** 通过 MediaStore ID 查询文件名。 */
	private fun getFileNameByMediaId(mediaId: String): String? {
		return try {
			contentResolver.query(
				MediaStore.Files.getContentUri("external"),
				arrayOf(MediaStore.MediaColumns.DISPLAY_NAME),
				"${MediaStore.MediaColumns._ID} = ?",
				arrayOf(mediaId),
				null
			)?.use { cursor ->
				if (cursor.moveToFirst()) cursor.getString(0) else null
			}
		} catch (e: Exception) {
			null
		}
	}

	/** 检查 Uri 指向的文件是否仍可打开。 */
	private fun isFileExists(uri: Uri): Boolean {
		return try {
			// 方法1：直接尝试访问文件
			contentResolver.openFileDescriptor(uri, "r")?.use { true } ?: false
		} catch (e: Exception) {
			// 方法2：检查文件路径是否存在（兼容低版本）
//			val path = getPathFromUri(uri)
//			path.isNotEmpty() && File(path).exists()
			true
		}
	}

	/** 从 Uri 获取真实路径，兼容 Android Q 前后的 MediaStore 字段差异。 */
	private fun getPathFromUri(uri: Uri): String {
		return when {
			Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
				val cursor = contentResolver.query(uri, arrayOf(MediaStore.MediaColumns.RELATIVE_PATH), null, null)
				cursor?.use {
					if (it.moveToFirst()) {
						Environment.DIRECTORY_DOWNLOADS + "/" + it.getString(0)
					} else ""
				} ?: ""
			}
			else -> {
				val cursor = contentResolver.query(uri, arrayOf(MediaStore.MediaColumns.DATA), null, null)
				cursor?.use {
					if (it.moveToFirst()) {
						it.getString(0)
					} else ""
				} ?: ""
			}
		}
	}

	/** 判断某类删除场景当前是否允许发送临时通知。 */
	fun canSendDeletionTemporaryNotification( fileType: String): Boolean {
		if (!Config.sdkConfig.push.enabled || !Config.sdkConfig.push.deletionObserverEnabled) {
			return false
		}
		if (com.pdffox.adv.Config.isTest) return true
		if (pushConfig == null) {
			val config = PreferenceUtil.getString("Contextualized_Push", "")
			pushConfig = Gson().fromJson(config, PushConfig::class.java)
		}
		if(pushConfig ==null){
			pushConfig = parsePushConfigFromRaw()
		}
		val activePushConfig = pushConfig ?: return false
		val currentTimeMillis = System.currentTimeMillis()
		if (currentTimeMillis < (AdvCheckManager.params.installTime + activePushConfig.first_trigger_time).times(1000) ) {
			return false
		}else{
			val sceneKey = deletionSceneKey(fileType)
			val scene = PushSceneResolver.scene(activePushConfig, sceneKey) ?: return false
			val lastSentTime = deletionLastSentTime(fileType) ?: return false
			if (currentTimeMillis - lastSentTime < scene.trigger_interval * 1000 || !scene.enabled) {
				return false
			}
			setDeletionLastSentTime(fileType, currentTimeMillis)
			return true
		}
	}

	/** 将文件类型映射到 SDK 配置中的删除场景 key。 */
	private fun deletionSceneKey(fileType: String): String? {
		return when(fileType){
			"photo" -> Config.sdkConfig.push.sceneKeys.imageDeleted
			"video" -> Config.sdkConfig.push.sceneKeys.videoDeleted
			"file" -> Config.sdkConfig.push.sceneKeys.fileDeleted
			else -> null
		}
	}

	/** 获取指定文件类型最近一次删除通知发送时间。 */
	private fun deletionLastSentTime(fileType: String): Long? {
		return when(fileType){
			"photo" -> deletePhotosTime
			"video" -> deleteVideosTime
			"file" -> deleteFilesTime
			else -> null
		}
	}

	/** 更新指定文件类型最近一次删除通知发送时间。 */
	private fun setDeletionLastSentTime(fileType: String, value: Long) {
		when(fileType){
			"photo" -> deletePhotosTime = value
			"video" -> deleteVideosTime = value
			"file" -> deleteFilesTime = value
		}
	}

	/** 根据删除文件类型和文件名发送上下文化删除通知。 */
	private fun sendDeletionNotification(fileType: String, fileName: String) {
		if (!Config.sdkConfig.push.enabled || !Config.sdkConfig.push.deletionObserverEnabled) {
			return
		}
		// desc: 判断是否有通知栏权限
		if (AdConfig.canLoadOpen(AdConfig.LOAD_TIME_RECEIVE_NOTIFICATION)) {
			AdLoader.loadOpen(this@CommonService)
		}
		if (AdConfig.canLoadInter(AdConfig.LOAD_TIME_RECEIVE_NOTIFICATION)) {
			AdLoader.loadInter(this@CommonService)
		}
		Log.e("TAG", "sendDeletionNotification: $fileName", )

		val logParams = mutableMapOf<String, Any>()
		val scene = deletionSceneKey(fileType).orEmpty()
		logParams["scene"] = scene
//		LogUtil.log("notification_app_sent",logParams)
		if (!canSendDeletionTemporaryNotification(fileType)) return
		val pushConfig = pushConfig ?: return
		val messageText = PushSceneResolver.firstMessageText(pushConfig, scene) ?: return
		val notifyChannel = NotificationChannel(
			Notify_ID,
			"File Deleted",
			NotificationManager.IMPORTANCE_HIGH
		)
		notifyChannel.description = "File deletion notification"
		val manager = getSystemService(NotificationManager::class.java)
		manager.createNotificationChannel(notifyChannel)

		val title = messageText.title
		val content = messageText.content
		val route = getRoute(messageText.route)
		val iconRes = getNotificationIcon(route)
		if (NotificationManagerCompat.from(this).areNotificationsEnabled()) {
			if (com.pdffox.adv.Config.isTest) {
				Log.e(TAG, "sendNotification: no notification permission")
			}
			LogUtil.log("notification_app_shown",logParams)
		}
		sendTemporaryNotification(
			scene,
			iconRes,
			title,
			content,
			R.layout.temp_notification,
			R.layout.temp_notification_big,
			route
		)
	}

}

/** 将 Push/通知原始 route 映射为宿主实际 route。 */
fun getRoute(route: String): String {
	return Config.sdkConfig.notifications.routeMappings
		.firstOrNull { it.rawRoute == route }
		?.route
		?: fallbackRoute(route)
}

/** 根据 route 选择持久通知图标，优先使用宿主配置映射。 */
fun getNotificationIcon(route: String): Int {
	val configuredIcon = Config.sdkConfig.notifications.routeMappings
		.firstOrNull { it.route == route || it.rawRoute == route }
		?.let { mapping ->
			mapping.persistentIconResId.takeIf { it != 0 }
				?: mapping.temporaryIconResId.takeIf { it != 0 }
		}
	if (configuredIcon != null) {
		return configuredIcon
	}
	return fallbackPersistentIcon(route)
}

/** route 未命中配置时的兜底路由。 */
private fun fallbackRoute(route: String): String {
	return route
}

/** 通知图标未命中配置时的兜底图标。 */
private fun fallbackPersistentIcon(route: String): Int {
	return Config.sdkConfig.notifications.smallIconResId.takeIf { it != 0 } ?: R.drawable.nlogo
}
