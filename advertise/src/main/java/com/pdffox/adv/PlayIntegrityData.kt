package com.pdffox.adv

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

/** Play Integrity 服务端解析结果根节点。 */
@Serializable
data class Root(
	val accountDetails: AccountDetails? = null,
	val appIntegrity: AppIntegrity? = null,
	val deviceIntegrity: DeviceIntegrity? = null,
	val requestDetails: RequestDetails? = null,
)

/** 账号授权相关完整性信息。 */
@Serializable
data class AccountDetails(
	val appLicensingVerdict: String? = null,
)

/** App 包名、证书、版本和 Play 识别状态。 */
@Serializable
data class AppIntegrity(
	val appRecognitionVerdict: String? = null,
	val certificateSha256Digest: List<String> = emptyList(),
	val packageName: String? = null,
	val versionCode: String? = null,
)

/** 设备完整性 verdict 列表。 */
@Serializable
data class DeviceIntegrity(
	val deviceRecognitionVerdict: List<String> = emptyList(),
)

/** Play Integrity 请求上下文信息。 */
@Serializable
data class RequestDetails(
	val requestHash: String? = null,
	val requestPackageName: String? = null,
	val timestampMillis: String? = null,
)

private val playIntegrityJson = Json {
	ignoreUnknownKeys = true
	explicitNulls = false
}

/** 解析服务端返回的 Play Integrity data JSON。 */
fun parseJson(jsonString: String): Root {
	return playIntegrityJson.decodeFromString(jsonString)
}
