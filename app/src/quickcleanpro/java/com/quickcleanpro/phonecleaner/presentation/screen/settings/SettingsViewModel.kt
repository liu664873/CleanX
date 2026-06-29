package com.quickcleanpro.phonecleaner.presentation.screen.settings

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import com.quickcleanpro.phonecleaner.R
import com.quickcleanpro.phonecleaner.domain.repository.SettingsRepository
import com.quickcleanpro.phonecleaner.presentation.common.appContextOrPreview
import com.quickcleanpro.phonecleaner.presentation.common.permission.CleanXPermissionItem
import com.quickcleanpro.phonecleaner.presentation.common.permission.CleanXPermissionRegistry
import com.quickcleanpro.phonecleaner.presentation.common.settingsRepositoryOrPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

enum class SettingsPermissionType(@StringRes val titleRes: Int) {
    Storage(R.string.settings_storage_permission),
    UsageData(R.string.settings_usage_data_permission),
    Location(R.string.settings_location_permission),
    Notification(R.string.settings_notification_permission),
}

data class SettingsPermissionItem(
    val type: SettingsPermissionType,
    @StringRes val titleRes: Int,
    val checked: Boolean,
)

data class SettingsUiState(
    val temperatureUnit: String = "C",
    val permissions: List<SettingsPermissionItem> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
)

class SettingsViewModel(
    private val repository: SettingsRepository = settingsRepositoryOrPreview(),
) : ViewModel() {
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        refreshState()
    }

    fun refreshState() {
        val snapshot = readSnapshot()
        _uiState.value =
            SettingsUiState(
                temperatureUnit = snapshot.temperatureUnit,
                permissions = buildPermissionItems(snapshot),
                isLoading = false,
            )
    }

    fun updateTemperatureUnit(unit: String) {
        if (unit != "C" && unit != "F") return
        repository.saveTemperatureUnit(unit)
        _uiState.update { it.copy(temperatureUnit = unit) }
    }

    private fun readSnapshot(): SettingsSnapshot {
        val context = appContextOrPreview()
        val manager = CleanXPermissionRegistry.permissionItemManager(context)
        return SettingsSnapshot(
            temperatureUnit = repository.readTemperatureUnit(),
            hasStoragePermission = manager.status(context, CleanXPermissionItem.StorageFiles).granted,
            hasAppUsageAccess = manager.status(context, CleanXPermissionItem.UsageAccess).granted,
            hasLocationPermission = manager.status(context, CleanXPermissionItem.Location).granted,
            hasNotificationAccess = manager.status(context, CleanXPermissionItem.NotificationListener).granted,
        )
    }

    private fun buildPermissionItems(snapshot: SettingsSnapshot): List<SettingsPermissionItem> =
        listOf(
            SettingsPermissionItem(SettingsPermissionType.Storage, SettingsPermissionType.Storage.titleRes, snapshot.hasStoragePermission),
            SettingsPermissionItem(SettingsPermissionType.UsageData, SettingsPermissionType.UsageData.titleRes, snapshot.hasAppUsageAccess),
            SettingsPermissionItem(SettingsPermissionType.Location, SettingsPermissionType.Location.titleRes, snapshot.hasLocationPermission),
            SettingsPermissionItem(SettingsPermissionType.Notification, SettingsPermissionType.Notification.titleRes, snapshot.hasNotificationAccess),
        )
}

internal fun SettingsPermissionType.toPermissionItem(): CleanXPermissionItem =
    when (this) {
        SettingsPermissionType.Storage -> CleanXPermissionItem.StorageFiles
        SettingsPermissionType.UsageData -> CleanXPermissionItem.UsageAccess
        SettingsPermissionType.Location -> CleanXPermissionItem.Location
        SettingsPermissionType.Notification -> CleanXPermissionItem.NotificationListener
    }

private data class SettingsSnapshot(
    val temperatureUnit: String,
    val hasStoragePermission: Boolean,
    val hasAppUsageAccess: Boolean,
    val hasLocationPermission: Boolean,
    val hasNotificationAccess: Boolean,
)
