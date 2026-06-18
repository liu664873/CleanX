package com.quickcleanpro.phonecleaner.presentation.screen.files.duplicates.views

import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quickcleanpro.phonecleaner.R
import com.quickcleanpro.phonecleaner.presentation.common.components.CleanXBlue
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.DuplicateFileEntry
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.DuplicateGroupItem
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.duplicateFileKey
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.components.FileManagerCardColor
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.components.FileManagerDivider
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.components.FileManagerListBottomPadding
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.components.FileManagerMutedNavy
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.components.FileManagerNavy
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.components.FileManagerSelectAllAction
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.components.SelectionCircle
import com.quickcleanpro.phonecleaner.utils.FileSizeFormatter

@Composable
internal fun DuplicateFilesGroupListView(
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
                color = FileManagerCardColor,
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.file_duplicate_warning),
                        color = FileManagerNavy,
                        fontSize = 16.sp,
                        lineHeight = 24.sp,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = { showWarning = false },
                        modifier = Modifier
                            .width(70.dp)
                            .height(35.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CleanXBlue),
                        contentPadding = PaddingValues(horizontal = 12.dp)
                    ) {
                        Text(stringResource(R.string.file_got_it), fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = FileManagerCardColor,
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(duplicateFileCount.toString(), color = FileManagerNavy, fontSize = 20.sp, lineHeight = 24.sp, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(stringResource(R.string.nav_duplicate_files), color = FileManagerMutedNavy, fontSize = 16.sp, lineHeight = 24.sp)
                    Spacer(modifier = Modifier.weight(1f))
                    FileManagerSelectAllAction(
                        selected = allSelected,
                        onClick = onToggleAll,
                        compact = true,
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    groups.chunked(2).forEach { rowGroups ->
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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
                .height(160.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFE0EAF9))
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 2.dp)
                    .fillMaxWidth(0.88f)
                    .height(12.dp)
                    .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                    .background(Color(0xFFD6E2F5))
            )
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 6.dp)
                    .fillMaxWidth(0.94f)
                    .height(12.dp)
                    .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                    .background(Color(0xFFDCE7F8))
            )
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(top = 18.dp, start = 10.dp)
                    .size(18.dp)
                    .clip(RoundedCornerShape(50))
                    .background(CleanXBlue),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = group.duplicateCount.toString(),
                    color = Color.White,
                    fontSize = 14.sp,
                    lineHeight = 14.sp,
                    fontWeight = FontWeight.Normal,
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
                    .padding(top = 18.dp, end = 10.dp)
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
                color = FileManagerNavy,
                fontSize = 16.sp,
                lineHeight = 19.sp,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 12.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = group.name,
            color = FileManagerNavy,
            fontSize = 18.sp,
            lineHeight = 22.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = pluralStringResource(
                R.plurals.duplicate_files_count,
                group.duplicateCount,
                group.duplicateCount
            ),
            color = FileManagerNavy,
            fontSize = 14.sp,
            lineHeight = 18.sp,
        )
    }
}

@Composable
internal fun DuplicateFilesGroupDetailView(
    group: DuplicateGroupItem,
    selectedFileKeys: Set<String>,
    scrollState: ScrollState,
    onToggleFile: (DuplicateFileEntry) -> Unit,
    onAutoSelect: () -> Unit,
    onToggleGroupSelection: () -> Unit,
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
        Spacer(modifier = Modifier.height(34.dp))
        Box(
            modifier = Modifier
                .size(167.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(119.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White)
            )
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                DuplicateFileIcon(modifier = Modifier.size(44.dp))
            }
        }

        Spacer(modifier = Modifier.height(6.dp))
        Text(
            group.name,
            color = FileManagerNavy,
            fontSize = 18.sp,
            lineHeight = 22.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.widthIn(max = 220.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            DuplicateOutlineButton(
                text = stringResource(R.string.file_auto_select),
                onClick = onAutoSelect,
            )
        }
        Spacer(modifier = Modifier.height(10.dp))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = FileManagerCardColor,
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(group.files.size.toString(), color = FileManagerNavy, fontSize = 20.sp, lineHeight = 24.sp, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${stringResource(R.string.nav_duplicate_files)}($selectedGroupSizeLabel)",
                        color = FileManagerMutedNavy,
                        fontSize = 16.sp,
                        lineHeight = 24.sp,
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    DuplicateOutlineButton(
                        text = stringResource(if (selectedGroupSize > 0L) R.string.file_unselect else R.string.file_select),
                        onClick = onToggleGroupSelection,
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))
                FileManagerDivider()
                Spacer(modifier = Modifier.height(16.dp))

                group.files.forEachIndexed { index, file ->
                    val fileKey = duplicateFileKey(file)
                    DuplicateFileRow(
                        file = file,
                        selected = fileKey in selectedFileKeys,
                        onToggleSelection = { onToggleFile(file) }
                    )
                    if (index != group.files.lastIndex) {
                        Spacer(modifier = Modifier.height(16.dp))
                        FileManagerDivider()
                        Spacer(modifier = Modifier.height(16.dp))
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
            .clickable { onToggleSelection() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        DuplicateFileIcon(modifier = Modifier.size(44.dp))
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.file_path_label, file.path),
                color = FileManagerNavy,
                fontSize = 18.sp,
                lineHeight = 21.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text("${file.date} ${file.sizeLabel}", color = FileManagerNavy, fontSize = 16.sp, lineHeight = 19.sp)
            if (file.note != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    localizedDuplicateFileNote(file.note),
                    color = CleanXBlue,
                    fontSize = 14.sp,
                    lineHeight = 17.sp
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
private fun DuplicateOutlineButton(
    text: String,
    onClick: () -> Unit,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.height(26.dp),
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.35.dp, CleanXBlue),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
    ) {
        Text(text, color = CleanXBlue, fontSize = 12.sp, lineHeight = 14.sp, fontWeight = FontWeight.Medium)
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
