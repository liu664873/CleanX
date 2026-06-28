package com.quickcleanpro.phonecleaner.operation

import com.quickcleanpro.phonecleaner.config.FeatureKey

sealed interface FeatureOperationEvent {
    val feature: FeatureKey

    data class ScanStarted(
        override val feature: FeatureKey,
    ) : FeatureOperationEvent

    data class ScanFinished(
        override val feature: FeatureKey,
        val hasResult: Boolean,
    ) : FeatureOperationEvent

    data class ActionRequested(
        override val feature: FeatureKey,
        val action: OperationAction,
    ) : FeatureOperationEvent

    data class OperationStarted(
        override val feature: FeatureKey,
        val action: OperationAction,
    ) : FeatureOperationEvent

    data class OperationFinished(
        override val feature: FeatureKey,
        val action: OperationAction,
        val success: Boolean,
    ) : FeatureOperationEvent

    data class ResultShown(
        override val feature: FeatureKey,
    ) : FeatureOperationEvent

    data class PermissionRejected(
        override val feature: FeatureKey,
    ) : FeatureOperationEvent

    data class ReturnHome(
        override val feature: FeatureKey,
    ) : FeatureOperationEvent
}

enum class OperationAction {
    CLEAN,
    DELETE,
    REMOVE_LOCATION,
}
