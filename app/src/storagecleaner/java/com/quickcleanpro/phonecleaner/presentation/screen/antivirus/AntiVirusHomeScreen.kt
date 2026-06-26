package com.quickcleanpro.phonecleaner.presentation.screen.antivirus

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.quickcleanpro.phonecleaner.R
import com.quickcleanpro.phonecleaner.presentation.common.components.popups.CleanXDecisionDialog
import com.quickcleanpro.phonecleaner.presentation.common.permission.CleanXProtectedAction
import com.quickcleanpro.phonecleaner.presentation.common.permission.LocalCleanXPermissionCoordinator
import com.quickcleanpro.phonecleaner.presentation.common.route.LocalRouter
import com.quickcleanpro.phonecleaner.presentation.common.route.Screen
import com.quickcleanpro.phonecleaner.presentation.screen.antivirus.views.AntiVirusHomeView
import com.quickcleanpro.phonecleaner.utils.SharedPreferencesUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun AntiVirusScreen(
    viewModel: VirusScanViewModel,
) {
    val router = LocalRouter.current
    val permissionCoordinator = LocalCleanXPermissionCoordinator.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val permissionRequiredText = stringResource(R.string.permission_title_required)
    var pendingScanMode by remember { mutableStateOf<VirusScanMode?>(null) }
    var scanPermissionPending by remember { mutableStateOf(false) }
    var showNotice by remember { mutableStateOf(false) }

    fun launchScan(mode: VirusScanMode) {
        fun navigateToScan() {
            router.navigate(
                when (mode) {
                    VirusScanMode.Quick -> Screen.VirusQuickScan
                    VirusScanMode.Deep -> Screen.VirusDeepScan
                },
            )
        }

        if (mode == VirusScanMode.Deep) {
            permissionCoordinator.guard(
                action = CleanXProtectedAction.VirusDeepScanStart,
                onGranted = {
                    scanPermissionPending = false
                    navigateToScan()
                },
                onRejected = {
                    scanPermissionPending = false
                },
            )
        } else {
            scanPermissionPending = false
            navigateToScan()
        }
    }

    fun launchScanAfterInstalledAppsAccess(mode: VirusScanMode) {
        if (scanPermissionPending) return
        scanPermissionPending = true
        scope.launch {
            val hasAccess = withContext(Dispatchers.IO) {
                hasInstalledAppsAccess(context.applicationContext)
            }
            if (!hasAccess) {
                scanPermissionPending = false
                Toast.makeText(context, permissionRequiredText, Toast.LENGTH_LONG).show()
                return@launch
            }
            launchScan(mode)
        }
    }

    fun requestScan(mode: VirusScanMode) {
        if (SharedPreferencesUtils.getBoolean(SharedPreferencesUtils.KEY_VIRUS_SCAN_NOTICE_ACCEPTED)) {
            launchScanAfterInstalledAppsAccess(mode)
        } else {
            pendingScanMode = mode
            showNotice = true
        }
    }

    LaunchedEffect(Unit) {
        if (!SharedPreferencesUtils.getBoolean(SharedPreferencesUtils.KEY_VIRUS_SCAN_NOTICE_ACCEPTED)) {
            showNotice = true
        }
    }

    AntiVirusHomeView(
        onDeepScan = {
            requestScan(VirusScanMode.Deep)
        },
        onQuickScan = {
            requestScan(VirusScanMode.Quick)
        },
        enabled = !scanPermissionPending,
    )

    if (showNotice) {
        CleanXDecisionDialog(
            title = stringResource(R.string.virus_scan_notice_title),
            message = stringResource(R.string.virus_scan_notice_message),
            onDismissRequest = { showNotice = false; router.goBack() },
            dismissText = stringResource(R.string.cancel),
            onDismissAction = { showNotice = false; router.goBack() },
            confirmText = stringResource(R.string.continue_scan),
            onConfirmAction = {
                SharedPreferencesUtils.putBoolean(SharedPreferencesUtils.KEY_VIRUS_SCAN_NOTICE_ACCEPTED, true, commit = true)
                showNotice = false
                pendingScanMode?.let { launchScanAfterInstalledAppsAccess(it) }
            },
        )
    }
}
