package com.mgplayer.tv.features.home.leftmenu.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Star
import com.mgplayer.tv.features.home.leftmenu.model.MenuItem
import com.mgplayer.tv.features.home.navigation.NestedScreens
import com.mgplayer.tv.features.home.navigation.NestedScreens.Home

object MenuData {
    val menuItems = listOf(
        MenuItem(Home.title, "Home", Icons.Outlined.Home),
    )

    val settingsItem = MenuItem(NestedScreens.Settings.title, "Settings", Icons.Outlined.Settings)

    val magazines = MenuItem(NestedScreens.Magazines.title, "Magazines", Icons.Outlined.Star)
}
