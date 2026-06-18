package com.quickcleanpro.phonecleaner.presentation.screen.tools.networkscan

import com.quickcleanpro.phonecleaner.domain.model.toolbox.NetworkScanResult
import java.util.concurrent.atomic.AtomicReference

object NetworkScanSessionStore {
    private val latestScan = AtomicReference<NetworkScanResult?>(null)

    fun save(scan: NetworkScanResult) {
        latestScan.set(scan)
    }

    fun get(): NetworkScanResult? = latestScan.get()

    fun clear() {
        latestScan.set(null)
    }
}
