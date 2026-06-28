package com.quickcleanpro.phonecleaner.navigation

interface AppNavigator {
    fun navigate(command: NavigationCommand): Boolean

    fun back(): Boolean = navigate(NavigationCommand.Back)

    fun home() {
        navigate(NavigationCommand.Home)
    }
}
