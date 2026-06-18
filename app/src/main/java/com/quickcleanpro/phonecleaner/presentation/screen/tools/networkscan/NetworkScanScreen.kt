package com.quickcleanpro.phonecleaner.presentation.screen.tools.networkscan

import androidx.compose.runtime.Composable
import com.quickcleanpro.phonecleaner.presentation.common.route.LocalRouter
import com.quickcleanpro.phonecleaner.presentation.common.route.Screen
import com.quickcleanpro.phonecleaner.presentation.screen.tools.networkscan.views.NetworkScanScreenState
import org.koin.androidx.compose.koinViewModel

@Composable
fun NetworkScanScreen(viewModel: NetworkScanViewModel = koinViewModel()) {
    val router = LocalRouter.current
    NetworkScanScreenState(
        viewModel = viewModel,
        onDevicesClick = { router.navigate(Screen.NetworkScanDevices) },
    )
}
