package com.quickcleanpro.phonecleaner.presentation.screen.tools.networkspeed

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.quickcleanpro.phonecleaner.R
import com.quickcleanpro.phonecleaner.presentation.common.components.CleanXScaffoldPage
import com.quickcleanpro.phonecleaner.presentation.common.components.buttons.CleanXPrimaryButton
import com.quickcleanpro.phonecleaner.presentation.screen.tools.networkspeed.views.NetworkSpeedContentView
import org.koin.androidx.compose.koinViewModel

@Composable
fun NetworkSpeedScreen(viewModel: NetworkSpeedViewModel = koinViewModel()) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    DisposableEffect(lifecycleOwner, viewModel) {
        val observer =
            LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME) {
                    viewModel.refreshNetworkStateUntilNetworkAvailable()
                }
            }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    CleanXScaffoldPage(
        title = stringResource(R.string.network_speed),
        titleFontSize = 20.sp,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        bottomBar = {
            if (uiState.phase != NetworkSpeedPhase.Result) {
                CleanXPrimaryButton(
                    text =
                        if (uiState.phase == NetworkSpeedPhase.Testing) {
                            stringResource(R.string.stop)
                        } else {
                            stringResource(R.string.run_speed_test)
                        },
                    onClick =
                        if (uiState.phase == NetworkSpeedPhase.Testing) {
                            viewModel::stopSpeedTest
                        } else {
                            viewModel::runSpeedTest
                        },
                    enabled = uiState.hasNetwork || uiState.phase == NetworkSpeedPhase.Testing,
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                )
            }
        },
    ) {
        NetworkSpeedContentView(uiState = uiState)
    }
}
