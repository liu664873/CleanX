package com.quickcleanpro.phonecleaner.presentation.common.route

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.navigation.NavHostController
import com.quickcleanpro.phonecleaner.navigation.AppNavigator
import com.quickcleanpro.phonecleaner.navigation.AppRoute
import com.quickcleanpro.phonecleaner.navigation.NavigationCommand

class RouteManager(
    val navController: NavHostController,
    private val interceptors: MutableList<NavigationInterceptor> = mutableListOf(),
    private val adManager: AdManager = NoOpAdManager,
) : AppNavigator {
    fun addInterceptor(interceptor: NavigationInterceptor) {
        interceptors.add(interceptor)
    }

    fun removeInterceptor(interceptor: NavigationInterceptor) {
        interceptors.remove(interceptor)
    }

    fun navigate(screen: Screen) = execute(AppNavigationEvent.Destination(screen.route))

    fun navigate(route: AppRoute) = execute(AppNavigationEvent.Destination(route.value))

    fun navigate(
        screen: Screen,
        args: Map<String, String>,
    ) = execute(AppNavigationEvent.Destination(screen.route, args))

    fun goBack() = execute(AppNavigationEvent.Back)

    fun goHome() = execute(AppNavigationEvent.Home)

    fun navigateAndClearStack(screen: Screen) = execute(AppNavigationEvent.ReplaceStack(screen.route))

    fun navigateAndClearStack(route: AppRoute) = execute(AppNavigationEvent.ReplaceStack(route.value))

    fun replaceCurrent(screen: Screen) = execute(AppNavigationEvent.ReplaceCurrent(screen.route))

    fun replaceCurrent(route: AppRoute) = execute(AppNavigationEvent.ReplaceCurrent(route.value))

    fun navigate(event: AppNavigationEvent): Boolean = execute(event)

    override fun navigate(command: NavigationCommand): Boolean =
        when (command) {
            NavigationCommand.Back -> goBack()
            is NavigationCommand.ClearTo -> navigateAndClearStack(command.route)
            NavigationCommand.Home -> {
                goHome()
                true
            }
            is NavigationCommand.Replace -> replaceCurrent(command.route)
            is NavigationCommand.To -> execute(AppNavigationEvent.Destination(command.route.value, command.args))
        }

    fun navigate(
        route: String,
        args: Map<String, String> = emptyMap(),
    ) = navigate(AppNavigationEvent.Destination(route, args))

    override fun back(): Boolean = goBack()

    override fun home() {
        goHome()
    }

    fun navigateAndClearStack(route: String) = execute(AppNavigationEvent.ReplaceStack(route))

    fun replaceCurrent(route: String) = execute(AppNavigationEvent.ReplaceCurrent(route))

    fun showAd(
        placement: String,
        onComplete: () -> Unit,
    ) {
        adManager.showAd(placement, onComplete)
    }

    private fun execute(event: AppNavigationEvent): Boolean {
        val finalEvent = runInterceptors(event) ?: return false
        navController.handleNavigationEvent(finalEvent, adManager)
        return true
    }

    private fun runInterceptors(event: AppNavigationEvent): AppNavigationEvent? {
        var current = event
        for (interceptor in interceptors) {
            when (val result = interceptor.intercept(current)) {
                InterceptResult.Proceed -> continue
                InterceptResult.Block -> return null
                is InterceptResult.Redirect -> current = result.event
            }
        }
        return current
    }
}

val LocalRouter =
    staticCompositionLocalOf<RouteManager> {
        error("RouteManager not provided. Wrap root with CompositionLocalProvider(LocalRouter provides ...)")
    }
