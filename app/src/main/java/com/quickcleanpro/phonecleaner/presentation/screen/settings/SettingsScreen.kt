package com.quickcleanpro.phonecleaner.presentation.screen.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quickcleanpro.phonecleaner.R
import com.quickcleanpro.phonecleaner.config.AppConfig
import com.quickcleanpro.phonecleaner.domain.repository.SettingsRepository
import com.quickcleanpro.phonecleaner.presentation.common.components.CleanXScaffoldPage
import com.quickcleanpro.phonecleaner.presentation.common.components.popups.SettingsRateDialog
import com.quickcleanpro.phonecleaner.presentation.common.components.popups.TemperatureUnitDialog
import com.quickcleanpro.phonecleaner.presentation.common.route.LocalRouter
import com.quickcleanpro.phonecleaner.presentation.common.route.Screen
import com.quickcleanpro.phonecleaner.presentation.common.utils.openUrl
import org.koin.compose.koinInject

private val SettingsCardBg = Color(0xFFF6F7FB)
private val SettingsNavy = Color(0xFF1D2959)
private val SettingsMuted = Color(0xA61D2959)
private val SettingsDividerColor = Color(0x332D3748)
private val SettingsCardRadius = 20.dp
@Composable
fun SettingsScreen(
    settingsRepository: SettingsRepository = koinInject(),
) {
    val router = LocalRouter.current
    val context = LocalContext.current
    var temperatureUnit by remember { mutableStateOf(settingsRepository.readTemperatureUnit().normalizeTemperatureUnit()) }
    var showTemperatureDialog by remember { mutableStateOf(false) }
    var showRateDialog by remember { mutableStateOf(false) }

    CleanXScaffoldPage(
        title = stringResource(R.string.nav_settings),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        SettingsPanel {
            SettingsValueRow(
                label = stringResource(R.string.settings_manage_permissions),
                value = null,
                onClick = { router.navigate(Screen.ManagePermissions) },
            )
            SettingsDivider()
            SettingsValueRow(
                label = stringResource(R.string.settings_temperature_unit),
                value = "${'\u00B0'}$temperatureUnit",
                onClick = { showTemperatureDialog = true },
            )
            SettingsDivider()
            SettingsValueRow(
                label = stringResource(R.string.settings_terms_of_service),
                value = null,
                onClick = { context.openUrl(AppConfig.TERMS_OF_SERVICE_URL) },
            )
            SettingsDivider()
            SettingsValueRow(
                label = stringResource(R.string.settings_privacy_policy),
                value = null,
                onClick = { context.openUrl(AppConfig.PRIVACY_POLICY_URL) },
            )
            SettingsDivider()
            SettingsValueRow(
                label = stringResource(R.string.settings_rate_us),
                value = null,
                onClick = { showRateDialog = true },
            )
        }
    }

    if (showTemperatureDialog) {
        TemperatureUnitDialog(
            selected = temperatureUnit,
            onDismiss = { showTemperatureDialog = false },
            onSelected = { unit ->
                val normalized = unit.normalizeTemperatureUnit()
                settingsRepository.saveTemperatureUnit(normalized)
                temperatureUnit = normalized
                showTemperatureDialog = false
            },
        )
    }

    if (showRateDialog) {
        SettingsRateDialog(onDismiss = { showRateDialog = false })
    }
}

@Composable
private fun SettingsPanel(
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = SettingsCardBg,
        shape = RoundedCornerShape(SettingsCardRadius),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            content = content,
        )
    }
}

@Composable
private fun SettingsValueRow(
    label: String,
    value: String?,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(24.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            ),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            color = SettingsNavy,
            fontSize = 18.sp,
            lineHeight = 24.sp,
            fontWeight = FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        if (value != null) {
            Text(
                text = value,
                color = SettingsNavy,
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
                maxLines = 1,
            )
        } else {
            Icon(
                painter = painterResource(id = R.mipmap.ic_next),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = SettingsNavy,
            )
        }
    }
}

@Composable
internal fun SettingsDivider() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp),
        color = SettingsDividerColor,
    ) {}
}

private fun String.normalizeTemperatureUnit(): String =
    if (equals("F", ignoreCase = true)) "F" else "C"

