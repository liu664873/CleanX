package com.quickcleanpro.phonecleaner.domain.usecase

import com.quickcleanpro.phonecleaner.domain.model.clean.MemoryCleanResult
import com.quickcleanpro.phonecleaner.domain.repository.CleanRepository

class MemoryCleanUseCase(
    private val repository: CleanRepository,
) {
    suspend operator fun invoke(): MemoryCleanResult = repository.cleanMemory()
}
