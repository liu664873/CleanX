package com.quickcleanpro.phonecleaner.presentation.screen.JunkClean.views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.quickcleanpro.phonecleaner.domain.model.CleanItem
import com.quickcleanpro.phonecleaner.domain.model.JunkCategory
import com.quickcleanpro.phonecleaner.presentation.common.components.CleanXBackground
import com.quickcleanpro.phonecleaner.presentation.screen.JunkClean.JunkCleanPhase
import com.quickcleanpro.phonecleaner.presentation.screen.JunkClean.JunkCleanUiState

@Composable
internal fun JunkCleanContentView(
    uiState: JunkCleanUiState,
    onToggleCategorySelection: (List<JunkCategory>) -> Unit,
    onToggleItem: (CleanItem) -> Unit,
    onContinueFromResult: () -> Unit,
) {
    when (uiState.phase) {
        JunkCleanPhase.Scanning -> JunkScanningView(uiState = uiState)
        JunkCleanPhase.Preview ->
            JunkScanResultView(
                groups = uiState.groups,
                checkedEmptyCategories = uiState.checkedEmptyCategories,
                selectedSummary = uiState.selectedSummary,
                onToggleCategorySelection = onToggleCategorySelection,
                onToggleItem = onToggleItem,
            )
        JunkCleanPhase.Cleaning -> JunkCleaningView()
        JunkCleanPhase.AwaitingAuthorization ->
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .background(CleanXBackground)
                        .padding(horizontal = 24.dp),
                contentAlignment = Alignment.Center,
            ) {
                AwaitingAuthorizationView(message = uiState.awaitingAuthorizationMessage.orEmpty())
            }
        JunkCleanPhase.Complete ->
            JunkCleanResultView(
                uiState = uiState.cleanResult,
            )
        JunkCleanPhase.Error ->
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .background(CleanXBackground)
                        .padding(horizontal = 24.dp),
                contentAlignment = Alignment.Center,
            ) {
                JunkCleanErrorView(
                    message = uiState.errorMessage ?: uiState.errorMessageRes?.let { stringResource(it) }.orEmpty(),
                )
            }

    }
}
