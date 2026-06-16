package com.quickcleanpro.phonecleaner.presentation.screen.scan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quickcleanpro.phonecleaner.R
import com.quickcleanpro.phonecleaner.domain.model.JunkCategory
import com.quickcleanpro.phonecleaner.domain.model.ScanResult
import com.quickcleanpro.phonecleaner.domain.state.SharedScanState
import com.quickcleanpro.phonecleaner.domain.usecase.ScanJunkUseCase
import com.quickcleanpro.phonecleaner.utils.FileSizeFormatter
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ScanViewModel(
    private val scanJunkUseCase: ScanJunkUseCase,
    private val sharedState: SharedScanState,
    private val ioDispatcher: CoroutineDispatcher,
) : ViewModel() {
    enum class ScanState {
        Idle,
        Scanning,
        Completed,
        Error,
    }

    data class ScanUiState(
        val scanState: ScanState = ScanState.Idle,
        val progress: Float = 0f,
        val currentCategory: JunkCategory? = null,
        val foundItemCount: Int = 0,
        val foundTotalSize: Long = 0,
        val formattedFoundSize: String = "0 B",
        val scanResult: ScanResult? = null,
        val errorMessageRes: Int? = null,
        val errorMessage: String? = null,
    )

    private val _uiState = MutableStateFlow(ScanUiState())
    private var progressJob: Job? = null
    private var scanJob: Job? = null
    private var hasStarted = false

    val uiState: StateFlow<ScanUiState> = _uiState.asStateFlow()

    fun startScanIfNeeded() {
        if (hasStarted) return
        startScanInternal(resetSession = true)
    }

    fun retryScan() {
        hasStarted = false
        startScanInternal(resetSession = true)
    }

    private fun startScanInternal(resetSession: Boolean) {
        if (_uiState.value.scanState == ScanState.Scanning) return
        hasStarted = true
        scanJob?.cancel()
        if (resetSession) {
            sharedState.clear()
        }
        observeProgress()
        _uiState.value = ScanUiState(scanState = ScanState.Scanning)

        scanJob =
            viewModelScope.launch(ioDispatcher) {
                try {
                    val result = scanJunkUseCase()
                    progressJob?.cancel()
                    _uiState.value =
                        buildUiState(
                            scanState = ScanState.Completed,
                            progress = 100f,
                            currentCategory = null,
                            foundCount = result.totalCount,
                            foundSize = result.totalSize,
                            scanResult = result,
                            errorMessageRes = null,
                            errorMessage = null,
                        )
                } catch (error: Exception) {
                    progressJob?.cancel()
                    _uiState.value =
                        _uiState.value.copy(
                            scanState = ScanState.Error,
                            errorMessageRes = R.string.scan_failed,
                            errorMessage = error.message,
                        )
                }
            }
    }

    private fun observeProgress() {
        progressJob?.cancel()
        progressJob =
            viewModelScope.launch {
                sharedState.scanProgress.collect { progress ->
                    val currentResult = sharedState.scanResult.value
                    _uiState.value =
                        buildUiState(
                            scanState = ScanState.Scanning,
                            progress = progress.percent,
                            currentCategory = progress.currentCategory,
                            foundCount = currentResult?.totalCount ?: progress.foundCount,
                            foundSize = currentResult?.totalSize ?: progress.foundSize,
                            scanResult = currentResult,
                            errorMessageRes = null,
                            errorMessage = null,
                        )
                }
            }
    }

    private fun buildUiState(
        scanState: ScanState,
        progress: Float,
        currentCategory: JunkCategory?,
        foundCount: Int,
        foundSize: Long,
        scanResult: ScanResult?,
        errorMessageRes: Int?,
        errorMessage: String?,
    ): ScanUiState =
        ScanUiState(
            scanState = scanState,
            progress = progress,
            currentCategory = currentCategory,
            foundItemCount = foundCount,
            foundTotalSize = foundSize,
            formattedFoundSize = FileSizeFormatter.format(foundSize),
            scanResult = scanResult,
            errorMessageRes = errorMessageRes,
            errorMessage = errorMessage,
        )

    override fun onCleared() {
        scanJob?.cancel()
        progressJob?.cancel()
        super.onCleared()
    }
}
