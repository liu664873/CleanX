package com.quickcleanpro.phonecleaner.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import com.quickcleanpro.phonecleaner.BuildConfig

private fun lightSchemeFor(colors: VariantColors) =
    lightColorScheme(
        primary = colors.primary,
        onPrimary = colors.textOnPrimary,
        primaryContainer = colors.primarySoft,
        secondary = Teal700,
        onSecondary = Gray50,
        secondaryContainer = Teal200,
        background = colors.gradientBackgroundTop,
        onBackground = colors.textPrimary,
        surface = colors.surfaceBackground,
        onSurface = colors.textPrimary,
        surfaceVariant = colors.subtlePanelBackground,
        onSurfaceVariant = colors.textMuted,
        error = Red500,
        onError = Gray50,
    )

private fun darkSchemeFor(colors: VariantColors) =
    darkColorScheme(
        primary = colors.primarySoft,
        onPrimary = Gray900,
        primaryContainer = colors.primary,
        secondary = Teal200,
        onSecondary = Gray900,
        secondaryContainer = Teal700,
        background = Gray900,
        onBackground = Gray50,
        surface = Gray900,
        onSurface = Gray50,
        surfaceVariant = Gray800,
        onSurfaceVariant = Gray400,
        error = Red500,
        onError = Gray50,
    )

private fun selectedVariantTheme(): VariantTheme =
    when (com.quickcleanpro.phonecleaner.config.VariantConfigs.current.themeKey) {
        "storage_cleaner" -> StorageCleanerTheme
        else -> DefaultVariantTheme
    }

@Composable
fun CleanXTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val variantTheme = selectedVariantTheme()
    val colorScheme =
        if (darkTheme) {
            darkSchemeFor(variantTheme.colors)
        } else {
            lightSchemeFor(variantTheme.colors)
        }

    CompositionLocalProvider(LocalVariantTheme provides variantTheme) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AppTypography,
            content = content,
        )
    }
}
