package com.quickcleanpro.phonecleaner.domain.model.toolbox

/**
 * UI 鐢ㄤ簬鏍囪瘑鏃犳硶褰掑睘鍒板叿浣撳簲鐢ㄧ殑绯荤粺鎴栨湭鐭ユ祦閲忋€? */
const val UNKNOWN_NETWORK_TRAFFIC_PACKAGE = "com.quickcleanpro.phonecleaner.unknown_network_traffic"

/**
 * 鍗曚釜搴旂敤鐨勪娇鐢ㄦ儏鍐点€? */
data class AppUsageInfo(
    val packageName: String,
    val appName: String,
    val totalForegroundMs: Long,
    val launchCount: Int
) {
    /** 宸叉牸寮忓寲鐨勫墠鍙颁娇鐢ㄦ椂闀裤€?*/
    val formattedTime: String get() = formatDuration(totalForegroundMs)
}

/**
 * 鍗曚釜搴旂敤鐨勭綉缁滅敤閲忋€? */
data class NetworkUsageApp(
    val appName: String,
    val packageName: String,
    val uid: Int,
    val rxBytes: Long,
    val txBytes: Long
) {
    /** 涓嬭浇涓庝笂浼犲悎璁″瓧鑺傛暟銆?*/
    val totalBytes: Long get() = rxBytes + txBytes
}

/**
 * 缃戠粶鐢ㄩ噺椤甸潰灞曠ず鎵€闇€鐨勬眹鎬绘暟鎹拰搴旂敤鏄庣粏銆? */
data class NetworkUsageInfo(
    val wifiRxBytes: Long,
    val wifiTxBytes: Long,
    val cellularRxBytes: Long,
    val cellularTxBytes: Long,
    val apps: List<NetworkUsageApp> = emptyList(),
    val wifiApps: List<NetworkUsageApp> = apps,
    val cellularApps: List<NetworkUsageApp> = emptyList(),
    val fallbackApps: List<NetworkUsageApp> = emptyList(),
    val isToday: Boolean,
    val needsUsageAccess: Boolean
) {
    /** Wi-Fi 涓嬭浇鍜屼笂浼犵殑鎬诲瓧鑺傛暟銆?*/
    val wifiTotalBytes: Long get() = wifiRxBytes + wifiTxBytes

    /** 铚傜獫缃戠粶涓嬭浇鍜屼笂浼犵殑鎬诲瓧鑺傛暟銆?*/
    val cellularTotalBytes: Long get() = cellularRxBytes + cellularTxBytes
}

/**
 * 缃戠粶娴嬮€熺粨鏋溿€? */
data class NetworkSpeedResult(
    val downloadMbps: String,
    val uploadMbps: String,
    val latencyMs: Long?,
    val measured: Boolean
)

/**
 * 缃戠粶娴嬮€熻繃绋嬩腑鐨勯樁娈垫€ц繘搴︺€? */
data class NetworkSpeedProgress(
    val downloadMbps: String? = null,
    val uploadMbps: String? = null,
    val latencyMs: Long? = null,
    val phase: String = "idle"
)

/**
 * 灞€鍩熺綉鎵弿寰楀埌鐨勮澶囦俊鎭€? */
data class NetworkDeviceInfo(
    val ip: String,
    val hostName: String,
    val macAddress: String
)

/**
 * Wi-Fi 鎵弿缁撴灉銆? */
data class NetworkScanResult(
    val ssid: String,
    val gatewayIp: String,
    val dnsIp: String,
    val deviceIp: String,
    val devices: List<NetworkDeviceInfo>,
    val hasWifi: Boolean
)

/**
 * 灏嗘绉掓椂闀挎牸寮忓寲涓?App Usage 灞曠ず鏂囨銆? */
fun formatDuration(totalMs: Long): String {
    val totalMinutes = (totalMs / 60_000L).coerceAtLeast(0L)
    val hours = totalMinutes / 60L
    val minutes = totalMinutes % 60L
    return when {
        hours > 0L && minutes > 0L -> "${hours}h ${minutes}m"
        hours > 0L -> "${hours}h"
        else -> "${minutes}m"
    }
}
