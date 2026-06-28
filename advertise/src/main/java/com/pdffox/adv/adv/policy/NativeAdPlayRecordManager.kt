package com.pdffox.adv.adv.policy

import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.pdffox.adv.BuildConfig
import com.pdffox.adv.adv.policy.data.AdRecord
import com.pdffox.adv.util.PreferenceUtil

/**
 * Native 广告播放记录管理器。
 *
 * 记录 Native 广告展示时间，用于 Native 独立策略中的频控和总量限制。
 */
object NativeAdPlayRecordManager {

	private const val TAG = "NativeAdPlayRecordManager"

	private val gson = Gson()
	private const val PREF_KEY = "native_ad_records_key"
	private const val PREF_LAST_LIMITED_TIME_KEY = "native_last_limited_time_key"

	/** 已持久化的 Native 广告播放记录列表。 */
	var ad_records: List<AdRecord> = emptyList()
		set(value) {
			field = value
			val json = gson.toJson(field)
			if (com.pdffox.adv.Config.isTest) {
				Log.e(TAG, "set value: ad_records = $value")
			}
			PreferenceUtil.commitString(PREF_KEY, json)
		}
		get() {
			val strRecords = PreferenceUtil.getString(PREF_KEY, "")
			if (strRecords!!.isNotEmpty()) {
				val type = object : TypeToken<List<AdRecord>>() {}.type
				field = gson.fromJson(strRecords, type)
			}
			return field
		}

	/** 最近一次触发 Native 全局播放上限的时间。 */
	var lastLimitedTime: Long = 0
		set(value) {
			field = value
			PreferenceUtil.commitLong(PREF_LAST_LIMITED_TIME_KEY, value)
		}
		get() {
			field = PreferenceUtil.getLong(PREF_LAST_LIMITED_TIME_KEY, 0)
			return field
		}

	init {
		loadFromPreference()
	}

	/** 从 SharedPreferences 恢复 Native 播放记录和限频时间。 */
	private fun loadFromPreference() {
		Log.e(TAG, "loadFromPreference: ad_records = $ad_records")
		val json = PreferenceUtil.getString(PREF_KEY, "")
		if (json!!.isNotEmpty()) {
			val type = object : TypeToken<List<AdRecord>>() {}.type
			ad_records = gson.fromJson(json, type) ?: emptyList()
		}
		lastLimitedTime = PreferenceUtil.getLong(PREF_LAST_LIMITED_TIME_KEY, 0)
	}

	/**
	 * 获取广告位在最近的时间间隔内的播放次数
	 * @param areakey 广告位key
	 * @param intervalSeconds 时间间隔，单位秒
	 * @return 广告位在最近的时间间隔内的播放次数
	 */
	fun getAdPlayCount(areakey: String, intervalSeconds: Long): Int {
		if (com.pdffox.adv.Config.isTest) {
			Log.e(TAG, "getAdPlayCount: ad_records = $ad_records" )
		}
		val endTime = System.currentTimeMillis()
		val startTime = endTime - intervalSeconds * 1000
		val playCount = ad_records.count { it.areakey == areakey && it.timestamp in startTime..endTime }
		if (com.pdffox.adv.Config.isTest) {
			Log.e(TAG, "getAdPlayCount: $areakey 过去$intervalSeconds 秒播放的广告次数 $playCount")
		}
		return playCount
	}

	/**
	 * 获取广告位上次播放时间
	 * @param areakey 广告位key
	 * @return 广告位上次播放时间，单位毫秒
	 */
	fun getAdLastPlayTime(areakey: String): Long {
		if (com.pdffox.adv.Config.isTest) {
			Log.e(TAG, "getAdPlayCount: ad_records = $ad_records" )
		}
		val lastPlayTime = ad_records.filter { it.areakey == areakey }.maxOfOrNull { it.timestamp } ?: 0
		if (com.pdffox.adv.Config.isTest) {
			Log.e(TAG, "getAdLastPlayTime: $lastPlayTime")
		}
		return lastPlayTime
	}

	/** 获取 startTime 到当前时间之间所有 Native 广告位的播放次数。 */
	fun getAllPlayTime(startTime: Long) : Int {
		if (com.pdffox.adv.Config.isTest) {
			Log.e(TAG, "getAdPlayCount: ad_records = $ad_records" )
		}
		return ad_records.count { it.timestamp in startTime..System.currentTimeMillis() }
	}

	/** 新增一条 Native 广告播放记录并持久化。 */
	fun addRecord(adRecord: AdRecord) {
		if (com.pdffox.adv.Config.isTest) {
			Log.e(TAG, "addRecord: $adRecord")
		}
		ad_records = ad_records + adRecord
	}

}
