package com.quickcleanpro.phonecleaner.domain.repository

import com.quickcleanpro.phonecleaner.domain.model.BatteryHistorySample
import kotlinx.coroutines.flow.StateFlow

interface BatteryHistoryRepository {
    val samples: StateFlow<List<BatteryHistorySample>>

    fun loadRecent(nowMillis: Long = System.currentTimeMillis()): List<BatteryHistorySample>

    fun append(
        sample: BatteryHistorySample,
        nowMillis: Long = System.currentTimeMillis(),
    ): List<BatteryHistorySample>
}
