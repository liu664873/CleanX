package com.quickcleanpro.phonecleaner.presentation.screen.toolbox

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.text.format.Formatter
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quickcleanpro.phonecleaner.R
import com.quickcleanpro.phonecleaner.domain.repository.ToolboxRepository
import com.quickcleanpro.phonecleaner.presentation.common.networkInfoReaderOrPreview
import com.quickcleanpro.phonecleaner.presentation.common.toolboxRepositoryOrPreview
import com.quickcleanpro.phonecleaner.domain.model.toolbox.NetworkScanResult
import com.quickcleanpro.phonecleaner.domain.model.toolbox.NetworkSpeedResult
import com.quickcleanpro.phonecleaner.domain.model.toolbox.NetworkSpeedProgress
import com.quickcleanpro.phonecleaner.domain.model.toolbox.NetworkUsageInfo
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.atomic.AtomicReference


internal data class NetworkSpeedUiState(
    val networkInfo: NetworkInfo = NetworkInfo.EMPTY,
    val hasNetwork: Boolean = false,
    val speedState: SpeedState = SpeedState.Idle,
    val speed: NetworkSpeedResult? = null,
    val progress: NetworkSpeedProgress = NetworkSpeedProgress()
) {
    val downloadLabel: String
        get() = when {
            progress.downloadMbps != null -> progress.downloadMbps.orEmpty()
            speed != null -> speed.downloadMbps
            else -> "--"
        }

    val uploadLabel: String
        get() = when {
            progress.uploadMbps != null -> progress.uploadMbps.orEmpty()
            speed != null -> speed.uploadMbps
            else -> "--"
        }

    val isDownloadTesting: Boolean
        get() = speedState == SpeedState.Running &&
            (progress.phase == "latency" || progress.phase == "download")

    val isUploadTesting: Boolean
        get() = speedState == SpeedState.Running && progress.phase == "upload"
}


internal class NetworkSpeedViewModel(
    private val repository: ToolboxRepository = toolboxRepositoryOrPreview(),
    private val networkInfoReader: () -> NetworkInfo = networkInfoReaderOrPreview(),
    private val testLoader: (((suspend () -> Unit)) -> Unit)? = null,
    private val networkRefreshRetryCount: Int = 12,
    private val networkRefreshRetryDelayMillis: Long = 500L
) : ViewModel() {

    constructor(
        repository: ToolboxRepository,
        context: Context
    ) : this(repository, { readNetworkInfo(context) })

    private val _uiState = MutableStateFlow(NetworkSpeedUiState())
    private var speedTestJob: Job? = null
    private var networkRefreshJob: Job? = null

    val uiState: StateFlow<NetworkSpeedUiState> = _uiState.asStateFlow()

    init {
        refreshNetworkState()
    }

    fun refreshNetworkState(): Boolean {
        var hasNetwork = false
        _uiState.update {
            hasNetwork = repository.isNetworkAvailable()
            it.copy(
                networkInfo = networkInfoReader(),
                hasNetwork = hasNetwork
            )
        }
        return hasNetwork
    }

    fun refreshNetworkStateUntilNetworkAvailable() {
        if (_uiState.value.speedState == SpeedState.Running) return
        networkRefreshJob?.cancel()
        networkRefreshJob = launchNetworkRefresh {
            repeat(networkRefreshRetryCount.coerceAtLeast(1)) { attempt ->
                if (refreshNetworkState()) return@launchNetworkRefresh
                if (attempt < networkRefreshRetryCount - 1 && networkRefreshRetryDelayMillis > 0L) {
                    delay(networkRefreshRetryDelayMillis)
                }
            }
        }
    }

    fun runSpeedTest() {
        val state = _uiState.value
        if (state.speedState == SpeedState.Running || !state.hasNetwork) return

        networkRefreshJob?.cancel()
        _uiState.update {
            it.copy(
                speedState = SpeedState.Running,
                speed = null,
                progress = NetworkSpeedProgress(phase = "latency")
            )
        }
        speedTestJob = launchLoad {
            try {
                val result = repository.runSpeedTestWithProgress { progress ->
                    _uiState.update { current ->
                        if (current.speedState == SpeedState.Running) {
                            current.copy(progress = progress)
                        } else {
                            current
                        }
                    }
                }
                _uiState.update { current ->
                    if (current.speedState == SpeedState.Running) {
                        current.copy(
                            speedState = SpeedState.Done,
                            speed = result,
                            progress = current.progress.copy(
                                downloadMbps = result.downloadMbps,
                                uploadMbps = result.uploadMbps,
                                latencyMs = result.latencyMs,
                                phase = "done"
                            )
                        )
                    } else {
                        current
                    }
                }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } finally {
                speedTestJob = null
            }
        }
    }

    fun stopSpeedTest() {
        speedTestJob?.cancel()
        speedTestJob = null
        networkRefreshJob?.cancel()
        _uiState.update {
            it.copy(
                speedState = SpeedState.Idle,
                speed = null,
                progress = it.progress.copy(phase = "idle")
            )
        }
    }

    private fun launchLoad(block: suspend () -> Unit): Job? {
        val loader = testLoader
        if (loader != null) {
            loader(block)
            return null
        } else {
            return viewModelScope.launch { block() }
        }
    }

    private fun launchNetworkRefresh(block: suspend () -> Unit): Job? {
        val loader = testLoader
        return if (loader != null) {
            loader(block)
            null
        } else {
            viewModelScope.launch { block() }
        }
    }
}


