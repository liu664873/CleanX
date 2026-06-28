package com.quickcleanpro.phonecleaner.variant

import androidx.compose.runtime.Composable
import com.quickcleanpro.phonecleaner.core.permission.AppPermission
import com.quickcleanpro.phonecleaner.presentation.common.permission.PermissionRequestTarget

data class PermissionPromptRequest(
    val target: PermissionRequestTarget,
    val missingPermission: AppPermission?,
)

interface VariantPermissionUi {
    @Composable
    fun PermissionPrompt(
        request: PermissionPromptRequest,
        onSubmit: () -> Unit,
        onDismiss: () -> Unit,
    )
}
