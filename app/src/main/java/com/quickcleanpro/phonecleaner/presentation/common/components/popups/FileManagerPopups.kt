package com.quickcleanpro.phonecleaner.presentation.common.components.popups

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quickcleanpro.phonecleaner.R
import com.quickcleanpro.phonecleaner.presentation.common.CleanXBlue
import com.quickcleanpro.phonecleaner.presentation.common.CleanXMutedText
import com.quickcleanpro.phonecleaner.presentation.common.CleanXPrimaryButton
import com.quickcleanpro.phonecleaner.presentation.common.CleanXText
import kotlinx.coroutines.delay

@Composable
internal fun StopScanDialog(onQuit: () -> Unit, onResume: () -> Unit) {
    AlertDialog(
        onDismissRequest = onResume,
        containerColor = Color.White,
        shape = RoundedCornerShape(10.dp),
        title = {
            Text(
                text = stringResource(R.string.file_stop_scan_title),
                color = CleanXText,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            OutlinedButton(onClick = onResume, shape = RoundedCornerShape(50)) {
                Text(stringResource(R.string.file_resume), color = CleanXBlue, fontSize = 16.sp)
            }
        },
        dismissButton = {
            TextButton(onClick = onQuit) {
                Text(stringResource(R.string.file_quit), color = Color(0xFFB3BDCB), fontSize = 16.sp)
            }
        }
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
    AlertDialog(
        onDismissRequest = onCancel,
        containerColor = Color.White,
        shape = RoundedCornerShape(10.dp),
        title = {
            Text(
                text = dialogTitle,
                color = CleanXText,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Text(
                text = dialogMessage,
                color = CleanXMutedText,
                fontSize = 16.sp,
                lineHeight = 20.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            OutlinedButton(onClick = onDelete, shape = RoundedCornerShape(50)) {
                Text(dialogConfirmText, color = CleanXBlue, fontSize = 16.sp)
            }
        },
        dismissButton = {
            TextButton(onClick = onCancel) {
                Text(stringResource(R.string.cancel), color = Color(0xFFB3BDCB), fontSize = 16.sp)
            }
        }
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

    AlertDialog(
        onDismissRequest = { closeDialogThenBack() },
        containerColor = Color.White,
        shape = RoundedCornerShape(10.dp),
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stringResource(R.string.file_scan_completed_no_results),
                    color = CleanXText,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(20.dp))
                CleanXPrimaryButton(
                    text = stringResource(R.string.file_back_to_main_page),
                    onClick = { closeDialogThenBack() }
                )
            }
        },
        confirmButton = {}
    )
}
