package com.pdffox.adv.adv.policy

import android.content.Context
import android.util.Log
import com.pdffox.adv.AdvRuntime
import com.pdffox.adv.Config
import com.pdffox.adv.adv.policy.data.AdPolicy
import com.pdffox.adv.adv.policy.data.AdUnit
import com.pdffox.adv.adv.policy.data.parseAdPolicy
import com.pdffox.adv.util.PreferenceUtil

/**
 * 全屏/Banner 广告策略管理器。
 *
 * 负责加载广告策略 JSON，并在广告展示前按全局开关、总量限制、广告位频控和概率进行拦截。
 */
object AdPolicyManager {

	private const val TAG = "AdPolicyManager"

	/** 当前生效的广告策略，来自本地 raw 或 Remote Config。 */
	var adPolicy: AdPolicy? = null

	/** 从本地 raw 资源读取兜底广告策略。 */
	fun loadPolicyFromLocal(context: Context) {
		val inputStream = context.resources.openRawResource(Config.resourceConfig.adPolicyRawResId)
		val strPolicy = inputStream.bufferedReader().use { it.readText() }
		setPolicyFromJson(strPolicy)
	}

	/** 解析并应用广告策略 JSON；包名不匹配时跳过，避免错用其他宿主策略。 */
	fun setPolicyFromJson(jsonString: String) {
		if (jsonString.isBlank()) {
			return
		}
		if (com.pdffox.adv.Config.isTest) {
			Log.e(TAG, "setPolicyFromJson: $jsonString" )
		}
		val adPolicy = parseAdPolicy(jsonString)

		if (com.pdffox.adv.Config.isTest) {
			Log.e(TAG, "setPolicyFromJson: packageName = ${AdvRuntime.currentPackageName()} adPolicy = $adPolicy")
		}
		val packageName = AdvRuntime.currentPackageName()
		if (adPolicy.package_name.isNotBlank() && packageName != adPolicy.package_name) {
			Log.w(TAG, "setPolicyFromJson: skip policy for ${adPolicy.package_name}, current package is $packageName")
			return
		}
		setPolicy(adPolicy)
	}

	/** 直接设置已解析的广告策略对象。 */
	fun setPolicy(adPolicy: AdPolicy) {
		this.adPolicy = adPolicy
	}

	/** 根据广告区域 key 查找对应广告位策略。 */
	fun getAdUnit(areakey: String): AdUnit? {
		return adPolicy?.ad_units?.find { it.areakey == areakey }
	}

	/** 检查指定广告区域 key 当前是否允许展示。 */
	fun checkAdUnit(areakey: String): Boolean {
		// 启动页首个开屏广告会传入 openPageAdv，在这里找到对应广告位策略并执行完整校验。
		val adUnit = getAdUnit(areakey)
		val result = checkAdUnit(adUnit)
		if (com.pdffox.adv.Config.isTest) {
			logDebug("checkAdUnit: $areakey ${adUnit?.rate} ${adUnit?.frequency_caps} result = $result")
		}
		return result
	}

	fun checkInterstitialAdUnit(areakey: String): Boolean {
		return checkAdUnit(areakey)
	}

	/** 对已找到的广告位策略执行完整策略校验。 */
	fun checkAdUnit(adUnit: AdUnit?): Boolean {
		if (adUnit == null) {
			// 本地/远程策略中找不到该广告位时，按不展示处理并继续业务流程。
			return false
		}
		val checkGlobalAdSwitch = checkGlobalAdSwitch()
		val checkLimit = checkLimit()
		val checkAdUnitsFrequency = checkAdUnitsFrequency(adUnit)
		val checkCheckAdUnitRate = checkCheckAdUnitRate(adUnit)
		Log.e(TAG, "checkAdUnit: " +
				"checkGlobalAdSwitch = $checkGlobalAdSwitch, " +
				"checkLimit = $checkLimit, " +
				"checkAdUnitsFrequency = $checkAdUnitsFrequency, " +
				"checkCheckAdUnitRate = $checkCheckAdUnitRate"
		)
		return checkGlobalAdSwitch &&
				checkLimit &&
				checkAdUnitsFrequency &&
				checkCheckAdUnitRate
	}

