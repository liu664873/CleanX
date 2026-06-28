package com.pdffox.adv.update

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.IntentSender
import android.net.Uri
import android.util.Log
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability

/**
 * 强制更新辅助器。
 *
 * 优先使用 Google Play in-app update 的立即更新流程；不可用或失败时回退到 Play 商店详情页。
 */
object AppUpdateHelper {

	private const val TAG = "AppUpdateHelper"
	private const val UPDATE_REQUEST_CODE = 4001

	/** 检查是否有可用更新并启动立即更新流程，失败时打开商店页。 */
	fun forceImmediateUpdate(activity: Activity) {
		val appUpdateManager = AppUpdateManagerFactory.create(activity)
		appUpdateManager.appUpdateInfo
			.addOnSuccessListener { appUpdateInfo ->
				when (appUpdateInfo.updateAvailability()) {
					UpdateAvailability.UPDATE_AVAILABLE -> {
						if (appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
							startImmediateUpdate(appUpdateManager, appUpdateInfo, activity)
						} else {
							openStore(activity)
						}
					}
					UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS -> {
						startImmediateUpdate(appUpdateManager, appUpdateInfo, activity)
					}
					else -> {
						openStore(activity)
					}
				}
			}
			.addOnFailureListener { exception ->
				Log.e(TAG, "forceImmediateUpdate: failed to query availability", exception)
				openStore(activity)
			}
	}

	/** 启动 Google Play 立即更新流程。 */
	private fun startImmediateUpdate(
		appUpdateManager: AppUpdateManager,
		appUpdateInfo: AppUpdateInfo,
		activity: Activity,
	) {
		try {
			val flowStarted = appUpdateManager.startUpdateFlowForResult(
				appUpdateInfo,
				activity,
				AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE)
					.setAllowAssetPackDeletion(true)
					.build(),
				UPDATE_REQUEST_CODE,
			)
			if (!flowStarted) {
				openStore(activity)
			}
		} catch (intentException: IntentSender.SendIntentException) {
			Log.e(TAG, "startImmediateUpdate: unable to start flow", intentException)
			openStore(activity)
		}
	}

	/** 打开 Play 商店详情页；没有商店 App 时回退网页，最后关闭当前任务栈。 */
	private fun openStore(activity: Activity) {
		val packageName = activity.packageName
		val marketIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName"))
			.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
		try {
			activity.startActivity(marketIntent)
		} catch (activityNotFound: ActivityNotFoundException) {
			val webIntent = Intent(
				Intent.ACTION_VIEW,
				Uri.parse("https://play.google.com/store/apps/details?id=$packageName"),
			).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
			activity.startActivity(webIntent)
		} finally {
			activity.finishAffinity()
		}
	}
}
