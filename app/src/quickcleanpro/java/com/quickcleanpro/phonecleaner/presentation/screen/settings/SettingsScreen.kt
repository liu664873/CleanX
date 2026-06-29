package com.quickcleanpro.phonecleaner.presentation.screen.settings

import android.os.SystemClock
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.quickcleanpro.phonecleaner.R
import com.quickcleanpro.phonecleaner.config.AppConfig
import com.quickcleanpro.phonecleaner.presentation.common.CleanXBackground
import com.quickcleanpro.phonecleaner.presentation.common.CleanXBlue
import com.quickcleanpro.phonecleaner.presentation.common.CleanXCardColor
import com.quickcleanpro.phonecleaner.presentation.common.CleanXCardShape
import com.quickcleanpro.phonecleaner.presentation.common.CleanXContentPadding
import com.quickcleanpro.phonecleaner.presentation.common.CleanXMutedText
import com.quickcleanpro.phonecleaner.presentation.common.CleanXPillShape
import com.quickcleanpro.phonecleaner.presentation.common.CleanXPrimaryButton
import com.quickcleanpro.phonecleaner.presentation.common.CleanXRateDialog
import com.quickcleanpro.phonecleaner.presentation.common.CleanXRowHeight
import com.quickcleanpro.phonecleaner.presentation.common.CleanXScaffold
import com.quickcleanpro.phonecleaner.presentation.common.CleanXSubtlePanel
import com.quickcleanpro.phonecleaner.presentation.common.CleanXSuccess
import com.quickcleanpro.phonecleaner.presentation.common.CleanXText
import com.quickcleanpro.phonecleaner.presentation.common.permission.LocalCleanXPermissionCoordinator
import com.quickcleanpro.phonecleaner.presentation.theme.QuickCleanTheme
import com.quickcleanpro.phonecleaner.presentation.common.utils.openUrl

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onManagePermissions: () -> Unit,
    viewModel: SettingsViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showTemperatureDialog by remember { mutableStateOf(false) }
    var showRateDialog by remember { mutableStateOf(false) }

    CleanXScaffold(
        titleRes = R.string.settings,
        onBack = onBack,
        horizontalPadding = CleanXContentPadding
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(CleanXBackground)
                .padding(paddingValues)
                .padding(horizontal = CleanXContentPadding)
        ) {
            SettingsPanel {
                SettingsRow(
                    title = stringResource(R.string.settings_manage_permissions),
                    value = null,
                    onClick = onManagePermissions
                )
                SettingsRow(
                    title = stringResource(R.string.settings_temperature_unit),
                    value = "\u00B0${uiState.temperatureUnit}",
                    onClick = { showTemperatureDialog = true }
                )
                SettingsRow(
                    title = stringResource(R.string.settings_terms_of_service),
                    value = null,
                    onClick = { context.openUrl(AppConfig.TERMS_OF_SERVICE_URL) }
                )
                SettingsRow(
                    title = stringResource(R.string.settings_privacy_policy),
                    value = null,
                    onClick = { context.openUrl((AppConfig.PRIVACY_POLICY_URL)) }
                )
                SettingsRow(
                    title = stringResource(R.string.settings_rate_us),
                    value = null,
                    onClick = { showRateDialog = true }
                )
            }
        }
    }

    if (showTemperatureDialog) {
        TemperatureUnitDialog(
            selected = uiState.temperatureUnit,
            onDismiss = { showTemperatureDialog = false },
            onSelected = { unit ->
                viewModel.updateTemperatureUnit(unit)
                showTemperatureDialog = false
            }
        )
    }

    if (showRateDialog) {
        CleanXRateDialog(onDismiss = { showRateDialog = false })
    }
}

