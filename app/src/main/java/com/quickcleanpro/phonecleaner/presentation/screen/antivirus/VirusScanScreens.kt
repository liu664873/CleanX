package com.quickcleanpro.phonecleaner.presentation.screen.antivirus

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.quickcleanpro.phonecleaner.R
import com.quickcleanpro.phonecleaner.presentation.common.components.popups.CleanXDecisionDialog
import com.quickcleanpro.phonecleaner.presentation.common.permission.CleanXProtectedAction
import com.quickcleanpro.phonecleaner.presentation.common.permission.LocalCleanXPermissionCoordinator
import com.quickcleanpro.phonecleaner.presentation.common.route.LocalRouter
import com.quickcleanpro.phonecleaner.presentation.common.route.Screen
import com.quickcleanpro.phonecleaner.presentation.screen.antivirus.views.VirusScanningView
import com.quickcleanpro.phonecleaner.utils.SharedPreferencesUtils
import kotlinx.coroutines.delay

@Composable
fun QuickScanVirusScreen(
    viewModel: VirusScanViewModel,
) {
    VirusScanContent(
        mode = VirusScanMode.Quick,
        viewModel = viewModel,
    )
}

@Composable
fun DeepScanVirusScreen(
    viewModel: VirusScanViewModel,
) {
    VirusScanContent(
        mode = VirusScanMode.Deep,
        viewModel = viewModel,
    )
}

@Composable
private fun VirusScanContent(
    mode: VirusScanMode,
    viewModel: VirusScanViewModel,
) {
    val router = LocalRouter.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val permissionCoordinator = LocalCleanXPermissionCoordinator.current
    var scanStarted by remember(mode) { mutableStateOf(false) }
    var showNotice by remember(mode) { mutableStateOf(false) }

    fun startScanAfterNotice() {
        if (mode == VirusScanMode.Deep) {
            permissionCoordinator.guard(CleanXProtectedAction.VirusDeepScanStart) {
                viewModel.startScan(mode)
                scanStarted = true
            }
        } else {
            viewModel.startScan(mode)
            scanStarted = true
        }
    }

    LaunchedEffect(mode, permissionCoordinator) {
        scanStarted = false
        if (SharedPreferencesUtils.getBoolean(SharedPreferencesUtils.KEY_VIRUS_SCAN_NOTICE_ACCEPTED)) {
            startScanAfterNotice()
        } else {
            showNotice = true
        }
    }

    LaunchedEffect(scanStarted, uiState.scanCompleted, uiState.effectiveThreatCount) {
        if (scanStarted && uiState.scanCompleted) {
            if (uiState.effectiveThreatCount > 0) {
                router.replaceCurrent(Screen.VirusResult)
            } else {
                router.replaceCurrent(Screen.NoVirusResult)
            }
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            viewModel.clearError()
            delay(200L)
            router.goBack()
        }
    }

    DisposableEffect(Unit) {
        onDispose { viewModel.cancelScan() }
    }

    VirusScanningView(
        mode = mode,
        uiState = uiState,
    )

    if (showNotice) {
        CleanXDecisionDialog(
            title = stringResource(R.string.virus_scan_notice_title),
            message = stringResource(R.string.virus_scan_notice_message),
            onDismissRequest = {
                showNotice = false
                router.goBack()
            },
            dismissText = stringResource(R.string.cancel),
            onDismissAction = {
                showNotice = false
                router.goBack()
            },
            confirmText = stringResource(R.string.continue_scan),
            onConfirmAction = {
                SharedPreferencesUtils.putBoolean(SharedPreferencesUtils.KEY_VIRUS_SCAN_NOTICE_ACCEPTED, true)
                showNotice = false
                startScanAfterNotice()
            },
        )
    }
}
