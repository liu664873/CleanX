package com.pdffox.adv.adv

import android.content.Context
import android.util.Log
import com.pdffox.adv.AdMobConfig
import com.pdffox.adv.AdvertiseSdkConfig
import com.pdffox.adv.Config
import kotlinx.serialization.Serializable
import org.json.JSONArray

/**
 * 广告位 ID 管理器。
 *
 * 保存宿主配置或远程配置下发的 AdMob 广告位，并在测试环境自动切换到 Google 官方测试广告位。
 */
object AdvIDs {

	private var ADMOB_BANNER_ID = Config.sdkConfig.adMob.bannerId
	private var ADMOB_INTERSTITIAL_ID = Config.sdkConfig.adMob.interstitialId
	private var ADMOB_NATIVE_ID = Config.sdkConfig.adMob.nativeId
	private var ADMOB_OPEN_ID = Config.sdkConfig.adMob.openId
	private var TEST_ADMOB_BANNER_ID = Config.sdkConfig.adMob.testBannerId
	private var TEST_ADMOB_INTERSTITIAL_ID = Config.sdkConfig.adMob.testInterstitialId
	private var TEST_ADMOB_NATIVE_ID = Config.sdkConfig.adMob.testNativeId
	private var TEST_ADMOB_OPEN_ID = Config.sdkConfig.adMob.testOpenId
	private var DEBUG_NATIVE_IDS_JSON = Config.sdkConfig.adMob.debugNativeIdsJson

	private var ADMOB_NATIVE_IDS = CircularQueue<NativeAdId>(1)

	/** 应用 SDK 配置中的广告位 ID。 */
	fun configure(config: AdvertiseSdkConfig) {
		configureAdMob(config.adMob)
	}

	/** 写入 AdMob 广告位配置和测试广告位配置。 */
	private fun configureAdMob(config: AdMobConfig) {
		ADMOB_BANNER_ID = config.bannerId
		ADMOB_INTERSTITIAL_ID = config.interstitialId
		ADMOB_NATIVE_ID = config.nativeId
		ADMOB_OPEN_ID = config.openId
		TEST_ADMOB_BANNER_ID = config.testBannerId
		TEST_ADMOB_INTERSTITIAL_ID = config.testInterstitialId
		TEST_ADMOB_NATIVE_ID = config.testNativeId
		TEST_ADMOB_OPEN_ID = config.testOpenId
		DEBUG_NATIVE_IDS_JSON = config.debugNativeIdsJson
		setNativeIDs(config.nativeIdsJson)
	}

	/** 从宿主配置的本地 raw 资源读取兜底 Native 高/中/低价广告位队列。 */
	fun loadNativeIDsFromLocal(context: Context) {
		val resId = Config.resourceConfig.nativeAdIdsRawResId
		if (resId == 0) {
			return
		}
		val nativeAdIds = context.resources.openRawResource(resId).bufferedReader().use { it.readText() }
		setNativeIDs(nativeAdIds)
	}

	/**
	 * 设置 Native 高/中/低价广告位队列。
	 *
	 * 测试环境会强制使用 debugNativeIdsJson，避免误请求线上广告位。
	 */
	fun setNativeIDs(nativeAdIds: String) {
		if (nativeAdIds == "") return
		val ids = if (com.pdffox.adv.Config.isTest) {
			Log.e("NativeAdIDs", "setNativeIDs: $nativeAdIds" )
			DEBUG_NATIVE_IDS_JSON.trimIndent()
		} else {
			nativeAdIds
		}

		try {
			// 1. 解析 JSON 字符串
			if (com.pdffox.adv.Config.isTest) {
				Log.e("AdvIDs", "setNativeIDs: $ids" )
			}
			val jsonArray = JSONArray(ids)
			val count = jsonArray.length()
			if (count > 0) {
				// 2. 核心修改：重新创建指定容量的队列
				ADMOB_NATIVE_IDS = CircularQueue(count)

				// 3. 循环添加到队列中
				for (i in 0 until count) {
					val jsonObject = jsonArray.getJSONObject(i)
					val highPriceID = jsonObject.optString("highPriceID")
					val midPriceID = jsonObject.optString("midPriceID")
					val lowPriceID = jsonObject.optString("lowPriceID")

					// 入队
					ADMOB_NATIVE_IDS.enqueue(NativeAdId(i, highPriceID, midPriceID, lowPriceID))
				}
			}
		} catch (e: Exception) {
			Log.e("AdvIDs", "Error parsing nativeAdIds: ${e.message}")
		}
	}

