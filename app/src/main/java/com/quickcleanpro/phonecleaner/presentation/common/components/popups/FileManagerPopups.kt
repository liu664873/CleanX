package com.quickcleanpro.phonecleaner.presentation.common.components.popups

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import com.quickcleanpro.phonecleaner.R
import kotlinx.coroutines.delay

@Composable
internal fun StopScanDialog(onQuit: () -> Unit, onResume: () -> Unit) {
    CleanXDecisionDialog(
        title = stringResource(R.string.file_stop_scan_title),
        onDismissRequest = onResume,
        dismissText = stringResource(R.string.file_quit),
        onDismissAction = onQuit,
        confirmText = stringResource(R.string.file_resume),
        onConfirmAction = onResume,
    )
}

@Composable
internal fun DeleteConfirmDialog(
    onCancel: () -> Unit,
    onDelete: () -> Unit,
    title: String? = null,
    message: String? = null,
    confirmText: String? = null
) {
    val dialogTitle = title ?: stringResource(R.string.file_delete_permanently_title)
    val dialogMessage = message ?: stringResource(R.string.file_delete_permanently_message)
    val dialogConfirmText = confirmText ?: stringResource(R.string.file_delete)
    CleanXDecisionDialog(
        title = dialogTitle,
        message = dialogMessage,
        onDismissRequest = onCancel,
        dismissText = stringResource(R.string.cancel),
        onDismissAction = onCancel,
        confirmText = dialogConfirmText,
        onConfirmAction = onDelete,
    )
}

@Composable
internal fun NoResultsDialog(onBack: () -> Unit) {
    var showDialog by remember { mutableStateOf(true) }
    var pendingBack by remember { mutableStateOf(false) }

    fun closeDialogThenBack() {
        if (pendingBack) return
        showDialog = false
        pendingBack = true
    }

    LaunchedEffect(pendingBack) {
        if (!pendingBack) return@LaunchedEffect
        delay(200L)
        pendingBack = false
        onBack()
    }

    if (!showDialog) return

    CleanXSingleActionDialog(
        title = stringResource(R.string.file_scan_completed_no_results),
        actionText = stringResource(R.string.file_back_to_main_page),
        onDismissRequest = { closeDialogThenBack() },
        onAction = { closeDialogThenBack() },
    )
}
