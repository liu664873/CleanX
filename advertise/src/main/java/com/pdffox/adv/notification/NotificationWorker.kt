package com.pdffox.adv.notification

import android.content.Context
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.pdffox.adv.Config
import java.util.concurrent.TimeUnit

/** WorkManager 定时通知任务。 */
class NotificationWorker(appContext: Context, workerParams: WorkerParameters) : Worker(appContext, workerParams) {
	/** 执行通知发送，并在成功后安排下一天同类型通知。 */
	override fun doWork(): Result {
		if (!Config.sdkConfig.notifications.enabled) {
			return Result.success()
		}
		val notificationType = inputData.getString("notificationType") ?: return Result.failure()
		NotificationManager.sendNotificationBatchIfAllowed(applicationContext, notificationType, notificationType)
		scheduleNextWork(notificationType)
		return Result.success()
	}

	/** 当前任务执行后重新安排 24 小时后的下一次任务。 */
	private fun scheduleNextWork(notificationType: String) {
		if (!Config.sdkConfig.notifications.enabled) {
			return
		}
		val workRequest = OneTimeWorkRequestBuilder<NotificationWorker>()
			.setInitialDelay(24 * 60L, TimeUnit.MINUTES)
			.setInputData(workDataOf("notificationType" to notificationType))
			.build()
		WorkManager.getInstance(applicationContext).enqueue(workRequest)
	}
}
