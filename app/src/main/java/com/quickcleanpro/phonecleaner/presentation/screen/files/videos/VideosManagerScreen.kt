package com.quickcleanpro.phonecleaner.presentation.screen.files.videos

import androidx.compose.runtime.Composable
import com.quickcleanpro.phonecleaner.presentation.common.permission.PermissionGateConfig
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.MediaFileCollectionScreenState
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.VideosManagerViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun VideosManagerScreen(
    permissionGateConfig: PermissionGateConfig? = null
) {
    val viewModel: VideosManagerViewModel = koinViewModel()
    MediaFileCollectionScreenState(
        viewModel = viewModel,
        permissionGateConfig = permissionGateConfig
    )
}
