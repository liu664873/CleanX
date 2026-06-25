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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.quickcleanpro.phonecleaner.presentation.common.route.LocalRouter
import com.quickcleanpro.phonecleaner.presentation.common.route.Screen
import com.quickcleanpro.phonecleaner.presentation.screen.antivirus.views.VirusScanningView
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
    var scanStarted by remember(mode) { mutableStateOf(false) }

    LaunchedEffect(mode) {
        scanStarted = true
        viewModel.startScan(mode)
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

    DisposableEffect(mode) {
        onDispose { viewModel.cancelScan() }
    }

    VirusScanningView(
        mode = mode,
        uiState = uiState,
    )
}
