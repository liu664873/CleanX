package com.quickcleanpro.phonecleaner.presentation.screen.applock.views

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quickcleanpro.phonecleaner.R
import com.quickcleanpro.phonecleaner.domain.model.applock.AppLockApp
import com.quickcleanpro.phonecleaner.presentation.common.components.buttons.CleanXSettingsToggleRow
import com.quickcleanpro.phonecleaner.presentation.screen.applock.AppLockUiState

@Composable
internal fun AppLockManageView(
    uiState: AppLockUiState,
    onOpenSearch: () -> Unit,
    onTogglePackage: (String) -> Unit,
    onToggleAll: () -> Unit,
    onAutoLockChange: (Boolean) -> Unit
) {
    val noAppsText = stringResource(R.string.app_lock_no_apps)
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 28.dp, bottom = 50.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { SearchEntryCard(onClick = onOpenSearch) }
        item {
            AutoLockCard(
                checked = uiState.autoLockEnabled,
                onClick = { onAutoLockChange(!uiState.autoLockEnabled) }
            )
        }
        item {
            CheckActionCard(
                title = stringResource(R.string.lock_all_apps),
                checked = uiState.allAppsLocked,
                onClick = onToggleAll
            )
        }
        if (uiState.isLoading && uiState.apps.isEmpty()) {
            item { LoadingCard() }
        } else {
            appSections(
                apps = uiState.apps,
                emptyText = noAppsText,
                onTogglePackage = onTogglePackage
            )
        }
    }
}

internal fun LazyListScope.appSections(
    apps: List<AppLockApp>,
    emptyText: String,
    onTogglePackage: (String) -> Unit
) {
    if (apps.isEmpty()) {
        item { EmptyCard(text = emptyText) }
        return
    }
    val lockedApps = apps.filter { it.isLocked }
    val unlockedApps = apps.filterNot { it.isLocked }
    if (lockedApps.isNotEmpty()) {
        item {
            AppLockSectionCard(title = stringResource(R.string.locked_apps)) {
                AppLockRows(
                    apps = lockedApps,
                    onTogglePackage = onTogglePackage,
                    showDividers = false
                )
            }
        }
    }
    if (unlockedApps.isNotEmpty()) {
        item {
            AppLockSectionCard(title = stringResource(R.string.my_apps)) {
                AppLockRows(
                    apps = unlockedApps,
                    onTogglePackage = onTogglePackage,
                    showDividers = false
                )
            }
        }
    }
}

@Composable
internal fun SearchEntryCard(onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clickable { onClick() },
        color = AppLockCardColor,
        shape = RoundedCornerShape(AppLockCardRadius)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(R.mipmap.ic_ser),
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = stringResource(R.string.search_app),
                color = AppLockPlaceholderText,
                fontSize = 18.sp
            )
        }
    }
}

@Composable
internal fun AutoLockCard(
    checked: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        color = AppLockCardColor,
        shape = RoundedCornerShape(AppLockCardRadius)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CleanXSettingsToggleRow(
                label = stringResource(R.string.turn_on_auto_lock),
                checked = checked,
                onClick = onClick,
            )
            AppLockDivider()
            Text(
                text = stringResource(R.string.turn_on_auto_lock_hint),
                color = AppLockSecondaryText,
                fontSize = 18.sp,
                lineHeight = 24.sp,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
internal fun CheckActionCard(
    title: String,
    checked: Boolean,
    onClick: () -> Unit
) {
    CleanXSettingsToggleRow(
        label = title,
        checked = checked,
        onClick = onClick,
    )
}

@Composable
private fun AppLockSectionCard(
    title: String,
    content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit
) {
    AppLockCard {
        Text(
            text = title,
            color = AppLockNavy,
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(12.dp))
        AppLockDivider()
        Spacer(modifier = Modifier.height(12.dp))
        content()
    }
}