	/** 轮询获取下一组 Native 广告位 ID，取出后重新入队形成循环。 */
	fun getNextNativeAdId(): NativeAdId? {
		val id = ADMOB_NATIVE_IDS.dequeue() ?: return null
		ADMOB_NATIVE_IDS.enqueue(id)
		return id
	}

	/** 兼容旧接口：直接覆盖四类 AdMob 广告位 ID。 */
	fun setAdmobIDs(
		bannerId: String,
		interstitialAdId: String,
		nativeAdId: String,
		openAdId: String,
	) {
		if (bannerId.isNotBlank()) ADMOB_BANNER_ID = bannerId
		if (interstitialAdId.isNotBlank()) ADMOB_INTERSTITIAL_ID = interstitialAdId
		if (nativeAdId.isNotBlank()) ADMOB_NATIVE_ID = nativeAdId
		if (openAdId.isNotBlank()) ADMOB_OPEN_ID = openAdId
	}

	/** 测试环境返回测试广告位，正式环境返回生产广告位。 */
	private fun selectId(testId: String, prodId: String): String {
		return if (Config.isTest) testId else prodId
	}

	/** 当前生效的 Banner 广告位 ID。 */
	fun getAdmobBannerId() = selectId(TEST_ADMOB_BANNER_ID, ADMOB_BANNER_ID)

	/** 当前生效的插屏广告位 ID。 */
	fun getAdmobInterstitialId() = selectId(TEST_ADMOB_INTERSTITIAL_ID, ADMOB_INTERSTITIAL_ID)

	/** 当前生效的 Native 广告位 ID。 */
	fun getAdmobNativeId() = selectId(TEST_ADMOB_NATIVE_ID, ADMOB_NATIVE_ID)

	/** 当前生效的 App Open 广告位 ID。 */
	fun getAdmobOpenId() = selectId(TEST_ADMOB_OPEN_ID, ADMOB_OPEN_ID)
}

/** 一组 Native 广告位，分别对应高价、中价和兜底广告位。 */
data class NativeAdId(val index: Int, val hId: String, val mId: String, val aId: String)

/** 固定容量循环队列，用于轮询 Native 广告位配置。 */
class CircularQueue<T>(private val capacity: Int) {
	// 实际存储数组，大小比容量多1，用于区分空和满
	private val queue = arrayOfNulls<Any>(capacity +1)
	private var front = 0
	private var rear = 0

	// 检查是否已满
	fun isFull(): Boolean = (rear + 1) % (capacity + 1) == front

	// 检查是否为空
	fun isEmpty(): Boolean = front == rear

	// 入队 (Enqueue)
	fun enqueue(element: T): Boolean {
		if (isFull()) return false
		queue[rear] = element
		rear = (rear + 1) % (capacity + 1)
		return true
	}

	// 出队 (Dequeue)
	@Suppress("UNCHECKED_CAST")
	fun dequeue(): T? {
		if (isEmpty()) return null
		val element = queue[front] as T
		queue[front] = null // 释放引用
		front = (front + 1) % (capacity + 1)
		return element
	}

	// 查看队首元素
	@Suppress("UNCHECKED_CAST")
	fun peek(): T? = if (isEmpty()) null else queue[front] as T

	// 新增清空方法，方便重新加载 ID
	fun clear() {
		front = 0
		rear = 0
		queue.fill(null)
	}
}
