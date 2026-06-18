package com.quickcleanpro.phonecleaner.presentation.screen.files.common.views.list

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.LruCache
import android.util.Size as AndroidSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quickcleanpro.phonecleaner.R
import com.quickcleanpro.phonecleaner.presentation.common.components.CleanXBlue
import com.quickcleanpro.phonecleaner.presentation.common.components.CleanXMutedText
import com.quickcleanpro.phonecleaner.presentation.common.components.CleanXSegmentedTabs
import com.quickcleanpro.phonecleaner.presentation.common.components.CleanXTabItem
import com.quickcleanpro.phonecleaner.presentation.common.components.CleanXText
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.FileManagerMediaGroup
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.FileManagerMediaItem
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.FileManagerMediaTab
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.components.FileManagerListBottomPadding
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.components.FileManagerCardColor
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.components.FileManagerMutedNavy
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.components.FileManagerNavy
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.components.FileManagerSelectAllAction
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.components.SelectionCircle
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.components.localizedFileManagerTabTitle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.ceil
import kotlin.math.max

private const val FileManagerGridThumbnailPx = 512
private const val FileManagerMaxDetailImagePx = 2048
private const val FileManagerBitmapCacheKb = 24 * 1024

internal enum class FileManagerImageQuality {
    Grid,
    Detail
}

private data class FileManagerBitmapCacheKey(
    val uri: String,
    val quality: FileManagerImageQuality,
    val targetSizePx: Int
)

private val FileManagerBitmapCache = object : LruCache<FileManagerBitmapCacheKey, Bitmap>(FileManagerBitmapCacheKb) {
    override fun sizeOf(key: FileManagerBitmapCacheKey, value: Bitmap): Int =
        value.byteCount / 1024
}

