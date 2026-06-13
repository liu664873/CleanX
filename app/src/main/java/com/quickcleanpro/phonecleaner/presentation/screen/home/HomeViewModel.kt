package com.quickcleanpro.phonecleaner.presentation.screen.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quickcleanpro.phonecleaner.domain.repository.AppLockRepository
import com.quickcleanpro.phonecleaner.domain.repository.DeviceInfoRepository
import com.quickcleanpro.phonecleaner.domain.repository.SettingsRepository
import com.quickcleanpro.phonecleaner.data.source.notification.ToolNotificationSpec
import com.quickcleanpro.phonecleaner.data.source.notification.ToolNotificationSpecs
import com.quickcleanpro.phonecleaner.presentation.common.appLockRepositoryOrPreview
import com.quickcleanpro.phonecleaner.presentation.common.deviceInfoRepositoryOrPreview
import com.quickcleanpro.phonecleaner.domain.model.device.BatteryInfo
import com.quickcleanpro.phonecleaner.domain.model.device.StorageInfo
import com.quickcleanpro.phonecleaner.presentation.common.settingsRepositoryOrPreview
import com.quickcleanpro.phonecleaner.presentation.navigation.Screen
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private const val AUTO_RATE_PROMPT_DELAY_MS = 15_000L
private const val AUTO_RATE_PROMPT_COOLDOWN_MS = 24L * 60 * 60 * 1000

data class HomeSummaryUiState(
    val storageInfo: StorageInfo = StorageInfo(0, 0, 0),
    val batteryInfo: BatteryInfo = BatteryInfo(-1, "Unknown", 0f, -1, "Unknown", 0, "Unknown"),
    val deviceModel: String = "Unknown",
    val androidVersion: String = "Android --",
    val lockedAppCount: Int = 0,
    val isLoading: Boolean = true
)

class HomeViewModel constructor(
    private val repository: DeviceInfoRepository,
    private val settingsRepository: SettingsRepository,
    private val appLockRepository: AppLockRepository,
    private val ioDispatcher: CoroutineDispatcher,
    private val autoRateDispatcher: CoroutineDispatcher = Dispatchers.Main,
    private val autoRatePromptDelayMillis: Long = AUTO_RATE_PROMPT_DELAY_MS
) : ViewModel() {

    constructor() : this(
        deviceInfoRepositoryOrPreview(),
        settingsRepositoryOrPreview(),
        appLockRepositoryOrPreview(),
    )

    var selectedTabIndex by mutableIntStateOf(0)
        private set

    var showAutoRateDialog by mutableStateOf(false)
        private set

    var exitPromptSpec by mutableStateOf<ToolNotificationSpec?>(null)
        private set

    private val _summaryState = MutableStateFlow(HomeSummaryUiState())
    private var autoRatePromptJob: Job? = null
    private var featureClicked = false
    private var externalBlockingPromptActive = false

    val summaryState: StateFlow<HomeSummaryUiState> = _summaryState.asStateFlow()

    init {
        refreshSummary()
        scheduleAutoRatePrompt()
    }

    fun selectTab(index: Int) {
        selectedTabIndex = index
    }

    fun onFeatureClicked() {
        featureClicked = true
        autoRatePromptJob?.cancel()
    }

    fun dismissAutoRateDialog() {
        showAutoRateDialog = false
    }

    fun setExternalBlockingPromptActive(active: Boolean) {
        if (externalBlockingPromptActive == active) return
        externalBlockingPromptActive = active
        if (active) {
            autoRatePromptJob?.cancel()
            return
        }
        scheduleAutoRatePrompt()
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
        onFeatureClicked()
        exitPromptSpec = null
        return spec
    }

    fun refreshSummary() {
        viewModelScope.launch(ioDispatcher) {
            val currentState = _summaryState.value
            val loadedState = runCatching {
                val hardware = repository.hardwareInfo()
                HomeSummaryUiState(
                    storageInfo = repository.internalStorageInfo(),
                    batteryInfo = repository.batteryInfo(),
                    deviceModel = hardware.model,
                    androidVersion = hardware.androidVersion,
                    lockedAppCount = runCatching { appLockRepository.lockedAppCount() }.getOrDefault(0),
                    isLoading = false
                )
            }.getOrElse {
                currentState.copy(isLoading = false)
            }
            _summaryState.value = loadedState
        }
    }

    private fun scheduleAutoRatePrompt() {
        autoRatePromptJob?.cancel()
        if (!canShowAutoRatePrompt()) return
        if (externalBlockingPromptActive) return

        autoRatePromptJob = viewModelScope.launch(autoRateDispatcher) {
            delay(autoRatePromptDelayMillis)
            if (featureClicked) return@launch
            if (externalBlockingPromptActive) return@launch
            settingsRepository.saveLastAutoRatePromptAt(System.currentTimeMillis())
            showAutoRateDialog = true
        }
    }

    private fun canShowAutoRatePrompt(): Boolean {
        val lastPromptAt = settingsRepository.readLastAutoRatePromptAt()
        if (lastPromptAt <= 0L) return true
        return System.currentTimeMillis() - lastPromptAt >= AUTO_RATE_PROMPT_COOLDOWN_MS
    }

    private fun nextExitPromptSpec(): ToolNotificationSpec {
        val notificationBarSpec = ToolNotificationSpecs.first { it.route == Screen.NotificationBar.route }
        if (!settingsRepository.hasShownNotificationBarExitPrompt()) {
            settingsRepository.saveNotificationBarExitPromptShown()
            return notificationBarSpec
        }
        val suggestions = ToolNotificationSpecs.filterNot { it.route == Screen.Not+ificationBar.route }
        return suggestions.randomOrNull() ?: notificationBarSpec
    }

    override fun onCleared() {
        autoRatePromptJob?.cancel()
        super.onCleared()
    }
}
