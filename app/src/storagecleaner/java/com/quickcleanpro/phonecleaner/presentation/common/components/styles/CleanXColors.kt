package com.quickcleanpro.phonecleaner.presentation.common.components.styles

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import com.quickcleanpro.phonecleaner.presentation.theme.LocalVariantTheme

// ============================================================================
// 便捷别名 — 所有值都从 LocalVariantTheme 读取，保证主题可切换
// ============================================================================

// --- 旧版 CleanXColors 别名（向后兼容）---
@get:Composable
@get:ReadOnlyComposable
val Colors: com.quickcleanpro.phonecleaner.presentation.theme.VariantColors
    get() = LocalVariantTheme.current.colors

val CleanXBackground: Color @Composable @ReadOnlyComposable get() = Colors.pageBackground
val CleanXText: Color @Composable @ReadOnlyComposable get() = Colors.textPrimary
val CleanXMutedText: Color @Composable @ReadOnlyComposable get() = Colors.textMuted
val CleanXBlue: Color @Composable @ReadOnlyComposable get() = Colors.primary
val CleanXBlueDark: Color @Composable @ReadOnlyComposable get() = Colors.primaryDeep
val CleanXBlueSoft: Color @Composable @ReadOnlyComposable get() = Colors.primarySoft
val CleanXCardColor: Color @Composable @ReadOnlyComposable get() = Colors.cardBackground
val CleanXSoftPanel: Color @Composable @ReadOnlyComposable get() = Colors.softPanelBackground
val CleanXSubtlePanel: Color @Composable @ReadOnlyComposable get() = Colors.subtlePanelBackground
val CleanXDivider: Color @Composable @ReadOnlyComposable get() = Colors.divider
val CleanXSuccess: Color @Composable @ReadOnlyComposable get() = Colors.success
val CleanXWarning: Color @Composable @ReadOnlyComposable get() = Colors.warning
val CleanXDanger: Color @Composable @ReadOnlyComposable get() = Colors.danger

// 兼容旧版 VirusUiTheme 引用（所有值都从主题中获取）
val VirusBackgroundTop: Color @Composable @ReadOnlyComposable get() = Colors.gradientBackgroundTop
val VirusBackgroundBottom: Color @Composable @ReadOnlyComposable get() = Colors.gradientBackgroundBottom
val VirusBackgroundCard: Color @Composable @ReadOnlyComposable get() = Colors.virusBackgroundCard
val VirusTitle: Color @Composable @ReadOnlyComposable get() = Colors.navy
val VirusBody: Color @Composable @ReadOnlyComposable get() = Colors.textPrimary
val VirusSecondary: Color @Composable @ReadOnlyComposable get() = Colors.navyText
val VirusBlue: Color @Composable @ReadOnlyComposable get() = Colors.primary
val VirusBlueDeep: Color @Composable @ReadOnlyComposable get() = Colors.primaryDeep
val VirusTrackInactive: Color @Composable @ReadOnlyComposable get() = Colors.virusTrackInactive
val VirusTrackInactiveLine: Color @Composable @ReadOnlyComposable get() = Colors.virusTrackLine
val VirusDanger: Color @Composable @ReadOnlyComposable get() = Colors.dangerSoft
val VirusDivider: Color @Composable @ReadOnlyComposable get() = Colors.dividerNavy
val VirusOrange: Color @Composable @ReadOnlyComposable get() = Colors.orange
val VirusHigh: Color @Composable @ReadOnlyComposable get() = Colors.orangeHot
