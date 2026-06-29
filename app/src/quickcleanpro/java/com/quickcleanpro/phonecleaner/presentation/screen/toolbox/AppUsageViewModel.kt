package com.quickcleanpro.phonecleaner.presentation.screen.toolbox

import android.content.Intent
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quickcleanpro.phonecleaner.R
import com.quickcleanpro.phonecleaner.domain.repository.ToolboxRepository
import com.quickcleanpro.phonecleaner.presentation.common.toolboxRepositoryOrPreview
import com.quickcleanpro.phonecleaner.presentation.common.state.PermissionUiState
import com.quickcleanpro.phonecleaner.domain.model.toolbox.AppUsageInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale
import com.quickcleanpro.phonecleaner.presentation.common.appString
import com.quickcleanpro.phonecleaner.presentation.common.appQuantityString

internal enum class AppUsageMetricTab(@StringRes val titleRes: Int) {
    Duration(R.string.hours_spent),
    LaunchCount(R.string.times_opened)
}

internal data class AppUsageDisplayItem(
    val appName: String,
    val packageName: String,
    val value: String,
    val progress: Float,
    val iconText: String,
    val colorIndex: Int,
    val stopButtonState: AppStopButtonState
)

internal data class AppUsageUiState(
    val selectedRange: AppUsageDateRange = AppUsageDateRange.Today,
    val selectedTab: AppUsageMetricTab = AppUsageMetricTab.Duration,
    val permission: PermissionUiState = AppUsageViewModel.defaultPermission(granted = false),
    val usages: List<AppUsageInfo> = emptyList(),
    val runningPackages: Set<String> = emptySet(),
    val visibleItems: List<AppUsageDisplayItem> = emptyList(),
    val totalUsageLabel: String = AppUsageViewModel.formatDurationLabel(0L),
    val isLoading: Boolean = false
) {
    val hasAccess: Boolean get() = permission.granted
}

