package com.quickcleanpro.phonecleaner.presentation.screen.tools.notificationbar

import androidx.compose.runtime.Composable
import com.quickcleanpro.phonecleaner.presentation.screen.tools.common.notification.NotificationBarViewModel
import com.quickcleanpro.phonecleaner.presentation.screen.tools.notificationbar.views.NotificationBarScreenState
import org.koin.androidx.compose.koinViewModel

@Composable
fun NotificationBarScreen(viewModel: NotificationBarViewModel = koinViewModel()) {
    NotificationBarScreenState(viewModel = viewModel)
}
