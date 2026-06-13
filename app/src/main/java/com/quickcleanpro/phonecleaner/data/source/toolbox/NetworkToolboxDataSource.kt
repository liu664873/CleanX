package com.quickcleanpro.phonecleaner.data.source.toolbox

import android.app.AppOpsManager
import android.app.usage.NetworkStats
import android.app.usage.NetworkStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.TrafficStats
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Process
import android.provider.Settings
import android.text.format.Formatter
import com.quickcleanpro.phonecleaner.domain.model.toolbox.NetworkDeviceInfo
import com.quickcleanpro.phonecleaner.domain.model.toolbox.NetworkScanResult
import com.quickcleanpro.phonecleaner.domain.model.toolbox.NetworkSpeedProgress
import com.quickcleanpro.phonecleaner.domain.model.toolbox.NetworkSpeedResult
import com.quickcleanpro.phonecleaner.domain.model.toolbox.NetworkUsageApp
import com.quickcleanpro.phonecleaner.domain.model.toolbox.NetworkUsageInfo
import com.quickcleanpro.phonecleaner.domain.model.toolbox.UNKNOWN_NETWORK_TRAFFIC_PACKAGE
import com.quickcleanpro.phonecleaner.util.FileSizeFormatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.InetAddress
import java.net.URL
import java.util.Calendar
import java.util.Locale
import kotlin.math.max

/** 浠?NetworkStats 姹囨€诲嚭鏉ョ殑 UID 娴侀噺妗讹紝鐢ㄤ簬娴嬭瘯鍜岀函鍑芥暟杞崲銆?*/
data class NetworkUsageUidBucket(
    val uid: Int,
    val rxBytes: Long,
    val txBytes: Long,
)

/** UID 瀵瑰簲鐨勫寘鍚嶅拰搴旂敤鍚嶄俊鎭€?*/
data class NetworkUsagePackageInfo(
    val packageName: String,
    val appName: String,
)

/**
 * 灏?UID 缁村害鐨勭綉缁滅敤閲忚浆鎹负搴旂敤缁村害銆? *
 * Android 涓婂涓寘鍙兘鍏变韩鍚屼竴涓?UID銆傝繖閲岄€夋嫨绗竴涓彲瑙ｆ瀽鍖呬綔涓哄睍绀洪」锛? * 鏃犳硶瑙ｆ瀽鐨?UID 缁熶竴褰掑叆鈥滅郴缁熶笌鏈煡娴侀噺鈥濓紝渚夸簬 UI 琛ラ綈鍓╀綑鎬绘祦閲忋€? */
fun buildNetworkUsageAppsFromUidBuckets(
    uidBuckets: List<NetworkUsageUidBucket>,
    packageResolver: (Int) -> List<NetworkUsagePackageInfo>,
): List<NetworkUsageApp> =
    uidBuckets
        .filter { it.rxBytes + it.txBytes > 0L }
        .map { bucket ->
            val packageInfo = packageResolver(bucket.uid).firstOrNull()
            NetworkUsageApp(
                appName = packageInfo?.appName ?: "System & unknown traffic",
                packageName = packageInfo?.packageName ?: UNKNOWN_NETWORK_TRAFFIC_PACKAGE,
                uid = bucket.uid,
                rxBytes = bucket.rxBytes,
                txBytes = bucket.txBytes,
            )
        }.sortedByDescending { it.totalBytes }

/** 璇诲彇缃戠粶鐘舵€併€佺敤閲忋€佹祴閫熷拰灞€鍩熺綉璁惧鐨勫伐鍏峰叆鍙ｃ€?*/
object NetworkToolboxDataSource {
    /** UI 鐢ㄤ簬鏍囪瘑鏃犳硶褰掑睘鍒板叿浣撳簲鐢ㄧ殑绯荤粺鎴栨湭鐭ユ祦閲忋€?*/
    const val UNKNOWN_NETWORK_TRAFFIC_PACKAGE = com.quickcleanpro.phonecleaner.domain.model.toolbox.UNKNOWN_NETWORK_TRAFFIC_PACKAGE

    /** 鍒ゆ柇褰撳墠鏄惁瀛樺湪鍙敤缃戠粶銆?*/
    fun isNetworkAvailable(context: Context): Boolean {
        val connectivity = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val capabilities = connectivity.getNetworkCapabilities(connectivity.activeNetwork) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }

    /** 鍒ゆ柇褰撳墠娲昏穬缃戠粶鏄惁涓?Wi-Fi銆?*/
    fun isWifiConnected(context: Context): Boolean = activeNetworkHasTransport(context, NetworkCapabilities.TRANSPORT_WIFI)

