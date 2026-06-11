package com.clean.cleanx.presentation.screen.onboarding

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun OnboardingScanScreen(
    onContinueToHome: () -> Unit
) {
    OnboardingScanContent(
        viewModel = viewModel(),
        onContinueToHome = onContinueToHome
    )
}

@Composable
private fun OnboardingScanContent(
    viewModel: OnboardingScanViewModel,
    onContinueToHome: () -> Unit
) {

}