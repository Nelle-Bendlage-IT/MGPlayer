package com.mgplayer.tv.features.home

import androidx.compose.runtime.Composable

@Composable
fun HomeScreen(
    homeViewModel: HomeSidebarViewModel,
    onItemClick: (parent: Int, child: Int) -> Unit,
) {
    HomeScreenContent(
        onItemClick, homeViewModel.usedTopBar
    ) { homeViewModel.toggleTopBar() }
}
