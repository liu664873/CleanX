package com.quickcleanpro.phonecleaner.presentation.screen.antivirus

import androidx.compose.runtime.Composable
import com.quickcleanpro.phonecleaner.presentation.common.route.LocalRouter
import com.quickcleanpro.phonecleaner.presentation.common.route.Screen
import com.quickcleanpro.phonecleaner.presentation.screen.antivirus.views.AntiVirusHomeView

@Composable
fun AntiVirusScreen(
    viewModel: VirusScanViewModel,
) {
    val router = LocalRouter.current

    AntiVirusHomeView(
        onDeepScan = {
            viewModel.resetScanState()
            router.navigate(Screen.VirusDeepScan)
        },
        onQuickScan = {
            viewModel.resetScanState()
            router.navigate(Screen.VirusQuickScan)
        },
    )
}
