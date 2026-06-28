package com.pdffox.adv.adv

import android.app.Application
import com.google.android.gms.ads.MobileAds
import com.pdffox.adv.Config
import com.pdffox.adv.log.LogAdData
import com.pdffox.adv.log.LogAdParam
import com.pdffox.adv.log.LogUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * 广告 SDK 初始化器。
 *
 * 当前主要负责初始化 AdMob，并在初始化后启动 App Open 前后台生命周期监听。
 */
object AdvInit {
	@Volatile
	private var admobInitStarted = false

	/** 初始化已开启的广告平台和关联的展示辅助器。 */
	fun initAdv(context: Application) {
		if (Config.sdkConfig.adMob.enabled) {
			initAdmob(context)
		}
		if (Config.sdkConfig.adMob.enabled) {
			AppOpenHelper.startObserve()
		}
	}

	/** 初始化 AdMob SDK；重复调用只会执行一次，初始化耗时会通过埋点上报。 */
	fun initAdmob(context: Application) {
		if (admobInitStarted) {
			return
		}
		admobInitStarted = true
		CoroutineScope(Dispatchers.IO).launch {
			val advInitTime = System.currentTimeMillis()
			if (!Config.openAdmobMediation) {
				MobileAds.disableMediationAdapterInitialization(context)
			}
			MobileAds.initialize(context) { initializationStatus ->
				// 初始化完成后记录耗时，并在主线程标记 AppOpenHelper 可展示广告。
				LogUtil.log(
					LogAdData.adv_sdk_initcomplete,
					mapOf(LogAdParam.ad_platform to LogAdParam.ad_platform_admob, LogAdParam.duration to (System.currentTimeMillis() - advInitTime))
				)
				CoroutineScope(Dispatchers.Main).launch {
					AppOpenHelper.hasInitAdmob = true
				}
			}
		}
	}

}
