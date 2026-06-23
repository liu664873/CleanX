package com.quickcleanpro.phonecleaner.presentation.screen.files.common.views

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.quickcleanpro.phonecleaner.R
import com.quickcleanpro.phonecleaner.presentation.common.components.CleanXEmptyScanResult
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.FileOperationPhase

@Composable
internal fun FileOperationPhaseContent(
    phase: FileOperationPhase,
    scanningText: String,
    deletingText: String? = null,
    resultAmount: String,
    resultUnit: String,
    resultCaption: String,
    onContinue: () -> Unit,
    browsingContent: @Composable () -> Unit,
) {
    when (phase) {
        FileOperationPhase.Scanning -> FileManagerScanningView(text = scanningText)
        FileOperationPhase.Browsing,
        FileOperationPhase.ConfirmDelete -> browsingContent()
        FileOperationPhase.Deleting -> FileManagerDeletingView(deletingText ?: stringResource(R.string.file_deleting_files))
        FileOperationPhase.CompleteAnimation -> FileManagerCompleteView()
        FileOperationPhase.Result -> FileManagerResultView(
            amount = resultAmount,
            unit = resultUnit,
            caption = resultCaption,
            onContinue = onContinue,
        )
        FileOperationPhase.NoResults -> CleanXEmptyScanResult(
            message = stringResource(R.string.file_scan_completed_no_results),
        )
    }
}
