package com.quickcleanpro.phonecleaner.presentation.screen.files.photos

import androidx.compose.runtime.Composable
import com.quickcleanpro.phonecleaner.presentation.common.permission.PermissionGateConfig
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.FileManagerScreen
import com.quickcleanpro.phonecleaner.presentation.screen.files.photos.PhotosManagerViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun PhotosManagerScreen (
    permissionGateConfig: PermissionGateConfig? = null
) {
    val viewModel: PhotosManagerViewModel = koinViewModel()
    FileManagerScreen(
        viewModel = viewModel,
        permissionGateConfig = permissionGateConfig
    )
}
