package com.quickcleanpro.phonecleaner.presentation.screen.files.common.components

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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.produceState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import com.quickcleanpro.phonecleaner.presentation.common.CleanXFullScreenDeleteAnimation
import com.quickcleanpro.phonecleaner.presentation.common.CleanXMutedText
import com.quickcleanpro.phonecleaner.presentation.common.CleanXPrimaryButton
import com.quickcleanpro.phonecleaner.presentation.common.CleanXText
import com.quickcleanpro.phonecleaner.presentation.common.CommonResultCheckIcon
import com.quickcleanpro.phonecleaner.presentation.common.CommonResultContent
import com.quickcleanpro.phonecleaner.presentation.common.route.LocalRouter
import com.quickcleanpro.phonecleaner.utils.FileSizeFormatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

internal val FileManagerListBottomPadding = 112.dp
internal val FileManagerPageBrush = Brush.verticalGradient(
    colors = listOf(Color(0xFFE3ECFD), Color(0xFFDFEBF5))
)
internal val FileManagerCardColor = Color(0xFFF6F7FB)
internal val FileManagerNavy = Color(0xFF1D2959)
internal val FileManagerMutedNavy = Color(0xA61D2959)
internal val FileManagerDividerColor = Color(0xFFD4D7DE)

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
internal fun FileManagerSelectAllAction(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
) {
    Row(
        modifier = modifier.clickable { onClick() },
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(if (selected) R.string.file_unselect_all else R.string.file_select_all),
            color = CleanXBlue,
            fontSize = if (compact) 16.sp else 20.sp,
            lineHeight = 24.sp,
            fontWeight = FontWeight.Normal,
        )
        SelectionCircle(selected = selected)
    }
}

@Composable
internal fun FileCollectionResultContent(
    amount: String,
    unit: String,
    caption: String,
    onContinue: () -> Unit,
) {
    FileManagerResultContent(
        amount = amount,
        unit = unit,
        caption = caption,
        onContinue = onContinue,
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
    )
}

@Composable
private fun FileManagerResultContent(
    amount: String,
    unit: String,
    caption: String,
    onContinue: () -> Unit,
) {
    val router = LocalRouter.current
    CommonResultContent(
        onNavigateTool = { route -> router.navigateAndClearStack(route) },
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
