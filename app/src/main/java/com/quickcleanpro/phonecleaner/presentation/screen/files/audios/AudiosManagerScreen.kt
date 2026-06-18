package com.quickcleanpro.phonecleaner.presentation.screen.files.audios

import androidx.compose.runtime.Composable
import com.quickcleanpro.phonecleaner.presentation.common.permission.PermissionGateConfig
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.AudiosManagerViewModel
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.MediaFileCollectionScreenState
import org.koin.androidx.compose.koinViewModel

@Composable
fun AudiosManagerScreen(
    permissionGateConfig: PermissionGateConfig? = null
) {
    val viewModel: AudiosManagerViewModel = koinViewModel()
    MediaFileCollectionScreenState(
        viewModel = viewModel,
        permissionGateConfig = permissionGateConfig
    )
}
