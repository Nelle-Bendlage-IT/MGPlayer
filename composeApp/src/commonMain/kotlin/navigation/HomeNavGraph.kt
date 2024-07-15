package navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mgtvapi.viewModel.CommonViewModel
import com.mgtvapi.viewModel.HomeViewModel
import com.mgtvapi.viewModel.MagazineOverviewViewModel
import common.components.MGTopAppBar
import features.home.HomeScreen
import features.home.components.BottomBar
import features.magazineOverview.MagazineOverview
import features.settings.Settings
import org.koin.compose.koinInject

sealed class HomeRoute(val route: String) {
    data object Home : HomeRoute("home")

    data object Magazines : HomeRoute("magazines")

    data object Settings : HomeRoute("settings")

    data object MagazineOverview : HomeRoute("magazine/{magazineID}") {
        fun createRoute(magazineID: String): String = "magazines/$magazineID"
        fun createMagazineEpisodeRoute(magazineID: String, episodeID: String): String =
            "magazines/$magazineID/$episodeID"
    }
}

@Composable
fun HomeNavGraph(rootNavController: NavController) {
    val navController = rememberNavController()
    val homeViewModel: HomeViewModel = koinInject()
    val magazineViewModel = koinInject<MagazineOverviewViewModel>()
    val commonViewModel = koinInject<CommonViewModel>()
    val route = remember { mutableStateOf<HomeRoute>(HomeRoute.Home) }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .imePadding(),
        bottomBar = {
            BottomBar(navController = navController, route = route)
        },
        topBar = {
            MGTopAppBar(showBackButton = false, onBackPressed = {})
        }
    ) { innerPadding ->

        NavHost(
            navController = navController,
            startDestination = HomeRoute.Home.route,
        ) {
            composable(
                HomeRoute.Home.route,
                enterTransition = { fadeIn() },
                exitTransition = { fadeOut() },
                popEnterTransition = { fadeIn() },
                popExitTransition = { fadeOut() },
            ) {
                HomeScreen(
                    innerPadding = innerPadding,
                    onTitleClick = { magazineID, episodeID ->
                        commonViewModel.clipID = episodeID
                        rootNavController.navigate(
                            HomeRoute.MagazineOverview.createMagazineEpisodeRoute(
                                magazineID,
                                episodeID
                            )
                        ) {
                            launchSingleTop = true
                        }
                    },
                )
            }
            composable(
                HomeRoute.Magazines.route,
                enterTransition = { fadeIn() },
                exitTransition = { fadeOut() },
                popEnterTransition = { fadeIn() },
                popExitTransition = { fadeOut() },
            ) {
                MagazineOverview(
                    onItemClick = { id, magazine ->
                        magazineViewModel.chosenMagazine = magazine
                        rootNavController.navigate(HomeRoute.MagazineOverview.createRoute(id))
                    },
                    innerPadding = innerPadding
                )
            }
            composable(
                HomeRoute.Settings.route,
                enterTransition = { fadeIn() },
                exitTransition = { fadeOut() },
                popEnterTransition = { fadeIn() },
                popExitTransition = { fadeOut() },
            ) {
                Settings(
                    innerPadding = innerPadding,
                    onLogOutPressed = { homeViewModel.logout() }
                )
            }
        }
    }
}