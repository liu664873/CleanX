package com.quickcleanpro.phonecleaner.presentation.screen.files.common.views.detail

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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quickcleanpro.phonecleaner.R
import com.quickcleanpro.phonecleaner.presentation.common.components.CleanXBackground
import com.quickcleanpro.phonecleaner.presentation.common.components.CleanXText
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.FileDetailDisplayItem
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.FileDetailDisplayPreview
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.components.FileManagerItemTypeIcon
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.components.SelectionCircle
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.views.list.FileManagerImageQuality
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.views.list.FileManagerRealImage
import com.quickcleanpro.phonecleaner.utils.FileSizeFormatter

private val FileManagerDetailSelectionBarHeight = 48.dp
private val FileManagerDetailSelectionBarInset = 16.dp
private val DetailContentBottomPadding = FileManagerDetailSelectionBarHeight + 32.dp

@Composable
internal fun FileManagerDetailView(
    items: List<FileDetailDisplayItem>,
    initialIndex: Int,
    selectedIds: Set<Int>,
    selectedSize: Long,
    onToggleSelection: (Int) -> Unit,
) {
    if (items.isEmpty()) return

    val pagerState = rememberPagerState(
        initialPage = initialIndex.coerceIn(0, items.lastIndex),
        pageCount = { items.size },
    )
    val currentPage = pagerState.currentPage.coerceIn(0, items.lastIndex)
    val currentItem = items[currentPage]
    val selected = currentItem.id in selectedIds

    LaunchedEffect(items.size) {
        if (pagerState.currentPage > items.lastIndex) {
            pagerState.scrollToPage(items.lastIndex)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CleanXBackground),
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = DetailContentBottomPadding),
        ) { page ->
            FileManagerDetailPage(item = items[page])
        }

        FileManagerDetailSelectionBar(
            selectedSize = selectedSize,
            selected = selected,
            onToggleSelection = { onToggleSelection(currentItem.id) },
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth(),
        )
    }
}

@Composable
private fun FileManagerDetailPage(item: FileDetailDisplayItem) {
    when (val preview = item.preview) {
        is FileDetailDisplayPreview.MediaPreview -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.linearGradient(preview.item.colors)),
                contentAlignment = Alignment.Center,
            ) {
                FileManagerRealImage(
                    item = preview.item,
                    contentScale = ContentScale.Fit,
                    quality = FileManagerImageQuality.Detail,
                )
            }
        }
        is FileDetailDisplayPreview.FileIconPreview -> {
            FileManagerIconDetailPage(
                item = item,
                preview = preview,
            )
        }
    }
}

@Composable
private fun FileManagerIconDetailPage(
    item: FileDetailDisplayItem,
    preview: FileDetailDisplayPreview.FileIconPreview,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        FileManagerItemTypeIcon(kind = preview.kind, modifier = Modifier.size(96.dp))
        Text(
            text = item.name,
            color = CleanXText,
            fontSize = 20.sp,
            lineHeight = 24.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 24.dp),
        )
        Text(
            text = item.meta,
            color = Color(0xFF7D8EA8),
            fontSize = 15.sp,
            lineHeight = 19.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp),
        )
    }
}

@Composable
private fun FileManagerDetailSelectionBar(
    selectedSize: Long,
    selected: Boolean,
    onToggleSelection: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .padding(
                horizontal = FileManagerDetailSelectionBarInset,
                vertical = FileManagerDetailSelectionBarInset,
            )
            .fillMaxWidth()
            .height(FileManagerDetailSelectionBarHeight)
            .background(
                color = Color(0xFFEAF3FA),
                shape = RoundedCornerShape(10.dp),
            )
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.file_selected_size, FileSizeFormatter.format(selectedSize)),
            color = CleanXText,
            fontSize = 15.sp,
            lineHeight = 18.sp,
        )
        Spacer(modifier = Modifier.weight(1f))
        Row(
            modifier = Modifier.clickable { onToggleSelection() },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.file_select),
                color = CleanXText,
                fontSize = 15.sp,
                lineHeight = 18.sp,
            )
            SelectionCircle(
                selected = selected,
                modifier = Modifier.padding(start = 8.dp),
            )
        }
    }
}
