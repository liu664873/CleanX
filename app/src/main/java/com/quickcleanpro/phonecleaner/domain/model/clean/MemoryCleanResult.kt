package com.quickcleanpro.phonecleaner.domain.model.clean

import com.quickcleanpro.phonecleaner.util.FileSizeFormatter

/**
 * 鍐呭瓨娓呯悊缁撴灉銆? */
data class MemoryCleanResult(
    val killedCount: Int,
    val freedBytes: Long,
    val beforeAvailBytes: Long,
    val afterAvailBytes: Long,
) {
    /** 宸叉牸寮忓寲鐨勯噴鏀剧┖闂存枃妗堛€?*/
    val freedFormatted: String get() = FileSizeFormatter.format(freedBytes)

    /** 鍙敤鍐呭瓨鎻愬崌姣斾緥銆?*/
    val improvementPercent: Int get() =
        if (beforeAvailBytes > 0) {
            ((freedBytes.toFloat() / beforeAvailBytes) * 100).toInt().coerceAtMost(100)
        } else {
            0
        }
}
