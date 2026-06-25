package com.quickcleanpro.phonecleaner.presentation.screen.applock.views

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quickcleanpro.phonecleaner.domain.model.applock.AppLockApp
import com.quickcleanpro.phonecleaner.presentation.common.components.buttons.CleanXMiniSwitch

@Composable
internal fun AppLockRows(
    apps: List<AppLockApp>,
    onTogglePackage: (String) -> Unit,
    showDividers: Boolean
) {
    Column(verticalArrangement = Arrangement.spacedBy(if (showDividers) 0.dp else 24.dp)) {
        apps.forEachIndexed { index, app ->
            AppLockRow(app = app, onClick = { onTogglePackage(app.packageName) })
            if (showDividers && index != apps.lastIndex) {
                Spacer(modifier = Modifier.height(24.dp))
                AppLockDivider()
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun AppLockRow(
    app: AppLockApp,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        PackageAppIcon(
            packageName = app.packageName,
            fallbackText = app.appName.take(1).ifBlank { "A" }
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = app.appName,
            color = AppLockNavy,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(10.dp))
        CleanXMiniSwitch(checked = app.isLocked)
    }
}
