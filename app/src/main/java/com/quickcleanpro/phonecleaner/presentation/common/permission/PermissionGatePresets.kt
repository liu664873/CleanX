package com.quickcleanpro.phonecleaner.presentation.common.permission

import androidx.compose.runtime.Composable

data class PermissionGateConfig(
    val cleanXFeature: CleanXFeature,
    val onDenied: (() -> Unit)? = null,
    val deniedContent: (@Composable (onRetry: () -> Unit) -> Unit)? = null,
)
