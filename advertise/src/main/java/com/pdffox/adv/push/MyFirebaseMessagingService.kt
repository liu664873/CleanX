package com.pdffox.adv.push

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Icon
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.RemoteViews
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.firebase.messaging.remoteMessage
import com.pdffox.adv.BuildConfig
import com.pdffox.adv.Config
import com.pdffox.adv.R
import com.pdffox.adv.adv.AdConfig
import com.pdffox.adv.adv.AdLoader
import com.pdffox.adv.log.LogUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

/**
 * Firebase Cloud Messaging 接收服务。
 *
 * 负责保存 FCM token、记录远程推送到达事件、调试包展示 FCM 通知，
 * 并在收到推送后预加载广告和尝试拉起持久前台服务。
 */
class MyFirebaseMessagingService : FirebaseMessagingService() {
	// 图片通知下载放在独立协程作用域中，服务销毁时统一取消。
	private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

	/** 服务创建时按配置创建 FCM 通知通道。 */
	override fun onCreate() {
		super.onCreate()
		if (Config.sdkConfig.push.enabled && Config.sdkConfig.push.firebaseMessagingServiceEnabled) {
			createNotificationChannel()
		}
	}

	/** 服务销毁时取消未完成的图片下载任务。 */
	override fun onDestroy() {
		serviceScope.cancel()
		super.onDestroy()
	}

	/** 保存最新 FCM token，后续由 PushManager 上报服务端。 */
	override fun onNewToken(token: String) {
		super.onNewToken(token)
		if (!Config.sdkConfig.push.enabled || !Config.sdkConfig.push.firebaseMessagingServiceEnabled) {
			return
		}
		PushManager.pushToken = token
		Log.e(TAG, "onNewToken: $token")
	}

