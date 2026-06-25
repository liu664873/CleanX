package com.quickcleanpro.phonecleaner.presentation.screen.files.common

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/**
 * Base ViewModel for all file manager screens.
 *
 * Extracts the shared [FileOperationRunner] lifecycle and the common
 * [cancelActiveOperation] / [cancelDeletingAndReturnToBrowsing] methods
 * that are otherwise duplicated across 9 ViewModels.
 */
internal abstract class BaseFileManagerViewModel(
    protected val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    testLoader: (((suspend () -> Unit)) -> Unit)? = null,
) : ViewModel() {

    protected val operationRunner = FileOperationRunner(viewModelScope, ioDispatcher, testLoader)

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
}
