package com.quickcleanpro.phonecleaner.presentation.common.route

sealed interface AppNavigationEvent {
    data object Back : AppNavigationEvent

    data object Home : AppNavigationEvent

    /** Navigate to a route with optional args (appended as query params). */
    data class Destination(
        val route: String,
        val args: Map<String, String> = emptyMap(),
    ) : AppNavigationEvent

    /** Clear the back stack, then navigate to [route]. */
    data class ReplaceStack(
        val route: String,
    ) : AppNavigationEvent

    /** Remove the current destination from the stack, then navigate to [route]. */
    data class ReplaceCurrent(
        val route: String,
    ) : AppNavigationEvent

    /** Play an ad for [placement], then navigate to [route] when complete. */
    data class AdDestination(
        val route: String,
        val placement: String,
    ) : AppNavigationEvent
}