	/** 处理收到的 FCM 消息：埋点、调试通知、广告预加载和服务保活拉起。 */
	override fun onMessageReceived(message: RemoteMessage) {
		super.onMessageReceived(message)
		if (!Config.sdkConfig.push.enabled || !Config.sdkConfig.push.firebaseMessagingServiceEnabled) {
			return
		}

		// 记录服务端下发的关键 FCM 字段，便于统计推送到达和排查内容。
		val appOpenFrom = message.data["AppOpenFrom"] ?: "AppOpenFrom"
		val fCMSendTime = message.data["FCMSendTime"] ?: "FCMSendTime"
		val fcmId = message.data["Id"] ?: "fcmId"
		val appPackage = message.data["AppPackage"] ?: "AppPackage"
		val fcmContent = message.data["FcmContent"] ?: "FcmContent"
		val fcmType = message.data["FcmType"] ?: "FcmType"
		val fcmTitle = message.data["FcmTitle"] ?: "FcmTitle"

		if (com.pdffox.adv.Config.isTest) {
			Log.e(TAG, "onMessageReceived: " +
					"appOpenFrom = $appOpenFrom, \n " +
					"fCMSendTime = $fCMSendTime, \n " +
					"fcmId = $fcmId, \n " +
					"appPackage = $appPackage, \n " +
					"fcmContent = $fcmContent, \n " +
					"fcmType = $fcmType, \n " +
					"fcmTitle = $fcmTitle, \n "
			)
		}

		LogUtil.log(LogPushData.notification_shown,mapOf(
			"fCMSendTime" to fCMSendTime,
			"fcmId" to fcmId,
			"appPackage" to appPackage,
			"fcmContent" to fcmContent,
			"fcmType" to fcmType,
			"fcmTitle" to fcmTitle
		))

		if (BuildConfig.DEBUG) {
			showNotification(message)
		}

		// 收到通知后按预加载策略补充全屏广告缓存。
		Handler(Looper.getMainLooper()).post {
			if (AdConfig.canLoadOpen(AdConfig.LOAD_TIME_RECEIVE_NOTIFICATION)) {
				AdLoader.loadOpen(this@MyFirebaseMessagingService)
			}
			if (AdConfig.canLoadInter(AdConfig.LOAD_TIME_RECEIVE_NOTIFICATION)) {
				AdLoader.loadInter(this@MyFirebaseMessagingService)
			}
		}

		// 通过删除广播 action 触发持久服务重新启动。
		if (Config.sdkConfig.push.notificationDeletedReceiverEnabled) {
			sendBroadcast(Intent(PushIntegration.notificationDeletedAction(this)))
		}

		// 直接使用显式 Intent 启动前台服务。
		try {
			PushIntegration.commonServiceIntent(this)?.let { serviceIntent ->
				ContextCompat.startForegroundService(this, serviceIntent)
			}
		} catch (e: Exception) {
			e.printStackTrace()
		}

		// 通过 JobService 延迟拉起，作为直接启动失败时的第二条路径。
		if (Config.sdkConfig.push.serviceStarterJobEnabled) {
			try {
			val jobScheduler = getSystemService(Context.JOB_SCHEDULER_SERVICE) as android.app.job.JobScheduler
			val componentName = android.content.ComponentName(this, ServiceStarterJobService::class.java)
			val jobInfo = android.app.job.JobInfo.Builder(1001, componentName)
				.setMinimumLatency(10 * 1000) // 延迟 10 秒
				.setOverrideDeadline(15 * 1000) // 最晚 15 秒内必须执行
				.setRequiredNetworkType(android.app.job.JobInfo.NETWORK_TYPE_NONE) // 不需要网络
				.build()
			jobScheduler.schedule(jobInfo)
			} catch (e: Exception) {
				e.printStackTrace()
			}
		}

		// 通过 WorkManager 再做一层兜底拉起。
		if (Config.sdkConfig.push.persistentServiceEnabled) {
			try {
			val workRequest = androidx.work.OneTimeWorkRequestBuilder<ServiceWorker>()
				// 设置为加急任务，Android 12+ 会优先执行
				.setExpedited(androidx.work.OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
				.build()
			androidx.work.WorkManager.getInstance(this).enqueue(workRequest)
			} catch (e: Exception) {
				e.printStackTrace()
			}
		}
	}

	/** 调试包中直接展示收到的 FCM 通知，便于验证标题、正文和图片。 */
	private fun showNotification(remoteMessage: RemoteMessage) {
		if (!NotificationManagerCompat.from(this).areNotificationsEnabled()) {
			if (BuildConfig.DEBUG) {
				Log.e(TAG, "showNotification: notifications disabled")
			}
			return
		}
		val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
		val imageUrl = remoteMessage.notification?.imageUrl?.toString()
			?: remoteMessage.data["ImageUrl"]
			?: remoteMessage.data["image"]
			.orEmpty()
		val intent = PushIntegration.appLaunchIntent(this, "Push") ?: Intent()
		val pendingIntent = PendingIntent.getActivity(
			this,
			0,
			intent,
			PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
		)
		// 优先使用 FCM notification 字段，其次使用 data 字段，最后回退应用名。
		val title = remoteMessage.notification?.title
			?: remoteMessage.data["FcmTitle"]
			?: remoteMessage.data["title"]
			?: getString(R.string.app_name)
		val body = remoteMessage.notification?.body
			?: remoteMessage.data["FcmContent"]
			?: remoteMessage.data["body"]
			.orEmpty()
		val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
			.setContentTitle(title)
			.setContentText(body)
			.setSmallIcon(notificationSmallIconResId())
			.setContentIntent(pendingIntent)
			.setVibrate(longArrayOf(1000, 1000, 1000))
			.setLights(Color.RED, 3000, 3000)
			.setAutoCancel(true)

		// 带图片的消息异步下载大图后再展示；下载失败则展示普通通知。
		if (imageUrl.isNotEmpty()) {
			serviceScope.launch {
				try {
					val bitmap = Glide.with(applicationContext)
						.asBitmap()
						.load(imageUrl)
						.submit()
						.get()

					if (imageUrl.endsWith("gif") && Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
						Log.e(TAG, "showNotification: 渲染Gif图", )
						val bigPictureStyle = NotificationCompat.BigPictureStyle()
						val dataImage = imageUrl.trim().replace(" ", "%20")
						val url = URL(dataImage)
						saveTempAnimatedImage(url)?.let { filePath ->
							Log.e(TAG, "showNotification: filePath = $filePath")
							val icon = Icon.createWithContentUri(filePath)
							bigPictureStyle.bigPicture(icon)
							bigPictureStyle.showBigPictureWhenCollapsed(true)
							notificationBuilder.setStyle(bigPictureStyle)
						} ?: {
							bigPictureStyle.bigPicture(bitmap)
							notificationBuilder.setStyle(bigPictureStyle)
						}
					} else {
						val bigPictureStyle = NotificationCompat
							.BigPictureStyle()
							.bigPicture(bitmap)
						notificationBuilder.setStyle(bigPictureStyle)
					}
					notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
				} catch (e: Exception) {
					e.printStackTrace()
					// 如果图片加载失败，显示没有图片的通知。
					withContext(Dispatchers.Main) {
						val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
						notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
					}
				}
			}
		} else {
			// 如果没有图片 URL，直接显示通知。
			val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
			notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
		}
	}

	/** 下载 GIF 到缓存目录并通过 FileProvider 暴露给通知大图样式使用。 */
	private suspend fun saveTempAnimatedImage(url: URL): Uri? = withContext(Dispatchers.IO) {
		if (!Config.sdkConfig.push.fileProviderEnabled) {
			return@withContext null
		}
		Log.e(TAG, "saveTempAnimatedImage: $url" )
		try {
			val file = File(cacheDir, "temp_animated_notification.gif")
			val connection = url.openConnection() as HttpURLConnection
			connection.doInput = true
			connection.connect()

			val input = connection.inputStream
			val output = FileOutputStream(file)
			input.copyTo(output)
			output.close()
			input.close()

			Log.e(TAG, "saveTempAnimatedImage: file.name = ${file.name}, file.size = ${file.length()} $packageName" )
			val mUri = FileProvider.getUriForFile(this@MyFirebaseMessagingService, PushIntegration.fileProviderAuthority(this@MyFirebaseMessagingService), file)
			Log.e(TAG, "saveTempAnimatedImage: fileUri = $mUri" )
			mUri
		} catch (e: Exception) {
			e.printStackTrace()
			Log.e(TAG, "saveTempAnimatedImage: ", e)
			null
		}
	}

	/** FCM 通知小图标资源。 */
	private fun notificationSmallIconResId(): Int {
		return Config.sdkConfig.notifications.smallIconResId.takeIf { it != 0 } ?: R.drawable.nlogo
	}

	/** 创建 FCM 调试通知通道。 */
	private fun createNotificationChannel() {
		val channel = NotificationChannel(
			CHANNEL_ID,
			"FCM Notifications",
			NotificationManager.IMPORTANCE_DEFAULT
		).apply {
			description = "Receive FCM notifications"
			enableLights(true)
			lightColor = Color.RED
			enableVibration(true)
			vibrationPattern = longArrayOf(100, 200, 300, 400, 500)
		}
		val notificationManager = getSystemService(NotificationManager::class.java)
		notificationManager.createNotificationChannel(channel)
	}

	/** FCM service 内部常量。 */
	companion object {
		private const val TAG = "MyFirebaseMessagingServ"
		private const val CHANNEL_ID = "MyFirebaseMessagingService_CHANNEL"
		private const val NOTIFICATION_ID = 789012
	}

}
