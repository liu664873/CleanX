package com.quickcleanpro.phonecleaner.presentation.common.route

fun interface NavigationInterceptor {
    fun intercept(event: AppNavigationEvent): InterceptResult
}

sealed interface InterceptResult {
    data object Proceed : InterceptResult
    data object Block : InterceptResult
    data class Redirect(val event: AppNavigationEvent) : InterceptResult
}

/**
 * Automatically inserts an ad before navigating to routes that have a
 * configured [adPlacements] entry.
 *
 * @param adPlacements mapping from route (e.g. "scan") to ad placement ID.
 *        Routes not in this map pass through without an ad.
 */
class AdInterceptor(
    val adPlacements: Map<String, String>,
) : NavigationInterceptor {
    override fun intercept(event: AppNavigationEvent): InterceptResult {
        val route = (event as? AppNavigationEvent.Destination)?.route ?: return InterceptResult.Proceed
        val placement = adPlacements[route] ?: return InterceptResult.Proceed
        return InterceptResult.Redirect(
            AppNavigationEvent.AdDestination(route, placement)
        )
    }
}
