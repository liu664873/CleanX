package com.pdffox.adv.adv

import android.os.Process
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.remoteConfig
import com.pdffox.adv.AdvRuntime
import com.pdffox.adv.Config
import com.pdffox.adv.log.ThinkingAttr
import com.pdffox.adv.parseJson
import com.pdffox.adv.remoteconfig.RemoteConfigRouting
import com.pdffox.adv.util.PreferenceDelegate
import com.pdffox.adv.util.PreferenceUtil
import kotlinx.serialization.Serializable
import okhttp3.Dns
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.net.Inet4Address
import java.net.InetAddress
import java.net.UnknownHostException
import kotlin.system.exitProcess

/** Runtime install state shared by notification and logging flows. */
class AdvCheckParams {
	var isFirstOpen: Boolean by PreferenceDelegate("isFirstOpen", true)
	var installTime: Long by PreferenceDelegate("installTime", 0L)
}

@Serializable
data class IpGeoDetail(
	val ip: String?,
	val longitude: Double?,
	val latitude: Double?,
	val asn: String?,
	val isp: String?,
)

@Serializable
data class CidrRange(
	val address: ByteArray,
	val prefixLength: Int,
) {
	fun contains(target: ByteArray): Boolean {
		if (target.size != address.size) return false
		var remaining = prefixLength
		var index = 0
		while (remaining >= 8) {
			if (address[index] != target[index]) return false
			remaining -= 8
			index++
		}
		if (remaining > 0) {
			val mask = (0xFF shl (8 - remaining)) and 0xFF
			val lhs = address[index].toInt() and mask
			val rhs = target[index].toInt() and mask
			return lhs == rhs
		}
		return true
	}
}

object AdvCheckManager {
	private const val TAG = "AdvCheck"
	private const val PREF_IP_INFO_CHECKED = "pref_ip_info_checked"
	private const val PREF_IP_INFO_RESULT = "pref_ip_info_result"

	var params: AdvCheckParams = AdvCheckParams()
	private val cloudCidrs by lazy { loadCidrsFromRaw(Config.resourceConfig.cloudCidrsRawResId) }
	private val googleCidrs by lazy { loadCidrsFromRaw(Config.resourceConfig.googleCidrsRawResId) }

	@Volatile
	private var ipInfoCacheLoaded = false

	@Volatile
	private var ipInfoCachedResult: String? = null

	fun getIpInfoV2(): String? {
		if (!Config.isServerEnabled || Config.IPInfoV2Url.isBlank()) {
			return null
		}
		PreferenceUtil.init(AdvRuntime.application)
		if (ipInfoCacheLoaded) {
			return ipInfoCachedResult
		}
		if (PreferenceUtil.getBoolean(PREF_IP_INFO_CHECKED, false)) {
			val cachedResult = PreferenceUtil.getString(PREF_IP_INFO_RESULT, null)
			ipInfoCachedResult = cachedResult
			ipInfoCacheLoaded = true
			if (Config.isTest) {
				Log.e(TAG, "getIpInfoV2: use cached result = $cachedResult")
			}
			return cachedResult
		}
		val result = runCatching {
			val client = OkHttpClient()
			val request = Request.Builder()
				.url(Config.IPInfoV2Url)
				.get()
				.build()

			client.newCall(request).execute().use { response ->
				if (!response.isSuccessful) throw IllegalStateException("Unexpected code $response")
				val body = response.body.string()
				val parsed = parseIpInfoV2Detail(body)
				return@use applyIpInfoResult(parsed, "AdvCheckManager.getIpInfoV2")
			}
		}.onFailure {
			Log.e(TAG, "getIpInfoV2 error", it)
		}.getOrNull()

		PreferenceUtil.commitBoolean(PREF_IP_INFO_CHECKED, true)
		PreferenceUtil.commitString(PREF_IP_INFO_RESULT, result)
		ipInfoCachedResult = result
		ipInfoCacheLoaded = true
		return result
	}

