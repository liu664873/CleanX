package com.pdffox.adv.adv

import android.app.Activity
import android.util.Log
import com.google.android.ump.ConsentDebugSettings
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import com.pdffox.adv.BuildConfig
import com.pdffox.adv.util.PreferenceDelegate
import com.pdffox.adv.util.PreferenceUtil

/**
 * Google UMP 用户同意管理工具。
 *
 * 用于请求同意状态、启动页展示同意表单，以及在设置页展示隐私选项表单。
 */
object UMPUtil {

	private const val TAG = "UMPUtil"

	/** 当前是否需要向用户提供隐私选项入口。 */
	var isPrivacyOptionsRequired = false

	// 同意请求结果做短期缓存，避免每次启动都请求 UMP。
	var requestUMPTime: Long by PreferenceDelegate("requestUMPTime", 0L)
	var requestUMPResult: Boolean by PreferenceDelegate("requestUMPResult", false)

	/**
	 * 请求 UMP 同意信息更新。
	 *
	 * 返回 true 表示命中缓存，调用方可以继续流程；返回 false 表示已发起异步请求，结果通过 onComplete 返回。
	 */
	fun initUMP(activity: Activity, onComplete: (success: Boolean) -> Unit) : Boolean {
		val TAG = "initUMP"
		Log.e(TAG, "initUMP: ", )
		PreferenceUtil.init(activity.applicationContext)

		// 检测缓存结果
		var cacheTime = if (com.pdffox.adv.Config.isTest) {
			1000 * 60 * 1
		} else {
			1000 * 60 * 60 * 24
		}
		if (System.currentTimeMillis() - requestUMPTime < cacheTime) {
			Log.e(TAG, "initUMP: 命中缓存" )
			return true
		}

		// 测试环境模拟 EEA 地区，便于本地触发同意弹窗逻辑。
		val params = ConsentRequestParameters.Builder()
			// 如果是调试模式，可以打开下面代码，模拟测试环境
			.setConsentDebugSettings(
				if (com.pdffox.adv.Config.isTest) {
					ConsentDebugSettings.Builder(activity)
						.setDebugGeography(ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA)
						.addTestDeviceHashedId("11BF2FA96B84C5054A2D65A532710C0D")
						.build()
				} else {
					ConsentDebugSettings.Builder(activity)
						.build()
				}
			)
			.build()

		val consentInformation = UserMessagingPlatform.getConsentInformation(activity)
		if (com.pdffox.adv.Config.isTest) {
			consentInformation.reset()
		}

		val handler = android.os.Handler(activity.mainLooper)
		var isCallbackCalled = false

		// 6 秒超时任务，防止 UMP 网络请求阻塞启动流程。
		val timeoutRunnable = Runnable {
			if (!isCallbackCalled) {
				isCallbackCalled = true
				Log.e(TAG, "initUMP: requestConsentInfoUpdate 超时")
				onComplete.invoke(false)
			}
		}
		handler.postDelayed(timeoutRunnable, 6000)

		consentInformation.requestConsentInfoUpdate(
			activity,
			params,
			{
				Log.e(TAG, "initUMP: requestConsentInfoUpdate success: ${consentInformation.consentStatus} ${consentInformation.isConsentFormAvailable}", )
				if (!isCallbackCalled) {
					isCallbackCalled = true
					handler.removeCallbacks(timeoutRunnable)
					isPrivacyOptionsRequired = consentInformation.privacyOptionsRequirementStatus == ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED
					requestUMPTime = System.currentTimeMillis()
					requestUMPResult = isPrivacyOptionsRequired
					onComplete.invoke(true)
				}
			},
			{ formError ->
				Log.e(TAG, "UMP requestConsentInfoUpdate failed: ${formError.message}")
				if (!isCallbackCalled) {
					isCallbackCalled = true
					handler.removeCallbacks(timeoutRunnable)
					onComplete.invoke(false)
				}
			}
		)
		return false
	}

	/** 启动页场景展示 UMP 同意表单；无可用表单时直接继续。 */
	fun showSplashUMP(activity: Activity, onComplete: () -> Unit) {
		val consentInformation = UserMessagingPlatform.getConsentInformation(activity)
		if (consentInformation.isConsentFormAvailable) {
			// 启动页首个开屏广告展示前先加载并处理 UMP 表单，表单关闭后再继续广告链路。
			loadConsentForm(activity, onComplete)
		} else {
			// 当前地区或状态不需要 UMP 表单时，直接放行到 App Open 广告。
			onComplete.invoke()
		}
	}

	/** 加载并按当前同意状态展示 UMP 表单。 */
	private fun loadConsentForm(activity: Activity, onComplete: () -> Unit) {
		UserMessagingPlatform.loadConsentForm(
			activity,
			{ consentForm ->
				val consentInformation = UserMessagingPlatform.getConsentInformation(activity)
				if (consentInformation.consentStatus == ConsentInformation.ConsentStatus.REQUIRED) {
					// 需要用户同意时先展示 UMP 表单；表单关闭后才允许首个开屏广告继续展示。
					consentForm.show(
						activity,
						{
							// 用户完成同意后，重新检查状态并放行到开屏广告流程。
							val updatedConsentInformation = UserMessagingPlatform.getConsentInformation(activity)
							if (updatedConsentInformation.consentStatus == ConsentInformation.ConsentStatus.OBTAINED) {
								Log.e(TAG, "用户已同意")
							}
							onComplete.invoke()
						}
					)
				} else {
					// 已经有有效同意状态时不展示表单，直接进入首个开屏广告流程。
					onComplete.invoke()
				}
			},
			{ loadError ->
				Log.e(TAG, "UMP loadConsentForm failed: ${loadError.message}")
				// UMP 表单加载失败不能卡住启动页，直接继续后续开屏广告/导航链路。
				onComplete.invoke()
			}
		)
	}

	/** 展示隐私选项表单，供用户重新调整同意状态。 */
	fun showUMP(activity: Activity) {
		UserMessagingPlatform.showPrivacyOptionsForm(activity) { }
	}
}