internal class AppUsageViewModel(
    private val repository: ToolboxRepository = toolboxRepositoryOrPreview(),
    private val testLoader: (((suspend () -> Unit)) -> Unit)? = null
) : ViewModel() {

    constructor(repository: ToolboxRepository) : this(repository, null)

    private val _uiState = MutableStateFlow(
        AppUsageUiState(permission = defaultPermission(repository.hasAppUsageAccess()))
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
                visibleItems = buildVisibleItems(
                    usages = current.usages,
                    runningPackages = current.runningPackages,
                    selectedTab = tab
                )
            )
        }
    }

    fun refreshAfterResume() {
        repository.resetAppUsagePermissionCache()
        refreshUsage()
    }

    fun refreshRunningPackagesOnly() {
        repository.resetAppUsagePermissionCache()
        val hasAccess = repository.hasAppUsageAccess()
        if (!hasAccess) {
            _uiState.update {
                it.copy(
                    permission = defaultPermission(granted = false),
                    usages = emptyList(),
                    runningPackages = emptySet(),
                    visibleItems = emptyList(),
                    totalUsageLabel = formatDurationLabel(0L),
                    isLoading = false
                )
            }
            return
        }

        val usages = _uiState.value.usages
        if (usages.isEmpty()) {
            _uiState.update { it.copy(permission = defaultPermission(granted = true), isLoading = false) }
            return
        }

        launchLoad {
            val runningPackages = repository.runningPackages(usages.map { it.packageName }.toSet())
            _uiState.update { current ->
                current.copy(
                    permission = defaultPermission(granted = true),
                    runningPackages = runningPackages,
                    visibleItems = buildVisibleItems(
                        usages = current.usages,
                        runningPackages = runningPackages,
                        selectedTab = current.selectedTab
                    ),
                    isLoading = false
                )
            }
        }
    }

    fun usageSettingsIntent(): Intent = repository.appUsageSettingsIntent()

    fun appInfoIntent(packageName: String): Intent = repository.appInfoIntent(packageName)

    fun refreshUsage() {
        val hasAccess = repository.hasAppUsageAccess()
        _uiState.update {
            it.copy(
                permission = defaultPermission(hasAccess),
                usages = if (hasAccess) it.usages else emptyList(),
                runningPackages = if (hasAccess) it.runningPackages else emptySet(),
                visibleItems = if (hasAccess) it.visibleItems else emptyList(),
                totalUsageLabel = if (hasAccess) it.totalUsageLabel else formatDurationLabel(0L),
                isLoading = hasAccess
            )
        }
        if (!hasAccess) return

        launchLoad {
            val range = _uiState.value.selectedRange
            val (startMillis, endMillis) = range.timeBounds()
            val usages = repository.appUsageBetween(startMillis, endMillis)
            val runningPackages = repository.runningPackages(usages.map { it.packageName }.toSet())
            _uiState.update { current ->
                current.copy(
                    usages = usages,
                    runningPackages = runningPackages,
                    visibleItems = buildVisibleItems(
                        usages = usages,
                        runningPackages = runningPackages,
                        selectedTab = current.selectedTab
                    ),
                    totalUsageLabel = formatDurationLabel(usages.sumOf { it.totalForegroundMs }),
                    isLoading = false
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

    internal companion object {
        fun defaultPermission(granted: Boolean): PermissionUiState =
            PermissionUiState(
                granted = granted,
                title = appString(R.string.app_usage_permission_title),
                description = appString(R.string.app_usage_permission_desc),
                actionLabel = appString(R.string.grant)
            )

        fun formatDurationLabel(totalMs: Long): String =
            formatDuration(totalMs).replace(" ", "\n")

        fun formatLaunchCount(count: Int): String =
            appQuantityString(R.plurals.launch_times_count, count, count)

        private fun formatDuration(totalMs: Long): String {
            val totalMinutes = (totalMs / 60_000L).coerceAtLeast(0L)
            val hours = totalMinutes / 60L
            val minutes = totalMinutes % 60L
            return when {
                hours > 0L && minutes > 0L -> appString(R.string.duration_hours_minutes, hours, minutes)
                hours > 0L -> appString(R.string.duration_hours, hours)
                else -> appString(R.string.duration_minutes, minutes)
            }
        }

        fun buildVisibleItems(
            usages: List<AppUsageInfo>,
            runningPackages: Set<String>,
            selectedTab: AppUsageMetricTab,
            limit: Int = 8
        ): List<AppUsageDisplayItem> {
            if (usages.isEmpty()) return emptyList()

            val sortedUsages = when (selectedTab) {
                AppUsageMetricTab.Duration -> usages.sortedWith(
                    compareByDescending<AppUsageInfo> { it.totalForegroundMs }
                        .thenByDescending { it.launchCount }
                        .thenBy { it.appName.lowercase(Locale.US) }
                )

                AppUsageMetricTab.LaunchCount -> usages.sortedWith(
                    compareByDescending<AppUsageInfo> { it.launchCount }
                        .thenByDescending { it.totalForegroundMs }
                        .thenBy { it.appName.lowercase(Locale.US) }
                )
            }

            val maxValue = when (selectedTab) {
                AppUsageMetricTab.Duration ->
                    sortedUsages.maxOf { it.totalForegroundMs }.coerceAtLeast(1L)

                AppUsageMetricTab.LaunchCount ->
                    sortedUsages.maxOf { it.launchCount }.coerceAtLeast(1).toLong()
            }

            return sortedUsages.take(limit).mapIndexed { index, usage ->
                val progressValue = when (selectedTab) {
                    AppUsageMetricTab.Duration -> usage.totalForegroundMs
                    AppUsageMetricTab.LaunchCount -> usage.launchCount.toLong()
                }
                AppUsageDisplayItem(
                    appName = usage.appName,
                    packageName = usage.packageName,
                    value = when (selectedTab) {
                        AppUsageMetricTab.Duration -> formatDuration(usage.totalForegroundMs)
                        AppUsageMetricTab.LaunchCount -> formatLaunchCount(usage.launchCount)
                    },
                    progress = (progressValue.toFloat() / maxValue).coerceIn(0.08f, 1f),
                    iconText = usage.appName.take(1).ifBlank { "A" },
                    colorIndex = index,
                    stopButtonState = appStopButtonStateForPackage(
                        packageName = usage.packageName,
                        runningPackages = runningPackages
                    )
                )
            }
        }
        private fun appString(resId: Int, vararg args: Any): String =
            runCatching {
                com.quickcleanpro.phonecleaner.presentation.common.appString(resId, *args)
            }.getOrElse {
                when (resId) {
                    R.string.app_usage_permission_title -> "Usage Data Permission"
                    R.string.app_usage_permission_desc -> "Allow usage access to show real app opening times and time spent."
                    R.string.grant -> "Grant"
                    R.string.duration_hours_minutes -> "${args.getOrNull(0)}h ${args.getOrNull(1)}m"
                    R.string.duration_hours -> "${args.getOrNull(0)}h"
                    R.string.duration_minutes -> "${args.getOrNull(0)}m"
                    else -> ""
                }
            }

        private fun appQuantityString(resId: Int, quantity: Int, vararg args: Any): String =
            runCatching {
                com.quickcleanpro.phonecleaner.presentation.common.appQuantityString(resId, quantity, *args)
            }.getOrElse {
                if (quantity == 1) "$quantity time" else "$quantity times"
            }
    }
}
