package com.quickcleanpro.phonecleaner.di

import com.quickcleanpro.phonecleaner.presentation.screen.settings.ManagePermissionsViewModel
import kotlinx.coroutines.Dispatchers
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

fun currentVariantModules(): VariantKoinModules = StorageCleanerKoinModules

object StorageCleanerKoinModules : VariantKoinModules {
    override val modules =
        listOf(
            module {
                viewModel { ManagePermissionsViewModel(Dispatchers.IO) }
            },
        )
}
