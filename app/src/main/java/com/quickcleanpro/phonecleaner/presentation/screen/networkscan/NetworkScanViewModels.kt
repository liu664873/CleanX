package com.quickcleanpro.phonecleaner.presentation.screen.networkscan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quickcleanpro.phonecleaner.domain.model.toolbox.NetworkScanResult
import com.quickcleanpro.phonecleaner.domain.repository.NetworkRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
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

enum class NetworkScanState {
    Idle,
    Running,
    Done,
    Error,
}

data class NetworkScanUiState(
    val networkInfo: NetworkInfo = NetworkInfo(),
    val hasWifi: Boolean = false,
    val scanState: NetworkScanState = NetworkScanState.Idle,
    val scan: NetworkScanResult? = null,
    val scanTime: String = "--",
    val completedDetailCount: Int = 0,
    val errorMessage: String? = null,
)

object NetworkScanSessionStore {
    private val latestScan = AtomicReference<NetworkScanResult?>(null)

    fun save(scan: NetworkScanResult) {
        latestScan.set(scan)
    }

    fun get(): NetworkScanResult? = latestScan.get()

    fun clear() {
        latestScan.set(null)
    }
}

class NetworkScanViewModel(
    private val repository: NetworkRepository,
    private val networkInfoReader: () -> NetworkInfo,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val nowLabel: () -> String = {
        SimpleDateFormat("MM/dd/yyyy HH:mm:ss", Locale.US).format(Date())
    },
) : ViewModel() {
    private val _uiState = MutableStateFlow(NetworkScanUiState())
    private var networkRefreshJob: Job? = null
    private var scanJob: Job? = null

    val uiState: StateFlow<NetworkScanUiState> = _uiState.asStateFlow()

    init {
        refreshNetworkState()
    }

    fun refreshNetworkState(): Boolean {
        val hasWifi = repository.isWifiConnected()
        _uiState.update {
            it.copy(
                networkInfo = networkInfoReader(),
                hasWifi = hasWifi,
                errorMessage = null,
            )
        }
        return hasWifi
    }

    fun refreshNetworkStateUntilWifiConnected() {
        if (_uiState.value.scanState == NetworkScanState.Running) return
        networkRefreshJob?.cancel()
        networkRefreshJob =
            viewModelScope.launch(ioDispatcher) {
                repeat(NETWORK_REFRESH_RETRY_COUNT) { attempt ->
                    if (refreshNetworkState()) return@launch
                    if (attempt < NETWORK_REFRESH_RETRY_COUNT - 1) {
                        delay(NETWORK_REFRESH_RETRY_DELAY_MILLIS)
                    }
                }
            }
    }

    fun startScan() {
        val state = _uiState.value
        if (state.scanState == NetworkScanState.Running || !state.hasWifi) return

        networkRefreshJob?.cancel()
        scanJob?.cancel()
        NetworkScanSessionStore.clear()
        _uiState.update {
            it.copy(
                scanState = NetworkScanState.Running,
                scan = null,
                scanTime = "--",
                completedDetailCount = 0,
                errorMessage = null,
            )
        }
        scanJob =
            viewModelScope.launch(ioDispatcher) {
                runCatching {
                    val scan = repository.scanWifi()
                    NetworkScanSessionStore.save(scan)
                    repeat(SCAN_DETAIL_COUNT) { index ->
                        delay(PROGRESS_DELAY_MILLIS)
                        _uiState.update { it.copy(completedDetailCount = index + 1) }
                    }
                    scan
                }.onSuccess { scan ->
                    _uiState.update {
                        it.copy(
                            scanState = NetworkScanState.Done,
                            scan = scan,
                            scanTime = nowLabel(),
                            networkInfo =
                                it.networkInfo.copy(
                                    ssid = scan.ssid.takeUnless { ssid -> ssid == "<unknown ssid>" } ?: it.networkInfo.ssid,
                                    ip = scan.deviceIp,
                                ),
                            hasWifi = scan.hasWifi,
                            errorMessage = null,
                        )
                    }
                }.onFailure { error ->
                    _uiState.update {
                        it.copy(
                            scanState = NetworkScanState.Error,
                            errorMessage = error.message,
                        )
                    }
                }
            }
    }

    private companion object {
        private const val SCAN_DETAIL_COUNT = 6
        private const val PROGRESS_DELAY_MILLIS = 260L
        private const val NETWORK_REFRESH_RETRY_COUNT = 12
        private const val NETWORK_REFRESH_RETRY_DELAY_MILLIS = 500L
    }
}

data class NetworkScanDevicesUiState(
    val scan: NetworkScanResult? = null,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
) {
    val devices get() = scan?.devices.orEmpty()
}

class NetworkScanDevicesViewModel(
    private val repository: NetworkRepository,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ViewModel() {
    private val _uiState = MutableStateFlow(NetworkScanDevicesUiState())

    val uiState: StateFlow<NetworkScanDevicesUiState> = _uiState.asStateFlow()

    init {
        loadDevices()
    }

    fun loadDevices() {
        val cachedScan = NetworkScanSessionStore.get()
        if (cachedScan != null) {
            _uiState.value = NetworkScanDevicesUiState(scan = cachedScan, isLoading = false)
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch(ioDispatcher) {
            runCatching {
                repository.scanWifi()
            }.onSuccess { scan ->
                NetworkScanSessionStore.save(scan)
                _uiState.value = NetworkScanDevicesUiState(scan = scan, isLoading = false)
            }.onFailure { error ->
                _uiState.value =
                    NetworkScanDevicesUiState(
                        isLoading = false,
                        errorMessage = error.message,
                    )
            }
        }
    }
}
