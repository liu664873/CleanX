package com.quickcleanpro.phonecleaner.domain.usecase

import com.quickcleanpro.phonecleaner.domain.model.ScanResult
import com.quickcleanpro.phonecleaner.domain.repository.CleanRepository

/**
 * 鎵弿鍨冨溇鏂囦欢鐢ㄤ緥
 */
class ScanJunkUseCase(
    private val repository: CleanRepository,
) {
    suspend operator fun invoke(): ScanResult = repository.performFullScan()
}
