package com.quickcleanpro.phonecleaner.presentation.app

import android.content.Intent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.quickcleanpro.phonecleaner.data.source.notification.ToolNotificationIntentFactory
import com.quickcleanpro.phonecleaner.presentation.common.route.Screen

sealed interface AppLaunchRequest {
    data object Normal : AppLaunchRequest
    data class NotificationTarget(val route: String) : AppLaunchRequest
    data class ForegroundReturn(val previousRoute: String) : AppLaunchRequest
}

class AppLaunchCoordinator {
    var pendingRequest by mutableStateOf<AppLaunchRequest>(AppLaunchRequest.Normal)
        private set

    var currentRoute by mutableStateOf<String?>(null)
        private set

    private var started = false
    private var stoppedRoute: String? = null

    fun onCreate(intent: Intent?) {
        pendingRequest = ToolNotificationIntentFactory.targetRoute(intent)
            ?.let(AppLaunchRequest::NotificationTarget)
            ?: AppLaunchRequest.Normal
    }

    fun onNewIntent(intent: Intent?) {
        ToolNotificationIntentFactory.targetRoute(intent)?.let { route ->
            pendingRequest = AppLaunchRequest.NotificationTarget(route)
        }
    }

    fun onRouteChanged(route: String?) {
        currentRoute = route
    }

    fun onStart() {
        if (!started) {
            started = true
            return
        }
        if (pendingRequest is AppLaunchRequest.NotificationTarget) return
        val route = stoppedRoute
        if (route != null && route !in startupRoutes) {
            pendingRequest = AppLaunchRequest.ForegroundReturn(route)
        }
        stoppedRoute = null
    }

    fun onStop() {
        stoppedRoute = currentRoute?.takeIf { it !in startupRoutes }
    }

    fun consumeRequest(): AppLaunchRequest {
        val request = pendingRequest
        pendingRequest = AppLaunchRequest.Normal
        return request
    }

    private companion object {
        val startupRoutes =
            setOf(
                Screen.Splash.route,
                Screen.OnboardingScan.route,
            )
    }
}
