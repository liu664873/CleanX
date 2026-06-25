package com.quickcleanpro.phonecleaner.presentation.screen.tools.whatsappcleaner

import androidx.compose.runtime.Composable
import com.quickcleanpro.phonecleaner.presentation.screen.tools.whatsappcleaner.views.WhatsAppCleanerScreenState
import org.koin.androidx.compose.koinViewModel

@Composable
fun WhatsAppCleanerScreen(viewModel: WhatsAppCleanerViewModel = koinViewModel()) {
    WhatsAppCleanerScreenState(viewModel = viewModel)
}
