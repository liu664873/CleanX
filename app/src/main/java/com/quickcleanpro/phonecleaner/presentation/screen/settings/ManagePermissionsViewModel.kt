package com.quickcleanpro.phonecleaner.presentation.screen.settings

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quickcleanpro.phonecleaner.domain.repository.SettingsRepository
import com.quickcleanpro.phonecleaner.presentation.common.permission.CleanXPermissionRequestManager
import com.quickcleanpro.phonecleaner.presentation.common.permission.CleanXPermissionType
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
    val label: String,
    val type: CleanXPermissionType,
    val checked: Boolean,
)

data class ManagePermissionsUiState(
    val rows: List<ManagePermissionRowState> = emptyList(),
)

sealed interface ManagePermissionsEvent {
    data class LaunchRuntimePermissions(val permissions: List<String>) : ManagePermissionsEvent
    data class LaunchSettings(val intents: List<Intent>) : ManagePermissionsEvent
}

private data class ManagePermissionDefinition(
    val label: String,
    val type: CleanXPermissionType,
)

private val permissionDefinitions =
    listOf(
        ManagePermissionDefinition("Storage Permissions", CleanXPermissionType.StorageFiles),
        ManagePermissionDefinition("Usage Data Permissions", CleanXPermissionType.UsageAccess),
        ManagePermissionDefinition("Location Management", CleanXPermissionType.Location),
        ManagePermissionDefinition("Notification Toolbar", CleanXPermissionType.NotificationListener),
    )

private fun initialRows(): List<ManagePermissionRowState> =
    permissionDefinitions.map { definition ->
        ManagePermissionRowState(
            label = definition.label,
            type = definition.type,
            checked = false,
        )
    }

class ManagePermissionsViewModel(
    private val settingsRepository: SettingsRepository,
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
        type: CleanXPermissionType,
    ) {
        val appContext = context.applicationContext ?: context
        val missingRuntimePermissions =
            CleanXPermissionRequestManager.runtimePermissions(type)
                .filter { permission ->
                    ContextCompat.checkSelfPermission(appContext, permission) != PackageManager.PERMISSION_GRANTED
                }

        viewModelScope.launch {
            if (missingRuntimePermissions.isNotEmpty()) {
                eventsChannel.send(ManagePermissionsEvent.LaunchRuntimePermissions(missingRuntimePermissions))
                return@launch
            }

            val intents =
                listOfNotNull(
                    CleanXPermissionRequestManager.primarySettingsIntent(type, settingsRepository),
                    CleanXPermissionRequestManager.fallbackSettingsIntent(type, settingsRepository),
                    runCatching { settingsRepository.appSettingsIntent() }.getOrNull(),
                ).distinctBy { it.toUri(0) }
            eventsChannel.send(ManagePermissionsEvent.LaunchSettings(intents))
        }
    }

    fun onRuntimePermissionsResult(
        context: Context,
        grants: Map<String, Boolean>,
    ) {
        if (grants[Manifest.permission.ACCESS_FINE_LOCATION] == false) {
            runCatching { settingsRepository.saveLocationRuntimePermissionDenied() }
        }
        if (grants[Manifest.permission.POST_NOTIFICATIONS] == false) {
            runCatching { settingsRepository.saveNotificationRuntimePermissionDenied() }
        }
        refresh(context, refreshAgainAfterDelay = true)
    }

    fun onSettingsResult(context: Context) {
        refresh(context, refreshAgainAfterDelay = true)
    }

    fun onResume(context: Context) {
        refresh(context, refreshAgainAfterDelay = true)
    }

    private fun buildRows(context: Context): List<ManagePermissionRowState> =
        permissionDefinitions.map { definition ->
            permissionRow(definition, context)
        }

    private fun permissionRow(
        definition: ManagePermissionDefinition,
        context: Context,
    ): ManagePermissionRowState =
        ManagePermissionRowState(
            label = definition.label,
            type = definition.type,
            checked = isPermissionGrantedFresh(context, definition.type),
        )

    private fun isPermissionGrantedFresh(
        context: Context,
        type: CleanXPermissionType,
    ): Boolean {
        if (type == CleanXPermissionType.UsageAccess) {
            runCatching { settingsRepository.resetAppUsagePermissionCache() }
        }
        return CleanXPermissionRequestManager.isGranted(context, type, settingsRepository)
    }
}
