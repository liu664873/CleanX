package com.quickcleanpro.phonecleaner.presentation.screen.tools.networkscan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quickcleanpro.phonecleaner.domain.model.toolbox.NetworkScanResult
import com.quickcleanpro.phonecleaner.domain.repository.NetworkRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

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
