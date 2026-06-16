package com.quickcleanpro.phonecleaner.presentation.common.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 通用页面骨架，包含：
 * - 渐变背景
 * - 系统栏内边距
 * - 顶部应用栏（CleanXTopAppBar）
 * - 可滚动的内容区域
 *
 * @param title 顶栏标题
 * @param modifier 外部修饰符
 * @param titleFontSize 标题字体大小
 * @param fontWeight 标题字重
 * @param showBack 是否显示返回按钮
 * @param actions 顶栏右侧操作图标
 * @param backgroundBrush 页面背景渐变（默认浅蓝渐变）
 * @param contentPadding 内容区内边距（默认水平16dp，顶部16dp，底部32dp）
 * @param content 内容 Composable
 */
@Composable
fun CleanXPage(
    title: String,
    modifier: Modifier = Modifier,
    titleFontSize: TextUnit = 22.sp,
    fontWeight: FontWeight = FontWeight.SemiBold,
    showBack: Boolean = true,
    actions: @Composable (RowScope.() -> Unit)? = null,
    backgroundBrush: Brush = Brush.linearGradient(
        colors = listOf(Color(0xFFE3ECFD), Color(0xFFDFEBF5))
    ),
    contentPadding: PaddingValues = PaddingValues(
        start = 16.dp,
        end = 16.dp,
        top = 16.dp,
        bottom = 32.dp
    ),
    content: @Composable () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .systemBarsPadding()
            .background(brush = backgroundBrush)
    ) {
        // 顶栏
        CleanXTopAppBar(
            title = title,
            titleFontSize = titleFontSize,
            fontWeight = fontWeight,
            showBack = showBack,
            actions = actions
        )

        // 可滚动内容区
        Box(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(contentPadding)
        ) {
            content()
        }
    }
}