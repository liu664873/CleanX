package com.quickcleanpro.phonecleaner.utils

import android.content.Context
import android.content.Intent
import android.net.Uri

fun Context.openUrl(url: String): Boolean =
    runCatching {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        true
    }.getOrDefault(false)
