package com.quickcleanpro.phonecleaner.presentation.screen.tools.common.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.text.format.Formatter
import com.quickcleanpro.phonecleaner.R

data class NetworkInfo(
    val type: String = "--",
    val ssid: String = "--",
    val ip: String = "--",
)

internal fun readNetworkInfo(context: Context): NetworkInfo {
    val appContext = context.applicationContext
    val connectivity = appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val capabilities = connectivity.getNetworkCapabilities(connectivity.activeNetwork)
    val type =
        when {
            capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true -> appContext.getString(R.string.wifi)
            capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true -> appContext.getString(R.string.network_type_cellular)
            capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) == true -> appContext.getString(R.string.network_type_ethernet)
            else -> "--"
        }

    val wifiManager = appContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
    val wifiInfo = wifiManager?.connectionInfo
    val ssid =
        wifiInfo
            ?.ssid
            ?.removeSurrounding("\"")
            ?.takeIf { it.isNotBlank() && it != "<unknown ssid>" }
            ?: appContext.getString(R.string.unknown_ssid)
    val ip =
        wifiInfo
            ?.ipAddress
            ?.takeIf { it != 0 }
            ?.let { Formatter.formatIpAddress(it) }
            ?: "--"

    return NetworkInfo(
        type = type,
        ssid = ssid,
        ip = ip,
    )
}
