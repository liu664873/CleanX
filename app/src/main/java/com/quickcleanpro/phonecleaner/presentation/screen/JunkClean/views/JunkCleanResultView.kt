package com.quickcleanpro.phonecleaner.presentation.screen.JunkClean.views

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quickcleanpro.phonecleaner.R
import com.quickcleanpro.phonecleaner.presentation.common.components.CelebratingView
import com.quickcleanpro.phonecleaner.presentation.common.components.CleanXMutedText
import com.quickcleanpro.phonecleaner.presentation.common.components.CleanXPrimaryButton
import com.quickcleanpro.phonecleaner.presentation.common.components.CleanXText
import com.quickcleanpro.phonecleaner.presentation.common.components.CommonResultCheckIcon
import com.quickcleanpro.phonecleaner.presentation.common.components.CommonResultContent
import com.quickcleanpro.phonecleaner.presentation.screen.JunkClean.JunkCleanResultUiState
import kotlinx.coroutines.delay

@Composable
internal fun JunkCleanResultView(
    uiState: JunkCleanResultUiState,
) {

    var showContent by remember { mutableStateOf(false) }

    val formatted = uiState.formattedFreedSpace
    val showResult = uiState.hasVisibleResult && formatted.isNotBlank()
    val numberPart = if (showResult) formatted.substringBeforeLast(" ", formatted) else ""
    val unitPart = if (showResult) formatted.substringAfterLast(" ", "MB") else ""

    LaunchedEffect(Unit) {
        delay(800)
        showContent = true
    }

    if (!showContent) {
        // 播放庆祝动画（使用通用组件）
        CelebratingView()
    } else {
        CommonResultContent(
            onNavigateTool = {},
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Row() {
                    CommonResultCheckIcon(size = 45.dp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(numberPart, color = CleanXText, fontSize = 32.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(unitPart, color = CleanXText, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
                if (showResult) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text =
                            if (uiState.failedCount > 0) {
                                stringResource(R.string.clean_result_junk_removed_with_failed, uiState.failedCount)
                            } else {
                                stringResource(R.string.clean_result_junk_removed)
                            },
                        color = CleanXMutedText,
                        fontSize = 16.sp,
                        lineHeight = 20.sp,
                        textAlign = TextAlign.Center,
                    )
                } else {
                    Text(
                        text =
                            if (uiState.failedCount > 0) {
                                stringResource(R.string.clean_result_files_failed, uiState.failedCount)
                            } else {
                                stringResource(R.string.clean_result_done)
                            },
                        color = CleanXMutedText,
                        fontSize = 16.sp,
                        lineHeight = 22.sp,
                        textAlign = TextAlign.Center,
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

}