	private fun parseIp(raw: String): String? {
		return runCatching {
			val json = JSONObject(raw)
			json.optString("Ip").takeIf { it.isNotBlank() }
		}.getOrNull()
	}

	private fun parseIpInfoV2Detail(raw: String): IpGeoDetail? {
		return parseIpInfoPayload(raw)
	}

	private fun parseIpInfoPayload(raw: String): IpGeoDetail? {
		val parsedDetail = runCatching {
			val root = JSONObject(raw)
			val ip = root.optString("Ip").takeIf { it.isNotBlank() }
			val locationInfo = when (val value = root.opt("location_info")) {
				is JSONObject -> value.toString()
				is String -> value.takeIf { it.isNotBlank() }
				else -> null
			}
			val parsed = locationInfo?.let(::parseDetailFields) ?: parseDetailFields(raw)
			parsed?.let { detail ->
				if (detail.ip == null && ip != null) {
					detail.copy(ip = ip)
				} else {
					detail
				}
			}
		}.getOrNull()
		if (parsedDetail != null) {
			return parsedDetail
		}
		val ip = parseIp(raw) ?: return null
		return IpGeoDetail(ip = ip, longitude = null, latitude = null, asn = null, isp = null)
	}

	private fun parseDetailFields(detailJson: String): IpGeoDetail? {
		return runCatching {
			val json = JSONObject(detailJson)
			val ip = json.optString("ip").takeIf { it.isNotBlank() }
			val longitude = json.optDouble("longitude", Double.NaN).takeIf { !it.isNaN() }
			val latitude = json.optDouble("latitude", Double.NaN).takeIf { !it.isNaN() }
			val asn = json.optString("asn").takeIf { it.isNotBlank() }
			val isp = json.optString("isp").takeIf { it.isNotBlank() }
			IpGeoDetail(ip, longitude, latitude, asn, isp)
		}.getOrNull()
	}

	private fun applyIpInfoResult(parsed: IpGeoDetail?, source: String): String {
		if (parsed != null) {
			ThinkingAttr.setUserOnceAttr(
				ThinkingAttr.ip_info,
				"IPInfo ip=${parsed.ip}, longitude=${parsed.longitude}, latitude=${parsed.latitude}, asn=${parsed.asn}, isp=${parsed.isp}",
			)
			Config.ipCheckHasResult = true
			val ipValue = parsed.ip
			if (ipValue != null) {
				val inCloud = isIpInCidrs(ipValue, cloudCidrs)
				val inGoogle = isIpInCidrs(ipValue, googleCidrs)
				if (Config.isTest) {
					Log.e(TAG, "getIpInfoV2: ip=$ipValue inCloud=$inCloud inGoogle=$inGoogle")
				}
				if (inCloud || inGoogle) {
					Config.isGoogleIP = true
				}
			}
			if (parsed.isp?.contains("google", ignoreCase = true) == true) {
				Config.isGoogleIP = true
			}
			if (parsed.asn?.contains("15169") == true) {
				Config.isGoogleIP = true
			}
			if (Config.sdkConfig.remoteConfig.enabled && Config.remoteConfigHasResult) {
				val remoteConfig = Firebase.remoteConfig
				val adMapping = remoteConfig.getString("ad_mapping")
				RemoteConfigRouting.apply(
					remoteConfig = remoteConfig,
					adMapping = adMapping,
					source = source,
				)
			} else if (Config.isTest) {
				Log.e(TAG, "getIpInfoV2: Remote Config has not been applied yet")
			}
		}
		if (Config.isTest && parsed != null) {
			val detailSummary =
				"IP detail -> ip=${parsed.ip}, longitude=${parsed.longitude}, latitude=${parsed.latitude}, asn=${parsed.asn}, isp=${parsed.isp}"
			Log.e(TAG, detailSummary)
			return detailSummary
		}
		return "IP detail failed to parse"
	}

