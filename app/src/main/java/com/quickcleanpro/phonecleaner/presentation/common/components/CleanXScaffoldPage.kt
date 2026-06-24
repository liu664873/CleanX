package com.quickcleanpro.phonecleaner.presentation.common.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 通用页面骨架。
 */
@Composable
fun CleanXScaffoldPage(
    title: String,
    modifier: Modifier = Modifier,
    titleFontSize: TextUnit = 22.sp,
    fontWeight: FontWeight = FontWeight.SemiBold,
    showBack: Boolean = true,
    onBack: (() -> Unit)? = null,
    actions: @Composable (RowScope.() -> Unit)? = null,
    bottomBar: @Composable () -> Unit = {},
    backgroundBrush: Brush = Brush.linearGradient(
        colors = listOf(Color(0xFFE3ECFD), Color(0xFFDFEBF5)),
    ),
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
    scrollEnabled: Boolean = true,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable ColumnScope.() -> Unit,
) {
    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(brush = backgroundBrush),
        containerColor = Color.Transparent,
        topBar = {
            CleanXTopAppBar(
                title = title,
                titleFontSize = titleFontSize,
                fontWeight = fontWeight,
                showBack = showBack,
                onBack = onBack,
                actions = actions,
            )
        },
        bottomBar = bottomBar,
    ) { paddingValues ->
        val contentModifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(contentPadding)
            .then(
                if (scrollEnabled) {
                    Modifier.verticalScroll(rememberScrollState())
                } else {
                    Modifier
                },
            )

        val pageContent: @Composable () -> Unit = {
            Column(
                modifier = contentModifier,
                horizontalAlignment = horizontalAlignment,
                verticalArrangement = verticalArrangement,
            ) {
                content()
            }
        }

        pageContent()
    }
}
