package com.quickcleanpro.phonecleaner.presentation.common.route

fun interface NavigationInterceptor {
    fun intercept(event: AppNavigationEvent): InterceptResult
}

sealed interface InterceptResult {
    data object Proceed : InterceptResult
    data object Block : InterceptResult
    data class Redirect(val event: AppNavigationEvent) : InterceptResult
}

// ==================== Built-in Interceptors ====================

class PermissionInterceptor(
    private val requiredPermissions: Map<String, Set<String>> = emptyMap(),
    private val onRequestPermission: (Set<String>) -> Unit = {},
) : NavigationInterceptor {
    override fun intercept(event: AppNavigationEvent): InterceptResult {
        val route = (event as? AppNavigationEvent.Destination)?.route ?: return InterceptResult.Proceed
        val perms = requiredPermissions[route] ?: return InterceptResult.Proceed
        // TODO: integrate with real permission API
        return InterceptResult.Proceed
    }
}

class AdInterceptor(
    private val adPlacements: Map<String, String> = emptyMap(),
) : NavigationInterceptor {
    override fun intercept(event: AppNavigationEvent): InterceptResult {
        val route = (event as? AppNavigationEvent.Destination)?.route ?: return InterceptResult.Proceed
        val placement = adPlacements[route] ?: return InterceptResult.Proceed
        return InterceptResult.Redirect(
            AppNavigationEvent.AdDestination(route, placement)
        )
    }
}
