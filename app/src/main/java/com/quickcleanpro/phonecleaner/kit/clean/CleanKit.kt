package com.quickcleanpro.phonecleaner.kit.clean

import android.content.Context
import com.quickcleanpro.phonecleaner.data.repository.CleanRepositoryImpl
import com.quickcleanpro.phonecleaner.domain.repository.CleanRepository
import com.quickcleanpro.phonecleaner.domain.state.SharedScanState
import com.quickcleanpro.phonecleaner.domain.usecase.CleanJunkUseCase
import com.quickcleanpro.phonecleaner.domain.usecase.MemoryCleanUseCase
import com.quickcleanpro.phonecleaner.domain.usecase.ScanJunkUseCase

class CleanKit private constructor(
    val sharedScanState: SharedScanState,
    val repository: CleanRepository,
    val scanJunkUseCase: ScanJunkUseCase,
    val cleanJunkUseCase: CleanJunkUseCase,
    val memoryCleanUseCase: MemoryCleanUseCase,
) {
    companion object {
        fun create(
            context: Context,
            sharedScanState: SharedScanState = SharedScanState(),
        ): CleanKit {
            val repository = CleanRepositoryImpl(context.applicationContext, sharedScanState)
            return CleanKit(
                sharedScanState = sharedScanState,
                repository = repository,
                scanJunkUseCase = ScanJunkUseCase(repository),
                cleanJunkUseCase = CleanJunkUseCase(repository),
                memoryCleanUseCase = MemoryCleanUseCase(repository),
            )
        }
    }
}
