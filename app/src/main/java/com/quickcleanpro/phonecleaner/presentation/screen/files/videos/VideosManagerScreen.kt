package com.quickcleanpro.phonecleaner.presentation.screen.files.videos

import androidx.compose.runtime.Composable
import com.quickcleanpro.phonecleaner.presentation.common.permission.PermissionGateConfig
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.FileManagerScreen
import com.quickcleanpro.phonecleaner.presentation.screen.files.videos.VideosManagerViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun VideosManagerScreen(
    permissionGateConfig: PermissionGateConfig? = null
) {
    val viewModel: VideosManagerViewModel = koinViewModel()
    FileManagerScreen(
        viewModel = viewModel,
        permissionGateConfig = permissionGateConfig
    )
}
