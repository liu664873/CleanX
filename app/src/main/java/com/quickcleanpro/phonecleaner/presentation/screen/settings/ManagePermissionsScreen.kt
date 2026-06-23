package com.quickcleanpro.phonecleaner.presentation.screen.settings

import android.content.ActivityNotFoundException
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import com.quickcleanpro.phonecleaner.presentation.app.LocalExternalActivityLaunchHandler
import com.quickcleanpro.phonecleaner.presentation.common.components.CleanXScaffoldPage
import com.quickcleanpro.phonecleaner.presentation.screen.settings.views.ManagePermissionsContent
import com.quickcleanpro.phonecleaner.presentation.screen.settings.views.PermissionRowUi
import org.koin.androidx.compose.koinViewModel

@Composable
fun ManagePermissionsScreen(
    viewModel: ManagePermissionsViewModel = koinViewModel(),
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val externalActivityLaunchHandler = LocalExternalActivityLaunchHandler.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val settingsLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.StartActivityForResult(),
        ) {
            viewModel.onSettingsResult(context)
        }
    val runtimeLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions(),
        ) { grants ->
            viewModel.onRuntimePermissionsResult(context, grants)
        }

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

    LaunchedEffect(viewModel, context) {
        viewModel.events.collect { event ->
            when (event) {
                is ManagePermissionsEvent.LaunchRuntimePermissions -> {
                    runtimeLauncher.launch(event.permissions.toTypedArray())
                }
                is ManagePermissionsEvent.LaunchSettings -> {
                    event.intents.forEach { intent ->
                        try {
                            externalActivityLaunchHandler.markLaunch()
                            settingsLauncher.launch(intent)
                            return@collect
                        } catch (_: ActivityNotFoundException) {
                            externalActivityLaunchHandler.cancelLaunch()
                        } catch (_: Exception) {
                            externalActivityLaunchHandler.cancelLaunch()
                        }
                    }
                    viewModel.onSettingsResult(context)
                }
            }
        }
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
                        onClick = { viewModel.requestPermission(context, row.feature) },
                    )
                },
        )
        Spacer(modifier = Modifier.height(100.dp))
    }
}
