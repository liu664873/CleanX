package com.quickcleanpro.phonecleaner.core.permission

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

class PermissionManager<F : PermissionFeature>(
    specs: List<PermissionSpec<F>>,
    handlers: List<PermissionHandler> = commonPermissionHandlers(),
    private val denialStore: RuntimePermissionDenialStore = NoOpRuntimePermissionDenialStore,
) {
    private val specsByFeature = specs.associateBy { it.feature.key }
    private val handlersByPermission = handlers.associateBy { it.permission.key }
    private val pendingRuntimeRequestsByFeature = mutableMapOf<String, Map<String, AppPermission>>()

    fun status(
        context: Context,
        feature: F,
    ): PermissionStatus {
        val missing = missingPermissions(context, feature)
        return PermissionStatus(
            granted = missing.isEmpty(),
            missing = missing,
        )
    }

    fun missingPermissions(
        context: Context,
        feature: F,
    ): List<AppPermission> =
        specOf(feature)
            .permissions
            .filter { permission ->
                val handler = handlersByPermission[permission.key] ?: return@filter true
                !runCatching { handler.isGranted(context) }.getOrDefault(false)
            }

    fun requestPlan(
        context: Context,
        feature: F,
    ): PermissionRequestPlan {
        val missing = missingPermissions(context, feature)
        if (missing.isEmpty()) return PermissionRequestPlan.AlreadyGranted

        val permission = missing.first()
        val handler = handlersByPermission[permission.key] ?: return PermissionRequestPlan.Unavailable
        val runtimePermissions =
            handler.runtimePermissions(context)
                .filter { runtimePermission ->
                    ContextCompat.checkSelfPermission(context, runtimePermission) != PackageManager.PERMISSION_GRANTED
                }
                .toTypedArray()
        if (runtimePermissions.isNotEmpty() && !denialStore.hasDenied(permission)) {
            pendingRuntimeRequestsByFeature[feature.key] = runtimePermissions.associateWith { permission }
            return PermissionRequestPlan.RequestRuntime(runtimePermissions)
        }

        pendingRuntimeRequestsByFeature.remove(feature.key)
        val settingsIntents = handler.settingsIntents(context).distinctBy { it.toUri(0) }
        return if (settingsIntents.isNotEmpty()) {
            PermissionRequestPlan.OpenSettings(settingsIntents)
        } else {
            PermissionRequestPlan.Unavailable
        }
    }

    fun settingsPlan(
        context: Context,
        feature: F,
    ): PermissionRequestPlan {
        val missing = missingPermissions(context, feature)
        if (missing.isEmpty()) return PermissionRequestPlan.AlreadyGranted

        val permission = missing.first()
        val handler = handlersByPermission[permission.key] ?: return PermissionRequestPlan.Unavailable
        val settingsIntents = handler.settingsIntents(context).distinctBy { it.toUri(0) }
        return if (settingsIntents.isNotEmpty()) {
            PermissionRequestPlan.OpenSettings(settingsIntents)
        } else {
            PermissionRequestPlan.Unavailable
        }
    }

    fun onRuntimeResult(
        feature: F,
        result: Map<String, Boolean>,
    ) {
        val requestedPermissions = pendingRuntimeRequestsByFeature.remove(feature.key).orEmpty()
        if (result.isEmpty() || result.values.all { it }) return
        result
            .filterValues { granted -> !granted }
            .keys
            .mapNotNull { requestedPermissions[it] }
            .distinctBy { it.key }
            .forEach { permission ->
                denialStore.markDenied(permission)
            }
    }

    fun onRuntimeResult(
        context: Context,
        feature: F,
        result: Map<String, Boolean>,
    ) {
        val requestedPermissions = pendingRuntimeRequestsByFeature.remove(feature.key).orEmpty()
        if (result.isEmpty() || result.values.all { it }) return
        if (requestedPermissions.isNotEmpty()) {
            result
                .filterValues { granted -> !granted }
                .keys
                .mapNotNull { requestedPermissions[it] }
                .distinctBy { it.key }
                .forEach { permission ->
                    denialStore.markDenied(permission)
                }
            return
        }

        specOf(feature).permissions
            .filter { permission ->
                val handler = handlersByPermission[permission.key] ?: return@filter false
                handler.runtimePermissions(context).any { result[it] == false }
            }.forEach { permission ->
                denialStore.markDenied(permission)
            }
    }

    private fun specOf(feature: F): PermissionSpec<F> =
        specsByFeature[feature.key]
            ?: error("No permission spec registered for feature: ${feature.key}")
}
