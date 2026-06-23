package com.quickcleanpro.phonecleaner.core.permission

import android.content.Context
import android.content.Intent

interface PermissionHandler {
    val permission: AppPermission

    fun isGranted(context: Context): Boolean

    fun runtimePermissions(context: Context): List<String>

    fun settingsIntents(context: Context): List<Intent>
}

interface RuntimePermissionDenialStore {
    fun hasDenied(permission: AppPermission): Boolean

    fun markDenied(permission: AppPermission)
}

object NoOpRuntimePermissionDenialStore : RuntimePermissionDenialStore {
    override fun hasDenied(permission: AppPermission): Boolean = false

    override fun markDenied(permission: AppPermission) = Unit
}

