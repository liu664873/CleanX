package com.quickcleanpro.phonecleaner.presentation.screen.scan

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.quickcleanpro.phonecleaner.R
import com.quickcleanpro.phonecleaner.presentation.common.CleanXBackground
import com.quickcleanpro.phonecleaner.presentation.common.CleanXMutedText
import com.quickcleanpro.phonecleaner.presentation.common.CleanXText
import com.quickcleanpro.phonecleaner.presentation.common.components.CleanXScaffoldPage
import com.quickcleanpro.phonecleaner.presentation.common.components.animations.CleanSpiralAnimation
import com.quickcleanpro.phonecleaner.presentation.common.permission.PermissionGateConfig
import com.quickcleanpro.phonecleaner.presentation.screen.cleanresult.CleanResultViewModel
import com.quickcleanpro.phonecleaner.presentation.screen.result.AwaitingAuthorizationContent
import com.quickcleanpro.phonecleaner.presentation.screen.result.CleaningCompleteContent
import com.quickcleanpro.phonecleaner.presentation.screen.result.CleaningContent
import com.quickcleanpro.phonecleaner.presentation.screen.result.ErrorContent
import com.quickcleanpro.phonecleaner.presentation.screen.result.ResultBottomBar
import com.quickcleanpro.phonecleaner.presentation.screen.result.ResultListContent
import com.quickcleanpro.phonecleaner.presentation.screen.result.ResultViewModel
import com.quickcleanpro.phonecleaner.presentation.screen.result.SelectionSummary
import kotlinx.coroutines.delay

private enum class JunkRemovalPhase {
    Scanning,
    Preview,
    Cleaning,
    Complete
}

@Composable
fun JunkRemovalScreen(
    scanViewModel: ScanViewModel,
    resultViewModel: ResultViewModel,
    cleanResultViewModel: CleanResultViewModel,
    permissionGateConfig: PermissionGateConfig? = null,
    onNavigateBack: () -> Unit,
    onNavigateHome: () -> Unit,
) {
    val context = LocalContext.current
    val scanUiState by scanViewModel.uiState.collectAsStateWithLifecycle()
    val resultScreenState by resultViewModel.screenState.collectAsStateWithLifecycle()
    val selectedSummary by resultViewModel.selectedSummary.collectAsStateWithLifecycle()
    val cleanResultUiState by cleanResultViewModel.uiState.collectAsStateWithLifecycle()
    val scanStartedAt = remember { System.currentTimeMillis() }
    var phase by rememberSaveable { mutableStateOf(JunkRemovalPhase.Scanning) }

    val deleteAuthorizationLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.StartIntentSenderForResult(),
        ) { result ->
            resultViewModel.handleAuthorizationResult(result.resultCode == Activity.RESULT_OK)
        }

    LaunchedEffect(scanViewModel) {
        scanViewModel.startScanIfNeeded()
    }

    LaunchedEffect(scanUiState.scanState, scanUiState.scanResult, phase) {
        val scanResult = scanUiState.scanResult
        if (phase == JunkRemovalPhase.Scanning &&
            scanUiState.scanState == ScanViewModel.ScanState.Completed &&
            scanResult != null
        ) {
            val elapsed = System.currentTimeMillis() - scanStartedAt
            delay((1_000L - elapsed).coerceAtLeast(150L))
            resultViewModel.loadPreview()
            phase = JunkRemovalPhase.Preview
        }
    }

    LaunchedEffect(resultScreenState) {
        when (val state = resultScreenState) {
            is ResultViewModel.ScreenState.AwaitingDeleteAuthorization -> {
                deleteAuthorizationLauncher.launch(
                    IntentSenderRequest.Builder(state.deleteRequest.intentSender).build(),
                )
            }
            is ResultViewModel.ScreenState.Completed -> {
                cleanResultViewModel.loadResult()
                phase = JunkRemovalPhase.Complete
            }
            else -> Unit
        }
    }

    fun exitToHome() {
        cleanResultViewModel.clearResult()
        onNavigateHome()
    }

    BackHandler(enabled = phase == JunkRemovalPhase.Complete, onBack = ::exitToHome)

    CleanXScaffoldPage(
        title = stringResource(R.string.junk_removal),
        onBack = if (phase == JunkRemovalPhase.Complete) ::exitToHome else onNavigateBack,
        scrollEnabled = false,
        contentPadding = PaddingValues(0.dp),
        permissionGateConfig = permissionGateConfig,
        backgroundBrush = Brush.linearGradient(
            colors = listOf(Color(0xFFE3ECFD), Color(0xFFDFEBF5)),
        ),
        bottomBar = {
            if (phase == JunkRemovalPhase.Preview &&
                resultScreenState is ResultViewModel.ScreenState.Preview
            ) {
                ResultBottomBar(
                    selectedSummary = selectedSummary,
                    onClean = { resultViewModel.startCleaning(context) },
                )
            }
        },
    ) {
        when (phase) {
            JunkRemovalPhase.Scanning -> ScanProgressContent(uiState = scanUiState)
            JunkRemovalPhase.Preview -> PreviewPhaseContent(
                screenState = resultScreenState,
                selectedSummary = selectedSummary,
                viewModel = resultViewModel,
            )
            JunkRemovalPhase.Cleaning -> JunkRemovalCleaning()
            JunkRemovalPhase.Complete -> JunkRemovalCompletionContent(
                uiState = cleanResultUiState,
            )
        }
    }
}

