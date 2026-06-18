package com.quickcleanpro.phonecleaner.presentation.screen.files.largefiles

import androidx.compose.runtime.Composable
import com.quickcleanpro.phonecleaner.presentation.common.permission.PermissionGateConfig
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.LargeFilesManagerViewModel
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.ManagedFileCollectionScreenState
import org.koin.androidx.compose.koinViewModel

@Composable
fun LargeFilesManagerScreen(
    permissionGateConfig: PermissionGateConfig? = null
) {
    val viewModel: LargeFilesManagerViewModel = koinViewModel()
    ManagedFileCollectionScreenState(
        refreshOnResume = true,
        viewModel = viewModel,
        permissionGateConfig = permissionGateConfig
    )
}
