package features.home

import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.core.screen.Screen
import com.mgtvapi.viewModel.HomeViewModel
import com.mgtvapi.viewModel.MagazineOverviewViewModel
import common.components.MGTopAppBar
import features.home.components.BottomBar
import features.magazineOverview.MagazineOverview
import features.settings.Settings
import org.koin.compose.koinInject

sealed class HomeRoute(val route: String) {
    data object Home : HomeRoute("home")

    data object Tv : HomeRoute("tv")

    data object Settings : HomeRoute("settings")
}

class HomeLayout : Screen {
    @Composable
    override fun Content() {
        var route by remember { mutableStateOf<HomeRoute>(HomeRoute.Home) }
        val homeViewModel: HomeViewModel = koinInject()
        val magazineOverviewViewModel: MagazineOverviewViewModel =
            koinInject<MagazineOverviewViewModel>()

        Scaffold(
            topBar = {
                MGTopAppBar(onBackPressed = { }, showBackButton = false)
            },
            bottomBar = {
                BottomBar(
                    currentRoute = route,
                    onRouteSelected = { route = it },
                )
            },
        ) { innerPadding ->
            when (route) {
                HomeRoute.Home ->
                    Home(
                        mainFeedClipsState = homeViewModel.clips.collectAsState().value,
                        mainFeedClipsPaginationState = homeViewModel.paginationLoading.collectAsState().value,
                        getMainFeedClips = { offset, count ->
                            homeViewModel.getMainFeedClips(offset, count)
                        },
                        getMainFeedClipsPagination = { offset ->
                            homeViewModel.getMainFeedClipsPagination(offset)
                        },
                        isInitial = homeViewModel.isInitial.collectAsState().value,
                        innerPadding = innerPadding
                    )

                HomeRoute.Tv -> MagazineOverview(
                    magazines = magazineOverviewViewModel.magazines.collectAsState().value,
                    fetchMagazines = { magazineOverviewViewModel.fetchMagazines() },
                    innerPadding = innerPadding
                )

                HomeRoute.Settings -> Settings(
                    onLogOutPressed = { homeViewModel.logout() },
                    innerPadding = innerPadding
                )

            }
        }
    }
}
