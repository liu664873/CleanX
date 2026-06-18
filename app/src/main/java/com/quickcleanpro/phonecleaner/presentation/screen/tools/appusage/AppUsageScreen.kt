package com.quickcleanpro.phonecleaner.presentation.screen.tools.appusage

import androidx.compose.runtime.Composable
import com.quickcleanpro.phonecleaner.presentation.screen.tools.appusage.views.AppUsageScreenState
import org.koin.androidx.compose.koinViewModel

@Composable
fun AppUsageScreen(viewModel: AppUsageViewModel = koinViewModel()) {
    AppUsageScreenState(viewModel = viewModel)
}
