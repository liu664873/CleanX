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

    fun hasRequestedBefore(permission: AppPermission): Boolean = hasDenied(permission)

    fun markRequested(permission: AppPermission) = Unit

    fun shouldRequestRuntimePermission(
        context: Context,
        permission: AppPermission,
        runtimePermissions: Array<String>,
    ): Boolean = !hasDenied(permission)
}

object NoOpRuntimePermissionDenialStore : RuntimePermissionDenialStore {
    override fun hasDenied(permission: AppPermission): Boolean = false

    override fun markDenied(permission: AppPermission) = Unit
}