@Composable
private fun PreviewPhaseContent(
    screenState: ResultViewModel.ScreenState,
    selectedSummary: SelectionSummary,
    viewModel: ResultViewModel,
) {
    when (val state = screenState) {
        is ResultViewModel.ScreenState.Preview ->
            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .background(Color.Transparent),
            ) {
                ResultListContent(
                    groups = state.groups,
                    checkedEmptyCategories = state.checkedEmptyCategories,
                    selectedSummary = selectedSummary,
                    viewModel = viewModel,
                )
            }
        is ResultViewModel.ScreenState.AwaitingDeleteAuthorization ->
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .background(CleanXBackground)
                        .padding(horizontal = 24.dp),
                contentAlignment = Alignment.Center,
            ) {
                AwaitingAuthorizationContent(message = state.message)
            }
        is ResultViewModel.ScreenState.Error ->
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .background(CleanXBackground)
                        .padding(horizontal = 24.dp),
                contentAlignment = Alignment.Center,
            ) {
                ErrorContent(
                    message = state.message ?: stringResource(state.messageRes),
                )
            }
        is ResultViewModel.ScreenState.Cleaning -> CleaningContent()
        is ResultViewModel.ScreenState.Completed -> Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(Color.White),
        )
    }
}

@Composable
private fun JunkRemovalCompletionContent(
    uiState: CleanResultViewModel.CleanResultUiState,
) {
    val formatted = uiState.formattedFreedSpace
    val showResult = uiState.hasVisibleResult && formatted.isNotBlank()
    val numberPart = if (showResult) formatted.substringBeforeLast(" ", formatted) else ""
    val unitPart = if (showResult) formatted.substringAfterLast(" ", "MB") else ""

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            CleaningCompleteContent()
            Spacer(modifier = Modifier.height(30.dp))
            if (showResult) {
                androidx.compose.foundation.layout.Row(
                    verticalAlignment = Alignment.Bottom,
                ) {
                    Text(
                        text = numberPart,
                        color = CleanXText,
                        fontSize = 30.sp,
                        lineHeight = 36.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(modifier = Modifier.size(6.dp))
                    Text(
                        text = unitPart,
                        color = CleanXText,
                        fontSize = 16.sp,
                        lineHeight = 24.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 4.dp),
                    )
                }
                Spacer(modifier = Modifier.height(18.dp))
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
            } else if (uiState.failedCount > 0) {
                Text(
                    text = stringResource(R.string.clean_result_files_failed, uiState.failedCount),
                    color = CleanXMutedText,
                    fontSize = 16.sp,
                    lineHeight = 22.sp,
                    textAlign = TextAlign.Center,
                )
            } else {
                Text(
                    text = stringResource(R.string.clean_result_done),
                    color = CleanXMutedText,
                    fontSize = 16.sp,
                    lineHeight = 20.sp,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
fun JunkRemovalCleaning(){
    CleanSpiralAnimation {
        Image(
            modifier = Modifier.fillMaxSize(),
            painter = painterResource(R.drawable.ic_tran_can),
            contentDescription = null
        )
    }
}
