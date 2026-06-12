package com.quickcleanpro.phonecleaner.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = Blue700,
    onPrimary = Gray50,
    primaryContainer = Blue200,
    secondary = Teal700,
    onSecondary = Gray50,
    secondaryContainer = Teal200,
    background = Color(0xFFF7FAFD),
    onBackground = Color(0xFF2D3748),
    surface = Color(0xFFF7FAFD),
    onSurface = Color(0xFF2D3748),
    surfaceVariant = Color(0xFFEEF4F9),
    onSurfaceVariant = Color(0xFF8190A5),
    error = Red500,
    onError = Gray50
)

private val DarkColorScheme = darkColorScheme(
    primary = Blue200,
    onPrimary = Gray900,
    primaryContainer = Blue700,
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
    onError = Gray50
)

@Composable
fun CleanXTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}
