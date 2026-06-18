package com.quickcleanpro.phonecleaner.presentation.screen.files.screenshots

import androidx.compose.runtime.Composable
import com.quickcleanpro.phonecleaner.presentation.common.permission.PermissionGateConfig
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.MediaFileCollectionScreenState
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.ScreenshotsManagerViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun ScreenshotsManagerScreen(
    permissionGateConfig: PermissionGateConfig? = null
) {
    val viewModel: ScreenshotsManagerViewModel = koinViewModel()
    MediaFileCollectionScreenState(
        viewModel = viewModel,
        permissionGateConfig = permissionGateConfig
    )
}
