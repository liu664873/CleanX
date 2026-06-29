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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quickcleanpro.phonecleaner.R
import com.quickcleanpro.phonecleaner.presentation.common.CleanXBackground
import com.quickcleanpro.phonecleaner.presentation.common.CleanXBlue
import com.quickcleanpro.phonecleaner.presentation.common.CleanXHeader
import com.quickcleanpro.phonecleaner.presentation.common.CleanXMutedText
import com.quickcleanpro.phonecleaner.presentation.common.CleanXPrimaryTabs
import com.quickcleanpro.phonecleaner.presentation.common.CleanXTabItem
import com.quickcleanpro.phonecleaner.presentation.common.CleanXText
import com.quickcleanpro.phonecleaner.utils.FileSizeFormatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
internal fun FileCollectionHeader(
    title: String,
    onBack: () -> Unit,
    selectionAction: String?,
    onSelectionAction: () -> Unit
) {
    FileCollectionTopBar(title = title, onBack = onBack)
    FileCollectionSelectionAction(
        selectionAction = selectionAction,
        onSelectionAction = onSelectionAction
    )
}

@Composable
internal fun FileCollectionTopBar(
    title: String,
    onBack: () -> Unit,
    actionText: String? = null,
    actionEnabled: Boolean = true,
    onAction: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(CleanXBackground)
            .padding(horizontal = 16.dp)
    ) {
        CleanXHeader(
            title = title,
            onBack = onBack,
            actions = {
                if (actionText != null) {
                    Text(
                        text = actionText,
                        color = CleanXBlue.copy(alpha = if (actionEnabled) 1f else 0.45f),
                        fontSize = 16.sp,
                        modifier = Modifier.clickable(enabled = actionEnabled) { onAction() }
                    )
                }
            }
        )
    }
}

@Composable
internal fun FileCollectionSelectionAction(
    selectionAction: String?,
    onSelectionAction: () -> Unit
) {
    if (selectionAction == null) return

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(36.dp),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = selectionAction,
            color = CleanXBlue,
            fontSize = 16.sp,
            modifier = Modifier.clickable { onSelectionAction() }
        )
    }
}

@Composable
internal fun FileCollectionBrowserContent(
    config: FileCollectionConfig,
    selectedIds: Set<Int>,
    allSelected: Boolean,
    onToggleAll: () -> Unit,
    onToggleIds: (Set<Int>) -> Unit,
    onToggleGroup: (PhotoGroup) -> Unit,
    onSelect: (Int) -> Unit,
    onOpenDetail: (PhotoItem) -> Unit = {},
    scrollState: ScrollState,
    selectedMediaTabIndex: Int = 0,
    onMediaTabSelected: (Int) -> Unit = {}
) {
    when (config.layout) {
        CollectionLayout.Screenshots -> ScreenshotGridContent(
            items = config.items,
            allSelected = allSelected,
            selectedIds = selectedIds,
            scrollState = scrollState,
            onToggleAll = onToggleAll,
            onSelect = onSelect,
            onOpenDetail = onOpenDetail
        )
        CollectionLayout.MediaGrid -> MediaGridContent(
            tabs = config.tabs,
            items = config.items,
            selectedIds = selectedIds,
            scrollState = scrollState,
            onToggleVisibleItems = onToggleIds,
            onSelect = onSelect,
            onOpenDetail = onOpenDetail,
            selectedTabIndex = selectedMediaTabIndex,
            onTabSelected = onMediaTabSelected
        )
        CollectionLayout.SimilarPhotos -> SimilarPhotosContent(
            groups = config.groups,
            selectedIds = selectedIds,
            scrollState = scrollState,
            onToggleGroup = onToggleGroup,
            onSelect = onSelect,
            onOpenDetail = onOpenDetail
        )
        CollectionLayout.PhotoPrivacy -> PhotoPrivacyContent(
            items = config.items,
            selectedIds = selectedIds,
            allSelected = allSelected,
            onToggleAll = onToggleAll,
            onSelect = onSelect
        )
    }
}

