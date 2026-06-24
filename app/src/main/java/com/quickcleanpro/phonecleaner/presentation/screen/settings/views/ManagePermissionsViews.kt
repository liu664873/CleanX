package com.quickcleanpro.phonecleaner.presentation.screen.settings.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import com.quickcleanpro.phonecleaner.presentation.common.components.buttons.CleanXSettingsToggleRow
import com.quickcleanpro.phonecleaner.presentation.screen.settings.SettingsDivider

private val CardBg = Color(0xFFF6F7FB)
private val CardRadius = 20.dp

internal data class PermissionRowUi(
    val label: String,
    val checked: Boolean,
    val onClick: () -> Unit,
)

@Composable
internal fun ManagePermissionsContent(
    rows: List<PermissionRowUi>,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = CardBg,
        shape = RoundedCornerShape(CardRadius),
    ) {
        Column(
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            rows.forEachIndexed { index, row ->
                CleanXSettingsToggleRow(
                    label = row.label,
                    checked = row.checked,
                    onClick = row.onClick,
                )
                if (index < rows.lastIndex) {
                    SettingsDivider()
                }
            }
        }
    }
}
