package com.quickcleanpro.phonecleaner.data.repository

import android.content.Context
import com.quickcleanpro.phonecleaner.domain.model.BatteryHistorySample
import com.quickcleanpro.phonecleaner.domain.repository.BatteryHistoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.EOFException
import java.io.File
import java.io.FileOutputStream

class BatteryHistoryRepositoryImpl(
    private val historyFile: File
) : BatteryHistoryRepository {

    constructor(context: Context) : this(
        File(context.applicationContext.filesDir, HISTORY_FILE_NAME)
    )

    private val lock = Any()
    private val _samples = MutableStateFlow<List<BatteryHistorySample>>(emptyList())
    private var loaded = false
    private var lastCompactAtMillis = 0L

    override val samples: StateFlow<List<BatteryHistorySample>> = _samples.asStateFlow()

    override fun loadRecent(nowMillis: Long): List<BatteryHistorySample> =
        synchronized(lock) {
            loadRecentLocked(nowMillis, compact = true)
        }

    override fun append(sample: BatteryHistorySample, nowMillis: Long): List<BatteryHistorySample> =
        synchronized(lock) {
            val current = if (loaded) _samples.value else loadRecentLocked(nowMillis, compact = false)
            if (!sample.isUsable()) {
                return@synchronized current
            }

            val next = normalize(
                samples = current.filterNot { it.timestampMillis == sample.timestampMillis } + sample,
                nowMillis = nowMillis
            )
            if (sample.timestampMillis >= nowMillis - BATTERY_HISTORY_WINDOW_MILLIS) {
                appendRecord(sample)
            }
            _samples.value = next

            if (shouldCompact(nowMillis, next.size)) {
                rewrite(next)
                lastCompactAtMillis = nowMillis
            }
            next
        }

    private fun loadRecentLocked(nowMillis: Long, compact: Boolean): List<BatteryHistorySample> {
        val raw = readAllRecords()
        val recent = normalize(raw, nowMillis)
        loaded = true
        _samples.value = recent
        if (compact && (raw.size != recent.size || shouldCompact(nowMillis, recent.size))) {
            rewrite(recent)
            lastCompactAtMillis = nowMillis
        }
        return recent
    }

    private fun readAllRecords(): List<BatteryHistorySample> {
        if (!historyFile.exists()) return emptyList()
        val result = mutableListOf<BatteryHistorySample>()
        try {
            DataInputStream(BufferedInputStream(historyFile.inputStream())).use { input ->
                while (true) {
                    val timestamp = input.readLong()
                    val rawCurrent = input.readFloat()
                    val temperature = input.readFloat()
                    result += BatteryHistorySample(
                        timestampMillis = timestamp,
                        currentMa = rawCurrent.takeIf { it.isFiniteValue() },
                        temperatureC = temperature
                    )
                }
            }
        } catch (_: EOFException) {
            return result
        } catch (_: Exception) {
            return result
        }
    }

    private fun appendRecord(sample: BatteryHistorySample) {
        runCatching {
            historyFile.parentFile?.mkdirs()
            DataOutputStream(BufferedOutputStream(FileOutputStream(historyFile, true))).use { output ->
                output.writeLong(sample.timestampMillis)
                output.writeFloat(sample.currentMa ?: Float.NaN)
                output.writeFloat(sample.temperatureC)
            }
        }
    }

    private fun rewrite(samples: List<BatteryHistorySample>) {
        runCatching {
            historyFile.parentFile?.mkdirs()
            val tmpFile = File(historyFile.parentFile, "$HISTORY_FILE_NAME.tmp")
            DataOutputStream(BufferedOutputStream(FileOutputStream(tmpFile, false))).use { output ->
                samples.forEach { sample ->
                    output.writeLong(sample.timestampMillis)
                    output.writeFloat(sample.currentMa ?: Float.NaN)
                    output.writeFloat(sample.temperatureC)
                }
            }
            if (historyFile.exists()) {
                historyFile.delete()
            }
            if (!tmpFile.renameTo(historyFile)) {
                tmpFile.copyTo(historyFile, overwrite = true)
                tmpFile.delete()
            }
        }
    }

    private fun shouldCompact(nowMillis: Long, sampleCount: Int): Boolean =
        sampleCount > MAX_BATTERY_HISTORY_SAMPLES ||
            nowMillis - lastCompactAtMillis >= BATTERY_HISTORY_COMPACT_INTERVAL_MILLIS

    private fun normalize(
        samples: List<BatteryHistorySample>,
        nowMillis: Long
    ): List<BatteryHistorySample> {
        val cutoff = nowMillis - BATTERY_HISTORY_WINDOW_MILLIS
        val latestAllowed = nowMillis + BATTERY_HISTORY_FUTURE_TOLERANCE_MILLIS
        val byTimestamp = LinkedHashMap<Long, BatteryHistorySample>()
        samples
            .asSequence()
            .filter { it.isUsable() }
            .filter { it.timestampMillis in cutoff..latestAllowed }
            .sortedBy { it.timestampMillis }
            .forEach { byTimestamp[it.timestampMillis] = it }
        return byTimestamp.values
            .sortedBy { it.timestampMillis }
            .takeLast(MAX_BATTERY_HISTORY_SAMPLES)
    }

    companion object {
        const val BATTERY_HISTORY_SAMPLE_INTERVAL_MILLIS = 2_000L
        const val BATTERY_HISTORY_WINDOW_MILLIS = 24L * 60L * 60L * 1000L
        const val MAX_BATTERY_HISTORY_SAMPLES =
            (BATTERY_HISTORY_WINDOW_MILLIS / BATTERY_HISTORY_SAMPLE_INTERVAL_MILLIS).toInt() + 1

        private const val HISTORY_FILE_NAME = "battery_history.bin"
        private const val BATTERY_HISTORY_COMPACT_INTERVAL_MILLIS = 10L * 60L * 1000L
        private const val BATTERY_HISTORY_FUTURE_TOLERANCE_MILLIS = 60_000L
    }
}

private fun BatteryHistorySample.isUsable(): Boolean =
    timestampMillis > 0L &&
        temperatureC.isFiniteValue() &&
        currentMa?.isFiniteValue() != false

private fun Float.isFiniteValue(): Boolean =
    !isNaN() && !isInfinite()
