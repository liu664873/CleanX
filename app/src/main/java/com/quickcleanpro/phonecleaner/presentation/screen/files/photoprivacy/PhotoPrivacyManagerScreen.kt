package com.quickcleanpro.phonecleaner.presentation.screen.files.photoprivacy

import androidx.compose.runtime.Composable
import com.quickcleanpro.phonecleaner.presentation.common.permission.PermissionGateConfig
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.FileManagerScreen
import com.quickcleanpro.phonecleaner.presentation.screen.files.photoprivacy.PhotoPrivacyManagerViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun PhotoPrivacyManagerScreen(
    permissionGateConfig: PermissionGateConfig? = null
) {
    val viewModel: PhotoPrivacyManagerViewModel = koinViewModel()
    FileManagerScreen(
        viewModel = viewModel,
        permissionGateConfig = permissionGateConfig
    )
}
