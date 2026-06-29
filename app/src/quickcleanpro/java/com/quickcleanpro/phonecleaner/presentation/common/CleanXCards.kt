package com.quickcleanpro.phonecleaner.presentation.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class CleanXTileStyle(
    val background: Color = CleanXCardColor,
    val iconSize: Dp = 42.dp,
    val minHeight: Dp = 94.dp,
    val aspectRatio: Float = CleanXTileAspectRatio
)

@Composable
fun CleanXCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    containerColor: Color = CleanXCardColor.copy(alpha = 0.86f),
    content: @Composable ColumnScope.() -> Unit
) {
    val clickModifier = if (onClick != null) {
        Modifier.clickable { onClick() }
    } else {
        Modifier
    }

    Surface(
        modifier = modifier.then(clickModifier),
        shape = CleanXCardShape,
        color = containerColor
    ) {
        Column(
            modifier = Modifier.padding(contentPadding),
            content = content
        )
    }
}

@Composable
fun CleanXInfoPanel(
    modifier: Modifier = Modifier,
    background: Color,
    contentPadding: PaddingValues = PaddingValues(CleanXPanelPadding),
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier,
        color = background,
        shape = CleanXCardShape
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding),
            content = content
        )
    }
}

@Composable
fun CleanXIconTile(
    title: String,
    icon: Painter,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: CleanXTileStyle = CleanXTileStyle()
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = style.minHeight)
            .aspectRatio(style.aspectRatio)
            .clickable { onClick() },
        color = style.background,
        shape = CleanXTileShape
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = CleanXSmallPadding, vertical = CleanXItemSpacing),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = icon,
                contentDescription = title,
                modifier = Modifier.size(style.iconSize)
            )
            Spacer(modifier = Modifier.size(CleanXCompactPadding))
            Text(
                text = title,
                color = CleanXText,
                fontSize = CleanXTextBodySmall,
                lineHeight = CleanXLineBodySmall,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                maxLines = 2
            )
        }
    }
}

@Composable
fun CleanXSectionTitle(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        color = CleanXText,
        fontSize = CleanXTextTitle,
        lineHeight = CleanXLineSection,
        fontWeight = FontWeight.SemiBold,
        modifier = modifier
    )
}
