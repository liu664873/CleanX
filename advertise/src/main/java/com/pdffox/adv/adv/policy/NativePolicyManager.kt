package com.pdffox.adv.adv.policy

import android.content.Context
import android.util.Log
import com.pdffox.adv.Config
import com.pdffox.adv.adv.policy.data.AdNativePolicy
import com.pdffox.adv.adv.policy.data.AdUnit
import com.pdffox.adv.adv.policy.data.parseAdNativePolicy

/**
 * Native 广告策略管理器。
 *
 * Native 广告使用独立策略和播放记录，便于和全屏/Banner 广告分开控制展示频率。
 */
object NativePolicyManager {

	private const val TAG = "NativePolicyManager"

	/** 当前生效的 Native 广告策略。 */
	var adPolicy: AdNativePolicy? = null

	/** 从宿主配置的本地 raw 资源读取兜底 Native 广告策略。 */
	fun loadPolicyFromLocal(context: Context) {
		val resId = Config.resourceConfig.nativeAdPolicyRawResId
		if (resId == 0) {
			return
		}
		val strPolicy = context.resources.openRawResource(resId).bufferedReader().use { it.readText() }
		setPolicyFromJson(strPolicy)
	}

	/** 解析并应用 Native 广告策略 JSON。 */
	fun setPolicyFromJson(jsonString: String) {
		if (jsonString == "") {
			return
		}
		if (jsonString.isBlank()) {
			return
		}
		setPolicy(parseAdNativePolicy(jsonString))
	}

	/** 直接设置已解析的 Native 广告策略对象。 */
	fun setPolicy(adPolicy: AdNativePolicy) {
		this.adPolicy = adPolicy
	}

	/** 根据广告区域 key 查找 Native 广告位策略。 */
	fun getAdUnit(areakey: String): AdUnit? {
		return adPolicy?.ad_units?.find { it.areakey == areakey }
	}

	/** 检查指定 Native 广告区域当前是否允许展示。 */
	fun checkAdUnit(areakey: String): Boolean {
		if (areakey == "debug_page") {
			return true
		}
		val adUnit = getAdUnit(areakey)
		val result = checkAdUnit(adUnit)
		if (com.pdffox.adv.Config.isTest) {
			Log.e(TAG, "checkAdUnit: $areakey adUnit = $adUnit result = $result" )
		}
		return result
	}

	/** 对已找到的 Native 广告位执行限量、频控和概率校验。 */
	fun checkAdUnit(adUnit: AdUnit?): Boolean {
		if (adUnit == null) {
			if (com.pdffox.adv.Config.isTest) {
				Log.e(TAG, "checkAdUnit: adUnit is null" )
			}
			return false
		}
		return checkLimit() &&
				checkAdUnitsFrequency(adUnit) &&
				checkCheckAdUnitRate(adUnit)
	}

	/** 检查 Native 广告全局播放次数限制。 */
	fun checkLimit(): Boolean {
		val limited = adPolicy?.limited ?: 50
		val limitedLoadtimeSeconds = adPolicy?.limited_loadtime_seconds ?: 86400
		if (System.currentTimeMillis() - NativeAdPlayRecordManager.lastLimitedTime < limitedLoadtimeSeconds * 1000) {
			return false
		}
		val playCount = NativeAdPlayRecordManager.getAllPlayTime(NativeAdPlayRecordManager.lastLimitedTime)
		if (playCount >= limited) {
			NativeAdPlayRecordManager.lastLimitedTime = System.currentTimeMillis()
			if (com.pdffox.adv.Config.isTest) {
				Log.e(TAG, "checkLimit: playCount = $playCount limited = $limited")
			}
			return false
		}
		return true
	}

	/** 检查单个 Native 广告位的间隔、小时上限和 24 小时上限。 */
	fun checkAdUnitsFrequency(adUnit: AdUnit): Boolean {
		val frequencyCaps = adUnit.frequency_caps
		val maxPerHour = frequencyCaps.max_per_hour
		val maxPerDay = frequencyCaps.max_per_day
		val intervalSeconds = frequencyCaps.interval_seconds

		val lastTime = NativeAdPlayRecordManager.getAdLastPlayTime(adUnit.areakey)
		if (com.pdffox.adv.Config.isTest) {
			Log.e(TAG, "checkAdUnitsFrequency: areakey = ${adUnit.areakey} lastTime = $lastTime")
		}
		// 检查广告位是否在最近的时间间隔内播放过
		if (lastTime > 0 && System.currentTimeMillis() - lastTime < intervalSeconds * 1000) {
			return false
		}

		// 检查广告位在最近的时间间隔内是否播放次数超过限制
		val playCount = NativeAdPlayRecordManager.getAdPlayCount(adUnit.areakey, 60 * 60)
		if (com.pdffox.adv.Config.isTest) {
			Log.e(TAG, "checkAdUnitsFrequency: areakey = ${adUnit.areakey} playCount = $playCount maxPerHour = $maxPerHour")
		}
		if (playCount >= maxPerHour) {
			return false
		}

		// 检查广告位在最近的24小时内是否播放次数超过限制
		val playCount24h = NativeAdPlayRecordManager.getAdPlayCount(adUnit.areakey, 24 * 60 * 60)
		if (com.pdffox.adv.Config.isTest) {
			Log.e(TAG, "checkAdUnitsFrequency: areakey = ${adUnit.areakey} playCount24h = $playCount24h maxPerDay = $maxPerDay")
		}
		if (playCount24h >= maxPerDay) {
			return false
		}

		return true
	}

	/** 按广告位 rate 做随机抽样，控制 Native 广告实际展示概率。 */
	fun checkCheckAdUnitRate(adUnit: AdUnit): Boolean {
		val rate = adUnit.rate
		val randomValue = kotlin.random.Random.nextDouble(0.0, 1.0)
		return randomValue <= rate
	}

}