	private fun isIpInCidrs(ip: String, cidrs: List<CidrRange>): Boolean {
		val target = try {
			InetAddress.getByName(ip).address
		} catch (e: UnknownHostException) {
			Log.e(TAG, "isIpInCidrs: invalid ip $ip", e)
			return false
		}
		return cidrs.any { it.contains(target) }
	}

	private fun loadCidrsFromRaw(resId: Int): List<CidrRange> {
		val context = AdvRuntime.application
		return runCatching {
			context.resources.openRawResource(resId).bufferedReader().use { reader ->
				val root = JSONObject(reader.readText())
				val prefixes = root.optJSONArray("prefixes") ?: JSONArray()
				buildList {
					for (i in 0 until prefixes.length()) {
						val entry = prefixes.optJSONObject(i) ?: continue
						entry.optString("ipv4Prefix").takeIf { it.isNotBlank() }?.let { cidr ->
							parseCidr(cidr)?.let { add(it) }
						}
						entry.optString("ipv6Prefix").takeIf { it.isNotBlank() }?.let { cidr ->
							parseCidr(cidr)?.let { add(it) }
						}
					}
				}
			}
		}.onFailure {
			Log.e(TAG, "loadCidrsFromRaw failed", it)
		}.getOrElse { emptyList() }
	}

	private fun parseCidr(cidr: String): CidrRange? {
		val parts = cidr.split("/")
		if (parts.size != 2) return null
		val prefix = parts[1].toIntOrNull() ?: return null
		return try {
			val addr = InetAddress.getByName(parts[0]).address
			CidrRange(addr, prefix)
		} catch (e: Exception) {
			Log.e(TAG, "parseCidr failed for $cidr", e)
			null
		}
	}

	object IPv4FirstDns : Dns {
		override fun lookup(hostname: String): List<InetAddress> {
			val addresses = Dns.SYSTEM.lookup(hostname)
			return addresses.sortedBy { if (it is Inet4Address) 0 else 1 }
		}
	}

	fun checkToken(token: String) {
		if (!Config.isServerEnabled || Config.ParseTokenUrl.isBlank() || Config.sdkConfig.server.parseTokenKey.isBlank()) {
			return
		}
		Log.e(TAG, "checkToken: $token")
		val client = OkHttpClient
			.Builder()
			.dns(IPv4FirstDns)
			.build()
		val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
		val params = JSONObject().apply {
			put("Key", Config.sdkConfig.server.parseTokenKey)
			put("PackageName", AdvRuntime.currentPackageName())
			put("token", token)
		}
		Log.e(TAG, "checkToken: $token $params")
		val jsonBody = params.toString().toRequestBody(mediaType)
		val request = Request.Builder()
			.url(Config.ParseTokenUrl)
			.post(jsonBody)
			.build()
		try {
			client.newCall(request).execute().use { response ->
				val body = response.body.string()
				Log.e(TAG, "checkToken: $body")
				if (response.isSuccessful) {
					val json = JSONObject(body)
					val dataString = json.getString("data")
					Log.e(TAG, "checkToken: dataString = $dataString")
					val playIntegrityData = parseJson(dataString)
					val appIntegrity = playIntegrityData.appIntegrity
					if (appIntegrity == null) {
						Log.w(TAG, "checkToken: missing appIntegrity payload")
						return
					}
					if (appIntegrity.packageName.isNullOrBlank() || appIntegrity.versionCode.isNullOrBlank()) {
						Log.w(TAG, "checkToken: incomplete appIntegrity payload, skip enforcement")
						return
					}
					if (appIntegrity.appRecognitionVerdict == "UNRECOGNIZED_VERSION") {
						PreferenceUtil.commitBoolean("appRecognitionVerdict", false)
						Log.e(TAG, "checkToken: appRecognitionVerdict != PLAY_RECOGNIZED")
						AdvRuntime.finishCurrentActivity()
						Process.killProcess(Process.myPid())
						exitProcess(0)
					} else {
						PreferenceUtil.commitBoolean("appRecognitionVerdict", true)
					}
				}
			}
		} catch (e: Exception) {
			Log.e(TAG, "checkToken: ", e)
		}
	}
}
