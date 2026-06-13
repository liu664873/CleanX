package com.quickcleanpro.phonecleaner.common

sealed interface KitResult<out T> {
    data class Success<T>(
        val value: T,
    ) : KitResult<T>

    data class Failure(
        val throwable: Throwable,
    ) : KitResult<Nothing>
}
