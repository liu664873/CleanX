package com.quickcleanpro.phonecleaner.presentation.common.permission

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner

@Composable
fun rememberPermissionGranted(permissionGateConfig: PermissionGateConfig?): Boolean {
    val feature = permissionGateConfig?.cleanXFeature ?: return true
    return rememberPermissionGranted(feature)
}

@Composable
fun rememberPermissionGranted(feature: CleanXFeature?): Boolean {
    if (feature == null) return true

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val permissionManager = remember(context) {
        CleanXPermissionRegistry.permissionManager(context)
    }

    fun checkGranted(): Boolean =
        permissionManager.status(
            context = context,
            feature = feature,
        ).granted

    var granted by remember(feature, permissionManager) {
        mutableStateOf(checkGranted())
    }

    DisposableEffect(lifecycleOwner, feature, permissionManager) {
        val observer =
            LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME) {
                    granted = checkGranted()
                }
            }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    return granted
}

