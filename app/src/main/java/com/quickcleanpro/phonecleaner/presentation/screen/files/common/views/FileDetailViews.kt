package com.quickcleanpro.phonecleaner.presentation.screen.files.common.views

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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quickcleanpro.phonecleaner.R
import com.quickcleanpro.phonecleaner.presentation.common.CleanXBackground
import com.quickcleanpro.phonecleaner.presentation.common.CleanXBlue
import com.quickcleanpro.phonecleaner.presentation.common.CleanXText
import com.quickcleanpro.phonecleaner.presentation.common.cleanXDebouncedClick
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.components.SelectionCircle
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.components.FileTypeIcon
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.components.FileManagerNavy
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.components.FileManagerCardColor
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.PhotoItem
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.ManagedFileUiItem
import com.quickcleanpro.phonecleaner.utils.FileSizeFormatter

private val DetailSelectionBarHeight = 48.dp
private val DetailContentBottomPadding = 64.dp

@Composable
internal fun PhotoDetailScreen(
    items: List<PhotoItem>,
    initialIndex: Int,
    selectedIds: Set<Int>,
    selectedSize: Long,
    onBack: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null,
    onToggleSelection: (Int) -> Unit
) {
    if (items.isEmpty()) return

    val pagerState = rememberPagerState(
        initialPage = initialIndex.coerceIn(0, items.lastIndex),
        pageCount = { items.size }
    )
    val currentPage = pagerState.currentPage.coerceIn(0, items.lastIndex)
    val currentItem = items[currentPage]
    val selected = currentItem.id in selectedIds

    LaunchedEffect(items.size) {
        if (pagerState.currentPage > items.lastIndex) {
            pagerState.scrollToPage(items.lastIndex)
        }
    }

    val content: @Composable () -> Unit = {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
        ) { page ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.linearGradient(items[page].colors)),
                contentAlignment = Alignment.Center
            ) {
                RealPhotoImage(
                    item = items[page],
                    contentScale = ContentScale.Fit,
                    quality = PhotoImageQuality.Detail
                )
            }
        }
    }

    if (onBack != null && onDelete != null) {
        DetailChrome(
            currentPage = currentPage,
            totalPages = items.size,
            selectedCount = selectedIds.size,
            selectedSize = selectedSize,
            selected = selected,
            onBack = onBack,
            onDelete = onDelete,
            onToggleSelection = { onToggleSelection(currentItem.id) },
            content = content
        )
    } else {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(CleanXBackground)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = DetailContentBottomPadding)
            ) {
                content()
            }

            DetailSelectionBar(
                selectedSize = selectedSize,
                selected = selected,
                onToggleSelection = { onToggleSelection(currentItem.id) },
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
            )
        }
    }
}

@Composable
internal fun ManagedFileDetailScreen(
    items: List<ManagedFileUiItem>,
    initialIndex: Int,
    selectedIds: Set<Int>,
    selectedSize: Long,
    onToggleSelection: (Int) -> Unit
) {
    if (items.isEmpty()) return

    val pagerState = rememberPagerState(
        initialPage = initialIndex.coerceIn(0, items.lastIndex),
        pageCount = { items.size }
    )
    val currentPage = pagerState.currentPage.coerceIn(0, items.lastIndex)
    val currentItem = items[currentPage]
    val selected = currentItem.id in selectedIds

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CleanXBackground)
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = DetailContentBottomPadding)
        ) { page ->
            ManagedFileDetailPage(item = items[page])
        }

        DetailSelectionBar(
            selectedSize = selectedSize,
            selected = selected,
            onToggleSelection = { onToggleSelection(currentItem.id) },
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
        )
    }
}

@Composable
private fun DetailChrome(
    currentPage: Int,
    totalPages: Int,
    selectedCount: Int,
    selectedSize: Long,
    selected: Boolean,
    onBack: () -> Unit,
    onDelete: () -> Unit,
    onToggleSelection: () -> Unit,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CleanXBackground)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 96.dp, bottom = DetailContentBottomPadding)
        ) {
            content()
        }

        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .fillMaxWidth()
                .statusBarsPadding()
                .height(68.dp)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.cleanXDebouncedClick { onBack() },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.back),
                    tint = CleanXText,
                    modifier = Modifier.size(26.dp)
                )
                Text(
                    text = "${currentPage + 1}/$totalPages",
                    color = CleanXText,
                    fontSize = 18.sp,
                    lineHeight = 22.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = stringResource(R.string.file_delete_count, selectedCount),
                color = CleanXBlue,
                fontSize = 16.sp,
                modifier = Modifier.clickable(enabled = selectedCount > 0) {
                    onDelete()
                }
            )
        }

        DetailSelectionBar(
            selectedSize = selectedSize,
            selected = selected,
            onToggleSelection = onToggleSelection,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
        )
    }
}

@Composable
private fun DetailSelectionBar(
    selectedSize: Long,
    selected: Boolean,
    onToggleSelection: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(Color(0xFFEAF3FA))
            .navigationBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(DetailSelectionBarHeight)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.file_selected_size, FileSizeFormatter.format(selectedSize)),
                color = CleanXText,
                fontSize = 15.sp,
                lineHeight = 18.sp
            )
            Spacer(modifier = Modifier.weight(1f))
            Row(
                modifier = Modifier.clickable { onToggleSelection() },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.file_select),
                    color = CleanXText,
                    fontSize = 15.sp,
                    lineHeight = 18.sp
                )
                SelectionCircle(
                    selected = selected,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun ManagedFileDetailPage(item: ManagedFileUiItem) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        FileTypeIcon(kind = item.kind, modifier = Modifier.size(96.dp))
        Text(
            text = item.name,
            color = CleanXText,
            fontSize = 20.sp,
            lineHeight = 24.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 24.dp)
        )
        Text(
            text = item.meta,
            color = Color(0xFF7D8EA8),
            fontSize = 15.sp,
            lineHeight = 19.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

