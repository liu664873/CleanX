package com.quickcleanpro.phonecleaner.domain.state

import android.app.PendingIntent
import com.quickcleanpro.phonecleaner.domain.model.CleanResult
import com.quickcleanpro.phonecleaner.domain.model.JunkFile
import com.quickcleanpro.phonecleaner.domain.model.ScanProgress
import com.quickcleanpro.phonecleaner.domain.model.ScanResult
import com.quickcleanpro.phonecleaner.domain.model.clean.MemoryCleanResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Shared state for the main clean flow.
 *
 * This object stores scan and clean facts that must survive cross-screen flows.
 * UI display state, animation state, and navigation remain outside this module.
 */
class SharedScanState {
    private val _scanProgress = MutableStateFlow(ScanProgress.IDLE)
    private val _scanResult = MutableStateFlow<ScanResult?>(null)
    private val _sessionState = MutableStateFlow(ScanSessionState())
    private val _cleanupSummary = MutableStateFlow<CleanupSummary?>(null)
    private val _cleanResult = MutableStateFlow<CleanResult?>(null)
    private val _memoryResult = MutableStateFlow<MemoryCleanResult?>(null)
    private val _pendingDeleteAuthorization = MutableStateFlow<PendingDeleteAuthorization?>(null)

    val scanProgress: StateFlow<ScanProgress> = _scanProgress.asStateFlow()

    val scanResult: StateFlow<ScanResult?> = _scanResult.asStateFlow()

    val sessionState: StateFlow<ScanSessionState> = _sessionState.asStateFlow()

    val cleanupSummary: StateFlow<CleanupSummary?> = _cleanupSummary.asStateFlow()

    val cleanResult: StateFlow<CleanResult?> = _cleanResult.asStateFlow()

    val memoryResult: StateFlow<MemoryCleanResult?> = _memoryResult.asStateFlow()

    val pendingDeleteAuthorization: StateFlow<PendingDeleteAuthorization?> =
        _pendingDeleteAuthorization.asStateFlow()

    fun setScanProgress(value: ScanProgress) {
        _scanProgress.value = value
        _sessionState.value =
            _sessionState.value.copy(
                progress = value,
                scanResult = _scanResult.value,
            )
    }

    fun setScanResult(value: ScanResult) {
        _scanResult.value = value
        _sessionState.value =
            _sessionState.value.copy(
                progress = _scanProgress.value,
                scanResult = value,
            )
    }

    fun setPendingDeleteAuthorization(value: PendingDeleteAuthorization?) {
        _pendingDeleteAuthorization.value = value
    }

    fun setCleanResult(value: CleanResult) {
        _cleanResult.value = value
    }

    fun setMemoryResult(value: MemoryCleanResult) {
        _memoryResult.value = value
    }

    fun setCleanupSummary(value: CleanupSummary) {
        _cleanupSummary.value = value
    }

    fun removeCleanedFiles(cleanedFiles: List<JunkFile>) {
        val current = _scanResult.value ?: return
        if (cleanedFiles.isEmpty()) return

        val cleanedPaths = cleanedFiles.map { it.filePath }.toSet()
        val remainingFiles = current.junkFiles.filterNot { it.filePath in cleanedPaths }
        val remainingSize = remainingFiles.sumOf { it.fileSize }
        val updatedResult =
            current.copy(
                junkFiles = remainingFiles,
                totalSize = remainingSize,
                totalCount = remainingFiles.size,
                categoryBreakdown = remainingFiles.groupBy { it.category },
            )

        _scanResult.value = updatedResult
        _sessionState.value =
            _sessionState.value.copy(
                progress =
                    if (remainingFiles.isEmpty()) {
                        ScanProgress.IDLE
                    } else {
                        ScanProgress(
                            percent = 100f,
                            foundCount = remainingFiles.size,
                            foundSize = remainingSize,
                        )
                    },
                scanResult = updatedResult,
            )
        _scanProgress.value = _sessionState.value.progress
    }

    fun clearCleanResults() {
        _cleanResult.value = null
        _memoryResult.value = null
        _cleanupSummary.value = null
        _pendingDeleteAuthorization.value = null
    }

    fun clear() {
        _scanProgress.value = ScanProgress.IDLE
        _scanResult.value = null
        _sessionState.value = ScanSessionState()
        _cleanupSummary.value = null
        _cleanResult.value = null
        _memoryResult.value = null
        _pendingDeleteAuthorization.value = null
    }
}

data class ScanSessionState(
    val progress: ScanProgress = ScanProgress.IDLE,
    val scanResult: ScanResult? = null,
)

data class PendingDeleteAuthorization(
    val request: PendingIntent,
    val message: String,
    val pendingCount: Int,
)

data class CleanupSummary(
    val freedSpace: Long,
    val cleanedCount: Int,
    val failedCount: Int,
    val memoryFreedBytes: Long,
    val memoryProcessesKilled: Int,
) {
    val totalFreedBytes: Long
        get() = freedSpace + memoryFreedBytes

    val hasVisibleResult: Boolean
        get() = totalFreedBytes > 0L || cleanedCount > 0 || failedCount > 0 || memoryProcessesKilled > 0
}
