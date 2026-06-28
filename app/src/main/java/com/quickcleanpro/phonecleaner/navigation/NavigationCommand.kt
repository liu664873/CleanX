package com.quickcleanpro.phonecleaner.navigation

sealed interface NavigationCommand {
    data class To(
        val route: AppRoute,
        val args: Map<String, String> = emptyMap(),
    ) : NavigationCommand

    data class Replace(
        val route: AppRoute,
    ) : NavigationCommand

    data class ClearTo(
        val route: AppRoute,
    ) : NavigationCommand

    data object Back : NavigationCommand

    data object Home : NavigationCommand
}