	/** 全局广告开关，关闭时所有广告位都不能展示。 */
	fun checkGlobalAdSwitch(): Boolean {
		return adPolicy?.global_ad_switch ?: false
	}

	/** 检查全局播放次数限制，达到上限后进入 limited_loadtime_seconds 冷却窗口。 */
	fun checkLimit(): Boolean {
		val limited = adPolicy?.limited ?: 50
		val limitedLoadtimeSeconds = adPolicy?.limited_loadtime_seconds ?: 86400
		if (System.currentTimeMillis() - AdPlayRecordManager.lastLimitedTime < limitedLoadtimeSeconds * 1000) {
			return false
		}
		val playCount = AdPlayRecordManager.getAllPlayTime(AdPlayRecordManager.lastLimitedTime)
		if (playCount >= limited) {
			AdPlayRecordManager.lastLimitedTime = System.currentTimeMillis()
			if (com.pdffox.adv.Config.isTest) {
				Log.e(TAG, "checkLimit: playCount = $playCount limited = $limited")
			}
			return false
		}
		return true
	}

	/** 检查单个广告位的间隔、小时上限和 24 小时上限。 */
	fun checkAdUnitsFrequency(adUnit: AdUnit): Boolean {
		val frequencyCaps = adUnit.frequency_caps
		val maxPerHour = frequencyCaps.max_per_hour
		val maxPerDay = frequencyCaps.max_per_day
		val intervalSeconds = frequencyCaps.interval_seconds

		val lastTime = AdPlayRecordManager.getAdLastPlayTime(adUnit.areakey)
		if (com.pdffox.adv.Config.isTest) {
			Log.e(TAG, "checkAdUnitsFrequency: areakey = ${adUnit.areakey} lastTime = $lastTime")
		}
		// 检查广告位是否在最近的时间间隔内播放过
		if (lastTime > 0 && System.currentTimeMillis() - lastTime < intervalSeconds * 1000) {
			Log.e(TAG, "checkAdUnitsFrequency: 在最近的时间间隔内播放过" )
			return false
		}

		// 检查广告位在最近的时间间隔内是否播放次数超过限制
		val playCount = AdPlayRecordManager.getAdPlayCount(adUnit.areakey, 60 * 60)
		if (com.pdffox.adv.Config.isTest) {
			Log.e(TAG, "checkAdUnitsFrequency: areakey = ${adUnit.areakey} playCount = $playCount maxPerHour = $maxPerHour")
		}
		if (playCount >= maxPerHour) {
			Log.e(TAG, "checkAdUnitsFrequency: 超过每日播放次数最大限制" )
			return false
		}

		// 检查广告位在最近的24小时内是否播放次数超过限制
		val playCount24h = AdPlayRecordManager.getAdPlayCount(adUnit.areakey, 24 * 60 * 60)
		if (com.pdffox.adv.Config.isTest) {
			Log.e(TAG, "checkAdUnitsFrequency: areakey = ${adUnit.areakey} playCount24h = $playCount24h maxPerDay = $maxPerDay")
		}
		if (playCount24h >= maxPerDay) {
			Log.e(TAG, "checkAdUnitsFrequency: 最近的24小时内播放次数超过限制", )
			return false
		}

		return true
	}

	/** 按广告位 rate 做随机抽样，控制实际展示概率。 */
	fun checkCheckAdUnitRate(adUnit: AdUnit): Boolean {
		val rate = adUnit.rate
		val randomValue = kotlin.random.Random.nextDouble(0.0, 1.0)
		return randomValue <= rate
	}

	private fun logDebug(message: String) {
		runCatching { Log.e(TAG, message) }
	}

}
