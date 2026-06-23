package com.quickcleanpro.phonecleaner.presentation.screen.settings

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quickcleanpro.phonecleaner.core.permission.PermissionRequestPlan
import com.quickcleanpro.phonecleaner.presentation.common.permission.CleanXFeature
import com.quickcleanpro.phonecleaner.presentation.common.permission.CleanXPermissionRegistry
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

data class ManagePermissionRowState(
    val labelRes: Int,
    val feature: CleanXFeature,
    val checked: Boolean,
)

data class ManagePermissionsUiState(
    val rows: List<ManagePermissionRowState> = emptyList(),
)

sealed interface ManagePermissionsEvent {
    data class LaunchRuntimePermissions(
        val permissions: List<String>,
    ) : ManagePermissionsEvent

    data class LaunchSettings(
        val intents: List<Intent>,
    ) : ManagePermissionsEvent
}

private fun initialRows(): List<ManagePermissionRowState> =
    CleanXPermissionRegistry.manageItems.map { item ->
        ManagePermissionRowState(
            labelRes = item.labelRes,
            feature = item.feature,
            checked = false,
        )
    }

class ManagePermissionsViewModel(
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ManagePermissionsUiState(rows = initialRows()))
    val uiState: StateFlow<ManagePermissionsUiState> = _uiState.asStateFlow()

    private val eventsChannel = Channel<ManagePermissionsEvent>(Channel.BUFFERED)
    val events = eventsChannel.receiveAsFlow()

    fun load(context: Context) {
        refresh(context)
    }

    fun refresh(
        context: Context,
        refreshAgainAfterDelay: Boolean = false,
    ) {
        val appContext = context.applicationContext ?: context
        _uiState.value = ManagePermissionsUiState(rows = buildRows(appContext))
        if (refreshAgainAfterDelay) {
            viewModelScope.launch(ioDispatcher) {
                delay(300L)
                _uiState.value = ManagePermissionsUiState(rows = buildRows(appContext))
            }
        }
    }

    fun requestPermission(
        context: Context,
        feature: CleanXFeature,
    ) {
        val appContext = context.applicationContext ?: context
        val manager = CleanXPermissionRegistry.permissionManager(appContext)
        viewModelScope.launch {
            when (val plan = manager.requestPlan(appContext, feature)) {
                PermissionRequestPlan.AlreadyGranted -> refresh(appContext, refreshAgainAfterDelay = true)
                is PermissionRequestPlan.RequestRuntime -> {
                    eventsChannel.send(ManagePermissionsEvent.LaunchRuntimePermissions(plan.permissions.toList()))
                }
                is PermissionRequestPlan.OpenSettings -> {
                    eventsChannel.send(ManagePermissionsEvent.LaunchSettings(plan.intents))
                }
                PermissionRequestPlan.Unavailable -> refresh(appContext, refreshAgainAfterDelay = true)
            }
        }
    }

    fun onRuntimePermissionsResult(
        context: Context,
        grants: Map<String, Boolean>,
    ) {
        val appContext = context.applicationContext ?: context
        val manager = CleanXPermissionRegistry.permissionManager(appContext)
        CleanXPermissionRegistry.manageItems.forEach { item ->
            manager.onRuntimeResult(appContext, item.feature, grants)
        }
        refresh(appContext, refreshAgainAfterDelay = true)
    }

    fun onSettingsResult(context: Context) {
        refresh(context, refreshAgainAfterDelay = true)
    }

    fun onResume(context: Context) {
        refresh(context, refreshAgainAfterDelay = true)
    }

    private fun buildRows(context: Context): List<ManagePermissionRowState> {
        val manager = CleanXPermissionRegistry.permissionManager(context)
        return CleanXPermissionRegistry.manageItems.map { item ->
            ManagePermissionRowState(
                labelRes = item.labelRes,
                feature = item.feature,
                checked = manager.status(context, item.feature).granted,
            )
        }
    }
}
