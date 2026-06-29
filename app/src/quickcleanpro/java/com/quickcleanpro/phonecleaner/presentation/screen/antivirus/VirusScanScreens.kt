package com.quickcleanpro.phonecleaner.presentation.screen.antivirus

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.quickcleanpro.phonecleaner.R
import com.quickcleanpro.phonecleaner.presentation.common.CleanXScanRingAnimation
import kotlinx.coroutines.delay

@Composable
fun QuickScanVirusScreen(
    viewModel: VirusScanViewModel,
    onBack: () -> Unit,
    onThreatsFound: () -> Unit,
    onNoThreats: () -> Unit
) {
    VirusScanContent(
        mode = VirusScanMode.Quick,
        viewModel = viewModel,
        onBack = onBack,
        onThreatsFound = onThreatsFound,
        onNoThreats = onNoThreats
    )
}

@Composable
fun DeepScanVirusScreen(
    viewModel: VirusScanViewModel,
    onBack: () -> Unit,
    onThreatsFound: () -> Unit,
    onNoThreats: () -> Unit
) {
    VirusScanContent(
        mode = VirusScanMode.Deep,
        viewModel = viewModel,
        onBack = onBack,
        onThreatsFound = onThreatsFound,
        onNoThreats = onNoThreats
    )
}

@Composable
private fun VirusScanContent(
    mode: VirusScanMode,
    viewModel: VirusScanViewModel,
    onBack: () -> Unit,
    onThreatsFound: () -> Unit,
    onNoThreats: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var scanStarted by remember(mode) { mutableStateOf(false) }

    LaunchedEffect(mode) {
        scanStarted = false
        viewModel.startScan(mode)
        scanStarted = true
    }

    LaunchedEffect(scanStarted, uiState.scanCompleted, uiState.effectiveThreatCount) {
        if (scanStarted && uiState.scanCompleted) {
            if (uiState.effectiveThreatCount > 0) {
                onThreatsFound()
            } else {
                onNoThreats()
            }
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            viewModel.clearError()
            delay(200L)
            onBack()
        }
    }

    DisposableEffect(Unit) {
        onDispose { viewModel.cancelScan() }
    }

    VirusPageScaffold(onBack = onBack) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .navigationBarsPadding()
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(330.dp),
                    contentAlignment = Alignment.TopCenter
                ) {
                    CleanXScanRingAnimation(
                        modifier = Modifier
                            .padding(top = 15.dp)
                            .size(300.dp),
                        ringModifier = Modifier.size(210.dp),
                        backgroundResId = R.mipmap.ic_scan_nor_bg,
                        ringColor = VirusBlue,
                        backgroundColor = Color(0xFFF1F9FC)
                    ) {
                        ThreatDrawableImage(
                            drawable = uiState.currentIcon,
                            fallback = R.mipmap.ic_protection,
                            modifier = Modifier.size(75.dp)
                        )
                    }
                }

                Text(
                    text = uiState.currentLabel,
                    color = VirusTitle,
                    fontSize = 15.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp)
                )
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 60.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                VirusProgressTrack(
                    mode = mode,
                    progress = uiState.progressFraction,
                    hasAdbRisk = uiState.hasAdbRisk,
                    appThreatCount = uiState.appThreatCount,
                    fileThreatCount = uiState.fileThreatCount
                )
                Spacer(modifier = Modifier.height(50.dp))
                Text(
                    text = stringResource(R.string.powered_by_trustlook),
                    color = Color(0xFF999999),
                    fontSize = 16.sp
                )
            }
        }
    }
}
