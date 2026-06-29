package com.quickcleanpro.phonecleaner.presentation.screen.cleanresult

import android.annotation.SuppressLint
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.quickcleanpro.phonecleaner.R
import com.quickcleanpro.phonecleaner.presentation.common.CleanXMutedText
import com.quickcleanpro.phonecleaner.presentation.common.CleanXText
import com.quickcleanpro.phonecleaner.presentation.common.CommonResultCheckIcon
import com.quickcleanpro.phonecleaner.presentation.common.CommonResultScreen
import com.quickcleanpro.phonecleaner.presentation.theme.QuickCleanTheme

@Composable
fun CleanResultScreen(
    viewModel: CleanResultViewModel,
    onNavigateHome: () -> Unit,
    onNavigateTool: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val formatted = uiState.formattedFreedSpace
    val showResult = uiState.hasVisibleResult && formatted.isNotBlank()
    val numPart = if (showResult) formatted.substringBeforeLast(" ",

        formatted) else ""
    val unitPart = if (showResult) formatted.substringAfterLast(" ", "MB") else ""

    BackHandler(onBack = onNavigateHome)

    CommonResultScreen(
        titleRes = R.string.junk_removal,
        onBack = onNavigateHome,
        onNavigateTool = onNavigateTool
    ) {
        CleanResultCompletionContent(
            showResult = showResult,
            numPart = numPart,
            unitPart = unitPart,
            failedCount = uiState.failedCount
        )
    }
}

@Composable
private fun CleanResultCompletionContent(
    showResult: Boolean,
    numPart: String,
    unitPart: String,
    failedCount: Int
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        CommonResultCheckIcon(size = 45.dp)
        Spacer(modifier = Modifier.height(20.dp))
        if (showResult) {
            Row(
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = numPart,
                    color = CleanXText,
                    fontSize = 30.sp,
                    lineHeight = 36.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.size(6.dp))
                Text(
                    text = unitPart,
                    color = CleanXText,
                    fontSize = 16.sp,
                    lineHeight = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }
            Spacer(modifier = Modifier.height(18.dp))
            Text(
                text = if (failedCount > 0) {
                    stringResource(R.string.clean_result_junk_removed_with_failed, failedCount)
                } else {
                    stringResource(R.string.clean_result_junk_removed)
                },
                color = CleanXMutedText,
                fontSize = 16.sp,
                lineHeight = 20.sp,
                textAlign = TextAlign.Center
            )
        } else if (failedCount > 0) {
            Text(
                text = stringResource(R.string.clean_result_files_failed, failedCount),
                color = CleanXMutedText,
                fontSize = 16.sp,
                lineHeight = 22.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 18.dp)
            )
        } else {
            Text(
                text = stringResource(R.string.clean_result_done),
                color = CleanXMutedText,
                fontSize = 16.sp,
                lineHeight = 20.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@SuppressLint("ViewModelConstructorInComposable")
@Composable
private fun PreviewCleanResultScreen() {
    QuickCleanTheme {
        val viewModel = CleanResultViewModel()
        viewModel.loadResult()
        CleanResultScreen(
            viewModel = viewModel,
            onNavigateHome = {},
            onNavigateTool = {}
        )
    }
}
