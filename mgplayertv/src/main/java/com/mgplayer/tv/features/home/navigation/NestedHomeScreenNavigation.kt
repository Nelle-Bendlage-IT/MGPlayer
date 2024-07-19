package com.mgplayer.tv.features.home.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.tv.material3.Text
import com.mgplayer.tv.features.home.HomeNestedScreen
import com.mgplayer.tv.navigation.tabEnterTransition
import com.mgplayer.tv.navigation.tabExitTransition
import com.mgtvapi.viewModel.HomeViewModel
import com.mgtvapi.viewModel.MagazineOverviewViewModel
import kotlinx.coroutines.flow.StateFlow
import org.koin.compose.koinInject

@Composable
fun NestedHomeScreenNavigation(
    usedTopBar: StateFlow<Boolean>,
    toggleTopBar: () -> Unit,
    navController: NavHostController,
    onItemClick: (parent: Int, child: Int) -> Unit,
) {
    val magazineOverviewViewModel: MagazineOverviewViewModel =
        koinInject<MagazineOverviewViewModel>()
    val homeViewModel = koinInject<HomeViewModel>()

    NavHost(navController = navController, startDestination = NestedScreens.Home.title) {
        composable(
            NestedScreens.Home.title,
            enterTransition = { tabEnterTransition() },
            exitTransition = { tabExitTransition() }) {
            HomeNestedScreen(
                onItemFocus = { _, _ -> }, onItemClick = onItemClick,
                mainFeedClipsState = homeViewModel.clips.collectAsState().value,
                mainFeedClipsPaginationState = homeViewModel.paginationLoading.collectAsState().value,
                getMainFeedClips = { offset, count ->
                    homeViewModel.getMainFeedClips(offset, count)
                },
                getMainFeedClipsPagination = { offset ->
                    homeViewModel.getMainFeedClipsPagination(offset)
                },
            )
        }

        composable(
            NestedScreens.Magazines.title,
            enterTransition = { tabEnterTransition() },
            exitTransition = { tabExitTransition() }) {
//            MoviesScreen(onItemClick)
            Text("ASDSASD")
        }


        composable(
            NestedScreens.Settings.title,
            enterTransition = { tabEnterTransition() },
            exitTransition = { tabExitTransition() }) {
//            SettingsScreen(toggleTopBar = toggleTopBar, usedTopBar = usedTopBar)
            Text("ASDSASD")

        }
    }
}