@Composable
internal fun MediaGridContent(
    tabs: List<ManagedFileTab>,
    items: List<PhotoItem>,
    selectedIds: Set<Int>,
    scrollState: ScrollState,
    onToggleVisibleItems: (Set<Int>) -> Unit,
    onSelect: (Int) -> Unit,
    onOpenDetail: (PhotoItem) -> Unit,
    selectedTabIndex: Int = 0,
    onTabSelected: (Int) -> Unit = {}
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
        CleanXPrimaryTabs(
            items = tabs.map { tab ->
                CleanXTabItem(
                    title = localizedFileTabTitle(tab.title),
                    value = tab.sizeLabel
                )
            },
            selectedIndex = selectedTabIndex,
            onSelected = onTabSelected,
            containerColor = Color.Transparent
        )

        Spacer(modifier = Modifier.height(18.dp))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            shape = RoundedCornerShape(10.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                GroupHeader(
                    leading = visibleItems.size.toString(),
                    trailing = stringResource(R.string.file_items),
                    selected = allSelected,
                    onClick = { onToggleVisibleItems(visibleIds) }
                )
                visibleItems.chunked(3).forEach { rowItems ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        rowItems.forEach { item ->
                            FileThumbnail(
                                item = item,
                                selected = item.id in selectedIds,
                                onOpen = { onOpenDetail(item) },
                                onToggleSelection = { onSelect(item.id) },
                                showPlayBadge = true,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        repeat(3 - rowItems.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun ManagedFileListContent(
    config: ManagedFileListConfig,
    items: List<ManagedFileUiItem>,
    selectedTabIndex: Int,
    selectedIds: Set<Int>,
    allSelected: Boolean,
    scrollState: ScrollState,
    onTabSelected: (Int) -> Unit,
    onToggleAll: () -> Unit,
    onSelect: (Int) -> Unit,
    onOpenDetail: (ManagedFileUiItem) -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(bottom = FileManagerListBottomPadding)
    ) {
        CleanXPrimaryTabs(
            items = config.tabs.map { tab ->
                CleanXTabItem(
                    title = localizedFileTabTitle(tab.title),
                    value = tab.sizeLabel
                )
            },
            selectedIndex = selectedTabIndex,
            onSelected = onTabSelected,
            containerColor = Color.Transparent
        )

        Spacer(modifier = Modifier.height(18.dp))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            shape = RoundedCornerShape(10.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(items.size.toString(), color = CleanXText, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(stringResource(R.string.file_items), color = CleanXMutedText, fontSize = 16.sp)
                    Spacer(modifier = Modifier.weight(1f))
                    Text(stringResource(R.string.file_select_all), color = CleanXText, fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    SelectionCircle(selected = allSelected, modifier = Modifier.clickable { onToggleAll() })
                }

                Spacer(modifier = Modifier.height(14.dp))

                items.forEachIndexed { index, item ->
                    ManagedFileRow(
                        item = item,
                        selected = item.id in selectedIds,
                        style = config.style,
                        onOpen = { onOpenDetail(item) },
                        onToggleSelection = { onSelect(item.id) }
                    )
                    if (index != items.lastIndex) {
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

internal fun filterMediaGridItems(tabTitle: String, items: List<PhotoItem>): List<PhotoItem> {
    fun PhotoItem.matchesFolder(name: String): Boolean =
        realFile?.bucketName?.contains(name, ignoreCase = true) == true ||
            realFile?.path?.contains("/$name/", ignoreCase = true) == true

    return when (tabTitle) {
        "DCIM" -> items.filter { it.matchesFolder("DCIM") || it.matchesFolder("Camera") }
        "Download" -> items.filter { it.matchesFolder("Download") }
        "Other" -> items.filterNot { it.matchesFolder("DCIM") || it.matchesFolder("Camera") || it.matchesFolder("Download") }
        else -> items
    }
}

internal fun filterManagedFileUiItems(tabTitle: String, items: List<ManagedFileUiItem>): List<ManagedFileUiItem> {
    fun ManagedFileUiItem.matchesFolder(name: String): Boolean =
        realFile?.bucketName?.contains(name, ignoreCase = true) == true ||
            realFile?.path?.contains("/$name/", ignoreCase = true) == true

    return when (tabTitle) {
        "Download" -> items.filter { it.matchesFolder("Download") }
        "Other" -> items.filterNot { it.matchesFolder("Download") }
        else -> items
    }
}

@Composable
internal fun ManagedFileRow(
    item: ManagedFileUiItem,
    selected: Boolean,
    style: ManagedFileListStyle,
    onOpen: () -> Unit,
    onToggleSelection: () -> Unit
) {
    val isDocumentsStyle = style == ManagedFileListStyle.Documents
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .clickable { onOpen() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isDocumentsStyle) {
            Image(
                painter = painterResource(id = R.drawable.ic_file_yellow),
                contentDescription = null,
                modifier = Modifier.size(34.dp)
            )
        } else {
            FileTypeIcon(kind = item.kind, modifier = Modifier.size(34.dp))
        }
        Spacer(modifier = Modifier.width(if (isDocumentsStyle) 14.dp else 12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.name,
                color = CleanXText,
                fontSize = if (isDocumentsStyle) 17.sp else 18.sp,
                lineHeight = if (isDocumentsStyle) 21.sp else 22.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = item.meta,
                color = if (isDocumentsStyle) Color(0xFF7F91AA) else CleanXMutedText,
                fontSize = 14.sp,
                lineHeight = 17.sp
            )
        }
        SelectionCircle(
            selected = selected,
            modifier = Modifier.clickable { onToggleSelection() }
        )
    }
}

@Composable
internal fun localizedFileTabTitle(title: String): String =
    when (title) {
        "All" -> stringResource(R.string.file_all)
        "Photo" -> stringResource(R.string.file_photo)
        "Pictures" -> stringResource(R.string.file_pictures)
        "Download" -> stringResource(R.string.file_download)
        "Other" -> stringResource(R.string.file_other)
        else -> title
    }

@Composable
internal fun FileTypeIcon(
    kind: ManagedFileKind,
    modifier: Modifier = Modifier
) {
    val colors = when (kind) {
        ManagedFileKind.LargeVideo -> listOf(Color(0xFFD92BFF), Color(0xFF921CF0))
        ManagedFileKind.Document -> listOf(Color(0xFFFF943F), Color(0xFFFF7A21))
    }
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(5.dp))
            .background(Brush.verticalGradient(colors)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = when (kind) {
                ManagedFileKind.LargeVideo -> Icons.Default.PlayArrow
                ManagedFileKind.Document -> Icons.AutoMirrored.Filled.InsertDriveFile
            },
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(22.dp)
        )
    }
}

