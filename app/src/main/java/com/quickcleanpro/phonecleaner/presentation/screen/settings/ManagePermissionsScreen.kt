package com.quickcleanpro.phonecleaner.presentation.screen.settings

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import androidx.compose.foundation.background
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.quickcleanpro.phonecleaner.R
import com.quickcleanpro.phonecleaner.domain.repository.SettingsRepository
import com.quickcleanpro.phonecleaner.presentation.common.components.CleanXScaffoldPage
import com.quickcleanpro.phonecleaner.presentation.common.permission.CleanXPermissionRequestManager
import com.quickcleanpro.phonecleaner.presentation.common.permission.CleanXPermissionType
import org.koin.compose.koinInject

private val CardBg = Color(0xFFF6F7FB)
private val Navy = Color(0xFF1D2959)
private val Divider15 = Color(0x332D3748)
private val CardRadius = 20.dp
private val ToggleTrackOn = Color(0xFFBADDFF)
private val ToggleTrackOff = Color(0xFFECF0F4)
private val ToggleThumbOn = Color(0xFF4179FC)
private val ToggleThumbOff = Color(0xFFAFBBD0)

@Composable
fun ManagePermissionsScreen(
    settingsRepository: SettingsRepository = koinInject(),
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var refreshKey by remember { mutableIntStateOf(0) }
    val permissionRows = buildPermissionRows()

    fun refresh() {
        refreshKey++
    }

    val settingsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) {
        refresh()
    }
    val runtimeLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { grants ->
        if (grants[Manifest.permission.ACCESS_FINE_LOCATION] == false) {
            runCatching { settingsRepository.saveLocationRuntimePermissionDenied() }
        }
        if (grants[Manifest.permission.POST_NOTIFICATIONS] == false) {
            runCatching { settingsRepository.saveNotificationRuntimePermissionDenied() }
        }
        refresh()
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) refresh()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    fun openSettingsIntent(intent: Intent?) {
        val fallback = runCatching { settingsRepository.appSettingsIntent() }.getOrNull()
        listOfNotNull(intent, fallback).forEach { target ->
            try {
                settingsLauncher.launch(target)
                return
            } catch (_: ActivityNotFoundException) {
            } catch (_: Exception) {
            }
        }
    }

    fun requestPermission(type: CleanXPermissionType) {
        val missingRuntimePermissions =
            CleanXPermissionRequestManager.runtimePermissions(type)
                .filter { permission ->
                    ContextCompat.checkSelfPermission(context, permission) != android.content.pm.PackageManager.PERMISSION_GRANTED
                }
                .toTypedArray()

        if (missingRuntimePermissions.isNotEmpty()) {
            runtimeLauncher.launch(missingRuntimePermissions)
            return
        }

        openSettingsIntent(
            CleanXPermissionRequestManager.primarySettingsIntent(type, settingsRepository)
                ?: CleanXPermissionRequestManager.fallbackSettingsIntent(type, settingsRepository),
        )
    }

    CleanXScaffoldPage(
        title = stringResource(R.string.settings_manage_permissions),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 28.dp),
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
                permissionRows.forEachIndexed { index, row ->
                    val checked = remember(refreshKey, row.type) {
                        CleanXPermissionRequestManager.isGranted(context, row.type, settingsRepository)
                    }
                    PermissionToggleRow(
                        label = row.label,
                        checked = checked,
                        onClick = { requestPermission(row.type) },
                    )
                    if (index < permissionRows.lastIndex) {
                        SettingsDivider()
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(100.dp))
    }
}

@Composable
private fun buildPermissionRows(): List<PermissionRow> =
    listOf(
        PermissionRow("Storage Permissions", CleanXPermissionType.StorageFiles),
        PermissionRow("Usage Data Permissions", CleanXPermissionType.UsageAccess),
        PermissionRow("Location Management", CleanXPermissionType.Location),
        PermissionRow("Notification Toolbar", CleanXPermissionType.NotificationListener),
    )

private data class PermissionRow(
    val label: String,
    val type: CleanXPermissionType,
)

@Composable
private fun PermissionToggleRow(
    label: String,
    checked: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
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
            color = Navy,
            fontSize = 18.sp,
            fontWeight = FontWeight.Normal,
            lineHeight = 24.sp,
        )
        PermissionMiniSwitch(
            checked = checked,
        )
    }
}

@Composable
private fun PermissionMiniSwitch(
    checked: Boolean,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.size(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(width = 22.dp, height = 9.dp)
                .clip(RoundedCornerShape(50))
                .background(if (checked) ToggleTrackOn else ToggleTrackOff)
        )
        Box(
            modifier = Modifier
                .align(if (checked) Alignment.CenterStart else Alignment.CenterEnd)
                .size(10.5.dp)
                .clip(CircleShape)
                .background(if (checked) ToggleThumbOn else ToggleThumbOff)
        )
    }
}
