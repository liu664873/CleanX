package com.quickcleanpro.phonecleaner.presentation.screen.scan

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.annotation.StringRes
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.quickcleanpro.phonecleaner.R
import com.quickcleanpro.phonecleaner.domain.model.JunkCategory
import com.quickcleanpro.phonecleaner.domain.model.ScanResult
import com.quickcleanpro.phonecleaner.presentation.common.CleanXBackground
import com.quickcleanpro.phonecleaner.presentation.common.CleanXBlue
import com.quickcleanpro.phonecleaner.presentation.common.CleanXContentPadding
import com.quickcleanpro.phonecleaner.presentation.common.CleanXMutedText
import com.quickcleanpro.phonecleaner.presentation.common.CleanXScaffold
import com.quickcleanpro.phonecleaner.presentation.common.CleanXScanRingAnimation
import com.quickcleanpro.phonecleaner.presentation.theme.QuickCleanTheme
import kotlinx.coroutines.delay

@Composable
fun ScanScreen(
    viewModel: ScanViewModel,
    onScanComplete: (ScanResult) -> Unit,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scanStartedAt = remember { System.currentTimeMillis() }

    LaunchedEffect(viewModel) {
        viewModel.startScanIfNeeded()
    }

    LaunchedEffect(uiState.scanState) {
        val scanResult = uiState.scanResult
        if (uiState.scanState == ScanViewModel.ScanState.Completed && scanResult != null) {
            val elapsed = System.currentTimeMillis() - scanStartedAt
            delay((1800L - elapsed).coerceAtLeast(650L))
            onScanComplete(scanResult)
        }
    }

    CleanXScaffold(
        titleRes = R.string.junk_removal,
        onBack = onNavigateBack,
        horizontalPadding = CleanXContentPadding
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(CleanXBackground)
                .padding(paddingValues)
                .padding(horizontal = CleanXContentPadding)
        ) {
            Spacer(modifier = Modifier.height(48.dp))
            ScanProgressContent(uiState = uiState)

            if (uiState.scanState == ScanViewModel.ScanState.Error) {
                Spacer(modifier = Modifier.height(28.dp))
                Text(
                    text = uiState.errorMessage ?: stringResource(R.string.scan_failed),
                    color = CleanXMutedText,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }

}

@Composable
private fun ScanProgressContent(uiState: ScanViewModel.ScanUiState) {
    val categoryLabel = uiState.currentCategory?.let { stringResource(it.titleRes) }
    val scanningText = categoryLabel?.let {
        stringResource(R.string.scan_scanning_category, it)
    } ?: stringResource(R.string.scan_loading_fallback)
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        CleanXScanRingAnimation(
            modifier = Modifier.size(260.dp),
            ringWidth = 20.dp,
            ringColor = CleanXBlue,
            backgroundColor = CleanXBlue.copy(alpha = 0.12f),
            animationDurationMillis = 900
        ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = uiState.formattedFoundSize.ifBlank { "0 B" },
                        color = CleanXBlue,
                        fontSize = 28.sp,
                        lineHeight = 32.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = scanningText,
                        color = CleanXMutedText,
                        fontSize = 13.sp,
                        lineHeight = 16.sp,
                        textAlign = TextAlign.Center
                    )
                }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF4F8FF)
@Composable
private fun PreviewScanScreen() {
    val mockState = ScanViewModel.ScanUiState(
        scanState = ScanViewModel.ScanState.Scanning,
        progress = 42f,
        foundItemCount = 38,
        foundTotalSize = 12_345_678L,
        formattedFoundSize = "11.8 MB",
        currentCategory = JunkCategory.CACHE
    )
    QuickCleanTheme {
        CleanXScaffold(
            titleRes = R.string.junk_removal,
            onBack = {},
            horizontalPadding = CleanXContentPadding
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(CleanXBackground)
                    .padding(paddingValues)
                    .padding(horizontal = CleanXContentPadding)
            ) {
                Spacer(modifier = Modifier.height(48.dp))
                ScanProgressContent(uiState = mockState)
            }
        }
    }
}

private val JunkCategory.titleRes: Int
    @StringRes
    get() = when (this) {
        JunkCategory.CACHE -> R.string.category_cache
        JunkCategory.TEMP_FILE -> R.string.category_temp
        JunkCategory.RESIDUAL -> R.string.category_residual
        JunkCategory.APK -> R.string.category_apk
        JunkCategory.DUPLICATE -> R.string.category_duplicate
        JunkCategory.LARGE_FILE -> R.string.category_large
    }
