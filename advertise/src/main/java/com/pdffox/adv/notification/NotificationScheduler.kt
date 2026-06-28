package com.pdffox.adv.notification

import android.content.Context
import android.util.Log
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.pdffox.adv.BuildConfig
import java.util.Calendar
import java.util.concurrent.TimeUnit

/** 定时通知调度器，基于 WorkManager 安排每日一次的通知任务。 */
object NotificationScheduler {

	private const val TAG = "NotificationScheduler"

	/** 调度下一次指定小时/分钟触发的通知任务。 */
	fun scheduleDailyNotification(context: Context, notificationType: String, hour: Int, minute: Int) {
		if (com.pdffox.adv.Config.isTest) {
			Log.e(TAG, "scheduleDailyNotification: notificationType = $notificationType, hour = $hour, minute = $minute")
		}
		val delay = calculateInitialDelay(hour, minute)
		val workRequest = OneTimeWorkRequestBuilder<NotificationWorker>()
			.setInitialDelay(delay, TimeUnit.MILLISECONDS)
			.setInputData(workDataOf("notificationType" to notificationType))
			.addTag("daily_notification")
			.build()
		WorkManager.getInstance(context).enqueue(workRequest)
	}

	/** 取消所有每日通知任务。 */
	fun cancelDailyNotification(context: Context) {
		WorkManager.getInstance(context).cancelAllWorkByTag("daily_notification")
	}

	/** 计算距离下一次目标时间的延迟，今天已过则顺延到明天。 */
	private fun calculateInitialDelay(hour: Int, minute: Int): Long {
		val now = Calendar.getInstance()
		val target = Calendar.getInstance().apply {
			set(Calendar.HOUR_OF_DAY, hour)
			set(Calendar.MINUTE, minute)
			set(Calendar.SECOND, 0)
			set(Calendar.MILLISECOND, 0)
		}
		if (target.before(now)) {
			target.add(Calendar.DAY_OF_YEAR, 1)
		}
		return target.timeInMillis - now.timeInMillis
	}
}
