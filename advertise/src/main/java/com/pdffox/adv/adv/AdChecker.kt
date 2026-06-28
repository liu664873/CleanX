package com.pdffox.adv.adv

import android.util.Log
import com.pdffox.adv.adv.AdPool.admobInterPool
import com.pdffox.adv.adv.AdPool.admobOpenPool
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.collections.component1
import kotlin.collections.component2

/**
 * 广告缓存过期检查器。
 *
 * 按固定间隔清理 App Open 和插屏广告池中过期的 AdMob 广告，避免展示超过缓存有效期的广告对象。
 */
object AdChecker {

	private const val TAG = "AdChecker"

	private val scope = CoroutineScope(Dispatchers.Default)
	private var job: Job? = null

	/** 启动定时清理任务；重复调用时不会创建多个协程。 */
	fun startAutoCheck(intervalMillis: Long = 10 * 60 * 1000) {
		if (job?.isActive == true) return
		job = scope.launch {
			while (isActive) {
				checkAd()
				delay(intervalMillis)
			}
		}
	}

	/** 停止广告缓存定时清理任务。 */
	fun stopAutoCheck() {
		job?.cancel()
		job = null
	}

	/** 立即执行一次所有广告池的过期检查。 */
	fun checkAd() {
		checkExpiredAdmobInter()
		checkExpiredAdmobOpen()
	}

	/** 移除超过 [AdConfig.adload_cache_time] 的插屏广告缓存。 */
	fun checkExpiredAdmobInter() {
		admobInterPool.entries.removeIf { (_, time) ->
			val cur = System.currentTimeMillis()
			Log.e(TAG, "checkExpiredAdmobInter: $cur $time ${AdConfig.adload_cache_time}")
			cur - time > AdConfig.adload_cache_time
		}
	}

	/** 移除超过 [AdConfig.adload_cache_time] 的 App Open 广告缓存。 */
	fun checkExpiredAdmobOpen() {
		admobOpenPool.entries.removeIf { (_, time) ->
			System.currentTimeMillis() - time > AdConfig.adload_cache_time
		}
	}

}
