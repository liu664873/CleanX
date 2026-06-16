package com.quickcleanpro.phonecleaner.presentation.screen.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quickcleanpro.phonecleaner.domain.model.device.StorageInfo
import com.quickcleanpro.phonecleaner.domain.repository.DeviceInfoRepository
import com.quickcleanpro.phonecleaner.utils.SharedPreferencesUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class OnboardingUiState(
    val deviceModel: String = "",
    val androidVersion: String = "",
    val screenSize: String = "",
    val batteryHealth: String = "",
    val batteryStatusText: String = "",
    val storageInfo: StorageInfo = StorageInfo(0, 0, 0),
)

class OnboardingScanViewModel(
    private val repository: DeviceInfoRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            val hardware = repository.hardwareInfo()
            val battery = repository.batteryInfo()
            val batteryStatus = repository.batteryStatusInfo()
            val storage = repository.internalStorageInfo()
            _uiState.value =
                OnboardingUiState(
                    deviceModel = hardware.model,
                    androidVersion = hardware.androidVersion,
                    screenSize = hardware.screenSize,
                    batteryHealth = battery.health,
                    batteryStatusText = batteryStatus.statusText,
                    storageInfo = storage,
                )
        }
    }

    fun markOnboardingScanCompleted() {
        SharedPreferencesUtils.putBoolean(
            key = SharedPreferencesUtils.KEY_ONBOARDING_SCAN_COMPLETED,
            value = true,
        )
    }
}