internal data class NetworkScanUiState(
    val networkInfo: NetworkInfo = NetworkInfo.EMPTY,
    val hasWifi: Boolean = false,
    val scanState: NetworkScanState = NetworkScanState.Idle,
    val scan: NetworkScanResult? = null,
    val scanTime: String = "--",
    val completedDetailCount: Int = 0
)

internal object NetworkScanSessionStore {
    private val latestScan = AtomicReference<NetworkScanResult?>(null)

    fun save(scan: NetworkScanResult) {
        latestScan.set(scan)
    }

    fun get(): NetworkScanResult? = latestScan.get()

    fun clear() {
        latestScan.set(null)
    }
}


internal class NetworkScanViewModel(
    private val repository: ToolboxRepository = toolboxRepositoryOrPreview(),
    private val networkInfoReader: () -> NetworkInfo = networkInfoReaderOrPreview(),
    private val nowLabel: () -> String = {
        SimpleDateFormat("MM/dd/yyyy HH:mm:ss", Locale.US).format(Date())
    },
    private val testLoader: (((suspend () -> Unit)) -> Unit)? = null,
    private val progressDelayMillis: Long = 260L,
    private val networkRefreshRetryCount: Int = 12,
    private val networkRefreshRetryDelayMillis: Long = 500L
) : ViewModel() {

    constructor(
        repository: ToolboxRepository,
        context: Context
    ) : this(repository, { readNetworkInfo(context) })

    private val _uiState = MutableStateFlow(NetworkScanUiState())
    private var networkRefreshJob: Job? = null

    val uiState: StateFlow<NetworkScanUiState> = _uiState.asStateFlow()

    init {
        refreshNetworkState()
    }

    fun refreshNetworkState(): Boolean {
        var hasWifi = false
        _uiState.update {
            hasWifi = repository.isWifiConnected()
            it.copy(
                networkInfo = networkInfoReader(),
                hasWifi = hasWifi
            )
        }
        return hasWifi
    }

    fun refreshNetworkStateUntilWifiConnected() {
        networkRefreshJob?.cancel()
        networkRefreshJob = launchNetworkRefresh {
            repeat(networkRefreshRetryCount.coerceAtLeast(1)) { attempt ->
                if (refreshNetworkState()) return@launchNetworkRefresh
                if (attempt < networkRefreshRetryCount - 1 && networkRefreshRetryDelayMillis > 0L) {
                    delay(networkRefreshRetryDelayMillis)
                }
            }
        }
    }

    fun startScan() {
        val state = _uiState.value
        if (state.scanState == NetworkScanState.Running || !state.hasWifi) return

        networkRefreshJob?.cancel()
        _uiState.update {
            it.copy(
                scanState = NetworkScanState.Running,
                scan = null,
                scanTime = "--",
                completedDetailCount = 0
            )
        }
        NetworkScanSessionStore.clear()
        launchLoad {
            val scan = repository.scanWifi()
            NetworkScanSessionStore.save(scan)
            repeat(SCAN_DETAIL_COUNT) { index ->
                if (progressDelayMillis > 0L) delay(progressDelayMillis)
                _uiState.update { it.copy(completedDetailCount = index + 1) }
            }
            _uiState.update {
                it.copy(
                    scanState = NetworkScanState.Done,
                    scan = scan,
                    scanTime = nowLabel()
                )
            }
        }
    }

    private fun launchLoad(block: suspend () -> Unit) {
        val loader = testLoader
        if (loader != null) {
            loader(block)
        } else {
            viewModelScope.launch { block() }
        }
    }

    private fun launchNetworkRefresh(block: suspend () -> Unit): Job? {
        val loader = testLoader
        return if (loader != null) {
            loader(block)
            null
        } else {
            viewModelScope.launch { block() }
        }
    }

    private companion object {
        private const val SCAN_DETAIL_COUNT = 6
    }
}


