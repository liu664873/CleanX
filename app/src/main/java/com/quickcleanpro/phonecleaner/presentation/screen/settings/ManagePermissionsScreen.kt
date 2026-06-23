package com.quickcleanpro.phonecleaner.presentation.screen.settings

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.quickcleanpro.phonecleaner.R
import com.quickcleanpro.phonecleaner.presentation.common.components.CleanXScaffoldPage
import com.quickcleanpro.phonecleaner.presentation.common.permission.LocalCleanXPermissionCoordinator
import com.quickcleanpro.phonecleaner.presentation.screen.settings.views.ManagePermissionsContent
import com.quickcleanpro.phonecleaner.presentation.screen.settings.views.PermissionRowUi
import org.koin.androidx.compose.koinViewModel

@Composable
fun ManagePermissionsScreen(
    viewModel: ManagePermissionsViewModel = koinViewModel(),
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val permissionCoordinator = LocalCleanXPermissionCoordinator.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    DisposableEffect(lifecycleOwner, viewModel, context) {
        val observer =
            LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME) {
                    viewModel.onResume(context)
                }
            }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(viewModel, context) {
        viewModel.load(context)
    }

    CleanXScaffoldPage(
        title = stringResource(R.string.settings_manage_permissions),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 28.dp),
    ) {
        ManagePermissionsContent(
            rows =
                uiState.rows.map { row ->
                    PermissionRowUi(
                        label = stringResource(row.labelRes),
                        checked = row.checked,
                        onClick = {
                            permissionCoordinator.guard(viewModel.actionFor(row.feature)) {
                                viewModel.refresh(context, refreshAgainAfterDelay = true)
                            }
                        },
                    )
                },
        )
        Spacer(modifier = Modifier.height(100.dp))
    }
}
