package com.pdffox.adv.util

import android.content.Context
import android.content.SharedPreferences
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * SDK 内部 SharedPreferences 工具。
 *
 * 所有持久化状态统一写入名为 preference 的私有 SharedPreferences。
 */
object PreferenceUtil {
	private var sharedPreferences: SharedPreferences? = null
	private var editor: SharedPreferences.Editor? = null

	private const val TAG = "PreferenceUtil"

	/** 初始化偏好存储，必须在读写前调用。 */
	fun init(context: Context) {
		if (sharedPreferences == null) sharedPreferences =
			context.getSharedPreferences("preference", Context.MODE_PRIVATE)
	}


	/** 删除指定 key。 */
	fun removeByKey(key: String?) {
		editor = sharedPreferences!!.edit()
		editor!!.remove(key)
		editor!!.commit()
	}

	/** 清空 SDK 偏好存储。 */
	fun removeAll() {
		editor = sharedPreferences!!.edit()
		editor!!.clear()
		editor!!.commit()
	}

	/** 同步写入 String。 */
	fun commitString(key: String?, value: String?) {
		editor = sharedPreferences!!.edit()
		editor!!.putString(key, value)
		editor!!.commit()
	}

	/** 读取 String。 */
	fun getString(key: String?, failValue: String?): String? {
		return sharedPreferences!!.getString(key, failValue)
	}

	/** 同步写入 Int。 */
	fun commitInt(key: String?, value: Int) {
		editor = sharedPreferences!!.edit()
		editor!!.putInt(key, value)
		editor!!.commit()
	}

	/** 读取 Int。 */
	fun getInt(key: String?, failValue: Int): Int {
		return sharedPreferences!!.getInt(key, failValue)
	}

	/** 同步写入 Long。 */
	fun commitLong(key: String?, value: Long) {
		editor = sharedPreferences!!.edit()
		editor!!.putLong(key, value)
		editor!!.commit()
	}

	/** 读取 Long。 */
	fun getLong(key: String?, failValue: Long): Long {
		return sharedPreferences!!.getLong(key, failValue)
	}

	/** 同步写入 Boolean。 */
	fun commitBoolean(key: String?, value: Boolean) {
		editor = sharedPreferences!!.edit()
		editor!!.putBoolean(key, value)
		editor!!.commit()
	}

	/** 读取 Boolean。 */
	fun getBoolean(key: String?, failValue: Boolean): Boolean {
		return sharedPreferences!!.getBoolean(key, failValue)
	}

	/** 同步写入 Double；SharedPreferences 无 Double 类型，内部以字符串保存。 */
	fun commitDouble(key: String?, value: Double) {
		editor = sharedPreferences!!.edit()
		editor!!.putString(key, value.toString() + "")
		editor!!.commit()
	}

	/** 读取 Double。 */
	fun getDouble(key: String?, failValue: Double): Double {
		val strValue: String = sharedPreferences!!.getString(key, "")!!
		if (strValue.isEmpty()) return failValue
		return strValue.toDouble()
	}

	/** 同步写入 Float。 */
	fun commitFloat(key: String?, value: Float) {
		editor = sharedPreferences!!.edit()
		editor!!.putFloat(key, value)
		editor!!.commit()
	}

	/** 读取 Float。 */
	fun getFloat(key: String?, failValue: Float): Float {
		return sharedPreferences!!.getFloat(key, failValue)
	}
}

/**
 * SharedPreferences 属性委托。
 *
 * 支持 Int、Long、Boolean、Float、String、Double 这些 SDK 内部常用类型。
 */
class PreferenceDelegate<T>(
	private val key: String,
	private val defaultValue: T
) : ReadWriteProperty<Any?, T> {

	/** 根据默认值类型从 PreferenceUtil 读取对应类型。 */
	override fun getValue(thisRef: Any?, property: KProperty<*>): T {
		return when (defaultValue) {
			is Int -> PreferenceUtil.getInt(key, defaultValue) as T
			is Long -> PreferenceUtil.getLong(key, defaultValue) as T
			is Boolean -> PreferenceUtil.getBoolean(key, defaultValue) as T
			is Float -> PreferenceUtil.getFloat(key, defaultValue) as T
			is String -> PreferenceUtil.getString(key, defaultValue) as T
			is Double -> PreferenceUtil.getDouble(key, defaultValue) as T
			else -> throw IllegalArgumentException("Unsupported type.")
		}
	}

	/** 根据写入值类型保存到 PreferenceUtil。 */
	override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
		when (value) {
			is Int -> PreferenceUtil.commitInt(key, value)
			is Long -> PreferenceUtil.commitLong(key, value)
			is Boolean -> PreferenceUtil.commitBoolean(key, value)
			is Float -> PreferenceUtil.commitFloat(key, value)
			is String -> PreferenceUtil.commitString(key, value)
			is Double -> PreferenceUtil.commitDouble(key, value)
			else -> throw IllegalArgumentException("Unsupported type.")
		}
	}
}
