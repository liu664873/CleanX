package com.quickcleanpro.phonecleaner.presentation.screen.applock.views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.quickcleanpro.phonecleaner.presentation.screen.applock.AppLockPage
import com.quickcleanpro.phonecleaner.presentation.screen.applock.AppLockUiState

@Composable
internal fun AppLockContentView(
    uiState: AppLockUiState,
    onOpenSearch: () -> Unit,
    onSearch: (String) -> Unit,
    onTogglePackage: (String) -> Unit,
    onToggleAll: () -> Unit,
    onStartChangePin: () -> Unit,
    onDigit: (Char) -> Unit,
    onDeleteDigit: () -> Unit,
    onMonitoringChange: (Boolean) -> Unit,
    onAutoLockChange: (Boolean) -> Unit,
    onVibrationChange: (Boolean) -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppLockBackground)
    ) {
        when (uiState.page) {
            AppLockPage.SelectApps -> AppLockSelectAppsView(
                uiState = uiState,
                onTogglePackage = onTogglePackage
            )
            AppLockPage.Pin -> AppLockPinView(
                uiState = uiState,
                onDigit = onDigit,
                onDelete = onDeleteDigit
            )
            AppLockPage.Manage -> AppLockManageView(
                uiState = uiState,
                onOpenSearch = onOpenSearch,
                onTogglePackage = onTogglePackage,
                onToggleAll = onToggleAll,
                onAutoLockChange = onAutoLockChange
            )
            AppLockPage.Search -> AppLockSearchView(
                uiState = uiState,
                onSearch = onSearch,
                onTogglePackage = onTogglePackage
            )
            AppLockPage.Settings -> AppLockSettingsView(
                uiState = uiState,
                onMonitoringChange = onMonitoringChange,
                onVibrationChange = onVibrationChange,
                onStartChangePin = onStartChangePin
            )
        }
    }
}
