package com.pdffox.adv.notification

import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.pdffox.adv.BuildConfig
import com.pdffox.adv.util.PreferenceUtil
import kotlinx.serialization.Serializable

/** 单条通知发送记录。 */
@Serializable
data class NotificationRecord(
	val name: String,
	val timestamp: Long,
)

/**
 * 通知发送记录管理器。
 *
 * 记录已发送通知的触发器名称和时间，用于 1 小时/24 小时配额、全局间隔和单触发器间隔判断。
 */
object NotificationRecordManager {

	private const val TAG = "NotificationRecordManager"
	private const val PREF_KEY = "notification_records_key"

	private val gson = Gson()

	/** 已持久化的通知发送记录。 */
	var notification_records: List<NotificationRecord> = emptyList()
		set(value) {
			field = value
			val json = gson.toJson(field)
			if (BuildConfig.DEBUG) {
				Log.e(TAG, "set value: ad_records = $value")
			}
			PreferenceUtil.commitString(PREF_KEY, json)
		}
		get() {
			val strRecords = PreferenceUtil.getString(PREF_KEY, "")
			if (strRecords!!.isNotEmpty()) {
				val type = object : TypeToken<List<NotificationRecord>>() {}.type
				field = gson.fromJson(strRecords, type)
			}
			return field
		}

	init {
		loadFromPreference()
	}

	/** 从 SharedPreferences 恢复通知发送记录。 */
	private fun loadFromPreference() {
		Log.e(TAG, "loadFromPreference: notification_records = $notification_records")
		val json = PreferenceUtil.getString(PREF_KEY, "")
		if (json!!.isNotEmpty()) {
			val type = object : TypeToken<List<NotificationRecord>>() {}.type
			notification_records = gson.fromJson(json, type) ?: emptyList()
		}
	}

	/** 新增一条通知发送记录并持久化。 */
	fun addRecord(notificationRecord: NotificationRecord) {
		if (BuildConfig.DEBUG) {
			Log.e(TAG, "addRecord: $notificationRecord")
		}
		notification_records = notification_records + notificationRecord
	}


	/** 获取最近 24 小时内的通知发送次数。 */
	fun get24HRecordsCount() : Int {
		return notification_records.count {
			it.timestamp > System.currentTimeMillis() - 24 * 60 * 60 * 1000
		}
	}

	/** 获取最近 1 小时内的通知发送次数。 */
	fun get1HRecordsCount() : Int {
		return notification_records.count {
			it.timestamp > System.currentTimeMillis() - 60 * 60 * 1000
		}
	}

	/** 获取最近一次任意通知发送记录。 */
	fun getLastRecord() : NotificationRecord? {
		return notification_records.maxByOrNull { it.timestamp }
	}

	/** 获取指定触发器最近一次通知发送记录。 */
	fun getLastRecord(name: String) : NotificationRecord? {
		return notification_records
			.filter { it.name == name }
			.maxByOrNull { it.timestamp }
	}
}