@Composable
fun ManagePermissionsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = viewModel()
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val permissionCoordinator = LocalCleanXPermissionCoordinator.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var settingsLaunchInProgress by remember { mutableStateOf(false) }
    var lastSettingsLaunchAt by remember { mutableStateOf(0L) }
    var hasResumedOnce by remember { mutableStateOf(false) }

    DisposableEffect(lifecycleOwner, viewModel) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                settingsLaunchInProgress = false
                if (hasResumedOnce) {
                    viewModel.refreshState()
                } else {
                    hasResumedOnce = true
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    CleanXScaffold(
        titleRes = R.string.settings_manage_permissions,
        onBack = onBack,
        horizontalPadding = CleanXContentPadding
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(CleanXBackground)
                .padding(paddingValues)
                .padding(horizontal = CleanXContentPadding)
        ) {
            SettingsPanel {
                uiState.permissions.forEach { permission ->
                    PermissionRow(
                        title = stringResource(permission.titleRes),
                        checked = permission.checked,
                        onClick = {
                            val now = SystemClock.elapsedRealtime()
                            if (settingsLaunchInProgress || now - lastSettingsLaunchAt < 900L) {
                                return@PermissionRow
                            }
                            settingsLaunchInProgress = true
                            lastSettingsLaunchAt = now
                            permissionCoordinator.openSettings(
                                item = permission.type.toPermissionItem(),
                                onGranted = {
                                    settingsLaunchInProgress = false
                                    viewModel.refreshState()
                                },
                                onRejected = {
                                    settingsLaunchInProgress = false
                                    viewModel.refreshState()
                                },
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsPanel(content: @Composable () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = CleanXCardColor,
        shape = CleanXCardShape
    ) {
        Column(modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp)) {
            content()
        }
    }
}

@Composable
private fun SettingsRow(
    title: String,
    value: String?,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(CleanXRowHeight)
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = CleanXText,
            fontSize = 16.sp,
            lineHeight = 22.sp,
            modifier = Modifier.weight(1f)
        )
        if (value != null) {
            Text(
                text = value,
                color = CleanXText,
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        } else {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = CleanXMutedText,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
private fun PermissionRow(
    title: String,
    checked: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(CleanXRowHeight)
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = CleanXText,
            fontSize = 16.sp,
            modifier = Modifier.weight(1f)
        )
        Switch(
            checked = checked,
            onCheckedChange = { onClick() },
            modifier = Modifier
                .size(width = 34.dp, height = 20.dp)
                .scale(0.92f),
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = CleanXBlue,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = CleanXSubtlePanel,
                checkedBorderColor = Color.Transparent,
                uncheckedBorderColor = Color.Transparent
            )
        )
    }
}

@Composable
fun PermissionRequiredDialog(
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = CleanXContentPadding),
        color = CleanXCardColor,
        shape = CleanXCardShape
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.permission_title_required),
                color = CleanXText,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = stringResource(R.string.permission_storage_desc),
                color = CleanXMutedText,
                fontSize = 16.sp,
                lineHeight = 21.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            PermissionBullet("1", stringResource(R.string.permission_hint_no_share_private))
            PermissionBullet("2", stringResource(R.string.permission_hint_no_collect_private))
            Spacer(modifier = Modifier.height(18.dp))
            CleanXPrimaryButton(text = stringResource(R.string.submit), onClick = onSubmit)
        }
    }
}

@Composable
private fun PermissionBullet(index: String, text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .background(CleanXSuccess, CleanXPillShape),
            contentAlignment = Alignment.Center
        ) {
            Text(text = index, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.size(10.dp))
        Text(text = text, color = CleanXMutedText, fontSize = 16.sp, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun TemperatureUnitDialog(
    selected: String,
    onDismiss: () -> Unit,
    onSelected: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CleanXCardColor,
        shape = CleanXCardShape,
        title = {
            Text(
                stringResource(R.string.settings_temperature_unit),
                color = CleanXText,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                TemperatureOption("F", selected == "F", onClick = { onSelected("F") })
                TemperatureOption("C", selected == "C", onClick = { onSelected("C") })
            }
        },
        confirmButton = {}
    )
}

@Composable
private fun TemperatureOption(
    unit: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(unit, color = CleanXText, fontSize = 18.sp, modifier = Modifier.weight(1f))
        if (selected) {
            Icon(
                Icons.Default.Check,
                contentDescription = null,
                tint = CleanXBlue,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF4F8FF)
@Composable
private fun PreviewSettingsScreen() {
    QuickCleanTheme { SettingsScreen(onBack = {}, onManagePermissions = {}) }
}

@Preview(showBackground = true, backgroundColor = 0xFFF4F8FF)
@Composable
private fun PreviewManagePermissionsScreen() {
    QuickCleanTheme { ManagePermissionsScreen(onBack = {}) }
}
