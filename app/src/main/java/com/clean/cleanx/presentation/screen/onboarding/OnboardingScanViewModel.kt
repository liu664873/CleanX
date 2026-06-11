package com.clean.cleanx.presentation.screen.onboarding

internal data class OnboardingScanUiState(
    val deviceModel: String = "Unknown",
    val androidVersion: String = "Android --",
    val screenSize: String = "--",
    val batteryHealth: String = "Unknown",
    val batteryStatusText: String = "--",
    val storageInfo: StorageInfo = StorageInfo(0, 0, 0),
    val isLoading: Boolean = true
)

