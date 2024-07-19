package com.mgplayer.tv.features.home.navigation

sealed class NestedScreens(val title: String) {
    data object Home : NestedScreens("home")
    data object Magazines : NestedScreens("magazines")
    data object Settings : NestedScreens("settings")
}
