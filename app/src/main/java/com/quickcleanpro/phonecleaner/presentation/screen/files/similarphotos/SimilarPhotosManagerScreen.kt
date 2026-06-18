package com.quickcleanpro.phonecleaner.presentation.screen.files.similarphotos

import androidx.compose.runtime.Composable
import com.quickcleanpro.phonecleaner.presentation.common.permission.PermissionGateConfig
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.FileManagerScreen
import com.quickcleanpro.phonecleaner.presentation.screen.files.similarphotos.SimilarPhotosManagerViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun SimilarPhotosManagerScreen(
    permissionGateConfig: PermissionGateConfig? = null
) {
    val viewModel: SimilarPhotosManagerViewModel = koinViewModel()
    FileManagerScreen(
        viewModel = viewModel,
        permissionGateConfig = permissionGateConfig
    )
}
