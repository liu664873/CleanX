package com.quickcleanpro.phonecleaner.presentation.common.components.styles

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quickcleanpro.phonecleaner.presentation.theme.LocalVariantTheme

// ============================================================================
//   现在所有尺寸/圆角都从 LocalVariantTheme 取，保证主题可切换
// ============================================================================

private val Dimens: com.quickcleanpro.phonecleaner.presentation.theme.VariantDimens
    @Composable @ReadOnlyComposable get() = LocalVariantTheme.current.dimens

private val Shapes: com.quickcleanpro.phonecleaner.presentation.theme.VariantShapes
    @Composable @ReadOnlyComposable get() = LocalVariantTheme.current.shapes

// ---------- Spacing ----------
val CleanXPagePadding: Dp @Composable @ReadOnlyComposable get() = Dimens.pagePadding
val CleanXContentPadding: Dp @Composable @ReadOnlyComposable get() = Dimens.contentPadding
val CleanXCompactPadding: Dp @Composable @ReadOnlyComposable get() = Dimens.compactPadding
val CleanXSmallPadding: Dp @Composable @ReadOnlyComposable get() = Dimens.smallPadding
val CleanXMediumPadding: Dp @Composable @ReadOnlyComposable get() = Dimens.mediumPadding
val CleanXLargePadding: Dp @Composable @ReadOnlyComposable get() = Dimens.largePadding
val CleanXGridSpacing: Dp @Composable @ReadOnlyComposable get() = Dimens.gridSpacing
val CleanXItemSpacing: Dp @Composable @ReadOnlyComposable get() = Dimens.itemSpacing
val CleanXSectionSpacing: Dp @Composable @ReadOnlyComposable get() = Dimens.sectionSpacing
val CleanXHeaderTopPadding: Dp @Composable @ReadOnlyComposable get() = Dimens.headerTopPadding
val CleanXHeaderBottomPadding: Dp @Composable @ReadOnlyComposable get() = Dimens.headerBottomPadding
val CleanXPanelPadding: Dp @Composable @ReadOnlyComposable get() = Dimens.panelPadding
val CleanXButtonHorizontalPadding: Dp @Composable @ReadOnlyComposable get() = Dimens.buttonHorizontalPadding
val CleanXButtonVerticalPadding: Dp @Composable @ReadOnlyComposable get() = Dimens.buttonVerticalPadding

// ---------- Sizes ----------
val CleanXButtonHeight: Dp @Composable @ReadOnlyComposable get() = Dimens.buttonHeight
val CleanXCompactButtonHeight: Dp @Composable @ReadOnlyComposable get() = Dimens.compactButtonHeight
val CleanXRowHeight: Dp @Composable @ReadOnlyComposable get() = Dimens.rowHeight
val CleanXIconButtonSize: Dp @Composable @ReadOnlyComposable get() = Dimens.iconButtonSize
val CleanXActionIconSize: Dp @Composable @ReadOnlyComposable get() = Dimens.actionIconSize
val CleanXHeaderIconSize: Dp @Composable @ReadOnlyComposable get() = Dimens.headerIconSize

// ---------- Shapes ----------
val CleanXCardShape: RoundedCornerShape @Composable @ReadOnlyComposable get() = Shapes.card
val CleanXTileShape: RoundedCornerShape @Composable @ReadOnlyComposable get() = Shapes.tile
val CleanXSmallShape: RoundedCornerShape @Composable @ReadOnlyComposable get() = Shapes.small
val CleanXPillShape: RoundedCornerShape @Composable @ReadOnlyComposable get() = Shapes.pill
val CleanXButtonShape: RoundedCornerShape @Composable @ReadOnlyComposable get() = Shapes.button
val CleanXTileAspectRatio: Float @Composable @ReadOnlyComposable get() = Shapes.fileTileAspectRatio

// 兼容 VirusUiTheme 的形状引用
val VirusPanelShape: RoundedCornerShape @Composable @ReadOnlyComposable get() = Shapes.virusPanel
val VirusButtonShape: RoundedCornerShape @Composable @ReadOnlyComposable get() = Shapes.virusButton

// ---------- Text sizes (保留为纯常量，排版变化通过 VariantTypography 控制) ----------
val CleanXTextHero = 42.sp
val CleanXTextDisplay = 28.sp
val CleanXTextTitle = 20.sp
val CleanXTextSubtitle = 18.sp
val CleanXTextBody = 16.sp
val CleanXTextBodySmall = 15.sp
val CleanXTextCaption = 14.sp
val CleanXTextTiny = 12.sp
val CleanXLineHero = 44.sp
val CleanXLineDisplay = 32.sp
val CleanXLineTitle = 27.sp
val CleanXLineSection = 24.sp
val CleanXLineSubtitle = 22.sp
val CleanXLineBody = 20.sp
val CleanXLineBodySmall = 19.sp
val CleanXLineCaption = 18.sp
val CleanXLineTiny = 14.sp
