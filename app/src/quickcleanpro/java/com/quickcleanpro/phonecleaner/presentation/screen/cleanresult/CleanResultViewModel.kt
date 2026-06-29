package com.quickcleanpro.phonecleaner.presentation.screen.cleanresult

import androidx.lifecycle.ViewModel
import com.quickcleanpro.phonecleaner.domain.state.CleanupSummary
import com.quickcleanpro.phonecleaner.domain.state.SharedScanState
import com.quickcleanpro.phonecleaner.utils.FileSizeFormatter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.quickcleanpro.phonecleaner.presentation.common.sharedScanStateOrPreview

class CleanResultViewModel constructor(
    private val sharedState: SharedScanState
) : ViewModel() {

    constructor() : this(sharedScanStateOrPreview())

    data class CleanResultUiState(
        val freedSpace: Long = 0L,
        val cleanedCount: Int = 0,
        val failedCount: Int = 0,
        val memoryFreedBytes: Long = 0L,
        val memoryProcessesKilled: Int = 0,
        val totalFreedBytes: Long = 0L,
        val formattedFreedSpace: String = "",
        val hasVisibleResult: Boolean = false
    )

    private val _uiState = MutableStateFlow(CleanResultUiState())

    val uiState: StateFlow<CleanResultUiState> = _uiState.asStateFlow()

    fun loadResult() {
        val summary = sharedState.cleanupSummary.value ?: sharedState.cleanResult.value?.let { result ->
            CleanupSummary(
                freedSpace = result.freedSpace,
                cleanedCount = result.successCount,
                failedCount = result.failedCount,
                memoryFreedBytes = sharedState.memoryResult.value?.freedBytes ?: 0L,
                memoryProcessesKilled = sharedState.memoryResult.value?.killedCount ?: 0
            )
        } ?: CleanupSummary(
            freedSpace = 0L,
            cleanedCount = 0,
            failedCount = 0,
            memoryFreedBytes = 0L,
            memoryProcessesKilled = 0
        )

        _uiState.value = CleanResultUiState(
            freedSpace = summary.freedSpace,
            cleanedCount = summary.cleanedCount,
            failedCount = summary.failedCount,
            memoryFreedBytes = summary.memoryFreedBytes,
            memoryProcessesKilled = summary.memoryProcessesKilled,
            totalFreedBytes = summary.totalFreedBytes,
            formattedFreedSpace = summary.totalFreedBytes
                .takeIf { it > 0L }
                ?.let(FileSizeFormatter::format)
                .orEmpty(),
            hasVisibleResult = summary.hasVisibleResult
        )
    }

    fun clearResult() {
        sharedState.clearCleanResults()
    }
}
