package com.mgplayer.tv.features.home

import androidx.lifecycle.ViewModel
import com.mgplayer.tv.features.home.leftmenu.data.MenuData
import com.mgplayer.tv.utils.toMutable
import com.mgplayer.tv.features.home.leftmenu.model.MenuItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class HomeSidebarViewModel : ViewModel() {
    val menuItems: StateFlow<List<MenuItem>> = MutableStateFlow(emptyList())
    val menuState: StateFlow<Boolean> = MutableStateFlow(false)
    private val _usedTopBar: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val usedTopBar: StateFlow<Boolean> = _usedTopBar

    init {
        menuItems.toMutable().value = MenuData.menuItems
    }

    fun menuClosed() {
        menuState.toMutable().value = false
    }

    fun menuOpen() {
        menuState.toMutable().value = true
    }

    fun toggleTopBar(){
        _usedTopBar.value = !_usedTopBar.value
    }
}
