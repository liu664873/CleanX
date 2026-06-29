package com.quickcleanpro.phonecleaner.presentation.theme

import androidx.compose.runtime.Composable

@Composable
fun QuickCleanTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit,
) {
    CleanXTheme(darkTheme = darkTheme, content = content)
}
