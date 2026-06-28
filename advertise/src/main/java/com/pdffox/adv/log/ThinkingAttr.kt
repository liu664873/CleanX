package com.pdffox.adv.log

import android.app.Activity
import cn.thinkingdata.analytics.TDAnalytics
import com.pdffox.adv.Config
import com.pdffox.adv.util.PreferenceUtil
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * ThinkingData 用户属性工具。
 *
 * 保存用户属性 key，并封装 userSet、userSetOnce、userAdd 等用户属性写入方式。
 */
object ThinkingAttr {

	// ThinkingData 用户属性字段名。
	val device_id = "device_id"
	val first_visit_country = "first_visit_country"
	val first_visit_language = "first_visit_language"
	val install_time = "install_time"
	val first_open_time = "first_open_time"
	val latest_open_time = "latest_open_time"
	val total_open_num = "total_open_num"
	val ad_strategy = "ad_strategy"
	val singular_media_source = "singular_media_source"
	val singular_campaign = "singular_campaign"
	val singular_adset = "singular_adset"
	val singular_ad = "singular_ad"
	val total_ad_revenue = "total_ad_revenue"
	val total_ad_inter_num = "total_ad_inter_num"
	val total_ad_open_num = "total_ad_open_num"
	val total_ad_time = "total_ad_time"
	val total_use_time = "total_use_time"
	val total_openfile_num = "total_openfile_num"
	val total_openocr_num = "total_openocr_num"
	val ab_test = "ab_test"
	val if_setting_default = "if_setting_default"
	val ip_info = "ip_info"

	val has_notification_permission = "has_notification_permission"
	val has_allfile_permission = "has_allfile_permission"

	/** 设置可覆盖的用户属性。 */
	fun setUserAttr(key: String, value: Any) {
		if (!Config.sdkConfig.thinking.enabled) return
		val userProperties =  JSONObject()
		userProperties.put(key, value)
		TDAnalytics.userSet(userProperties);
	}

	/** 设置只写一次的用户属性。 */
	fun setUserOnceAttr(key: String, value: String) {
		if (!Config.sdkConfig.thinking.enabled) return
		val userProperties =  JSONObject()
		userProperties.put(key, value)
		TDAnalytics.userSetOnce(userProperties);
	}

	/** 对数值型用户属性做累加。 */
	fun setUserAddAttr(key: String, value: Any) {
		if (!Config.sdkConfig.thinking.enabled) return
		val userProperties =  JSONObject()
		userProperties.put(key, value)
		TDAnalytics.userAdd(userProperties);
	}

	/** 设置可覆盖的用户属性，保留旧调用名。 */
	fun setUserSetAttr(key: String, value: Any) {
		if (!Config.sdkConfig.thinking.enabled) return
		val userProperties =  JSONObject()
		userProperties.put(key, value)
		TDAnalytics.userSet(userProperties);
	}

	/** 获取首次访问国家码，并持久化首次结果。 */
	fun getFirstCountry(context: Activity) :String {
		var strC = PreferenceUtil.getString("getFirstCountry", "")
		if (strC.equals("")) {
			val locale = context.resources.configuration.locales.get(0)
			strC = locale.country // 返回国家代码，如 "CN", "US"
			PreferenceUtil.commitString("getFirstCountry", strC)
		}
		return strC ?: "unknow"
	}

	/** 获取首次访问语言码，并持久化首次结果。 */
	fun getFirstLanguage(context: Activity) :String {
		var strL = PreferenceUtil.getString("getFirstLanguage", "")
		if (strL.equals("")) {
			val locale = context.resources.configuration.locales.get(0)
			strL = locale.language // 返回国家代码，如 "CN", "US"
			PreferenceUtil.commitString("getFirstLanguage", strL)
		}
		return strL ?: "unknow"
	}

	/** 获取首次打开时间，按洛杉矶时区格式化并持久化。 */
	fun getFirstOpenTime() : String {
		var firstOpenTime = PreferenceUtil.getString("mmFirstOpenTime", "")
		if (firstOpenTime.equals("")) {
			val firstOpen = System.currentTimeMillis()
			firstOpenTime = convertToCaliforniaTime(firstOpen)
			PreferenceUtil.commitString("mmFirstOpenTime", firstOpenTime)
		}
		return firstOpenTime ?: "unknow"
	}

	var mLatestOpenTime = ""
	/** 获取本次进程内最近打开时间。 */
	fun getLatestOpenTime() : String {
		if (mLatestOpenTime == "") {
			mLatestOpenTime = convertToCaliforniaTime(System.currentTimeMillis())
		}
		return mLatestOpenTime
	}

	/** 累加总打开次数。 */
	fun addTotalOpenNum() {
		val totalOpenNum = PreferenceUtil.getInt("totalOpenNum", 1)
		PreferenceUtil.commitInt("totalOpenNum", totalOpenNum + 1)
	}

	/** 获取总打开次数。 */
	fun getTotalOpenNum() : Int {
		val totalOpenNum = PreferenceUtil.getInt("totalOpenNum", 1)
		return totalOpenNum
	}

	/** 将毫秒时间戳转换为洛杉矶时区时间字符串。 */
	fun convertToCaliforniaTime(timestampMillis: Long): String {
		val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
		sdf.timeZone = TimeZone.getTimeZone("America/Los_Angeles")
		return sdf.format(Date(timestampMillis))
	}
}
