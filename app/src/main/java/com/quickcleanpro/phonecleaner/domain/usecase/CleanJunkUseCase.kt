package com.quickcleanpro.phonecleaner.domain.usecase

import com.quickcleanpro.phonecleaner.domain.model.CleanItem
import com.quickcleanpro.phonecleaner.domain.model.CleanResult
import com.quickcleanpro.phonecleaner.domain.repository.CleanRepository

/**
 * 娓呯悊鏂囦欢鐢ㄤ緥
 */
class CleanJunkUseCase(
    private val repository: CleanRepository,
) {
    suspend operator fun invoke(selectedItems: List<CleanItem>): CleanResult = repository.cleanFiles(selectedItems)
}
