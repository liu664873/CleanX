package com.quickcleanpro.phonecleaner.domain.usecase

import com.quickcleanpro.phonecleaner.domain.repository.CleanRepository
import com.quickcleanpro.phonecleaner.domain.model.clean.MemoryCleanResult

class MemoryCleanUseCase(private val repository: CleanRepository) {

    suspend operator fun invoke(): MemoryCleanResult {
        return repository.cleanMemory()
    }
}
