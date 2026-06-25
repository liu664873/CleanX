package com.quickcleanpro.phonecleaner.presentation.screen.JunkClean

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.quickcleanpro.phonecleaner.R
import com.quickcleanpro.phonecleaner.presentation.common.components.CleanXScaffoldPage
import com.quickcleanpro.phonecleaner.presentation.common.components.popups.StopScanDialog
import com.quickcleanpro.phonecleaner.presentation.common.permission.CleanXProtectedAction
import com.quickcleanpro.phonecleaner.presentation.common.permission.LocalCleanXPermissionCoordinator
import com.quickcleanpro.phonecleaner.presentation.screen.JunkClean.views.JunkCleanContentView
import com.quickcleanpro.phonecleaner.presentation.screen.JunkClean.views.JunkScanResultBottomBar

@Composable
fun JunkCleanScreen(
    viewModel: JunkCleanViewModel,
    onNavigateBack: () -> Unit,
    onNavigateHome: () -> Unit,
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val permissionCoordinator = LocalCleanXPermissionCoordinator.current
    var showStopDialog by remember { mutableStateOf(false) }

    val deleteAuthorizationLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.StartIntentSenderForResult(),
        ) { result ->
            viewModel.handleAuthorizationResult(result.resultCode == Activity.RESULT_OK)
        }

    LaunchedEffect(viewModel, permissionCoordinator) {
        permissionCoordinator.guard(
            action = CleanXProtectedAction.JunkStartScan,
            onGranted = {
                viewModel.startScanIfNeeded()
            },
            onRejected = {
                viewModel.clearResult()
                onNavigateHome()
            },
        )
    }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is JunkCleanEvent.RequestDeleteAuthorization -> {
                    deleteAuthorizationLauncher.launch(
                        IntentSenderRequest.Builder(event.deleteRequest.intentSender).build(),
                    )
                }
            }
        }
    }

    fun exitToHome() {
        viewModel.clearResult()
        onNavigateHome()
    }

    fun handleBack() {
        when (uiState.phase) {
            JunkCleanPhase.Scanning -> {
                viewModel.cancelActiveOperation()
                showStopDialog = true
            }
            JunkCleanPhase.Cleaning -> {
                viewModel.cancelCleaningAndReturnToPreview()
                showStopDialog = true
            }
            JunkCleanPhase.Complete -> exitToHome()
            else -> onNavigateBack()
        }
    }

    BackHandler(
        enabled = uiState.phase == JunkCleanPhase.Scanning ||
            uiState.phase == JunkCleanPhase.Cleaning ||
            uiState.phase == JunkCleanPhase.Complete,
        onBack = ::handleBack,
    )

    CleanXScaffoldPage(
        title = stringResource(R.string.junk_removal),
        onBack = ::handleBack,
        scrollEnabled = false,
        contentPadding = PaddingValues(0.dp),
        backgroundBrush = Brush.linearGradient(
            colors = listOf(Color(0xFFE3ECFD), Color(0xFFDFEBF5)),
        ),
        bottomBar = {
            if (uiState.phase == JunkCleanPhase.Preview) {
                JunkScanResultBottomBar(
                    selectedSummary = uiState.selectedSummary,
                    onClean = {
                        permissionCoordinator.guard(CleanXProtectedAction.JunkCleanSelected) {
                            viewModel.startCleaning(context)
                        }
                    },
                )
            }
        },
    ) {
        JunkCleanContentView(
            uiState = uiState,
            onToggleCategorySelection = viewModel::toggleCategorySelection,
            onToggleItem = { item -> viewModel.toggleItemSelection(item.junkFile.id) },
            onContinueFromResult = ::exitToHome,
        )
    }

    if (showStopDialog) {
        StopScanDialog(
            onQuit = {
                showStopDialog = false
                viewModel.cancelActiveOperation()
                onNavigateBack()
            },
            onResume = {
                showStopDialog = false
                if (uiState.phase == JunkCleanPhase.Scanning) {
                    viewModel.startScanIfNeeded()
                }
            },
        )
    }
}
