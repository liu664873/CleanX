package com.quickcleanpro.phonecleaner.presentation.screen.files.common

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quickcleanpro.phonecleaner.config.FeatureKey
import com.quickcleanpro.phonecleaner.domain.model.file.ManagedFileItem
import com.quickcleanpro.phonecleaner.operation.FeatureOperationEvent
import com.quickcleanpro.phonecleaner.operation.OperationAction
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

/**
 * Base ViewModel for all file manager screens.
 *
 * Extracts the shared [FileOperationRunner] lifecycle and the common
 * [cancelActiveOperation] / [cancelDeletingAndReturnToBrowsing] methods
 * that are otherwise duplicated across 9 ViewModels.
 */
internal abstract class BaseFileManagerViewModel(
    protected val featureKey: FeatureKey,
    protected val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    testLoader: (((suspend () -> Unit)) -> Unit)? = null,
) : ViewModel() {
    protected data class FileOperationOutcome(
        val freedBytes: Long = 0L,
        val changedCount: Int = 0,
    )

    protected val operationRunner = FileOperationRunner(viewModelScope, ioDispatcher, testLoader)
    private val operationEventsChannel = Channel<FeatureOperationEvent>(Channel.BUFFERED)
    val operationEvents: Flow<FeatureOperationEvent> = operationEventsChannel.receiveAsFlow()

    /**
     * Cancel any running scan or delete operation without changing UI state.
     */
    fun cancelActiveOperation() {
        operationRunner.cancelActiveOperation()
    }

    /**
     * Cancel a running delete and reset the phase back to browsing.
     * Safe to call during any phase — only transitions if currently [FileOperationPhase.Deleting].
     */
    fun cancelDeletingAndReturnToBrowsing() {
        operationRunner.cancelActiveOperation()
        onCancelDeletingPhase()
    }

    /**
     * Hook for subclasses to reset their phase from Deleting → Browsing.
     */
    protected abstract fun onCancelDeletingPhase()

    protected fun trackScanStarted() {
        trackOperationEvent(FeatureOperationEvent.ScanStarted(featureKey))
    }

    protected fun trackScanFinished(hasResult: Boolean) {
        trackOperationEvent(FeatureOperationEvent.ScanFinished(featureKey, hasResult))
    }

    protected fun trackActionRequested(action: OperationAction) {
        trackOperationEvent(FeatureOperationEvent.ActionRequested(featureKey, action))
    }

    protected fun trackOperationEvent(event: FeatureOperationEvent) {
        operationEventsChannel.trySend(event)
    }

    protected fun runFileOperation(
        selectedFiles: List<ManagedFileItem>,
        action: OperationAction,
        onEmptySelection: () -> Unit,
        onStart: () -> Unit,
        operationDelayMillis: Long,
        completeDelayMillis: Long,
        operation: suspend () -> FileOperationOutcome,
        isSuccessful: (FileOperationOutcome) -> Boolean = { true },
        onCompleteAnimation: suspend (FileOperationOutcome) -> Unit,
        onResult: () -> Unit,
        onFailure: (Throwable) -> Unit,
    ) {
        if (selectedFiles.isEmpty()) {
            onEmptySelection()
            return
        }

        onStart()
        trackOperationEvent(FeatureOperationEvent.OperationStarted(featureKey, action))
        operationRunner.launch {
            runCatching {
                val outcome = operation()
                if (!isSuccessful(outcome)) {
                    error(deletionFailedMessage())
                }
                operationRunner.delayIfNeeded(operationDelayMillis)
                onCompleteAnimation(outcome)
                trackOperationEvent(FeatureOperationEvent.OperationFinished(featureKey, action, success = true))
                operationRunner.delayIfNeeded(completeDelayMillis)
                onResult()
                trackOperationEvent(FeatureOperationEvent.ResultShown(featureKey))
            }.onFailure { error ->
                if (error is CancellationException) throw error
                trackOperationEvent(FeatureOperationEvent.OperationFinished(featureKey, action, success = false))
                onFailure(error)
            }
        }
    }
}
