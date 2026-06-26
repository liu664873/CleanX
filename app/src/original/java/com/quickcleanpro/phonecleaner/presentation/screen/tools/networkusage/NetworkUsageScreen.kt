package com.quickcleanpro.phonecleaner.presentation.screen.tools.networkusage

import androidx.compose.runtime.Composable
import com.quickcleanpro.phonecleaner.presentation.screen.tools.networkusage.views.NetworkUsageScreenState
import org.koin.androidx.compose.koinViewModel

@Composable
fun NetworkUsageScreen(viewModel: NetworkUsageViewModel = koinViewModel()) {
    NetworkUsageScreenState(viewModel = viewModel)
}
