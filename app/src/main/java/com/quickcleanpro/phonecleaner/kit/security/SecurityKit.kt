package com.quickcleanpro.phonecleaner.kit.security

import android.content.Context

class SecurityKit private constructor(val appPackageName: String) {
    companion object {
        fun create(context: Context): SecurityKit =
            SecurityKit(context.applicationContext.packageName)
    }
}
