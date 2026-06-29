package com.quickcleanpro.phonecleaner.presentation.screen.toolbox

internal data class NetworkInfo(
    val type: String,
    val ssid: String,
    val ip: String,
    val downstreamMbps: String,
    val upstreamMbps: String
) {
    companion object {
        val EMPTY = NetworkInfo("--", "--", "--", "--", "--")
    }
}
