package com.quickcleanpro.phonecleaner.presentation.app

import android.content.Intent
import com.quickcleanpro.phonecleaner.presentation.common.route.Screen
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AppLaunchCoordinatorTest {
    private var now = 0L

    @Test
    fun firstStartDoesNotRequestForegroundReturn() {
        val coordinator = coordinator()

        coordinator.onRouteChanged(Screen.Home.route)
        coordinator.onStart()

        assertEquals(AppLaunchRequest.Normal, coordinator.pendingRequest)
    }

    @Test
    fun quickForegroundReturnDoesNotRequestSplash() {
        val coordinator = startedCoordinator()

        coordinator.onRouteChanged(Screen.Home.route)
        coordinator.onStop()
        now += 10_000L
        coordinator.onStart()

        assertEquals(AppLaunchRequest.Normal, coordinator.pendingRequest)
    }

    @Test
    fun foregroundReturnAfterIntervalRequestsSplashForPreviousRoute() {
        val coordinator = startedCoordinator()

        coordinator.onRouteChanged(Screen.AppUsage.route)
        coordinator.onStop()
        now += 31_000L
        coordinator.onStart()

        assertEquals(
            AppLaunchRequest.ForegroundReturn(Screen.AppUsage.route),
            coordinator.pendingRequest,
        )
    }

    @Test
    fun externalActivityReturnDoesNotRequestSplashEvenAfterInterval() {
        val coordinator = startedCoordinator()

        coordinator.onRouteChanged(Screen.ManagePermissions.route)
        coordinator.markExternalActivityLaunch()
        coordinator.onStop()
        now += 31_000L
        coordinator.onStart()

        assertEquals(AppLaunchRequest.Normal, coordinator.pendingRequest)
    }

    @Test
    fun startupRoutesDoNotRequestForegroundReturn() {
        val coordinator = startedCoordinator()

        coordinator.onRouteChanged(Screen.Splash.route)
        coordinator.onStop()
        now += 31_000L
        coordinator.onStart()

        assertEquals(AppLaunchRequest.Normal, coordinator.pendingRequest)
    }

    @Test
    fun notificationTargetIsNotOverwrittenByForegroundReturn() {
        val coordinator = startedCoordinator(
            targetRouteResolver = { Screen.NotificationCleaner.route },
        )

        coordinator.onRouteChanged(Screen.Home.route)
        coordinator.onStop()
        now += 31_000L
        coordinator.onNewIntent(Intent())
        coordinator.onStart()

        assertEquals(
            AppLaunchRequest.NotificationTarget(Screen.NotificationCleaner.route),
            coordinator.pendingRequest,
        )
    }

    @Test
    fun consumeRequestIfCurrentDoesNotConsumeNewerRequest() {
        val coordinator = startedCoordinator(
            targetRouteResolver = { Screen.NotificationCleaner.route },
        )

        coordinator.onRouteChanged(Screen.Home.route)
        coordinator.onStop()
        now += 31_000L
        coordinator.onStart()
        val foregroundRequest = coordinator.pendingRequest
        coordinator.onNewIntent(Intent())

        val consumed = coordinator.consumeRequestIfCurrent(foregroundRequest)

        assertTrue(!consumed)
        assertEquals(
            AppLaunchRequest.NotificationTarget(Screen.NotificationCleaner.route),
            coordinator.pendingRequest,
        )
    }

    private fun startedCoordinator(
        targetRouteResolver: (Intent?) -> String? = { null },
    ): AppLaunchCoordinator {
        val coordinator = coordinator(targetRouteResolver)
        coordinator.onStart()
        return coordinator
    }

    private fun coordinator(
        targetRouteResolver: (Intent?) -> String? = { null },
    ): AppLaunchCoordinator =
        AppLaunchCoordinator(
            foregroundResumeIntervalMs = 30_000L,
            elapsedRealtime = { now },
            targetRouteResolver = targetRouteResolver,
        )
}
