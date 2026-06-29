package com.quickcleanpro.phonecleaner.presentation.common

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import com.quickcleanpro.phonecleaner.R

@Composable
fun CleanXScaffold(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = CleanXBackground,
    horizontalPadding: Dp = CleanXPagePadding,
    actions: @Composable RowScope.() -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        modifier = modifier,
        containerColor = containerColor,
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(containerColor)
                    .padding(horizontal = horizontalPadding)
            ) {
                CleanXHeader(title = title, onBack = onBack, actions = actions)
            }
        },
        bottomBar = bottomBar
    ) { paddingValues ->
        content(paddingValues)
    }
}

@Composable
fun CleanXScaffold(
    @StringRes titleRes: Int,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = CleanXBackground,
    horizontalPadding: Dp = CleanXPagePadding,
    actions: @Composable RowScope.() -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit
) {
    CleanXScaffold(
        title = stringResource(titleRes),
        onBack = onBack,
        modifier = modifier,
        containerColor = containerColor,
        horizontalPadding = horizontalPadding,
        actions = actions,
        bottomBar = bottomBar,
        content = content
    )
}

@Composable
fun CleanXHeader(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    actions: @Composable RowScope.() -> Unit = {}
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(top = CleanXHeaderTopPadding, bottom = CleanXHeaderBottomPadding),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(CleanXIconButtonSize)
                .clip(CleanXPillShape)
                .cleanXDebouncedClick { onBack() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.back),
                tint = CleanXText,
                modifier = Modifier.size(CleanXHeaderIconSize)
            )
        }
        Spacer(modifier = Modifier.size(CleanXCompactPadding / 2))
        Text(
            text = title,
            color = CleanXText,
            fontSize = CleanXTextTitle,
            lineHeight = CleanXLineTitle,
            fontWeight = FontWeight.W500,
            modifier = Modifier.weight(1f)
        )
        actions()
    }
}
