package com.pdffox.adv.push

import android.util.Log
import cn.thinkingdata.analytics.TDAnalytics
import com.pdffox.adv.Config
import com.pdffox.adv.util.PreferenceDelegate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.Locale

/** Push token 上报服务端的请求体。 */
@Serializable
data class PushData(
	val PackageName: String,
	val Token: String,
	val Language: String,
	val DistinctId: String,
)

/**
 * Push 服务端同步管理器。
 *
 * 目前用于在 App 退出/后台场景把包名、策略 Tag、FCM token、语言和用户 distinctId 上报服务端。
 */
object PushManager {

	private const val TAG = "PushManager"

	/** 当前 FCM token，收到新 token 后持久化。 */
	var pushToken: String by PreferenceDelegate("pushToken", "")

	/** 通知服务端 App 已退出或进入后台，以便服务端做推送策略判断。 */
	fun notifyServerAppExit() {
		if (!Config.sdkConfig.push.enabled || !Config.isServerEnabled || Config.PushUrl.isBlank()) {
			return
		}
		CoroutineScope(Dispatchers.IO).launch {
			val client = OkHttpClient.Builder()
				.connectTimeout(2, java.util.concurrent.TimeUnit.SECONDS)
				.readTimeout(2, java.util.concurrent.TimeUnit.SECONDS)
				.writeTimeout(2, java.util.concurrent.TimeUnit.SECONDS)
				.build()

			val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
			val pushData = PushData(
				PackageName = Config.packageName,
				Token = pushToken,
				Language = Locale.getDefault().language,
				// ThinkingData 关闭时不读取 distinctId，避免初始化未开启的 SDK。
				if (Config.sdkConfig.thinking.enabled) TDAnalytics.getDistinctId() else ""
			)
			Log.e(TAG, "notifyServerAppExit: $pushData" )
			val jsonBody = Json.encodeToString(pushData).toRequestBody(mediaType)
			val request = Request.Builder()
				.url(Config.PushUrl)
				.post(jsonBody)
				.build()
			try {
				client.newCall(request).execute().use { response ->
					val body = response.body.string()
					Log.e(TAG, "notifyServerAppExit:  $body")
				}
			} catch (e: Exception) {
				Log.e(TAG, "Exception notifyServerAppExit: $e")
			}
		}
	}

}
