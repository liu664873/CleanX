package com.quickcleanpro.phonecleaner.presentation.screen.scan

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.quickcleanpro.phonecleaner.R
import com.quickcleanpro.phonecleaner.domain.model.JunkCategory
import com.quickcleanpro.phonecleaner.domain.model.ScanResult
import com.quickcleanpro.phonecleaner.presentation.common.components.CleanXScaffoldPage
import com.quickcleanpro.phonecleaner.presentation.common.permission.PermissionGateConfig
import com.quickcleanpro.phonecleaner.presentation.common.components.animations.CleanSpiralAnimation
import kotlinx.coroutines.delay

private val Navy = Color(0xFF1D2959)
private val MutedText = Color(0xFF8190A5)
private val DarkText = Color(0xFF2D3748)

@Composable
fun ScanScreen(
    viewModel: ScanViewModel,
    onScanComplete: (ScanResult) -> Unit,
    permissionGateConfig: PermissionGateConfig? = null,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scanStartedAt = remember { System.currentTimeMillis() }

    LaunchedEffect(viewModel) {
        viewModel.startScanIfNeeded()
    }

    LaunchedEffect(uiState.scanState, uiState.scanResult) {
        val scanResult = uiState.scanResult
        if (uiState.scanState == ScanViewModel.ScanState.Completed && scanResult != null) {
            val elapsed = System.currentTimeMillis() - scanStartedAt
            delay((1_800L - elapsed).coerceAtLeast(650L))
            onScanComplete(scanResult)
        }
    }

    CleanXScaffoldPage(
        title = stringResource(R.string.junk_removal),
        scrollEnabled = false,
        contentPadding = PaddingValues(0.dp),
        permissionGateConfig = permissionGateConfig,
    ) {
        ScanProgressContent(uiState = uiState)
    }
}

@Composable
private fun ScanProgressContent(uiState: ScanViewModel.ScanUiState) {
    val categoryLabel = uiState.currentCategory?.let { stringResource(it.titleRes) }
    val scanningText =
        categoryLabel?.let { stringResource(R.string.scan_scanning_category, it) }
            ?: stringResource(R.string.scan_loading_fallback)

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CleanSpiralAnimation(
                content = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = uiState.formattedFoundSize.substringBeforeLast(" ", uiState.formattedFoundSize),
                            fontSize = 34.sp,
                            lineHeight = 38.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = DarkText,
                            textAlign = TextAlign.Center,
                        )
                        Text(
                            text = uiState.formattedFoundSize.substringAfterLast(" ", "B"),
                            fontSize = 18.sp,
                            lineHeight = 22.sp,
                            fontWeight = FontWeight.Medium,
                            color = MutedText,
                            textAlign = TextAlign.Center,
                        )
                    }
                },
            )

            Spacer(modifier = Modifier.height(56.dp))

            Text(
                text =
                    if (uiState.scanState == ScanViewModel.ScanState.Error) {
                        uiState.errorMessage ?: uiState.errorMessageRes?.let { stringResource(it) }
                            ?: stringResource(R.string.scan_failed)
                    } else {
                        scanningText
                    },
                fontSize = 18.sp,
                lineHeight = 24.sp,
                fontWeight = FontWeight.Normal,
                color = if (uiState.scanState == ScanViewModel.ScanState.Error) MutedText else Navy,
                textAlign = TextAlign.Center,
                modifier = Modifier.size(width = 260.dp, height = 60.dp),
            )
        }
    }
}

private val JunkCategory.titleRes: Int
    @StringRes
    get() =
        when (this) {
            JunkCategory.CACHE -> R.string.category_cache
            JunkCategory.TEMP_FILE -> R.string.category_temp
            JunkCategory.RESIDUAL -> R.string.category_residual
            JunkCategory.APK -> R.string.category_apk
            JunkCategory.DUPLICATE -> R.string.category_duplicate
            JunkCategory.LARGE_FILE -> R.string.category_large
        }
