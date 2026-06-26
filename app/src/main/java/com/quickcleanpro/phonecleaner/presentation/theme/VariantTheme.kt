package com.quickcleanpro.phonecleaner.presentation.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

interface VariantTheme {
    val colors: VariantColors
    val dimens: VariantDimens
    val shapes: VariantShapes
    val typography: VariantTypography
}

data class VariantColors(
    val pageBackground: Color = Color(0xFFF7FAFD),
    val cardBackground: Color = Color.White,
    val surfaceBackground: Color = Color(0xFFF7FAFD),
    val softPanelBackground: Color = Color.White,
    val subtlePanelBackground: Color = Color(0xFFEFF6FC),
    val gradientBackgroundTop: Color = Color(0xFFE3ECFD),
    val gradientBackgroundBottom: Color = Color(0xFFDFEBF5),
    val primary: Color = Color(0xFF4179FC),
    val primaryDeep: Color = Color(0xFF1AA7EC),
    val primarySoft: Color = Color(0xFFEAF7FE),
    val textPrimary: Color = Color(0xFF2D3748),
    val textSecondary: Color = Color(0xFF7F8EAA),
    val textMuted: Color = Color(0xFF8190A5),
    val textOnPrimary: Color = Color.White,
    val success: Color = Color(0xFF19C979),
    val successBackground: Color = Color(0xFFEFFFF4),
    val successBorder: Color = Color(0xFFC5F7D4),
    val warning: Color = Color(0xFFECAD1A),
    val danger: Color = Color(0xFFFF6B3D),
    val dangerHot: Color = Color(0xFFEC521A),
    val dangerSoft: Color = Color(0xFFEE4D52),
    val orange: Color = Color(0xFFEC751A),
    val orangeHot: Color = Color(0xFFEC521A),
    val divider: Color = Color(0xFFE2EAF3),
    val dividerSubtle: Color = Color(0x0D1D2959),
    val dividerNavy: Color = Color(0x261D2959),
    val dividerDeep: Color = Color(0x332D3748),
    val navy: Color = Color(0xFF1D2959),
    val navyMuted: Color = Color(0xA61D2959),
    val navyText: Color = Color(0xA61D2959),
    val navyOnPrimary: Color = Color.White,
    val tabActiveBackground: Color = Color.White,
    val tabInactiveText: Color = Color(0xFF8DA3D5),
    val pinSelected: Color = Color(0xFF1D2959),
    val pinUnselectedBorder: Color = Color(0xFF66749A),
    val pinError: Color = Color(0xFFEC521A),
    val pinKeyBackground: Color = Color.White.copy(alpha = 0.65f),
    val pinPlaceholderText: Color = Color(0x591D2959),
    val storageBlue: Color = Color(0xFF2281FD),
    val storageYellow: Color = Color(0xFFFDBB22),
    val storageOrange: Color = Color(0xFFFD6F22),
    val virusTrackInactive: Color = Color(0xFF9FA6C5),
    val virusTrackLine: Color = Color(0xFF8392A7),
    val virusBackgroundCard: Color = Color(0xFFF6F7FB),
    val virusScanGradientStart: Color = Color.White,
    val notificationGradientStart: Color = Color(0xFF90C5FB),
    val notificationGradientEnd: Color = Color(0xFF88C9FB),
    val notificationBlueBackground: Color = Color(0xFFDEF4FF),
    val notificationBlueIcon: Color = Color(0xFF1AA7EC),
    val notificationBadgeRed: Color = Color(0xFFFE6361),
    val notificationBadgeGreen: Color = Color(0xFF30C85A),
    val notificationPromptBackground: Color = Color(0xFFEDF5FF),
    val notificationTitleText: Color = Color(0xFF1D2959),
    val notificationMutedRow: Color = Color(0xFFE7EDF5),
    val splashTextMuted: Color = Color(0xFFD9D9D9),
    val splashButtonBg: Color = Color(0xFFD9D9D9),
    val permissionsDeclinedBackground: Color = Color(0xFFFEF3F2),
    val permissionsDeclinedBorder: Color = Color(0xFFFCC5C2),
    val settingsNavBackground: Color = Color(0xFFF6F7FB),
)

data class VariantDimens(
    val pagePadding: androidx.compose.ui.unit.Dp = 16.dp,
    val contentPadding: androidx.compose.ui.unit.Dp = 13.dp,
    val compactPadding: androidx.compose.ui.unit.Dp = 8.dp,
    val smallPadding: androidx.compose.ui.unit.Dp = 10.dp,
    val mediumPadding: androidx.compose.ui.unit.Dp = 12.dp,
    val largePadding: androidx.compose.ui.unit.Dp = 20.dp,
    val gridSpacing: androidx.compose.ui.unit.Dp = 10.dp,
    val itemSpacing: androidx.compose.ui.unit.Dp = 12.dp,
    val sectionSpacing: androidx.compose.ui.unit.Dp = 20.dp,
    val headerTopPadding: androidx.compose.ui.unit.Dp = 12.dp,
    val headerBottomPadding: androidx.compose.ui.unit.Dp = 14.dp,
    val panelPadding: androidx.compose.ui.unit.Dp = 14.dp,
    val buttonHorizontalPadding: androidx.compose.ui.unit.Dp = 18.dp,
    val buttonVerticalPadding: androidx.compose.ui.unit.Dp = 14.dp,
    val buttonHeight: androidx.compose.ui.unit.Dp = 50.dp,
    val compactButtonHeight: androidx.compose.ui.unit.Dp = 36.dp,
    val rowHeight: androidx.compose.ui.unit.Dp = 52.dp,
    val iconButtonSize: androidx.compose.ui.unit.Dp = 40.dp,
    val actionIconSize: androidx.compose.ui.unit.Dp = 24.dp,
    val headerIconSize: androidx.compose.ui.unit.Dp = 28.dp,
)

data class VariantShapes(
    val card: RoundedCornerShape = RoundedCornerShape(14.dp),
    val tile: RoundedCornerShape = RoundedCornerShape(12.dp),
    val small: RoundedCornerShape = RoundedCornerShape(10.dp),
    val pill: RoundedCornerShape = RoundedCornerShape(60),
    val button: RoundedCornerShape = RoundedCornerShape(10),
    val fileTileAspectRatio: Float = 1.7f,
    val virusPanel: RoundedCornerShape = RoundedCornerShape(12.dp),
    val virusButton: RoundedCornerShape = RoundedCornerShape(10.dp),
)

class VariantTypography

object DefaultVariantTheme : VariantTheme {
    override val colors = VariantColors()
    override val dimens = VariantDimens()
    override val shapes = VariantShapes()
    override val typography = VariantTypography()
}

val LocalVariantTheme =
    staticCompositionLocalOf<VariantTheme> {
        DefaultVariantTheme
    }
