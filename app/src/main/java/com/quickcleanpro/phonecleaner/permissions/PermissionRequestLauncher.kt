package com.quickcleanpro.phonecleaner.permissions

import android.content.Context
import com.quickcleanpro.phonecleaner.core.permission.AppPermission
import com.quickcleanpro.phonecleaner.core.permission.PermissionHandler
import com.quickcleanpro.phonecleaner.core.permission.commonPermissionHandlers

class PermissionRequestLauncher(
    private val context: Context,
    handlers: List<PermissionHandler> = commonPermissionHandlers(),
) : PermissionController {
    private val capability = PermissionCapability(handlers)
    private val handlersByPermission = handlers.associateBy { it.permission.key }

    override fun state(permission: AppPermission): PermissionState =
        capability.state(context, permission)

    override fun request(
        permission: AppPermission,
        onResult: (PermissionResult) -> Unit,
    ) {
        val handler = handlersByPermission[permission.key]
        if (handler == null) {
            onResult(PermissionResult.Unavailable)
            return
        }
        val settingsIntent = handler.settingsIntents(context).firstOrNull()
        if (settingsIntent == null) {
            onResult(PermissionResult.Unavailable)
            return
        }
        val launched = runCatching {
            context.startActivity(settingsIntent)
        }.isSuccess
        onResult(
            if (launched) {
                PermissionResult.Denied(permanently = false)
            } else {
                PermissionResult.Unavailable
            },
        )
    }
}
