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
import androidx.compose.foundation.ScrollState
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quickcleanpro.phonecleaner.R
import com.quickcleanpro.phonecleaner.presentation.common.CleanXBackground
import com.quickcleanpro.phonecleaner.presentation.common.CleanXBlue
import com.quickcleanpro.phonecleaner.presentation.common.CleanXHeader
import com.quickcleanpro.phonecleaner.presentation.common.CleanXMutedText
import com.quickcleanpro.phonecleaner.presentation.common.CleanXText
import com.quickcleanpro.phonecleaner.utils.FileSizeFormatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
internal fun DuplicateGroupsContent(
    groups: List<DuplicateGroupItem>,
    selectedFileKeys: Set<String>,
    allSelected: Boolean,
    scrollState: ScrollState,
    onToggleAll: () -> Unit,
    onOpenGroup: (DuplicateGroupItem) -> Unit
) {
    val duplicateFileCount = remember(groups) { groups.sumOf { it.files.size } }
    var showWarning by remember { mutableStateOf(true) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(bottom = FileManagerListBottomPadding)
    ) {
        if (showWarning) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shape = RoundedCornerShape(10.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.file_duplicate_warning),
                        color = Color(0xFF7D8EA8),
                        fontSize = 14.sp,
                        lineHeight = 16.sp,
                        modifier = Modifier.weight(1f)
                    )
                    Button(
                        onClick = { showWarning = false },
                        modifier = Modifier
                            .width(72.dp)
                            .height(36.dp),
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(containerColor = CleanXBlue),
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        Text(stringResource(R.string.file_got_it), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            shape = RoundedCornerShape(10.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(duplicateFileCount.toString(), color = CleanXText, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(5.dp))
                Text(stringResource(R.string.nav_duplicate_files), color = CleanXMutedText, fontSize = 16.sp)
                Spacer(modifier = Modifier.weight(1f))
                Text(stringResource(R.string.file_select_all), color = CleanXText, fontSize = 16.sp)
                Spacer(modifier = Modifier.width(8.dp))
                SelectionCircle(selected = allSelected, modifier = Modifier.clickable { onToggleAll() })
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            groups.chunked(2).forEach { rowGroups ->
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    rowGroups.forEach { group ->
                        val groupDeleteKeys = group.files.drop(1).map(::duplicateFileKey).toSet()
                        DuplicateGroupCard(
                            group = group,
                            selected = groupDeleteKeys.any { it in selectedFileKeys },
                            onOpen = { onOpenGroup(group) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    repeat(2 - rowGroups.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
internal fun DuplicateGroupCard(
    group: DuplicateGroupItem,
    selected: Boolean,
    onOpen: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.clickable { onOpen() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFFEAF0F7))
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(10.dp)
                    .size(18.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color(0xFF9AA7BA)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = group.duplicateCount.toString(),
                    color = Color.White,
                    fontSize = 12.sp,
                    lineHeight = 12.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    style = TextStyle(
                        platformStyle = PlatformTextStyle(includeFontPadding = false)
                    )
                )
            }
            SelectionCircle(
                selected = selected,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(10.dp)
            )
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(60.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                DuplicateFileIcon(modifier = Modifier.size(44.dp))
            }
            Text(
                text = group.sizeLabel,
                color = CleanXText,
                fontSize = 14.sp,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 14.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = group.name,
            color = CleanXText,
            fontSize = 14.sp,
            lineHeight = 17.sp,
            maxLines = 1
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = pluralStringResource(
                R.plurals.duplicate_files_count,
                group.duplicateCount,
                group.duplicateCount
            ),
            color = CleanXMutedText,
            fontSize = 10.sp,
            lineHeight = 12.sp
        )
    }
}

@Composable
internal fun DuplicateGroupDetailContent(
    group: DuplicateGroupItem,
    selectedFileKeys: Set<String>,
    scrollState: ScrollState,
    onToggleFile: (DuplicateFileEntry) -> Unit,
    onAutoSelect: () -> Unit
) {
    val selectedGroupSize = group.files
        .filter { duplicateFileKey(it) in selectedFileKeys }
        .sumOf { it.realFile?.sizeBytes ?: 0L }
    val selectedGroupSizeLabel = remember(selectedGroupSize) { FileSizeFormatter.format(selectedGroupSize) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(bottom = FileManagerListBottomPadding),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(42.dp))
        Box(
            modifier = Modifier
                .size(118.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFFEAF0F7)),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                DuplicateFileIcon(modifier = Modifier.size(44.dp))
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
        Text(group.name, color = CleanXText, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(32.dp))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            shape = RoundedCornerShape(10.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(group.files.size.toString(), color = CleanXText, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(stringResource(R.string.nav_duplicate_files), color = CleanXMutedText, fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(selectedGroupSizeLabel, color = CleanXMutedText, fontSize = 10.sp)
                    Spacer(modifier = Modifier.weight(1f))
                    OutlinedButton(
                        onClick = onAutoSelect,
                        modifier = Modifier.height(30.dp),
                        shape = RoundedCornerShape(50),
                        contentPadding = PaddingValues(horizontal = 10.dp)
                    ) {
                        Text(stringResource(R.string.file_auto_select), color = CleanXBlue, fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                group.files.forEachIndexed { index, file ->
                    val fileKey = duplicateFileKey(file)
                    DuplicateFileRow(
                        file = file,
                        selected = fileKey in selectedFileKeys,
                        onToggleSelection = { onToggleFile(file) }
                    )
                    if (index != group.files.lastIndex) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(Color(0xFFE9EEF5))
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun DuplicateFileRow(
    file: DuplicateFileEntry,
    selected: Boolean,
    onToggleSelection: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        DuplicateFileIcon(modifier = Modifier.size(44.dp))
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.file_path_label, file.path),
                color = CleanXText,
                fontSize = 18.sp,
                lineHeight = 21.sp,
                fontWeight = FontWeight.Bold
            )
            Text("${file.date} ${file.sizeLabel}", color = CleanXMutedText, fontSize = 14.sp, lineHeight = 17.sp)
            if (file.note != null) {
                Text(
                    localizedDuplicateFileNote(file.note),
                    color = CleanXBlue,
                    fontSize = 12.sp,
                    lineHeight = 15.sp
                )
            }
        }
        SelectionCircle(
            selected = selected,
            modifier = Modifier.clickable { onToggleSelection() }
        )
    }
}

@Composable
private fun localizedDuplicateFileNote(note: String): String =
    when (note) {
        "Removal not recommended" -> stringResource(R.string.file_removal_not_recommended)
        else -> note
    }

@Composable
private fun DuplicateFileIcon(modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(id = R.drawable.ic_file_blue),
        contentDescription = null,
        modifier = modifier
    )
}
