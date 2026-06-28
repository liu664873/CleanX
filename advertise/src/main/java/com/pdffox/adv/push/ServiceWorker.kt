package com.pdffox.adv.push

import android.content.Context
import androidx.core.content.ContextCompat
import com.pdffox.adv.Config
import androidx.work.Worker
import androidx.work.WorkerParameters

/** 通过 WorkManager 拉起持久前台服务的兜底任务。 */
class ServiceWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

	/** 执行一次服务启动；成功返回 success，启动失败返回 failure。 */
	override fun doWork(): Result {
		// Create the host service intent only when the host app configured one.
		if (!Config.sdkConfig.push.enabled || !Config.sdkConfig.push.persistentServiceEnabled) {
			return Result.success()
		}
		val serviceIntent = PushIntegration.commonServiceIntent(applicationContext) ?: return Result.success()

		try {
			ContextCompat.startForegroundService(applicationContext, serviceIntent)
			return Result.success()
		} catch (e: Exception) {
			e.printStackTrace()
			return Result.failure()
		}
	}
}
