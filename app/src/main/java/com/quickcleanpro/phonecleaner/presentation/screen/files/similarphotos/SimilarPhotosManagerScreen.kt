package com.quickcleanpro.phonecleaner.presentation.screen.files.similarphotos

import androidx.compose.runtime.Composable
import com.quickcleanpro.phonecleaner.presentation.common.permission.PermissionGateConfig
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.MediaFileCollectionScreenState
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.SimilarPhotosManagerViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun SimilarPhotosManagerScreen(
    permissionGateConfig: PermissionGateConfig? = null
) {
    val viewModel: SimilarPhotosManagerViewModel = koinViewModel()
    MediaFileCollectionScreenState(
        viewModel = viewModel,
        permissionGateConfig = permissionGateConfig
    )
}
