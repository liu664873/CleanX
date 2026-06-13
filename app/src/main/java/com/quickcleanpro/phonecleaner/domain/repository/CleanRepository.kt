package com.quickcleanpro.phonecleaner.domain.repository

import com.quickcleanpro.phonecleaner.domain.model.CleanItem
import com.quickcleanpro.phonecleaner.domain.model.CleanResult
import com.quickcleanpro.phonecleaner.domain.model.ScanProgress
import com.quickcleanpro.phonecleaner.domain.model.ScanResult
import com.quickcleanpro.phonecleaner.domain.model.clean.MemoryCleanResult
import kotlinx.coroutines.flow.Flow

/**
 * Core clean capability contract.
 *
 * Domain callers depend on this abstraction rather than concrete scanner or
 * delete implementations.
 */
interface CleanRepository {
    val scanProgress: Flow<ScanProgress>

    suspend fun performFullScan(): ScanResult

    suspend fun cleanFiles(selectedItems: List<CleanItem>): CleanResult

    suspend fun cleanMemory(): MemoryCleanResult
}
