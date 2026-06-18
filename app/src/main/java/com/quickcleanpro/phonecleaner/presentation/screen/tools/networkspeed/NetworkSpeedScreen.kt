package com.quickcleanpro.phonecleaner.presentation.screen.tools.networkspeed

import androidx.compose.runtime.Composable
import com.quickcleanpro.phonecleaner.presentation.screen.tools.networkspeed.views.NetworkSpeedScreenState
import org.koin.androidx.compose.koinViewModel

@Composable
fun NetworkSpeedScreen(viewModel: NetworkSpeedViewModel = koinViewModel()) {
    NetworkSpeedScreenState(viewModel = viewModel)
}
