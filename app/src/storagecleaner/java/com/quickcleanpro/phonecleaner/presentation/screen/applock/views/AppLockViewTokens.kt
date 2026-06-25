package com.quickcleanpro.phonecleaner.presentation.screen.applock.views

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.quickcleanpro.phonecleaner.presentation.theme.LocalVariantTheme

internal const val PIN_LENGTH = 4

private val c @Composable @ReadOnlyComposable get() = LocalVariantTheme.current.colors

internal val AppLockBackground: Color @Composable @ReadOnlyComposable get() = c.pageBackground
internal val AppLockCardColor: Color @Composable @ReadOnlyComposable get() = c.virusBackgroundCard
internal val AppLockNavy: Color @Composable @ReadOnlyComposable get() = c.navy
internal val AppLockSecondaryText: Color @Composable @ReadOnlyComposable get() = c.navyText
internal val AppLockPlaceholderText: Color @Composable @ReadOnlyComposable get() = c.pinPlaceholderText
internal val AppLockDividerColor: Color @Composable @ReadOnlyComposable get() = c.dividerDeep
internal val AppLockCardRadius: Dp @Composable @ReadOnlyComposable get() = c.let { 12.dp } // TODO: move to VariantDimens
internal val PinSelectedColor: Color @Composable @ReadOnlyComposable get() = c.pinSelected
internal val PinUnselectedBorderColor: Color @Composable @ReadOnlyComposable get() = c.pinUnselectedBorder
internal val PinKeyBackground: Color @Composable @ReadOnlyComposable get() = c.pinKeyBackground
internal val PinErrorColor: Color @Composable @ReadOnlyComposable get() = c.pinError
