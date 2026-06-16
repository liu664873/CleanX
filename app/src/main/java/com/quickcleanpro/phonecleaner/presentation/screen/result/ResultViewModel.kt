package com.quickcleanpro.phonecleaner.presentation.screen.result

import android.app.PendingIntent
import android.content.Context
import android.os.Build
import android.provider.MediaStore
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
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val CLEANING_ANIMATION_MIN_MILLIS = 1_600L

internal val DefaultResultDisplayCategories =
    listOf(
        JunkCategory.CACHE,
        JunkCategory.TEMP_FILE,
        JunkCategory.RESIDUAL,
        JunkCategory.APK,
        JunkCategory.LARGE_FILE,
    )

class ResultViewModel(
    private val sharedState: SharedScanState,
    private val memoryCleanUseCase: MemoryCleanUseCase,
    private val ioDispatcher: CoroutineDispatcher,
    private val cleaningAnimationMinMillis: Long = CLEANING_ANIMATION_MIN_MILLIS,
) : ViewModel() {
    sealed class ScreenState {
        data class Preview(
            val groups: List<CategoryCleanGroup>,
            val checkedCount: Int,
            val checkedSize: Long,
            val checkedEmptyCategories: Set<JunkCategory> = emptySet(),
        ) : ScreenState()

        data class Cleaning(val freedSpace: Long = 0L) : ScreenState()

        data class AwaitingDeleteAuthorization(
            val deleteRequest: PendingIntent,
            val message: String,
        ) : ScreenState()

        data class Completed(
            val result: CleanResult,
            val memoryResult: MemoryCleanResult? = null,
        ) : ScreenState()

        data class Error(val messageRes: Int, val message: String? = null) : ScreenState()
    }

    private data class CleaningExecutionState(
        val pendingAuthorizationOutcomes: List<JunkDeleteOutcome> = emptyList(),
        val directCleanedFiles: List<JunkFile> = emptyList(),
        val directFailedFiles: List<JunkFile> = emptyList(),
        val directFreedSpace: Long = 0L,
        val memoryResult: MemoryCleanResult? = null,
    )

    private val _screenState =
        MutableStateFlow<ScreenState>(ScreenState.Error(R.string.result_loading))
    private val _selectedSummary = MutableStateFlow(SelectionSummary())

    private var cleaningExecutionState = CleaningExecutionState()
    private var checkedEmptyCategories: Set<JunkCategory> = emptySet()

    val screenState: StateFlow<ScreenState> = _screenState.asStateFlow()
    val selectedSummary: StateFlow<SelectionSummary> = _selectedSummary.asStateFlow()

    fun loadPreview() {
        val scanResult: ScanResult? = sharedState.scanResult.value
        if (scanResult == null) {
            _screenState.value = ScreenState.Error(R.string.result_no_data)
            _selectedSummary.value = SelectionSummary()
            return
        }

        val groups = buildPreviewGroups(scanResult)
        checkedEmptyCategories = defaultEmptyCheckedCategories(groups)
        publishPreview(groups)
    }

    fun toggleItemSelection(
        categoryIndex: Int,
        itemIndex: Int,
    ) {
        val state = _screenState.value as? ScreenState.Preview ?: return
        val groups = state.groups.toMutableList()
        val group = groups.getOrNull(categoryIndex) ?: return
        val items = group.items.toMutableList()
        val item = items.getOrNull(itemIndex) ?: return
        items[itemIndex] = item.copy(isChecked = !item.isChecked)
        groups[categoryIndex] = group.copy(items = items)
        checkedEmptyCategories -= group.category
        publishPreview(groups)
    }

    fun toggleCategorySelection(categoryIndex: Int) {
        val state = _screenState.value as? ScreenState.Preview ?: return
        val groups = state.groups.toMutableList()
        val group = groups.getOrNull(categoryIndex) ?: return
        if (group.items.isEmpty()) {
            toggleEmptyCategorySelection(group.category, groups)
            return
        }
        val allChecked = group.items.all { it.isChecked }
        groups[categoryIndex] = group.copy(items = group.items.map { it.copy(isChecked = !allChecked) })
        checkedEmptyCategories -= group.category
        publishPreview(groups)
    }

    fun toggleCategorySelection(category: JunkCategory) {
        val state = _screenState.value as? ScreenState.Preview ?: return
        val categoryIndex = state.groups.indexOfFirst { it.category == category }
        if (categoryIndex >= 0) {
            toggleCategorySelection(categoryIndex)
        } else {
            toggleEmptyCategorySelection(category, state.groups)
        }
    }

    fun startCleaning(context: Context) {
        val state = _screenState.value as? ScreenState.Preview ?: return
        val selectedItems = state.groups.flatMap { it.items }.filter { it.isChecked }
        if (selectedItems.isEmpty()) {
            _screenState.value =
                ScreenState.Error(
                    if (checkedEmptyCategories.isNotEmpty()) {
                        R.string.result_zero_byte_selection_hint
                    } else {
                        R.string.result_select_at_least_one
                    },
                )
            return
        }

        _screenState.value = ScreenState.Cleaning()
        val cleaningStartedAt = System.currentTimeMillis()
        viewModelScope.launch {
            try {
                val outcomes =
                    withContext(ioDispatcher) {
                        selectedItems.map { JunkFileDeleteHelper.delete(context, it.junkFile) }
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
                    val request = MediaStore.createDeleteRequest(context.contentResolver, uris)
                    val pendingAuthorization =
                        PendingDeleteAuthorization(
                            request = request,
                            message = context.getString(R.string.result_confirm_system_deletion, uris.size),
                            pendingCount = uris.size,
                        )
                    sharedState.setPendingDeleteAuthorization(pendingAuthorization)
                    _screenState.value =
                        ScreenState.AwaitingDeleteAuthorization(
                            deleteRequest = request,
                            message = pendingAuthorization.message,
                        )
                } else {
                    delayRemainingCleaningAnimation(cleaningStartedAt)
                    finishCleaning(
                        extraCleanedFiles = emptyList(),
                        extraFailedFiles = pending.map { it.junkFile },
                        extraFreedSpace = 0L,
                    )
                }
            } catch (error: Exception) {
                _screenState.value = ScreenState.Error(R.string.result_clean_error, error.message)
            }
        }
    }

    fun handleAuthorizationResult(approved: Boolean) {
        val pending = cleaningExecutionState.pendingAuthorizationOutcomes
        if (pending.isEmpty()) return

        sharedState.setPendingDeleteAuthorization(null)
        _screenState.value = ScreenState.Cleaning()
        val cleaningStartedAt = System.currentTimeMillis()
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
            } catch (error: Exception) {
                _screenState.value =
                    ScreenState.Error(R.string.result_clean_error_after_authorization, error.message)
            }
        }
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

        sharedState.removeCleanedFiles(cleanedFiles)
        sharedState.setCleanResult(result)
        if (memoryResult != null) {
            sharedState.setMemoryResult(memoryResult)
        }
        sharedState.setCleanupSummary(
            CleanupSummary(
                freedSpace = result.freedSpace,
                cleanedCount = result.successCount,
                failedCount = result.failedCount,
                memoryFreedBytes = memoryResult?.freedBytes ?: 0L,
                memoryProcessesKilled = memoryResult?.killedCount ?: 0,
            ),
        )

        cleaningExecutionState = CleaningExecutionState()
        _screenState.value = ScreenState.Completed(result, memoryResult)
    }

    private suspend fun delayRemainingCleaningAnimation(startedAtMillis: Long) {
        val remainingMillis = cleaningAnimationMinMillis - (System.currentTimeMillis() - startedAtMillis)
        if (remainingMillis > 0L) delay(remainingMillis)
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
        _selectedSummary.value = summary
        _screenState.value =
            ScreenState.Preview(
                groups = groups,
                checkedCount = summary.checkedCount,
                checkedSize = summary.checkedSize,
                checkedEmptyCategories = checkedEmptyCategories,
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
}

data class SelectionSummary(
    val checkedCount: Int = 0,
    val checkedSize: Long = 0L,
    val checkedEmptyCategoryCount: Int = 0,
)
