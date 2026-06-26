package com.quickcleanpro.phonecleaner.presentation.screen.applock.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.quickcleanpro.phonecleaner.R
import com.quickcleanpro.phonecleaner.presentation.common.components.buttons.CleanXSettingsNavigationRow
import com.quickcleanpro.phonecleaner.presentation.screen.applock.AppLockUiState

@Composable
internal fun AppLockSettingsView(
    uiState: AppLockUiState,
    onMonitoringChange: (Boolean) -> Unit,
    onVibrationChange: (Boolean) -> Unit,
    onStartChangePin: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        CheckActionCard(
            title = stringResource(R.string.enable),
            checked = uiState.monitoringEnabled,
            onClick = { onMonitoringChange(!uiState.monitoringEnabled) }
        )
        CheckActionCard(
            title = stringResource(R.string.haptic_feedback),
            checked = uiState.vibrationEnabled,
            onClick = { onVibrationChange(!uiState.vibrationEnabled) }
        )
        CleanXSettingsNavigationRow(
            label = stringResource(R.string.change_pin),
            onClick = onStartChangePin
        )
    }
}
