package com.quickcleanpro.phonecleaner.di

import com.quickcleanpro.phonecleaner.presentation.screen.onboarding.OnboardingScanViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val presentationModule = module {
    viewModel { OnboardingScanViewModel(get()) }
}