    /** 鍒ゆ柇褰撳墠娲昏穬缃戠粶鏄惁涓鸿渹绐濈綉缁溿€?*/
    fun isMobileConnected(context: Context): Boolean = activeNetworkHasTransport(context, NetworkCapabilities.TRANSPORT_CELLULAR)

    /** 鍒ゆ柇鏄惁鎷ユ湁缃戠粶鐢ㄩ噺璇诲彇鏉冮檺銆?*/
    fun hasUsageAccess(context: Context): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val uid = Process.myUid()
        val mode =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                appOps.unsafeCheckOpNoThrow(
                    AppOpsManager.OPSTR_GET_USAGE_STATS,
                    uid,
                    context.packageName,
                )
            } else {
                @Suppress("DEPRECATION")
                appOps.checkOpNoThrow(
                    AppOpsManager.OPSTR_GET_USAGE_STATS,
                    uid,
                    context.packageName,
                )
            }
        return mode == AppOpsManager.MODE_ALLOWED
    }

    /** 鍒涘缓璺宠浆鍒扮郴缁熺敤閲忔潈闄愰〉鐨?Intent銆?*/
    fun usageAccessIntent(): Intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)

    /** 璇诲彇浠婃棩缃戠粶鐢ㄩ噺锛涙棤鏉冮檺鎴栬鍙栧け璐ユ椂閫€鍥炲埌 TrafficStats 鍚姩鍚庣疮璁″€笺€?*/
    suspend fun readUsage(context: Context): NetworkUsageInfo =
        withContext(Dispatchers.IO) {
            val hasAccess = hasUsageAccess(context)
            val cal =
                Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
            val start = cal.timeInMillis
            val end = System.currentTimeMillis()

            if (hasAccess) {
                runCatching {
                    val stats = context.getSystemService(Context.NETWORK_STATS_SERVICE) as NetworkStatsManager
                    val wifi = stats.readSummary(NetworkCapabilities.TRANSPORT_WIFI, start, end)
                    val cellular = stats.readSummary(NetworkCapabilities.TRANSPORT_CELLULAR, start, end)
                    val wifiApps = stats.readPerAppUsage(context, ConnectivityManager.TYPE_WIFI, start, end)
                    val cellularApps = stats.readPerAppUsage(context, ConnectivityManager.TYPE_MOBILE, start, end)
                    NetworkUsageInfo(
                        wifiRxBytes = wifi.first,
                        wifiTxBytes = wifi.second,
                        cellularRxBytes = cellular.first,
                        cellularTxBytes = cellular.second,
                        apps = wifiApps,
                        wifiApps = wifiApps,
                        cellularApps = cellularApps,
                        fallbackApps = emptyList(),
                        isToday = true,
                        needsUsageAccess = false,
                    )
                }.getOrElse { trafficStatsUsage(needsUsageAccess = true) }
            } else {
                trafficStatsUsage(needsUsageAccess = true)
            }
        }

    /** 鎵ц涓€娆＄綉缁滄祴閫燂紝娴嬮€熺珯鐐逛笉鍙敤鏃朵娇鐢ㄧ郴缁熼摼璺甫瀹戒綔涓哄厹搴曞睍绀恒€?*/
    suspend fun runSpeedTest(context: Context): NetworkSpeedResult =
        withContext(Dispatchers.IO) {
            val latency = measureLatency()
            val download = measureDownloadMbps()
            val upload = measureUploadMbps()
            if (download != null || upload != null) {
                NetworkSpeedResult(formatMbps(download ?: 0.0), formatMbps(upload ?: 0.0), latency, true)
            } else {
                val connectivity = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                val caps = connectivity.getNetworkCapabilities(connectivity.activeNetwork)
                NetworkSpeedResult(
                    downloadMbps = caps?.linkDownstreamBandwidthKbps?.takeIf { it > 0 }?.let { formatMbps(it / 1000.0) } ?: "--",
                    uploadMbps = caps?.linkUpstreamBandwidthKbps?.takeIf { it > 0 }?.let { formatMbps(it / 1000.0) } ?: "--",
                    latencyMs = latency,
                    measured = false,
                )
            }
        }

    /** 鎵ц甯﹂樁娈靛洖璋冪殑缃戠粶娴嬮€燂紝鏂逛究椤甸潰灞曠ず涓嬭浇銆佷笂浼犲拰寤惰繜鐨勬笎杩涚粨鏋溿€?*/
    suspend fun runSpeedTestWithProgress(
        context: Context,
        onProgress: (NetworkSpeedProgress) -> Unit,
    ): NetworkSpeedResult =
        withContext(Dispatchers.IO) {
            onProgress(NetworkSpeedProgress(phase = "latency"))
            val latency = measureLatency()
            onProgress(NetworkSpeedProgress(latencyMs = latency, phase = "download"))

            val download = measureDownloadMbps()
            val downloadLabel = download?.let(::formatMbps)
            onProgress(
                NetworkSpeedProgress(
                    downloadMbps = downloadLabel,
                    latencyMs = latency,
                    phase = "upload",
                ),
            )

            val upload = measureUploadMbps()
            val uploadLabel = upload?.let(::formatMbps)
            val result =
                if (download != null || upload != null) {
                    NetworkSpeedResult(
                        downloadMbps = downloadLabel ?: formatMbps(0.0),
                        uploadMbps = uploadLabel ?: formatMbps(0.0),
                        latencyMs = latency,
                        measured = true,
                    )
                } else {
                    networkCapabilitiesSpeed(context, latency)
                }

            onProgress(
                NetworkSpeedProgress(
                    downloadMbps = result.downloadMbps,
                    uploadMbps = result.uploadMbps,
                    latencyMs = result.latencyMs,
                    phase = "done",
                ),
            )
            result
        }

    /** 鎵弿褰撳墠 Wi-Fi 缃戠粶涓殑鏈満銆佺綉鍏冲拰鍙揪璁惧銆?*/
    suspend fun scanWifi(context: Context): NetworkScanResult =
        withContext(Dispatchers.IO) {
            val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
            val dhcp = wifiManager?.dhcpInfo
            val wifiInfo = wifiManager?.connectionInfo
            val currentIp = dhcp?.ipAddress?.takeIf { it != 0 }?.let { Formatter.formatIpAddress(it) } ?: "--"
            val gateway = dhcp?.gateway?.takeIf { it != 0 }?.let { Formatter.formatIpAddress(it) } ?: "--"
            val dns = dhcp?.dns1?.takeIf { it != 0 }?.let { Formatter.formatIpAddress(it) } ?: "--"
            val ssid = wifiInfo?.ssid?.removeSurrounding("\"")?.takeIf { it.isNotBlank() && it != "<unknown ssid>" } ?: "<unknown ssid>"

            val devices =
                if (currentIp != "--") {
                    val prefix = currentIp.substringBeforeLast('.')
                    if (prefix.isNotBlank()) {
                        scanSubnet(prefix, currentIp, gateway)
                    } else {
                        readArpDevices()
                    }
                } else {
                    readArpDevices()
                }

            NetworkScanResult(
                ssid,
                gateway,
                dns,
                currentIp,
                devices.distinctBy { it.ip }.sortedBy { ipSortKey(it.ip) },
                wifiManager?.isWifiEnabled == true && currentIp != "--",
            )
        }

    /** 灏嗗瓧鑺傛暟鏍煎紡鍖栦负椤甸潰灞曠ず鏂囨湰銆?*/
    fun formatBytes(bytes: Long): String = FileSizeFormatter.format(bytes)

    /** 鍒ゆ柇娲昏穬缃戠粶鏄惁鍖呭惈鎸囧畾浼犺緭绫诲瀷銆?*/
    private fun activeNetworkHasTransport(
        context: Context,
        transport: Int,
    ): Boolean {
        val connectivity = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val capabilities = connectivity.getNetworkCapabilities(connectivity.activeNetwork) ?: return false
        return capabilities.hasTransport(transport)
    }

    /** 浣跨敤绯荤粺鎬绘祦閲忎綔涓烘棤鏉冮檺鏃剁殑鍏滃簳缃戠粶鐢ㄩ噺銆?*/
    private fun trafficStatsUsage(needsUsageAccess: Boolean): NetworkUsageInfo {
        val rx = TrafficStats.getTotalRxBytes().takeIf { it != TrafficStats.UNSUPPORTED.toLong() } ?: 0L
        val tx = TrafficStats.getTotalTxBytes().takeIf { it != TrafficStats.UNSUPPORTED.toLong() } ?: 0L
        val fallbackApps =
            if (rx + tx > 0L) {
                listOf(
                    NetworkUsageApp(
                        appName = "System & unknown traffic",
                        packageName = UNKNOWN_NETWORK_TRAFFIC_PACKAGE,
                        uid = -1,
                        rxBytes = rx,
                        txBytes = tx,
                    ),
                )
            } else {
                emptyList()
            }
        return NetworkUsageInfo(
            wifiRxBytes = rx,
            wifiTxBytes = tx,
            cellularRxBytes = 0L,
            cellularTxBytes = 0L,
            apps = fallbackApps,
            wifiApps = emptyList(),
            cellularApps = emptyList(),
            fallbackApps = fallbackApps,
            isToday = false,
            needsUsageAccess = needsUsageAccess,
        )
    }

    /** 璇诲彇鏌愮缃戠粶绫诲瀷鐨勮澶囩骇鏀跺彂姹囨€汇€?*/
    private fun NetworkStatsManager.readSummary(
        transport: Int,
        start: Long,
        end: Long,
    ): Pair<Long, Long> {
        val netType =
            if (transport == NetworkCapabilities.TRANSPORT_CELLULAR) {
                ConnectivityManager.TYPE_MOBILE
            } else {
                ConnectivityManager.TYPE_WIFI
            }
        val bucket = querySummaryForDevice(netType, null, start, end)
        return bucket.rxBytes to bucket.txBytes
    }

    /** 璇诲彇鏌愮缃戠粶绫诲瀷涓嬫寜搴旂敤鑱氬悎鐨勭敤閲忋€?*/
    private fun NetworkStatsManager.readPerAppUsage(
        context: Context,
        networkType: Int,
        start: Long,
        end: Long,
    ): List<NetworkUsageApp> {
        val pm = context.packageManager
        val packageInfosByUid =
            pm
                .getInstalledApplications(PackageManager.GET_META_DATA)
                .filter { it.uid > 0 && (it.flags and ApplicationInfo.FLAG_SYSTEM) == 0 }
                .groupBy { it.uid }
                .mapValues { (_, apps) ->
                    apps.map { app ->
                        NetworkUsagePackageInfo(
                            packageName = app.packageName,
                            appName = app.loadLabel(pm).toString(),
                        )
                    }
                }

        val buckets =
            packageInfosByUid.keys.mapNotNull { uid ->
                runCatching {
                    val summary = queryDetailsForUid(networkType, null, start, end, uid)
                    var rx = 0L
                    var tx = 0L
                    val item = NetworkStats.Bucket()
                    summary.use {
                        while (it.hasNextBucket()) {
                            it.getNextBucket(item)
                            rx += item.rxBytes
                            tx += item.txBytes
                        }
                    }
                    NetworkUsageUidBucket(uid = uid, rxBytes = rx, txBytes = tx)
                }.getOrNull()
            }

        return buildNetworkUsageAppsFromUidBuckets(
            uidBuckets = buckets,
            packageResolver = { uid -> packageInfosByUid[uid].orEmpty() },
        ).take(8)
    }

    /** 閫氳繃涓€涓交閲?HTTP 璇锋眰浼扮畻缃戠粶寤惰繜銆?*/
    private suspend fun measureLatency(): Long? =
        withContext(Dispatchers.IO) {
            runCatching {
                val start = System.nanoTime()
                val connection = URL("https://www.google.com/generate_204").openConnection() as HttpURLConnection
                connection.connectTimeout = 2000
                connection.readTimeout = 2000
                connection.requestMethod = "GET"
                connection.inputStream.close()
                max(1, (System.nanoTime() - start) / 1_000_000L)
            }.getOrNull()
        }

    /** 涓嬭浇鍥哄畾澶у皬鏁版嵁浼扮畻涓嬭閫熷害銆?*/
    private suspend fun measureDownloadMbps(): Double? =
        withContext(Dispatchers.IO) {
            runCatching {
                val url = URL("https://speed.cloudflare.com/__down?bytes=1048576")
                val connection = url.openConnection() as HttpURLConnection
                connection.connectTimeout = 5000
                connection.readTimeout = 7000
                val start = System.nanoTime()
                var total = 0L
                connection.inputStream.use { input ->
                    val buffer = ByteArray(32 * 1024)
                    while (true) {
                        val read = input.read(buffer)
                        if (read <= 0) break
                        total += read
                    }
                }
                val secs = (System.nanoTime() - start) / 1_000_000_000.0
                if (total > 0L && secs > 0.0) (total * 8.0 / secs) / 1_000_000.0 else null
            }.getOrNull()
        }

    /** 涓婁紶鍥哄畾澶у皬鏁版嵁浼扮畻涓婅閫熷害銆?*/
    private suspend fun measureUploadMbps(): Double? =
        withContext(Dispatchers.IO) {
            runCatching {
                val payload = ByteArray(256 * 1024) { (it % 251).toByte() }
                val connection = URL("https://speed.cloudflare.com/__up").openConnection() as HttpURLConnection
                connection.connectTimeout = 5000
                connection.readTimeout = 7000
                connection.requestMethod = "POST"
                connection.doOutput = true
                connection.setRequestProperty("Content-Type", "application/octet-stream")
                val start = System.nanoTime()
                connection.outputStream.use { it.write(payload) }
                connection.inputStream.close()
                val secs = (System.nanoTime() - start) / 1_000_000_000.0
                if (secs > 0.0) (payload.size * 8.0 / secs) / 1_000_000.0 else null
            }.getOrNull()
        }

    /** 浣跨敤绯荤粺 NetworkCapabilities 涓殑閾捐矾甯﹀浣滀负娴嬮€熷け璐ユ椂鐨勫厹搴曠粨鏋溿€?*/
    private fun networkCapabilitiesSpeed(
        context: Context,
        latency: Long?,
    ): NetworkSpeedResult {
        val connectivity = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val caps = connectivity.getNetworkCapabilities(connectivity.activeNetwork)
        return NetworkSpeedResult(
            downloadMbps = caps?.linkDownstreamBandwidthKbps?.takeIf { it > 0 }?.let { formatMbps(it / 1000.0) } ?: "--",
            uploadMbps = caps?.linkUpstreamBandwidthKbps?.takeIf { it > 0 }?.let { formatMbps(it / 1000.0) } ?: "--",
            latencyMs = latency,
            measured = false,
        )
    }

    /** 闄愬苟鍙戞壂鎻忓悓涓€缃戞锛岄伩鍏嶄竴娆℃€у惎鍔ㄨ繃澶氭帰娴嬩换鍔￠€犳垚璧勬簮鍘嬪姏銆?*/
    private suspend fun scanSubnet(
        prefix: String,
        currentIp: String,
        gateway: String,
    ): List<NetworkDeviceInfo> =
        coroutineScope {
            val seed = listOf(currentIp, gateway).filter { it != "--" }
            val targets = (1..254).map { "$prefix.$it" }
            targets.chunked(32).flatMap { chunk ->
                chunk
                    .map { ip ->
                        async(Dispatchers.IO) {
                            try {
                                currentCoroutineContext().ensureActive()
                                val addr = InetAddress.getByName(ip)
                                if (addr.isReachable(200) || ip in seed) {
                                    NetworkDeviceInfo(ip, addr.canonicalHostName?.takeIf { it != ip } ?: "unknown", readMacFromArp(ip))
                                } else {
                                    null
                                }
                            } catch (_: Exception) {
                                null
                            }
                        }
                    }.awaitAll()
                    .filterNotNull()
            }
        }

    /** 浠?ARP 琛ㄨ鍙栧凡鍙戠幇鐨勫眬鍩熺綉璁惧銆?*/
    private fun readArpDevices(): List<NetworkDeviceInfo> =
        runCatching {
            BufferedReader(InputStreamReader(java.io.FileInputStream("/proc/net/arp"))).useLines { lines ->
                lines
                    .drop(1)
                    .mapNotNull { line ->
                        val parts = line.trim().split(Regex("\\s+"))
                        val ip = parts.getOrNull(0) ?: return@mapNotNull null
                        val mac = parts.getOrNull(3)?.takeIf { it != "00:00:00:00:00:00" } ?: "unknown"
                        NetworkDeviceInfo(ip, "unknown", mac)
                    }.toList()
            }
        }.getOrDefault(emptyList())

    /** 鏌ヨ鎸囧畾 IP 鍦?ARP 琛ㄤ腑鐨?MAC 鍦板潃銆?*/
    private fun readMacFromArp(ip: String): String = readArpDevices().firstOrNull { it.ip == ip }?.macAddress ?: "unknown"

    /** 鏍煎紡鍖?Mbps 鏁板€硷紝缁熶竴淇濈暀涓€浣嶅皬鏁般€?*/
    private fun formatMbps(value: Double): String = String.format(Locale.US, "%.1f", value.coerceAtLeast(0.0))

    /** 灏?IP 杞垚鍙帓搴忕殑鏁板€硷紝淇濊瘉璁惧鍒楄〃椤哄簭绋冲畾銆?*/
    private fun ipSortKey(ip: String): Long = ip.split('.').fold(0L) { acc, p -> acc * 256 + (p.toLongOrNull() ?: 0L) }
}
