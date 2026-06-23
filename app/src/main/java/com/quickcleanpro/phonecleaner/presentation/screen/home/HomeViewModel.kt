package com.quickcleanpro.phonecleaner.presentation.screen.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quickcleanpro.phonecleaner.data.source.notification.ToolNotificationSpec
import com.quickcleanpro.phonecleaner.data.source.notification.ToolNotificationSpecs
import com.quickcleanpro.phonecleaner.domain.model.device.BatteryInfo
import com.quickcleanpro.phonecleaner.domain.model.device.StorageInfo
import com.quickcleanpro.phonecleaner.domain.repository.AppLockRepository
import com.quickcleanpro.phonecleaner.domain.repository.DeviceInfoRepository
import com.quickcleanpro.phonecleaner.domain.repository.SettingsRepository
import com.quickcleanpro.phonecleaner.presentation.common.route.Screen
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HomeSummaryUiState(
    val storageInfo: StorageInfo = StorageInfo(0, 0, 0),
    val batteryInfo: BatteryInfo = BatteryInfo(-1, "Unknown", 0f, -1, "Unknown", 0, "Unknown"),
    val batteryStatusText: String = "Unknown",
    val deviceModel: String = "Unknown",
    val androidVersion: String = "Android --",
    val lockedAppCount: Int = 0,
    val isLoading: Boolean = true,
)

class HomeViewModel(
    private val repository: DeviceInfoRepository,
    private val appLockRepository: AppLockRepository,
    private val settingsRepository: SettingsRepository,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ViewModel() {
    private val _summaryState = MutableStateFlow(HomeSummaryUiState())
    val summaryState: StateFlow<HomeSummaryUiState> = _summaryState.asStateFlow()

    var exitPromptSpec by mutableStateOf<ToolNotificationSpec?>(null)
        private set

    init {
        refreshSummary()
    }

    fun requestExitPrompt() {
        if (exitPromptSpec != null) return
        exitPromptSpec = nextExitPromptSpec()
    }

    fun dismissExitPrompt() {
        exitPromptSpec = null
    }

    fun consumeExitPromptForNavigation(): ToolNotificationSpec? {
        val spec = exitPromptSpec ?: return null
        exitPromptSpec = null
        return spec
    }

    fun refreshSummary() {
        viewModelScope.launch(ioDispatcher) {
            val currentState = _summaryState.value
            val loadedState =
                runCatching {
                    val hardware = repository.hardwareInfo()
                    HomeSummaryUiState(
                        storageInfo = repository.internalStorageInfo(),
                        batteryInfo = repository.batteryInfo(),
                        batteryStatusText = repository.batteryStatusInfo().statusText,
                        deviceModel = hardware.model,
                        androidVersion = hardware.androidVersion,
                        lockedAppCount = runCatching { appLockRepository.lockedAppCount() }.getOrDefault(0),
                        isLoading = false,
                    )
                }.getOrElse {
                    currentState.copy(isLoading = false)
                }
            _summaryState.value = loadedState
        }
    }

    private fun nextExitPromptSpec(): ToolNotificationSpec {
        val notificationBarSpec = ToolNotificationSpecs.first { it.route == Screen.NotificationBar.route }
        if (!settingsRepository.hasShownNotificationBarExitPrompt()) {
            settingsRepository.saveNotificationBarExitPromptShown()
            return notificationBarSpec
        }
        val suggestions = ToolNotificationSpecs.filterNot { it.route == Screen.NotificationBar.route }
        return suggestions.randomOrNull() ?: notificationBarSpec
    }

}
