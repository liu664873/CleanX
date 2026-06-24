package com.quickcleanpro.phonecleaner.presentation.screen.JunkClean

import android.app.PendingIntent
import android.content.Context
import android.os.Build
import android.provider.MediaStore
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quickcleanpro.phonecleaner.R
import com.quickcleanpro.phonecleaner.data.repository.JunkAuthorizedDeleteResult
import com.quickcleanpro.phonecleaner.data.repository.JunkDeleteOutcome
import com.quickcleanpro.phonecleaner.data.repository.JunkFileDeleteHelper
import com.quickcleanpro.phonecleaner.domain.model.CategoryCleanGroup
import com.quickcleanpro.phonecleaner.domain.model.CleanItem
import com.quickcleanpro.phonecleaner.domain.model.CleanResult
import com.quickcleanpro.phonecleaner.domain.model.JunkCategory
import com.quickcleanpro.phonecleaner.domain.model.JunkFile
import com.quickcleanpro.phonecleaner.domain.model.ScanResult
import com.quickcleanpro.phonecleaner.domain.model.clean.MemoryCleanResult
import com.quickcleanpro.phonecleaner.domain.state.CleanupSummary
import com.quickcleanpro.phonecleaner.domain.state.PendingDeleteAuthorization
import com.quickcleanpro.phonecleaner.domain.state.SharedScanState
import com.quickcleanpro.phonecleaner.domain.usecase.MemoryCleanUseCase
import com.quickcleanpro.phonecleaner.domain.usecase.ScanJunkUseCase
import com.quickcleanpro.phonecleaner.utils.FileSizeFormatter
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val SCAN_PREVIEW_MIN_MILLIS = 1_000L
private const val CLEANING_ANIMATION_MIN_MILLIS = 1_600L

internal val DefaultResultDisplayCategories =
    listOf(
        JunkCategory.CACHE,
        JunkCategory.TEMP_FILE,
        JunkCategory.RESIDUAL,
        JunkCategory.APK,
        JunkCategory.LARGE_FILE,
    )

enum class JunkCleanPhase {
    Scanning,
    Preview,
    Cleaning,
    AwaitingAuthorization,
    Complete,
    Error,
}
enum class JunkCleanScanState {
    Idle,
    Scanning,
    Completed,
    Error,
}

data class JunkCleanResultUiState(
    val freedSpace: Long = 0L,
    val cleanedCount: Int = 0,
    val failedCount: Int = 0,
    val memoryFreedBytes: Long = 0L,
    val memoryProcessesKilled: Int = 0,
    val totalFreedBytes: Long = 0L,
    val formattedFreedSpace: String = "",
    val hasVisibleResult: Boolean = false,
)

data class SelectionSummary(
    val checkedCount: Int = 0,
    val checkedSize: Long = 0L,
    val checkedEmptyCategoryCount: Int = 0,
)

data class JunkCleanUiState(
    val phase: JunkCleanPhase = JunkCleanPhase.Scanning,
    val scanState: JunkCleanScanState = JunkCleanScanState.Idle,
    val progress: Float = 0f,
    val currentCategory: JunkCategory? = null,
    val foundItemCount: Int = 0,
    val foundTotalSize: Long = 0L,
    val formattedFoundSize: String = "0 B",
    val groups: List<CategoryCleanGroup> = emptyList(),
    val checkedEmptyCategories: Set<JunkCategory> = emptySet(),
    val selectedSummary: SelectionSummary = SelectionSummary(),
    val cleanResult: JunkCleanResultUiState = JunkCleanResultUiState(),
    val awaitingAuthorizationMessage: String? = null,
    @StringRes val errorMessageRes: Int? = null,
    val errorMessage: String? = null,
)

sealed interface JunkCleanEvent {
    data class RequestDeleteAuthorization(val deleteRequest: PendingIntent) : JunkCleanEvent
}

