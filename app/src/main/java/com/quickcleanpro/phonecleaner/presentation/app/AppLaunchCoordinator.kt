package com.quickcleanpro.phonecleaner.presentation.app

import android.content.Intent
import android.os.SystemClock
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.quickcleanpro.phonecleaner.data.source.notification.ToolNotificationIntentFactory
import com.quickcleanpro.phonecleaner.navigation.AppRoute

sealed interface AppLaunchRequest {
    data object Normal : AppLaunchRequest
    data class NotificationTarget(val route: String) : AppLaunchRequest
    data class ForegroundReturn(val previousRoute: String) : AppLaunchRequest
}

class AppLaunchCoordinator(
    private val foregroundResumeIntervalMs: Long = DEFAULT_FOREGROUND_RESUME_INTERVAL_MS,
    private val elapsedRealtime: () -> Long = SystemClock::elapsedRealtime,
    private val targetRouteResolver: (Intent?) -> String? = ToolNotificationIntentFactory::targetRoute,
) {
    var pendingRequest by mutableStateOf<AppLaunchRequest>(AppLaunchRequest.Normal)
        private set

    var currentRoute by mutableStateOf<String?>(null)
        private set

    private var started = false
    private var stoppedRoute: String? = null
    private var stoppedAtElapsedMs: Long? = null
    private var externalActivityLaunchPending = false

    fun onCreate(intent: Intent?) {
        pendingRequest = targetRouteResolver(intent)
            ?.let(AppLaunchRequest::NotificationTarget)
            ?: AppLaunchRequest.Normal
    }

    fun onNewIntent(intent: Intent?) {
        targetRouteResolver(intent)?.let { route ->
            pendingRequest = AppLaunchRequest.NotificationTarget(route)
        }
    }

    fun onRouteChanged(route: String?) {
        currentRoute = route
    }

    fun onStart() {
        if (!started) {
            started = true
            clearStoppedState()
            return
        }
        if (pendingRequest is AppLaunchRequest.NotificationTarget) {
            clearStoppedState()
            return
        }
        if (externalActivityLaunchPending) {
            externalActivityLaunchPending = false
            clearStoppedState()
            return
        }
        val route = stoppedRoute
        val stoppedAt = stoppedAtElapsedMs
        val backgroundDurationMs =
            if (stoppedAt == null) {
                0L
            } else {
                elapsedRealtime() - stoppedAt
            }
        if (route != null &&
            route !in startupRoutes &&
            backgroundDurationMs >= foregroundResumeIntervalMs
        ) {
            pendingRequest = AppLaunchRequest.ForegroundReturn(route)
        }
        clearStoppedState()
    }

    fun onStop() {
        stoppedRoute = currentRoute?.takeIf { it !in startupRoutes }
        stoppedAtElapsedMs = stoppedRoute?.let { elapsedRealtime() }
    }

    fun onResume() {
        if (stoppedAtElapsedMs == null) {
            externalActivityLaunchPending = false
        }
    }

    fun markExternalActivityLaunch() {
        externalActivityLaunchPending = true
    }

    fun cancelExternalActivityLaunch() {
        externalActivityLaunchPending = false
    }

    fun consumeRequest(): AppLaunchRequest {
        val request = pendingRequest
        pendingRequest = AppLaunchRequest.Normal
        return request
    }

    fun consumeRequestIfCurrent(request: AppLaunchRequest): Boolean {
        if (pendingRequest != request) return false
        pendingRequest = AppLaunchRequest.Normal
        return true
    }

    private fun clearStoppedState() {
        stoppedRoute = null
        stoppedAtElapsedMs = null
    }

    private companion object {
        const val DEFAULT_FOREGROUND_RESUME_INTERVAL_MS = 30_000L

        val startupRoutes =
            setOf(
                AppRoute.Splash.value,
                AppRoute.OnboardingScan.value,
            )
    }
}