@Composable
internal fun FileManagerScreenshotGridView(
    items: List<FileManagerMediaItem>,
    allSelected: Boolean,
    selectedIds: Set<Int>,
    scrollState: ScrollState,
    onToggleAll: () -> Unit,
    onSelect: (Int) -> Unit,
    onOpenDetail: (FileManagerMediaItem) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(bottom = FileManagerListBottomPadding)
    ) {
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
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items.chunked(3).forEach { rowItems ->
                    Row(horizontalArrangement = Arrangement.spacedBy(3.5.dp)) {
                        rowItems.forEach { item ->
                            FileManagerThumbnail(
                                item = item,
                                selected = item.id in selectedIds,
                                onOpen = { onOpenDetail(item) },
                                onToggleSelection = { onSelect(item.id) },
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
internal fun FileManagerSimilarPhotosView(
    groups: List<FileManagerMediaGroup>,
    selectedIds: Set<Int>,
    scrollState: ScrollState,
    onToggleGroup: (FileManagerMediaGroup) -> Unit,
    onSelect: (Int) -> Unit,
    onOpenDetail: (FileManagerMediaItem) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding( bottom = FileManagerListBottomPadding)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            shape = RoundedCornerShape(10.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                groups.forEach { group ->
                    val groupIds = group.items.map { it.id }.toSet()
                    val groupSelected = selectedIds.containsAll(groupIds)
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        FileManagerGroupHeader(
                            leading = group.count.toString(),
                            trailing = stringResource(R.string.file_similar),
                            selected = groupSelected,
                            onClick = { onToggleGroup(group) }
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            group.items.chunked(3).forEach { rowItems ->
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    rowItems.forEach { item ->
                                        FileManagerThumbnail(
                                            item = item,
                                            selected = item.id in selectedIds,
                                            onOpen = { onOpenDetail(item) },
                                            onToggleSelection = { onSelect(item.id) },
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
        }
    }
}

@Composable
internal fun FileManagerPhotoPrivacyView(
    items: List<FileManagerMediaItem>,
    selectedIds: Set<Int>,
    allSelected: Boolean,
    onToggleAll: () -> Unit,
    onSelect: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = FileManagerListBottomPadding)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("${items.size}", color = FileManagerNavy, fontSize = 20.sp, lineHeight = 24.sp, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.width(4.dp))
            Text(stringResource(R.string.file_photos), color = FileManagerMutedNavy, fontSize = 16.sp, lineHeight = 24.sp)
            Spacer(modifier = Modifier.weight(1f))
            FileManagerSelectAllAction(
                selected = allSelected,
                onClick = onToggleAll,
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = FileManagerCardColor,
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = stringResource(R.string.file_photo_privacy_desc),
                color = FileManagerNavy,
                fontSize = 16.sp,
                lineHeight = 24.sp,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 20.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = FileManagerCardColor,
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(top = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                val firstDateSec = items.firstOrNull()?.realFile?.modifiedSeconds ?: 0L
                val dateLabel = if (firstDateSec > 0) {
                    SimpleDateFormat("yyyy-MM", Locale.US).format(Date(firstDateSec * 1000))
                } else {
                    stringResource(R.string.file_photos)
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(dateLabel, color = FileManagerNavy, fontSize = 20.sp, lineHeight = 24.sp, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.weight(1f))
                    FileManagerSelectAllAction(
                        selected = allSelected,
                        onClick = onToggleAll,
                        compact = true,
                    )
                }
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    items.chunked(3).forEach { rowItems ->
                        Row(horizontalArrangement = Arrangement.spacedBy(3.5.dp)) {
                            rowItems.forEach { item ->
                                FileManagerThumbnail(
                                    item = item,
                                    selected = item.id in selectedIds,
                                    onOpen = { onSelect(item.id) },
                                    onToggleSelection = { onSelect(item.id) },
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
}

@Composable
internal fun FileManagerGroupHeader(
    leading: String,
    trailing: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(leading, color = CleanXText, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.width(4.dp))
        Text(trailing, color = CleanXMutedText, fontSize = 16.sp)
        Spacer(modifier = Modifier.weight(1f))
        Text(stringResource(R.string.file_select_all), color = CleanXText, fontSize = 16.sp)
        Spacer(modifier = Modifier.width(8.dp))
        SelectionCircle(selected = selected, modifier = Modifier.clickable { onClick() })
    }
}

@Composable
internal fun FileManagerThumbnail(
    item: FileManagerMediaItem,
    selected: Boolean,
    onOpen: () -> Unit,
    onToggleSelection: () -> Unit,
    showPlayBadge: Boolean = false,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Brush.linearGradient(item.colors))
            .clickable { onOpen() }
    ) {
        FileManagerRealImage(item = item)
        if (item.bestPhoto) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .height(24.dp)
                    .background(Color.Black.copy(alpha = 0.32f)),
                contentAlignment = Alignment.Center
            ) {
                Text(stringResource(R.string.file_best_photo), color = Color.White, fontSize = 12.sp)
            }
        }
        if (showPlayBadge) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(31.dp)
            )
        }
        SelectionCircle(
            selected = selected,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp)
                .clickable { onToggleSelection() }
        )
    }
}

@Composable
internal fun FileManagerGalleryBrowserView(
    tabs: List<FileManagerMediaTab>,
    selectedTabIndex: Int,
    items: List<FileManagerMediaItem>,
    selectedIds: Set<Int>,
    scrollState: ScrollState,
    onTabSelected: (Int) -> Unit,
    onSelect: (Int) -> Unit,
    onSelectAll: () -> Unit,
    onOpenDetail: (FileManagerMediaItem) -> Unit
) {
    val allSelected = items.isNotEmpty() && selectedIds.containsAll(items.map { it.id })
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(bottom = FileManagerListBottomPadding),
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

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xFFF6F7FB),
            shape = RoundedCornerShape(12.dp),
        ) {
            Column(
                modifier = Modifier.padding(top = 20.dp, bottom = 0.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                val selectedTab = tabs.getOrNull(selectedTabIndex)
                val sizeLabel = selectedTab?.sizeLabel?.takeIf { it.isNotBlank() }
                val headerTitle =
                    if (selectedTabIndex == 0) {
                        stringResource(R.string.device_screen)
                    } else {
                        localizedFileManagerTabTitle(selectedTab?.title ?: stringResource(R.string.file_photo))
                    }
                FileManagerGalleryGroupHeader(
                    title = headerTitle,
                    sizeLabel = sizeLabel,
                    selected = allSelected,
                    onClick = onSelectAll,
                )
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    items.chunked(3).forEach { rowItems ->
                        Row(horizontalArrangement = Arrangement.spacedBy(3.5.dp)) {
                            rowItems.forEach { item ->
                                FileManagerGalleryTile(
                                    item = item,
                                    selected = item.id in selectedIds,
                                    onOpen = { onOpenDetail(item) },
                                    onToggleSelection = { onSelect(item.id) },
                                    modifier =
                                        Modifier
                                            .weight(1f)
                                            .height(114.dp),
                                )
                            }
                            repeat(3 - rowItems.size) {
                                Spacer(
                                    modifier =
                                        Modifier
                                            .weight(1f)
                                            .height(114.dp),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FileManagerGalleryGroupHeader(
    title: String,
    sizeLabel: String?,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            color = Color(0xFF1D2959),
            fontSize = 20.sp,
            lineHeight = 24.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            modifier = Modifier.widthIn(max = 190.dp),
        )
        if (sizeLabel != null) {
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "($sizeLabel)",
                color = Color(0xFF1D2959),
                fontSize = 16.sp,
                lineHeight = 20.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = stringResource(if (selected) R.string.file_unselect_all else R.string.file_select_all),
            color = CleanXBlue,
            fontSize = 16.sp,
            lineHeight = 19.sp,
            modifier = Modifier.clickable { onClick() },
        )
    }
}

@Composable
internal fun FileManagerGalleryTab(title: String, size: String?, selected: Boolean, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .width(68.dp)
            .clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = localizedFileManagerTabTitle(title),
            color = if (selected) CleanXText else CleanXMutedText,
            fontSize = 16.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
        Text(
            text = size ?: "",
            color = CleanXText,
            fontSize = 10.sp,
            lineHeight = 12.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .width(66.dp)
                .height(2.dp)
                .background(if (selected) CleanXBlue else Color.Transparent)
        )
    }
}

@Composable
internal fun FileManagerGalleryTile(
    item: FileManagerMediaItem,
    selected: Boolean,
    onOpen: () -> Unit,
    onToggleSelection: () -> Unit,
    modifier: Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Brush.linearGradient(item.colors))
            .clickable { onOpen() }
    ) {
        FileManagerRealImage(item = item)
        SelectionCircle(
            selected = selected,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(4.dp)
                .clickable { onToggleSelection() }
        )
    }
}

@Composable
internal fun FileManagerRealImage(
    item: FileManagerMediaItem,
    contentScale: ContentScale = ContentScale.Crop,
    quality: FileManagerImageQuality = FileManagerImageQuality.Grid
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val targetSizePx = remember(quality, configuration.screenWidthDp, configuration.screenHeightDp, density) {
        when (quality) {
            FileManagerImageQuality.Grid -> FileManagerGridThumbnailPx
            FileManagerImageQuality.Detail -> with(density) {
                max(configuration.screenWidthDp.dp.roundToPx(), configuration.screenHeightDp.dp.roundToPx())
                    .coerceAtLeast(FileManagerGridThumbnailPx)
                    .coerceAtMost(FileManagerMaxDetailImagePx)
            }
        }
    }
    val bitmap by produceState<Bitmap?>(initialValue = null, item.realFile?.uri, quality, targetSizePx) {
        val uri = item.realFile?.uri
        value = if (uri == null) {
            null
        } else {
            withContext(Dispatchers.IO) {
                loadFileManagerBitmap(context, uri, quality, targetSizePx)
            }
        }
    }

    val loadedBitmap = bitmap
    if (loadedBitmap != null) {
        Image(
            bitmap = loadedBitmap.asImageBitmap(),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = contentScale
        )
    } else {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = Color.White.copy(alpha = 0.18f),
                radius = size.minDimension * 0.48f,
                center = Offset(size.width * 0.68f, size.height * 0.34f)
            )
            drawRoundRect(
                color = Color.Black.copy(alpha = 0.12f),
                topLeft = Offset(0f, size.height * 0.72f),
                size = Size(size.width, size.height * 0.28f),
                cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
            )
        }
    }
}

private fun loadFileManagerBitmap(
    context: Context,
    uri: Uri,
    quality: FileManagerImageQuality,
    targetSizePx: Int
): Bitmap? {
    val cacheKey = FileManagerBitmapCacheKey(uri.toString(), quality, targetSizePx)
    FileManagerBitmapCache.get(cacheKey)?.let { return it }
    val bitmap = runCatching {
        when (quality) {
            FileManagerImageQuality.Grid -> loadFileManagerGridBitmap(context, uri, targetSizePx)
            FileManagerImageQuality.Detail -> loadFileManagerDetailBitmap(context, uri, targetSizePx)
        }
    }.getOrNull()
    if (bitmap != null) {
        FileManagerBitmapCache.put(cacheKey, bitmap)
    }
    return bitmap
}

private fun loadFileManagerGridBitmap(context: Context, uri: Uri, targetSizePx: Int): Bitmap {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        return context.contentResolver.loadThumbnail(uri, AndroidSize(targetSizePx, targetSizePx), null)
    }
    return decodeSampledBitmap(context, uri, targetSizePx)
        ?: run {
            @Suppress("DEPRECATION")
            MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
        }
}

private fun loadFileManagerDetailBitmap(context: Context, uri: Uri, targetSizePx: Int): Bitmap? {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        val source = ImageDecoder.createSource(context.contentResolver, uri)
        return ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
            val sourceSize = info.size
            val largestSide = max(sourceSize.width, sourceSize.height)
            if (largestSide > targetSizePx) {
                val scale = targetSizePx.toFloat() / largestSide.toFloat()
                decoder.setTargetSize(
                    (sourceSize.width * scale).toInt().coerceAtLeast(1),
                    (sourceSize.height * scale).toInt().coerceAtLeast(1)
                )
            }
            decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
        }
    }
    return decodeSampledBitmap(context, uri, targetSizePx)
}

private fun decodeSampledBitmap(context: Context, uri: Uri, targetSizePx: Int): Bitmap? {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    context.contentResolver.openInputStream(uri)?.use { stream ->
        BitmapFactory.decodeStream(stream, null, bounds)
    }
    if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

    val options = BitmapFactory.Options().apply {
        inSampleSize = calculateInSampleSize(bounds.outWidth, bounds.outHeight, targetSizePx)
        inPreferredConfig = Bitmap.Config.ARGB_8888
    }
    return context.contentResolver.openInputStream(uri)?.use { stream ->
        BitmapFactory.decodeStream(stream, null, options)
    }
}

private fun calculateInSampleSize(width: Int, height: Int, targetSizePx: Int): Int {
    val largestSide = max(width, height)
    if (largestSide <= targetSizePx) return 1
    val ratio = ceil(largestSide.toDouble() / targetSizePx.toDouble()).toInt()
    var sampleSize = 1
    while (sampleSize * 2 <= ratio) {
        sampleSize *= 2
    }
    return sampleSize
}

