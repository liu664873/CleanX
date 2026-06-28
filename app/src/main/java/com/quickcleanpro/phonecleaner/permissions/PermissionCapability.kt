package com.quickcleanpro.phonecleaner.permissions

import android.content.Context
import com.quickcleanpro.phonecleaner.core.permission.AppPermission
import com.quickcleanpro.phonecleaner.core.permission.PermissionHandler
import com.quickcleanpro.phonecleaner.core.permission.commonPermissionHandlers

class PermissionCapability(
    handlers: List<PermissionHandler> = commonPermissionHandlers(),
) {
    private val handlersByPermission = handlers.associateBy { it.permission.key }

    fun state(
        context: Context,
        permission: AppPermission,
    ): PermissionState {
        val handler = handlersByPermission[permission.key] ?: return PermissionState.Unavailable
        return if (runCatching { handler.isGranted(context) }.getOrDefault(false)) {
            PermissionState.Granted
        } else {
            PermissionState.Denied
        }
    }
}
