package com.quickcleanpro.phonecleaner.presentation.screen.applock.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.quickcleanpro.phonecleaner.R
import com.quickcleanpro.phonecleaner.presentation.screen.applock.AppLockUiState

@Composable
internal fun AppLockSelectAppsView(
    uiState: AppLockUiState,
    onTogglePackage: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 28.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        item {
            AppLockCard {
                when {
                    uiState.isLoading && uiState.apps.isEmpty() -> LoadingState()
                    uiState.apps.isEmpty() -> EmptyState(text = stringResource(R.string.app_lock_no_apps))
                    else -> AppLockRows(
                        apps = uiState.apps,
                        onTogglePackage = onTogglePackage,
                        showDividers = true
                    )
                }
            }
        }
    }
}