internal data class NetworkScanDevicesUiState(
    val scan: NetworkScanResult? = null,
    val isLoading: Boolean = true
) {
    val devices get() = scan?.devices.orEmpty()
}


internal class NetworkScanDevicesViewModel(
    private val repository: ToolboxRepository = toolboxRepositoryOrPreview(),
    private val testLoader: (((suspend () -> Unit)) -> Unit)? = null
) : ViewModel() {

    constructor(repository: ToolboxRepository) : this(repository, null)

    private val _uiState = MutableStateFlow(NetworkScanDevicesUiState())

    val uiState: StateFlow<NetworkScanDevicesUiState> = _uiState.asStateFlow()

    init {
        loadDevices()
    }

    fun loadDevices() {
        val cachedScan = NetworkScanSessionStore.get()
        if (cachedScan != null) {
            _uiState.update { it.copy(scan = cachedScan, isLoading = false) }
            return
        }
        _uiState.update { it.copy(isLoading = true) }
        launchLoad {
            val scan = runCatching { repository.scanWifi() }.getOrNull()
            if (scan != null) {
                NetworkScanSessionStore.save(scan)
            }
            _uiState.update { it.copy(scan = scan, isLoading = false) }
        }
    }

    private fun launchLoad(block: suspend () -> Unit) {
        val loader = testLoader
        if (loader != null) {
            loader(block)
        } else {
            viewModelScope.launch { block() }
        }
    }
}


internal data class NetworkUsageUiState(
    val selectedTab: Int = 1,
    val usage: NetworkUsageInfo? = null,
    val isLoading: Boolean = true
) {
    val selectedTotalBytes: Long
        get() = usage?.let { if (selectedTab == 0) it.cellularTotalBytes else it.wifiTotalBytes } ?: 0L

    val selectedApps
        get() = usage?.let {
            when {
                selectedTab == 0 && it.cellularApps.isNotEmpty() -> it.cellularApps
                selectedTab == 1 && it.wifiApps.isNotEmpty() -> it.wifiApps
                it.fallbackApps.isNotEmpty() -> it.fallbackApps
                else -> emptyList()
            }
        }.orEmpty()
}


internal class NetworkUsageViewModel(
    private val repository: ToolboxRepository = toolboxRepositoryOrPreview(),
    private val testLoader: (((suspend () -> Unit)) -> Unit)? = null
) : ViewModel() {

    constructor(repository: ToolboxRepository) : this(repository, null)

    private val _uiState = MutableStateFlow(NetworkUsageUiState())

    val uiState: StateFlow<NetworkUsageUiState> = _uiState.asStateFlow()

    init {
        refreshUsage()
    }

    fun selectTab(tab: Int) {
        _uiState.update { it.copy(selectedTab = tab.coerceIn(0, 1)) }
    }

    fun refreshUsage() {
        _uiState.update { it.copy(isLoading = true) }
        launchLoad {
            val usage = repository.readNetworkUsage()
            _uiState.update { it.copy(usage = usage, isLoading = false) }
        }
    }

    private fun launchLoad(block: suspend () -> Unit) {
        val loader = testLoader
        if (loader != null) {
            loader(block)
        } else {
            viewModelScope.launch { block() }
        }
    }
}


internal fun readNetworkInfo(context: Context): NetworkInfo {
    val appContext = context.applicationContext
    val connectivity = appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val capabilities = connectivity.getNetworkCapabilities(connectivity.activeNetwork)
    val type = when {
        capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true -> appContext.getString(R.string.wifi)
        capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true -> appContext.getString(R.string.network_type_cellular)
        capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) == true -> appContext.getString(R.string.network_type_ethernet)
        else -> "--"
    }
    val downstream = capabilities?.linkDownstreamBandwidthKbps
        ?.takeIf { it > 0 }
        ?.let { String.format(Locale.US, "%.1f", it / 1000.0) }
        ?: "--"
    val upstream = capabilities?.linkUpstreamBandwidthKbps
        ?.takeIf { it > 0 }
        ?.let { String.format(Locale.US, "%.1f", it / 1000.0) }
        ?: "--"

    val wifiManager = appContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
    val wifiInfo = wifiManager?.connectionInfo
    val ssid = wifiInfo?.ssid
        ?.removePrefix("\"")
        ?.removeSuffix("\"")
        ?.takeIf { it.isNotBlank() && it != "<unknown ssid>" }
        ?: appContext.getString(R.string.unknown_ssid)
    val ip = wifiInfo?.ipAddress
        ?.takeIf { it != 0 }
        ?.let { Formatter.formatIpAddress(it) }
        ?: "--"

    return NetworkInfo(
        type = type,
        ssid = ssid,
        ip = ip,
        downstreamMbps = downstream,
        upstreamMbps = upstream
    )
}
