package com.quickcleanpro.phonecleaner.domain.model.applock

data class AppLockApp(
    val packageName: String,
    val appName: String,
    val isLocked: Boolean,
)
