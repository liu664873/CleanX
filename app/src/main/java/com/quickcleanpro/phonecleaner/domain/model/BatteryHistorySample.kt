package com.quickcleanpro.phonecleaner.domain.model

data class BatteryHistorySample(
    val timestampMillis: Long,
    val currentMa: Float?,
    val temperatureC: Float
)
