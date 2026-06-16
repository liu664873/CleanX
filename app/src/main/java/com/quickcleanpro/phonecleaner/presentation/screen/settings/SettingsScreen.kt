package com.quickcleanpro.phonecleaner.presentation.screen.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.quickcleanpro.phonecleaner.R
import com.quickcleanpro.phonecleaner.config.AppConfig
import com.quickcleanpro.phonecleaner.domain.repository.SettingsRepository
import com.quickcleanpro.phonecleaner.presentation.common.CleanXPrimaryButton
import com.quickcleanpro.phonecleaner.presentation.common.components.CleanXScaffoldPage
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

@Composable
private fun TemperatureUnitDialog(
    selected: String,
    onDismiss: () -> Unit,
    onSelected: (String) -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SettingsCardBg,
        shape = RoundedCornerShape(20.dp),
        title = {
            Text(
                text = stringResource(R.string.settings_temperature_unit),
                color = SettingsNavy,
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                TemperatureOption(unit = "F", selected = selected == "F", onClick = { onSelected("F") })
                TemperatureOption(unit = "C", selected = selected == "C", onClick = { onSelected("C") })
            }
        },
        confirmButton = {},
    )
}

@Composable
private fun TemperatureOption(
    unit: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "${'\u00B0'}$unit",
            color = SettingsNavy,
            fontSize = 18.sp,
            modifier = Modifier.weight(1f),
        )
        if (selected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = Color(0xFF4179FC),
                modifier = Modifier.size(22.dp),
            )
        }
    }
}

@Composable
private fun SettingsRateDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    var rating by remember { mutableIntStateOf(0) }
    var showFeedback by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 22.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                if (showFeedback) {
                    RateFeedbackContent(onDismiss = onDismiss)
                } else {
                    RateSelectionContent(
                        rating = rating,
                        onRatingChange = { rating = it },
                        onSubmit = {
                            if (rating >= 4) {
                                onDismiss()
                                openGooglePlayRatePage(context)
                            } else {
                                showFeedback = true
                            }
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun RateSelectionContent(
    rating: Int,
    onRatingChange: (Int) -> Unit,
    onSubmit: () -> Unit,
) {
    Text(
        text = stringResource(R.string.rate_title),
        color = SettingsNavy,
        fontSize = 19.sp,
        lineHeight = 24.sp,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
    )
    Spacer(modifier = Modifier.height(18.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        repeat(5) { index ->
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = if (index < rating) Color(0xFFFFC52E) else Color(0xFFD3D6DC),
                modifier = Modifier
                    .size(38.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { onRatingChange(index + 1) },
                    ),
            )
        }
    }
    Spacer(modifier = Modifier.height(20.dp))
    CleanXPrimaryButton(
        text = stringResource(R.string.submit),
        onClick = onSubmit,
        enabled = rating > 0,
    )
}

@Composable
private fun RateFeedbackContent(onDismiss: () -> Unit) {
    Text(
        text = stringResource(R.string.rate_feedback_title),
        color = SettingsNavy,
        fontSize = 19.sp,
        lineHeight = 24.sp,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
    )
    Spacer(modifier = Modifier.height(12.dp))
    Text(
        text = stringResource(R.string.rate_feedback_message),
        color = SettingsMuted,
        fontSize = 15.sp,
        lineHeight = 20.sp,
        textAlign = TextAlign.Center,
    )
    Spacer(modifier = Modifier.height(20.dp))
    CleanXPrimaryButton(
        text = stringResource(R.string.ok),
        onClick = onDismiss,
    )
}

private fun openGooglePlayRatePage(context: Context) {
    val packageName = context.packageName
    val marketIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")).apply {
        setPackage("com.android.vending")
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName")).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    runCatching {
        context.startActivity(marketIntent)
    }.recoverCatching {
        context.startActivity(webIntent)
    }
}

private fun String.normalizeTemperatureUnit(): String =
    if (equals("F", ignoreCase = true)) "F" else "C"

