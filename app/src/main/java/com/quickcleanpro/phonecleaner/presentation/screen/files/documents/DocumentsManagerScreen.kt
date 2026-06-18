package com.quickcleanpro.phonecleaner.presentation.screen.files.documents

import androidx.compose.runtime.Composable
import com.quickcleanpro.phonecleaner.presentation.common.permission.PermissionGateConfig
import com.quickcleanpro.phonecleaner.presentation.screen.files.documents.DocumentsManagerViewModel
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.FileManagerScreen
import org.koin.androidx.compose.koinViewModel

@Composable
fun DocumentsManagerScreen(
    permissionGateConfig: PermissionGateConfig? = null
) {
    val viewModel: DocumentsManagerViewModel = koinViewModel()
    FileManagerScreen(
        refreshOnResume = true,
        viewModel = viewModel,
        permissionGateConfig = permissionGateConfig
    )
}
