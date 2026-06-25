package com.quickcleanpro.phonecleaner.presentation.screen.files.common.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quickcleanpro.phonecleaner.R
import com.quickcleanpro.phonecleaner.presentation.common.components.CleanXBlue
import com.quickcleanpro.phonecleaner.presentation.common.components.CleanXSegmentedTabs
import com.quickcleanpro.phonecleaner.presentation.common.components.CleanXTabItem
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.FileImageDisplayItem
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.FileListDisplayItem
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.FileListDisplayStyle
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.FileListIconKind
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.FileManagerTabDisplayItem
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.views.list.FileManagerRealImage

@Composable
internal fun FileManagerTopAction(
    actionText: String?,
    actionEnabled: Boolean = true,
    onAction: () -> Unit = {},
) {
    if (actionText == null) return

    Text(
        text = actionText,
        color = CleanXBlue.copy(alpha = if (actionEnabled) 1f else 0.45f),
        fontSize = 16.sp,
        modifier = Modifier.clickable(enabled = actionEnabled) { onAction() },
    )
}

@Composable
internal fun FileManagerMediaGridView(
    tabs: List<FileManagerTabDisplayItem>,
    items: List<FileImageDisplayItem>,
    selectedIds: Set<Int>,
    scrollState: ScrollState,
    onToggleVisibleItems: (Set<Int>) -> Unit,
    onSelect: (Int) -> Unit,
    onOpenDetail: (FileImageDisplayItem) -> Unit,
    selectedTabIndex: Int = 0,
    onTabSelected: (Int) -> Unit = {},
    showPlayBadge: Boolean = true,
) {
    val visibleItems = remember(items, selectedTabIndex) {
        filterMediaGridItems(tabs.getOrNull(selectedTabIndex)?.title.orEmpty(), items)
    }
    val visibleIds = remember(visibleItems) { visibleItems.map { it.id }.toSet() }
    val allSelected = visibleItems.isNotEmpty() && selectedIds.containsAll(visibleItems.map { it.id })
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(bottom = FileManagerListBottomPadding)
    ) {
        CleanXSegmentedTabs(
            items = tabs.map { tab ->
                CleanXTabItem(
                    title = localizedFileManagerTabTitle(tab.title),
                )
            },
            selectedIndex = selectedTabIndex,
            onSelected = onTabSelected,
            horizontalSpacing = 20.dp,
            horizontalPadding = 10.dp,
            verticalPadding = 8.dp,
            fontSize = 18.sp,
        )

        Spacer(modifier = Modifier.height(20.dp))

        FileManagerSelectAllAction(
            selected = allSelected,
            onClick = { onToggleVisibleItems(visibleIds) }
        )

        Spacer(modifier = Modifier.height(20.dp))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = FileManagerCardColor,
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                visibleItems.chunked(3).forEach { rowItems ->
                    Row(horizontalArrangement = Arrangement.spacedBy(3.5.dp)) {
                        rowItems.forEach { item ->
                            MediaGridTile(
                                item = item,
                                selected = item.id in selectedIds,
                                onOpen = { onOpenDetail(item) },
                                onToggleSelection = { onSelect(item.id) },
                                showPlayBadge = showPlayBadge,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(114.dp)
                            )
                        }
                        repeat(3 - rowItems.size) {
                            Spacer(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(114.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun FileManagerListView(
    tabs: List<FileManagerTabDisplayItem>,
    items: List<FileListDisplayItem>,
    selectedTabIndex: Int,
    selectedIds: Set<Int>,
    allSelected: Boolean,
    scrollState: ScrollState,
    style: FileListDisplayStyle,
    onTabSelected: (Int) -> Unit,
    onToggleAll: () -> Unit,
    onSelect: (Int) -> Unit,
    onOpenDetail: (FileListDisplayItem) -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(bottom = FileManagerListBottomPadding)
    ) {
        CleanXSegmentedTabs(
            items = tabs.map { tab ->
                CleanXTabItem(
                    title = localizedFileManagerTabTitle(tab.title),
                )
            },
            selectedIndex = selectedTabIndex,
            onSelected = onTabSelected,
            horizontalSpacing = 20.dp,
            horizontalPadding = 10.dp,
            verticalPadding = 8.dp,
            fontSize = 18.sp,
        )

        Spacer(modifier = Modifier.height(20.dp))

        FileManagerSelectAllAction(
            selected = allSelected,
            onClick = onToggleAll,
        )

        Spacer(modifier = Modifier.height(20.dp))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = FileManagerCardColor,
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 20.dp)) {
                FileManagerListCardHeader(
                    count = items.size,
                    selected = allSelected,
                    onToggleAll = onToggleAll,
                )

                Spacer(modifier = Modifier.height(20.dp))
                FileManagerDivider()
                Spacer(modifier = Modifier.height(16.dp))

                items.forEachIndexed { index, item ->
                    FileManagerListRow(
                        item = item,
                        selected = item.id in selectedIds,
                        style = style,
                        onOpen = { onOpenDetail(item) },
                        onToggleSelection = { onSelect(item.id) }
                    )
                    if (index != items.lastIndex) {
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
internal fun FileManagerAudioListView(
    tabs: List<FileManagerTabDisplayItem>,
    items: List<FileImageDisplayItem>,
    selectedIds: Set<Int>,
    scrollState: ScrollState,
    onToggleVisibleItems: (Set<Int>) -> Unit,
    onSelect: (Int) -> Unit,
    onOpenDetail: (FileImageDisplayItem) -> Unit,
    selectedTabIndex: Int = 0,
    onTabSelected: (Int) -> Unit = {}
) {
    val visibleItems = remember(items, selectedTabIndex) {
        filterMediaGridItems(tabs.getOrNull(selectedTabIndex)?.title.orEmpty(), items)
    }
    val visibleIds = remember(visibleItems) { visibleItems.map { it.id }.toSet() }
    val allSelected = visibleItems.isNotEmpty() && selectedIds.containsAll(visibleIds)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(bottom = FileManagerListBottomPadding)
    ) {
        CleanXSegmentedTabs(
            items = tabs.map { tab ->
                CleanXTabItem(title = localizedFileManagerTabTitle(tab.title))
            },
            selectedIndex = selectedTabIndex,
            onSelected = onTabSelected,
            horizontalSpacing = 20.dp,
            horizontalPadding = 10.dp,
            verticalPadding = 8.dp,
            fontSize = 18.sp,
        )

        Spacer(modifier = Modifier.height(20.dp))
        FileManagerSelectAllAction(
            selected = allSelected,
            onClick = { onToggleVisibleItems(visibleIds) },
        )
        Spacer(modifier = Modifier.height(20.dp))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = FileManagerCardColor,
            shape = RoundedCornerShape(12.dp),
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 20.dp)) {
                FileManagerListCardHeader(
                    count = visibleItems.size,
                    selected = allSelected,
                    onToggleAll = { onToggleVisibleItems(visibleIds) },
                )

                Spacer(modifier = Modifier.height(20.dp))
                FileManagerDivider()
                Spacer(modifier = Modifier.height(16.dp))

                visibleItems.forEachIndexed { index, item ->
                    FileManagerMediaFileRow(
                        item = item,
                        selected = item.id in selectedIds,
                        iconResId = R.drawable.ic_audio_yellow,
                        onOpen = { onOpenDetail(item) },
                        onToggleSelection = { onSelect(item.id) },
                    )
                    if (index != visibleItems.lastIndex) {
                        Spacer(modifier = Modifier.height(16.dp))
                        FileManagerDivider()
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

internal fun filterMediaGridItems(tabTitle: String, items: List<FileImageDisplayItem>): List<FileImageDisplayItem> {
    fun FileImageDisplayItem.matchesFolder(name: String): Boolean =
        bucketName?.contains(name, ignoreCase = true) == true ||
            path?.contains("/$name/", ignoreCase = true) == true

    return when (tabTitle) {
        "DCIM" -> items.filter { it.matchesFolder("DCIM") || it.matchesFolder("Camera") }
        "Download" -> items.filter { it.matchesFolder("Download") }
        "Music" -> items.filter { it.matchesFolder("Music") || it.matchesFolder("Audio") }.ifEmpty { items }
        "Other" -> items.filterNot { it.matchesFolder("DCIM") || it.matchesFolder("Camera") || it.matchesFolder("Download") }
        else -> items
    }
}

internal fun filterFileManagerListItems(tabTitle: String, items: List<FileListDisplayItem>): List<FileListDisplayItem> {
    fun FileListDisplayItem.matchesFolder(name: String): Boolean =
        bucketName?.contains(name, ignoreCase = true) == true ||
            path?.contains("/$name/", ignoreCase = true) == true

    return when (tabTitle) {
        "Download" -> items.filter { it.matchesFolder("Download") }
        "Other" -> items.filterNot { it.matchesFolder("Download") }
        else -> items
    }
}

@Composable
internal fun FileManagerListRow(
    item: FileListDisplayItem,
    selected: Boolean,
    style: FileListDisplayStyle,
    onOpen: () -> Unit,
    onToggleSelection: () -> Unit
) {
    val isDocumentsStyle = style == FileListDisplayStyle.Documents
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clickable { onOpen() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isDocumentsStyle) {
            Image(
                painter = painterResource(id = R.drawable.ic_file_blue),
                contentDescription = null,
                modifier = Modifier.size(44.dp)
            )
        } else {
            Image(
                painter = painterResource(id = R.drawable.ic_videos),
                contentDescription = null,
                modifier = Modifier.size(44.dp)
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = item.name,
                color = FileManagerNavy,
                fontSize = 20.sp,
                lineHeight = 24.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = item.meta,
                color = FileManagerNavy,
                fontSize = 16.sp,
                lineHeight = 19.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        SelectionCircle(
            selected = selected,
            modifier = Modifier.clickable { onToggleSelection() }
        )
    }
}

@Composable
internal fun FileManagerItemTypeIcon(
    kind: FileListIconKind,
    modifier: Modifier = Modifier
) {
    val colors = when (kind) {
        FileListIconKind.LargeVideo -> listOf(Color(0xFFD92BFF), Color(0xFF921CF0))
        FileListIconKind.Document -> listOf(Color(0xFFFF943F), Color(0xFFFF7A21))
    }
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(5.dp))
            .background(Brush.verticalGradient(colors)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = when (kind) {
                FileListIconKind.LargeVideo -> Icons.Default.PlayArrow
                FileListIconKind.Document -> Icons.AutoMirrored.Filled.InsertDriveFile
            },
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(22.dp)
        )
    }
}

@Composable
private fun FileManagerListCardHeader(
    count: Int,
    selected: Boolean,
    onToggleAll: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = count.toString(),
            color = FileManagerNavy,
            fontSize = 20.sp,
            lineHeight = 24.sp,
            fontWeight = FontWeight.Medium,
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = stringResource(R.string.file_items),
            color = FileManagerMutedNavy,
            fontSize = 16.sp,
            lineHeight = 24.sp,
        )
        Spacer(modifier = Modifier.weight(1f))
        FileManagerSelectAllAction(
            selected = selected,
            onClick = onToggleAll,
            compact = true,
        )
    }
}

@Composable
internal fun FileManagerDivider(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(FileManagerDividerColor)
    )
}

@Composable
private fun MediaGridTile(
    item: FileImageDisplayItem,
    selected: Boolean,
    onOpen: () -> Unit,
    onToggleSelection: () -> Unit,
    showPlayBadge: Boolean,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Brush.linearGradient(item.colors))
            .clickable { onOpen() }
    ) {
        FileManagerRealImage(item = item)
        if (showPlayBadge) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(31.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color.White.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
        SelectionCircle(
            selected = selected,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(4.dp)
                .clickable { onToggleSelection() },
        )
    }
}

@Composable
private fun FileManagerMediaFileRow(
    item: FileImageDisplayItem,
    selected: Boolean,
    iconResId: Int,
    onOpen: () -> Unit,
    onToggleSelection: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clickable { onOpen() },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(id = iconResId),
            contentDescription = null,
            modifier = Modifier.size(44.dp),
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = item.name.ifBlank { item.sizeLabel },
                color = FileManagerNavy,
                fontSize = 20.sp,
                lineHeight = 24.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = item.meta.ifBlank { item.sizeLabel },
                color = FileManagerNavy,
                fontSize = 16.sp,
                lineHeight = 19.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        SelectionCircle(
            selected = selected,
            modifier = Modifier.clickable { onToggleSelection() },
        )
    }
}

