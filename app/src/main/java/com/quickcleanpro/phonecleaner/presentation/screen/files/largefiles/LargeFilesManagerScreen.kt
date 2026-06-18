package com.quickcleanpro.phonecleaner.presentation.screen.files.largefiles

import androidx.compose.runtime.Composable
import com.quickcleanpro.phonecleaner.presentation.common.permission.PermissionGateConfig
import com.quickcleanpro.phonecleaner.presentation.screen.files.largefiles.LargeFilesManagerViewModel
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.FileManagerScreen
import org.koin.androidx.compose.koinViewModel

@Composable
fun LargeFilesManagerScreen(
    permissionGateConfig: PermissionGateConfig? = null
) {
    val viewModel: LargeFilesManagerViewModel = koinViewModel()
    FileManagerScreen(
        refreshOnResume = true,
        viewModel = viewModel,
        permissionGateConfig = permissionGateConfig
    )
}
