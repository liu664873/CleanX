package com.quickcleanpro.phonecleaner.presentation.screen.files

import android.app.Activity
import android.content.Context
import android.os.Build
import android.provider.MediaStore
import android.util.Size as AndroidSize
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.produceState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quickcleanpro.phonecleaner.R
import com.quickcleanpro.phonecleaner.presentation.common.CleanXBackground
import com.quickcleanpro.phonecleaner.presentation.common.CleanXBlue
import com.quickcleanpro.phonecleaner.presentation.common.CleanXBottomActionBar
import com.quickcleanpro.phonecleaner.presentation.common.CleanXFullScreenDeleteAnimation
import com.quickcleanpro.phonecleaner.presentation.common.CleanXHeader
import com.quickcleanpro.phonecleaner.presentation.common.CleanXMutedText
import com.quickcleanpro.phonecleaner.presentation.common.CleanXPrimaryButton
import com.quickcleanpro.phonecleaner.presentation.common.CleanXText
import com.quickcleanpro.phonecleaner.presentation.common.CommonResultCheckIcon
import com.quickcleanpro.phonecleaner.presentation.common.CommonResultContent
import com.quickcleanpro.phonecleaner.presentation.common.CommonResultScreen
import com.quickcleanpro.phonecleaner.utils.FileSizeFormatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

internal val FileManagerListBottomPadding = 112.dp

@Composable
internal fun SelectionCircle(selected: Boolean, modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(
            id = if (selected) R.drawable.ic_selected else R.drawable.ic_unselected
        ),
        contentDescription = null,
        modifier = modifier
            .size(21.dp)
    )
}

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
internal fun FileCollectionResultContent(
    amount: String,
    unit: String,
    caption: String,
    onContinue: () -> Unit,
    onNavigateTool: (String) -> Unit
) {
    FileManagerResultContent(
        amount = amount,
        unit = unit,
        caption = caption,
        onContinue = onContinue,
        onNavigateTool = onNavigateTool
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

@Composable
internal fun DeletingAnimationContent(fallbackText: String? = null) {
    val fallback = fallbackText ?: stringResource(R.string.delete_loading_fallback)
    CleanXFullScreenDeleteAnimation(
        fallbackText = fallback
    )
}

@Composable
internal fun CompleteAnimationContent() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(50))
                .background(Color(0xFFD6F4FF)),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Brush.verticalGradient(listOf(Color(0xFF54C6F5), CleanXBlue))),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(34.dp))
            }
        }
    }
}

@Composable
internal fun PhotosResultContent(
    onContinue: () -> Unit,
    onNavigateTool: (String) -> Unit,
    deletedSize: Long = 0L
) {
    val formattedSize = remember(deletedSize) { FileSizeFormatter.format(deletedSize) }
    val sizeNum = remember(formattedSize) { formattedSize.substringBefore(" ").takeIf { it.isNotBlank() } ?: "0" }
    val sizeUnit = remember(formattedSize) { formattedSize.substringAfter(" ", "B") }

    FileManagerResultContent(
        amount = sizeNum,
        unit = sizeUnit,
        caption = stringResource(R.string.file_deleted_in_cleanup),
        onContinue = onContinue,
        onNavigateTool = onNavigateTool
    )
}

@Composable
private fun FileManagerResultContent(
    amount: String,
    unit: String,
    caption: String,
    onContinue: () -> Unit,
    onNavigateTool: (String) -> Unit
) {
    CommonResultContent(
        onNavigateTool = onNavigateTool,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CommonResultCheckIcon(size = 45.dp)
            Spacer(modifier = Modifier.height(20.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(amount, color = CleanXText, fontSize = 32.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(6.dp))
                Text(unit, color = CleanXText, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(caption, color = CleanXMutedText, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(24.dp))
            CleanXPrimaryButton(
                text = stringResource(R.string.file_continue_managing),
                onClick = onContinue,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}
