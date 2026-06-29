package com.quickcleanpro.phonecleaner.presentation.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class CleanXTabItem(
    val title: String,
    val value: String? = null
)

@Composable
fun CleanXPrimaryTabs(
    items: List<CleanXTabItem>,
    selectedIndex: Int,
    onSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = CleanXCardColor.copy(alpha = 0.92f)
) {
    if (items.isEmpty()) return

    val safeSelectedIndex = selectedIndex.coerceIn(0, items.lastIndex)

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = containerColor,
        shape = CleanXTileShape
    ) {
        TabRow(
            selectedTabIndex = safeSelectedIndex,
            modifier = Modifier.fillMaxWidth(),
            containerColor = Color.Transparent,
            contentColor = CleanXText,
            indicator = {}
        ) {
            items.forEachIndexed { index, item ->
                val selected = index == safeSelectedIndex
                Tab(
                    selected = selected,
                    onClick = { onSelected(index) },
                    selectedContentColor = CleanXText,
                    unselectedContentColor = CleanXMutedText,
                    text = {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = item.title,
                                fontSize = if (selected) CleanXTextBody else CleanXTextCaption,
                                lineHeight = if (selected) CleanXLineBody else CleanXLineCaption,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            item.value?.let { value ->
                                Spacer(modifier = Modifier.height(CleanXCompactPadding / 2))
                                Text(
                                    text = value,
                                    fontSize = if (selected) 13.sp else CleanXTextTiny,
                                    lineHeight = if (selected) CleanXTextBody else CleanXTextBodySmall,
                                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                                    textAlign = TextAlign.Center,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                )
            }
        }
    }
}

fun Modifier.cleanXTabContentSwipe(
    selectedIndex: Int,
    itemCount: Int,
    onSelected: (Int) -> Unit,
    swipeThreshold: Dp = 48.dp
): Modifier = composed {
    if (itemCount <= 1) {
        this
    } else {
        val safeSelectedIndex = selectedIndex.coerceIn(0, itemCount - 1)
        val swipeThresholdPx = with(LocalDensity.current) { swipeThreshold.toPx() }

        pointerInput(safeSelectedIndex, itemCount, swipeThresholdPx) {
            var dragDistance = 0f
            detectHorizontalDragGestures(
                onDragStart = { dragDistance = 0f },
                onDragEnd = { dragDistance = 0f },
                onDragCancel = { dragDistance = 0f },
                onHorizontalDrag = { _, dragAmount ->
                    dragDistance += dragAmount
                    when {
                        dragDistance <= -swipeThresholdPx -> {
                            val nextIndex = (safeSelectedIndex + 1).coerceAtMost(itemCount - 1)
                            if (nextIndex != safeSelectedIndex) {
                                onSelected(nextIndex)
                            }
                            dragDistance = 0f
                        }
                        dragDistance >= swipeThresholdPx -> {
                            val previousIndex = (safeSelectedIndex - 1).coerceAtLeast(0)
                            if (previousIndex != safeSelectedIndex) {
                                onSelected(previousIndex)
                            }
                            dragDistance = 0f
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun CleanXSegmentTabs(
    tabs: List<String>,
    selectedIndex: Int,
    onSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = CleanXCardColor.copy(alpha = 0.86f)
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = containerColor,
        shape = CleanXPillShape
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(CleanXCompactPadding / 2),
            verticalAlignment = Alignment.CenterVertically
        ) {
            tabs.forEachIndexed { index, title ->
                val selected = selectedIndex == index
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(CleanXCompactButtonHeight - 2.dp)
                        .clip(CleanXPillShape)
                        .background(if (selected) CleanXBlue else Color.Transparent)
                        .clickable { onSelected(index) }
                        .padding(horizontal = CleanXCompactPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title,
                        color = if (selected) Color.White else CleanXMutedText,
                        fontSize = CleanXTextCaption,
                        lineHeight = CleanXLineCaption,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        maxLines = 1
                    )
                }
            }
        }
    }
}
