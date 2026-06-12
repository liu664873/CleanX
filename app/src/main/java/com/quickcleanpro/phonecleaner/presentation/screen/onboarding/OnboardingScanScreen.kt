package com.quickcleanpro.phonecleaner.presentation.screen.onboarding

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import org.koin.androidx.compose.koinViewModel

private const val OnboardingStepDelayMillis = 680L
private const val OnboardingScanLineDurationMillis = 1800
private const val OnboardingStatusRingDurationMillis = 1200

@Composable
fun OnboardingScanScreen(
    onContinueToHome: () -> Unit
) {
    OnboardingScanContent(
        onContinueToHome = onContinueToHome
    )
}

@Composable
private fun OnboardingScanContent(
    onContinueToHome: () -> Unit
) {
    val viewModel: OnboardingScanViewModel = koinViewModel()
    val lifecycleOwner = LocalLifecycleOwner.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var completedStep by remember { mutableIntStateOf(0) }

    DisposableEffect(lifecycleOwner, viewModel) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refresh()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(Unit) {
        for (step in 1..6) {
            delay(OnboardingStepDelayMillis)
            completedStep = step
        }
        delay(OnboardingStepDelayMillis)
        completedStep = 7
    }
}