package com.quickcleanpro.phonecleaner.presentation.screen.appusage

import android.content.Intent
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quickcleanpro.phonecleaner.R
import com.quickcleanpro.phonecleaner.domain.model.toolbox.AppUsageInfo
import com.quickcleanpro.phonecleaner.domain.repository.AppUsageRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale

enum class AppUsageDateRange(@StringRes val labelRes: Int) {
    Today(R.string.today),
    Yesterday(R.string.yesterday),
    Last7Days(R.string.last_7_days),
    Last30Days(R.string.last_30_days),
    ;

    fun timeBounds(nowMillis: Long = System.currentTimeMillis()): Pair<Long, Long> {
        val startToday =
            Calendar
                .getInstance()
                .apply {
                    timeInMillis = nowMillis
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis
        return when (this) {
            Today -> startToday to nowMillis
            Yesterday -> startToday - DAY_MILLIS to startToday
            Last7Days -> startToday - 6 * DAY_MILLIS to nowMillis
            Last30Days -> startToday - 29 * DAY_MILLIS to nowMillis
        }
    }

    private companion object {
        private const val DAY_MILLIS = 24L * 60L * 60L * 1000L
    }
}

enum class AppUsageMetricTab(@StringRes val titleRes: Int) {
    Duration(R.string.hours_spent),
    LaunchCount(R.string.times_opened),
}

data class AppUsageDisplayItem(
    val appName: String,
    val packageName: String,
    val totalForegroundMs: Long,
    val launchCount: Int,
    val progress: Float,
    val iconText: String,
    val colorIndex: Int,
    val isRunning: Boolean,
)

data class AppUsageUiState(
    val selectedRange: AppUsageDateRange = AppUsageDateRange.Today,
    val selectedTab: AppUsageMetricTab = AppUsageMetricTab.Duration,
    val hasAccess: Boolean = false,
    val usages: List<AppUsageInfo> = emptyList(),
    val runningPackages: Set<String> = emptySet(),
    val visibleItems: List<AppUsageDisplayItem> = emptyList(),
    val totalUsageMs: Long = 0L,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
)

class AppUsageViewModel(
    private val repository: AppUsageRepository,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ViewModel() {
    private val _uiState =
        MutableStateFlow(
            AppUsageUiState(
                hasAccess = repository.hasAppUsageAccess(),
            ),
        )
    val uiState: StateFlow<AppUsageUiState> = _uiState.asStateFlow()

    init {
        refreshUsage()
    }

    fun selectRange(range: AppUsageDateRange) {
        if (_uiState.value.selectedRange == range) return
        _uiState.update { it.copy(selectedRange = range) }
        refreshUsage()
    }

    fun selectTab(tab: AppUsageMetricTab) {
        if (_uiState.value.selectedTab == tab) return
        _uiState.update { current ->
            current.copy(
                selectedTab = tab,
                visibleItems =
                    buildVisibleItems(
                        usages = current.usages,
                        runningPackages = current.runningPackages,
                        selectedTab = tab,
                    ),
            )
        }
    }

    fun refreshAfterResume() {
        repository.resetAppUsagePermissionCache()
        refreshUsage()
    }

    fun appUsageSettingsIntent(): Intent = repository.appUsageSettingsIntent()

    fun appInfoIntent(packageName: String): Intent = repository.appInfoIntent(packageName)

    fun refreshUsage() {
        val hasAccess = repository.hasAppUsageAccess()
        _uiState.update { state ->
            state.copy(
                hasAccess = hasAccess,
                usages = if (hasAccess) state.usages else emptyList(),
                runningPackages = if (hasAccess) state.runningPackages else emptySet(),
                visibleItems = if (hasAccess) state.visibleItems else emptyList(),
                totalUsageMs = if (hasAccess) state.totalUsageMs else 0L,
                isLoading = hasAccess,
                errorMessage = null,
            )
        }
        if (!hasAccess) return

        viewModelScope.launch(ioDispatcher) {
            val selectedRange = _uiState.value.selectedRange
            val (startMillis, endMillis) = selectedRange.timeBounds()
            runCatching {
                val usages = repository.appUsageBetween(startMillis, endMillis)
                val runningPackages = repository.runningPackages(usages.map { it.packageName }.toSet())
                usages to runningPackages
            }.onSuccess { (usages, runningPackages) ->
                _uiState.update { current ->
                    current.copy(
                        hasAccess = true,
                        usages = usages,
                        runningPackages = runningPackages,
                        visibleItems =
                            buildVisibleItems(
                                usages = usages,
                                runningPackages = runningPackages,
                                selectedTab = current.selectedTab,
                            ),
                        totalUsageMs = usages.sumOf { it.totalForegroundMs },
                        isLoading = false,
                        errorMessage = null,
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = error.message,
                    )
                }
            }
        }
    }

    private companion object {
        private const val VISIBLE_ITEM_LIMIT = 8

        fun buildVisibleItems(
            usages: List<AppUsageInfo>,
            runningPackages: Set<String>,
            selectedTab: AppUsageMetricTab,
        ): List<AppUsageDisplayItem> {
            if (usages.isEmpty()) return emptyList()

            val sorted =
                when (selectedTab) {
                    AppUsageMetricTab.Duration ->
                        usages.sortedWith(
                            compareByDescending<AppUsageInfo> { it.totalForegroundMs }
                                .thenByDescending { it.launchCount }
                                .thenBy { it.appName.lowercase(Locale.US) },
                        )

                    AppUsageMetricTab.LaunchCount ->
                        usages.sortedWith(
                            compareByDescending<AppUsageInfo> { it.launchCount }
                                .thenByDescending { it.totalForegroundMs }
                                .thenBy { it.appName.lowercase(Locale.US) },
                        )
                }

            val maxValue =
                when (selectedTab) {
                    AppUsageMetricTab.Duration -> sorted.maxOf { it.totalForegroundMs }.coerceAtLeast(1L)
                    AppUsageMetricTab.LaunchCount -> sorted.maxOf { it.launchCount }.coerceAtLeast(1).toLong()
                }

            return sorted
                .take(VISIBLE_ITEM_LIMIT)
                .mapIndexed { index, usage ->
                    val value =
                        when (selectedTab) {
                            AppUsageMetricTab.Duration -> usage.totalForegroundMs
                            AppUsageMetricTab.LaunchCount -> usage.launchCount.toLong()
                        }
                    AppUsageDisplayItem(
                        appName = usage.appName,
                        packageName = usage.packageName,
                        totalForegroundMs = usage.totalForegroundMs,
                        launchCount = usage.launchCount,
                        progress = (value.toFloat() / maxValue).coerceIn(0.08f, 1f),
                        iconText = usage.appName.take(1).ifBlank { "A" },
                        colorIndex = index,
                        isRunning = usage.packageName in runningPackages,
                    )
                }
        }
    }
}
