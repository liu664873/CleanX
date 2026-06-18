package com.quickcleanpro.phonecleaner.presentation.screen.tools.networkspeed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quickcleanpro.phonecleaner.domain.model.toolbox.NetworkSpeedProgress
import com.quickcleanpro.phonecleaner.domain.model.toolbox.NetworkSpeedResult
import com.quickcleanpro.phonecleaner.domain.repository.NetworkRepository
import com.quickcleanpro.phonecleaner.presentation.screen.tools.common.network.NetworkInfo
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class NetworkSpeedPhase {
    Idle,
    Testing,
    Result,
    Error,
}

data class NetworkSpeedUiState(
    val networkInfo: NetworkInfo = NetworkInfo(),
    val hasNetwork: Boolean = false,
    val phase: NetworkSpeedPhase = NetworkSpeedPhase.Idle,
    val speed: NetworkSpeedResult? = null,
    val progress: NetworkSpeedProgress = NetworkSpeedProgress(),
    val errorMessage: String? = null,
) {
    val downloadLabel: String
        get() =
            when {
                progress.downloadMbps != null -> progress.downloadMbps
                speed != null -> speed.downloadMbps
                else -> "--"
            }

    val uploadLabel: String
        get() =
            when {
                progress.uploadMbps != null -> progress.uploadMbps
                speed != null -> speed.uploadMbps
                else -> "--"
            }

    val latencyLabel: String
        get() = progress.latencyMs?.toString() ?: speed?.latencyMs?.toString() ?: "--"
}

class NetworkSpeedViewModel(
    private val repository: NetworkRepository,
    private val networkInfoReader: () -> NetworkInfo,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ViewModel() {
    private val _uiState = MutableStateFlow(NetworkSpeedUiState())
    private var speedTestJob: Job? = null
    private var networkRefreshJob: Job? = null

    val uiState: StateFlow<NetworkSpeedUiState> = _uiState.asStateFlow()

    init {
        refreshNetworkState()
    }

    fun refreshNetworkState(): Boolean {
        val hasNetwork = repository.isNetworkAvailable()
        _uiState.update {
            it.copy(
                networkInfo = networkInfoReader(),
                hasNetwork = hasNetwork,
                errorMessage = null,
            )
        }
        return hasNetwork
    }

    fun refreshNetworkStateUntilNetworkAvailable() {
        if (_uiState.value.phase == NetworkSpeedPhase.Testing) return
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

    fun runSpeedTest() {
        val state = _uiState.value
        if (state.phase == NetworkSpeedPhase.Testing || !state.hasNetwork) return

        networkRefreshJob?.cancel()
        speedTestJob?.cancel()
        _uiState.update {
            it.copy(
                phase = NetworkSpeedPhase.Testing,
                speed = null,
                progress = NetworkSpeedProgress(phase = "latency"),
                errorMessage = null,
            )
        }
        speedTestJob =
            viewModelScope.launch(ioDispatcher) {
                try {
                    val result =
                        repository.runSpeedTestWithProgress { progress ->
                            _uiState.update { current ->
                                if (current.phase == NetworkSpeedPhase.Testing) {
                                    current.copy(progress = progress)
                                } else {
                                    current
                                }
                            }
                        }
                    _uiState.update {
                        it.copy(
                            phase = NetworkSpeedPhase.Result,
                            speed = result,
                            progress =
                                it.progress.copy(
                                    downloadMbps = result.downloadMbps,
                                    uploadMbps = result.uploadMbps,
                                    latencyMs = result.latencyMs,
                                    phase = "done",
                                ),
                            errorMessage = null,
                        )
                    }
                } catch (cancellation: CancellationException) {
                    throw cancellation
                } catch (error: Throwable) {
                    _uiState.update {
                        it.copy(
                            phase = NetworkSpeedPhase.Error,
                            errorMessage = error.message,
                        )
                    }
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
                phase = NetworkSpeedPhase.Idle,
                speed = null,
                progress = NetworkSpeedProgress(),
                errorMessage = null,
            )
        }
    }

    private companion object {
        private const val NETWORK_REFRESH_RETRY_COUNT = 12
        private const val NETWORK_REFRESH_RETRY_DELAY_MILLIS = 500L
    }
}