class JunkCleanViewModel(
    private val scanJunkUseCase: ScanJunkUseCase,
    private val sharedState: SharedScanState,
    private val memoryCleanUseCase: MemoryCleanUseCase,
    private val ioDispatcher: CoroutineDispatcher,
    private val scanPreviewMinMillis: Long = SCAN_PREVIEW_MIN_MILLIS,
    private val cleaningAnimationMinMillis: Long = CLEANING_ANIMATION_MIN_MILLIS,
) : ViewModel() {
    private data class CleaningExecutionState(
        val pendingAuthorizationOutcomes: List<JunkDeleteOutcome> = emptyList(),
        val directCleanedFiles: List<JunkFile> = emptyList(),
        val directFailedFiles: List<JunkFile> = emptyList(),
        val directFreedSpace: Long = 0L,
        val memoryResult: MemoryCleanResult? = null,
    )

    private val FINISHING_ANIMATION_MILLIS = 800L

    private val _uiState = MutableStateFlow(JunkCleanUiState())
    val uiState: StateFlow<JunkCleanUiState> = _uiState.asStateFlow()

    private val eventsChannel = Channel<JunkCleanEvent>(Channel.BUFFERED)
    val events = eventsChannel.receiveAsFlow()

    private var progressJob: Job? = null
    private var scanJob: Job? = null
    private var cleaningJob: Job? = null
    private var hasStarted = false
    private var checkedEmptyCategories: Set<JunkCategory> = emptySet()
    private var cleaningExecutionState = CleaningExecutionState()

    fun startScanIfNeeded() {
        if (hasStarted) return
        startScanInternal(resetSession = true)
    }

    fun retryScan() {
        hasStarted = false
        startScanInternal(resetSession = true)
    }

    fun toggleItemSelection(itemId: String) {
        val state = _uiState.value.takeIf { it.phase == JunkCleanPhase.Preview } ?: return
        val groups = state.groups.toMutableList()
        val categoryIndex =
            groups.indexOfFirst { group ->
                group.items.any { it.junkFile.id == itemId }
            }
        val group = groups.getOrNull(categoryIndex) ?: return
        val items =
            group.items.map { item ->
                if (item.junkFile.id == itemId) {
                    item.copy(isChecked = !item.isChecked)
                } else {
                    item
                }
            }
        groups[categoryIndex] = group.copy(items = items)
        checkedEmptyCategories -= group.category
        publishPreview(groups)
    }

    fun toggleCategorySelection(categories: List<JunkCategory>) {
        val state = _uiState.value.takeIf { it.phase == JunkCleanPhase.Preview } ?: return
        val targetCategories = categories.distinct()
        val groups = state.groups.toMutableList()
        val targetIndices =
            groups.mapIndexedNotNull { index, group ->
                index.takeIf { group.category in targetCategories && group.items.isNotEmpty() }
            }

        if (targetIndices.isEmpty()) {
            val category = targetCategories.firstOrNull() ?: return
            toggleEmptyCategorySelection(category, groups)
            return
        }

        val allChecked = targetIndices.flatMap { groups[it].items }.all { it.isChecked }
        targetIndices.forEach { index ->
            val group = groups[index]
            groups[index] = group.copy(items = group.items.map { it.copy(isChecked = !allChecked) })
        }
        checkedEmptyCategories -= targetCategories.toSet()
        publishPreview(groups)
    }

    fun startCleaning(context: Context) {
        val appContext = context.applicationContext ?: context
        val requestContext = context
        val state = _uiState.value.takeIf { it.phase == JunkCleanPhase.Preview } ?: return
        val selectedItems = state.groups.flatMap { it.items }.filter { it.isChecked }
        if (selectedItems.isEmpty()) {
            _uiState.value =
                state.copy(
                    phase = JunkCleanPhase.Error,
                    errorMessageRes =
                        if (checkedEmptyCategories.isNotEmpty()) {
                            R.string.result_zero_byte_selection_hint
                        } else {
                            R.string.result_select_at_least_one
                        },
                    errorMessage = null,
                )
            return
        }

        _uiState.value =
            state.copy(
                phase = JunkCleanPhase.Cleaning,
                awaitingAuthorizationMessage = null,
                errorMessageRes = null,
                errorMessage = null,
            )
        val cleaningStartedAt = System.currentTimeMillis()
        cleaningJob?.cancel()
        cleaningJob =
            viewModelScope.launch {
                try {
                    val outcomes =
                        withContext(ioDispatcher) {
                            selectedItems.map { JunkFileDeleteHelper.delete(requestContext, it.junkFile) }
                        }
                    val deleted = outcomes.filter { it.deleted }
                    val pending = outcomes.filter { !it.deleted && it.authorizationUri != null }
                    val failed = outcomes.filter { !it.deleted && it.authorizationUri == null }
                    val memoryResult = withContext(ioDispatcher) { memoryCleanUseCase() }

                    cleaningExecutionState =
                        CleaningExecutionState(
                            pendingAuthorizationOutcomes = pending,
                            directCleanedFiles = deleted.map { it.junkFile },
                            directFailedFiles = failed.map { it.junkFile },
                            directFreedSpace = deleted.sumOf { it.freedBytes },
                            memoryResult = memoryResult,
                        )

                    val uris = JunkFileDeleteHelper.collectAuthorizationUris(pending)
                    if (uris.isNotEmpty() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        val request = MediaStore.createDeleteRequest(requestContext.contentResolver, uris)
                        val pendingAuthorization =
                            PendingDeleteAuthorization(
                                request = request,
                                message = appContext.getString(R.string.result_confirm_system_deletion, uris.size),
                                pendingCount = uris.size,
                            )
                        sharedState.setPendingDeleteAuthorization(pendingAuthorization)
                        _uiState.value =
                            _uiState.value.copy(
                                phase = JunkCleanPhase.AwaitingAuthorization,
                                awaitingAuthorizationMessage = pendingAuthorization.message,
                            )
                        eventsChannel.send(JunkCleanEvent.RequestDeleteAuthorization(request))
                    } else {
                        delayRemainingCleaningAnimation(cleaningStartedAt)
                        finishCleaning(
                            extraCleanedFiles = emptyList(),
                            extraFailedFiles = pending.map { it.junkFile },
                            extraFreedSpace = 0L,
                        )
                    }
                } catch (error: CancellationException) {
                    throw error
                } catch (error: Exception) {
                    _uiState.value =
                        _uiState.value.copy(
                            phase = JunkCleanPhase.Error,
                            errorMessageRes = R.string.result_clean_error,
                            errorMessage = error.message,
                        )
                }
            }
    }

    fun handleAuthorizationResult(approved: Boolean) {
        val pending = cleaningExecutionState.pendingAuthorizationOutcomes
        if (pending.isEmpty()) return

        sharedState.setPendingDeleteAuthorization(null)
        _uiState.value =
            _uiState.value.copy(
                phase = JunkCleanPhase.Cleaning,
                awaitingAuthorizationMessage = null,
            )
        val cleaningStartedAt = System.currentTimeMillis()
        cleaningJob?.cancel()
        cleaningJob =
            viewModelScope.launch {
                try {
                    val authorizedResult =
                        withContext(ioDispatcher) {
                            if (approved) {
                                JunkFileDeleteHelper.finalizeAuthorizedDeletes(pending)
                            } else {
                                JunkAuthorizedDeleteResult(
                                    cleanedFiles = emptyList(),
                                    failedFiles = pending.map { it.junkFile },
                                    freedBytes = 0L,
                                )
                            }
                        }
                    delayRemainingCleaningAnimation(cleaningStartedAt)
                    finishCleaning(
                        extraCleanedFiles = authorizedResult.cleanedFiles,
                        extraFailedFiles = authorizedResult.failedFiles,
                        extraFreedSpace = authorizedResult.freedBytes,
                    )
                } catch (error: CancellationException) {
                    throw error
                } catch (error: Exception) {
                    _uiState.value =
                        _uiState.value.copy(
                            phase = JunkCleanPhase.Error,
                            errorMessageRes = R.string.result_clean_error_after_authorization,
                            errorMessage = error.message,
                        )
                }
            }
    }

    fun clearResult() {
        sharedState.clearCleanResults()
        _uiState.value = _uiState.value.copy(cleanResult = JunkCleanResultUiState())
    }

    private fun startScanInternal(resetSession: Boolean) {
        if (_uiState.value.scanState == JunkCleanScanState.Scanning) return
        hasStarted = true
        scanJob?.cancel()
        if (resetSession) {
            sharedState.clear()
        }
        val scanStartedAt = System.currentTimeMillis()
        observeProgress()
        _uiState.value =
            JunkCleanUiState(
                phase = JunkCleanPhase.Scanning,
                scanState = JunkCleanScanState.Scanning,
            )

        scanJob =
            viewModelScope.launch(ioDispatcher) {
                try {
                    val result = scanJunkUseCase()
                    progressJob?.cancel()
                    _uiState.value =
                        _uiState.value.copy(
                            scanState = JunkCleanScanState.Completed,
                            progress = 100f,
                            currentCategory = null,
                            foundItemCount = result.totalCount,
                            foundTotalSize = result.totalSize,
                            formattedFoundSize = FileSizeFormatter.format(result.totalSize),
                            errorMessageRes = null,
                            errorMessage = null,
                        )
                    delayRemainingScanAnimation(scanStartedAt)
                    loadPreview(result)
                } catch (error: CancellationException) {
                    throw error
                } catch (error: Exception) {
                    progressJob?.cancel()
                    _uiState.value =
                        _uiState.value.copy(
                            phase = JunkCleanPhase.Error,
                            scanState = JunkCleanScanState.Error,
                            errorMessageRes = R.string.scan_failed,
                            errorMessage = error.message,
                        )
                }
            }
        }

    fun cancelActiveOperation() {
        scanJob?.cancel()
        scanJob = null
        progressJob?.cancel()
        progressJob = null
        cleaningJob?.cancel()
        cleaningJob = null
        hasStarted = false
        _uiState.update { it.copy(scanState = JunkCleanScanState.Idle) }
        sharedState.setPendingDeleteAuthorization(null)
        cleaningExecutionState = CleaningExecutionState()
    }

    fun cancelCleaningAndReturnToPreview() {
        cleaningJob?.cancel()
        cleaningJob = null
        sharedState.setPendingDeleteAuthorization(null)
        cleaningExecutionState = CleaningExecutionState()
        _uiState.update { current ->
            if (current.phase == JunkCleanPhase.Cleaning || current.phase == JunkCleanPhase.AwaitingAuthorization) {
                current.copy(phase = JunkCleanPhase.Preview)
            } else {
                current
            }
        }
    }

    private fun observeProgress() {
        progressJob?.cancel()
        progressJob =
            viewModelScope.launch {
                sharedState.scanProgress.collect { progress ->
                    if (_uiState.value.phase != JunkCleanPhase.Scanning) return@collect
                    val currentResult = sharedState.scanResult.value
                    val foundSize = currentResult?.totalSize ?: progress.foundSize
                    _uiState.value =
                        _uiState.value.copy(
                            scanState = JunkCleanScanState.Scanning,
                            progress = progress.percent,
                            currentCategory = progress.currentCategory,
                            foundItemCount = currentResult?.totalCount ?: progress.foundCount,
                            foundTotalSize = foundSize,
                            formattedFoundSize = FileSizeFormatter.format(foundSize),
                            errorMessageRes = null,
                            errorMessage = null,
                        )
                }
            }
    }

    private fun loadPreview(scanResult: ScanResult?) {
        if (scanResult == null) {
            _uiState.value =
                _uiState.value.copy(
                    phase = JunkCleanPhase.Error,
                    errorMessageRes = R.string.result_no_data,
                    errorMessage = null,
                )
            return
        }

        val groups = buildPreviewGroups(scanResult)
        checkedEmptyCategories = defaultEmptyCheckedCategories(groups)
        publishPreview(groups)
    }

    private fun publishPreview(groups: List<CategoryCleanGroup>) {
        checkedEmptyCategories =
            checkedEmptyCategories
                .filter { category ->
                    groups.firstOrNull { it.category == category }?.items?.isEmpty() ?: true
                }
                .toSet()
        val summary =
            SelectionSummary(
                checkedCount = groups.sumOf { it.checkedCount },
                checkedSize = groups.sumOf { it.checkedSize },
                checkedEmptyCategoryCount = checkedEmptyCategories.size,
            )
        _uiState.value =
            _uiState.value.copy(
                phase = JunkCleanPhase.Preview,
                scanState = JunkCleanScanState.Completed,
                groups = groups,
                checkedEmptyCategories = checkedEmptyCategories,
                selectedSummary = summary,
                awaitingAuthorizationMessage = null,
                errorMessageRes = null,
                errorMessage = null,
            )
    }

    private fun toggleEmptyCategorySelection(
        category: JunkCategory,
        groups: List<CategoryCleanGroup>,
    ) {
        checkedEmptyCategories =
            if (category in checkedEmptyCategories) {
                checkedEmptyCategories - category
            } else {
                checkedEmptyCategories + category
            }
        publishPreview(groups)
    }

    private fun finishCleaning(
        extraCleanedFiles: List<JunkFile>,
        extraFailedFiles: List<JunkFile>,
        extraFreedSpace: Long,
    ) {
        val cleanedFiles = cleaningExecutionState.directCleanedFiles + extraCleanedFiles
        val failedFiles = cleaningExecutionState.directFailedFiles + extraFailedFiles
        val freedSpace = cleaningExecutionState.directFreedSpace + extraFreedSpace
        val result =
            CleanResult(
                cleanedFiles = cleanedFiles,
                freedSpace = freedSpace,
                failedFiles = failedFiles,
            )
        val memoryResult = cleaningExecutionState.memoryResult
        val summary =
            CleanupSummary(
                freedSpace = result.freedSpace,
                cleanedCount = result.successCount,
                failedCount = result.failedCount,
                memoryFreedBytes = memoryResult?.freedBytes ?: 0L,
                memoryProcessesKilled = memoryResult?.killedCount ?: 0,
            )

        sharedState.removeCleanedFiles(cleanedFiles)
        sharedState.setCleanResult(result)
        if (memoryResult != null) {
            sharedState.setMemoryResult(memoryResult)
        }
        sharedState.setCleanupSummary(summary)

        cleaningExecutionState = CleaningExecutionState()
        _uiState.value = _uiState.value.copy(
            phase = JunkCleanPhase.Complete,
            cleanResult = summary.toCleanResultUiState(),
            awaitingAuthorizationMessage = null,
            errorMessageRes = null,
            errorMessage = null,
        )
    }

    private suspend fun delayRemainingScanAnimation(startedAtMillis: Long) {
        val remainingMillis = scanPreviewMinMillis - (System.currentTimeMillis() - startedAtMillis)
        delay(remainingMillis.coerceAtLeast(150L))
    }

    private suspend fun delayRemainingCleaningAnimation(startedAtMillis: Long) {
        val remainingMillis = cleaningAnimationMinMillis - (System.currentTimeMillis() - startedAtMillis)
        if (remainingMillis > 0L) delay(remainingMillis)
    }

    private fun buildPreviewGroups(scanResult: ScanResult): List<CategoryCleanGroup> =
        scanResult.categoryBreakdown.map { (category, files) ->
            CategoryCleanGroup(
                category = category,
                items = files.map { CleanItem(junkFile = it, isChecked = true) },
            )
        }

    private fun defaultEmptyCheckedCategories(groups: List<CategoryCleanGroup>): Set<JunkCategory> {
        val groupsByCategory = groups.associateBy { it.category }
        val residualHasFiles =
            groupsByCategory[JunkCategory.RESIDUAL]?.items?.isNotEmpty() == true ||
                groupsByCategory[JunkCategory.DUPLICATE]?.items?.isNotEmpty() == true
        return DefaultResultDisplayCategories
            .filter { category ->
                when (category) {
                    JunkCategory.RESIDUAL -> !residualHasFiles
                    else -> groupsByCategory[category]?.items?.isEmpty() ?: true
                }
            }
            .toSet()
    }

    private fun CleanupSummary.toCleanResultUiState(): JunkCleanResultUiState =
        JunkCleanResultUiState(
            freedSpace = freedSpace,
            cleanedCount = cleanedCount,
            failedCount = failedCount,
            memoryFreedBytes = memoryFreedBytes,
            memoryProcessesKilled = memoryProcessesKilled,
            totalFreedBytes = totalFreedBytes,
            formattedFreedSpace =
                totalFreedBytes
                    .takeIf { it > 0L }
                    ?.let(FileSizeFormatter::format)
                    .orEmpty(),
            hasVisibleResult = hasVisibleResult,
        )

    override fun onCleared() {
        scanJob?.cancel()
        progressJob?.cancel()
        cleaningJob?.cancel()
        super.onCleared()
    }
}
