package com.quickcleanpro.phonecleaner.data.source.clean

import android.app.ActivityManager
import android.content.Context
import android.os.Process
import com.quickcleanpro.phonecleaner.domain.model.clean.MemoryCleanResult

object MemoryCleaner {
    /**
     * Attempts a conservative background-process cleanup.
     */
    fun clean(context: Context): MemoryCleanResult {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

        val beforeMem = ActivityManager.MemoryInfo().also { activityManager.getMemoryInfo(it) }
        val beforeAvail = beforeMem.availMem

        var killedCount = 0
        val myPid = Process.myPid()

        val runningProcesses =
            try {
                activityManager.runningAppProcesses
            } catch (_: SecurityException) {
                null
            }

        runningProcesses?.forEach { info ->
            if (info.pid == myPid) return@forEach
            if (info.importance >= ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) return@forEach
            try {
                activityManager.killBackgroundProcesses(info.processName)
                killedCount++
            } catch (_: Exception) {
            }
        }

        try {
            Thread.sleep(300)
        } catch (_: InterruptedException) {
        }

        val afterMem = ActivityManager.MemoryInfo().also { activityManager.getMemoryInfo(it) }
        val afterAvail = afterMem.availMem
        val freed = (afterAvail - beforeAvail).coerceAtLeast(0)

        return MemoryCleanResult(killedCount, freed, beforeAvail, afterAvail)
    }
}
