package features.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.core.screen.Screen
import com.mgtvapi.viewModel.HomeViewModel
import common.components.MGTopAppBar
import features.home.components.BottomBar
import org.koin.compose.koinInject

sealed class HomeRoute(val route: String) {
    data object Home : HomeRoute("home")

    data object Tv : HomeRoute("tv")

    data object Settings : HomeRoute("settings")
}

class HomeScreen() : Screen {
    @Composable
    override fun Content() {
        var route by remember { mutableStateOf<HomeRoute>(HomeRoute.Home) }
        val homeViewModel: HomeViewModel = koinInject()

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
            Box(modifier = Modifier.padding(innerPadding)) {
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
                        )

                    HomeRoute.Tv -> Text("HELLO")
                    HomeRoute.Settings -> Text("HELLO")
                }
            }
        }
    }
}
