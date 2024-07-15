package navigation

import VideoPlayer
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.mgtvapi.api.repository.MGTVApiRepository
import com.mgtvapi.api.repository.TokenManager
import com.mgtvapi.viewModel.CommonViewModel
import com.mgtvapi.viewModel.MagazineOverviewViewModel
import common.screens.EpisodeScreen
import common.screens.MagazineScreen
import features.login.LoginScreen
import getPlatform
import org.koin.compose.koinInject

object Routes {
    const val ROOT = "root_graph"
    const val HOME = "home_graph"
    const val EPISODE = "magazines/{magazineID}/{episodeID}"
    const val MAGAZINE = "magazines/{magazineID}"
    const val PLAYER = "magazines/video/{episodeID}"
    fun createPlayerRoute(episodeID: String) = "magazines/video/$episodeID"

}

@Composable
fun RootNavigationGraph() {
    val navController = rememberNavController()
    val tokenManager: TokenManager = koinInject()
    val repo: MGTVApiRepository = koinInject()
    val isLoggedIn by repo.isLoggedIn().collectAsState(tokenManager.email != null)
    val commonViewModel = koinInject<CommonViewModel>()
    val magazineViewModel = koinInject<MagazineOverviewViewModel>()
    if (isLoggedIn) {
        NavHost(
            navController = navController,
            route = Routes.ROOT,
            startDestination = Routes.HOME,
        ) {
            composable(
                route = Routes.HOME,
                enterTransition = { fadeIn() },
                exitTransition = { fadeOut() },
                popEnterTransition = { fadeIn() },
                popExitTransition = { fadeOut() },
            ) {
                HomeNavGraph(rootNavController = navController)
            }
            composable(
                route = Routes.EPISODE,
                enterTransition = { fadeIn() },
                exitTransition = { fadeOut() },
                popEnterTransition = { fadeIn() },
                popExitTransition = { fadeOut() },
                arguments = listOf(
                    navArgument("magazineID") {
                        type = NavType.StringType
                    },
                    navArgument("episodeID") {
                        type = NavType.StringType
                    }
                )
            ) {
                EpisodeScreen(
                    getClipFiles = { commonViewModel.getClipFiles() },
                    clipDataState = commonViewModel.clipData.collectAsState(),
                    onBackPressed = { navController.popBackStack() },
                    onChangeQuality = { file -> commonViewModel.file = file },
                    onPlayPressed = { navController.navigate(Routes.createPlayerRoute(it)) }
                )
            }
            composable(
                route = Routes.MAGAZINE,
                enterTransition = { fadeIn() },
                exitTransition = { fadeOut() },
                popEnterTransition = { fadeIn() },
                popExitTransition = { fadeOut() },
                arguments = listOf(
                    navArgument("magazineID") {
                        type = NavType.StringType
                    },
                )
            ) {
                MagazineScreen(
                    magazine = magazineViewModel.chosenMagazine!!,
                    onEpisodeClick = { magazineID, episodeID ->
                        navController.navigate(
                            HomeRoute.MagazineOverview.createMagazineEpisodeRoute(
                                magazineID,
                                episodeID
                            )
                        )
                    },
                    onBackPressed = { navController.popBackStack() },
                    onDispose = { magazineViewModel.onExit() }
                )
            }

            composable(
                route = Routes.PLAYER,
                enterTransition = { fadeIn() },
                exitTransition = { fadeOut() },
                popEnterTransition = { fadeIn() },
                popExitTransition = { fadeOut() },
                arguments = listOf(
                    navArgument("episodeID") {
                        type = NavType.StringType
                    },
                )
            ) {
                if (getPlatform().name == "Android") {
                    Box(Modifier.background(Color.Black)) {
                        VideoPlayer(
                            modifier = Modifier.fillMaxSize(),
                            url = commonViewModel.file!!.url,
                            playbackArtwork = commonViewModel.clip!!.image,
                            playbackTitle = commonViewModel.clip!!.projectTitle,
                            playbackSubtitle = commonViewModel.clip!!.title,
                            onVideoIsPlayingTask = { progress ->
                                commonViewModel.updateClipProgress(
                                    progress
                                )
                            }
                        )
                    }
                } else {
                    VideoPlayer(
                        modifier = Modifier.fillMaxSize(),
                        url = commonViewModel.file!!.url,
                        playbackArtwork = commonViewModel.clip!!.image,
                        playbackTitle = commonViewModel.clip!!.projectTitle,
                        playbackSubtitle = commonViewModel.clip!!.title,
                        onVideoIsPlayingTask = { progress ->
                            commonViewModel.updateClipProgress(
                                progress
                            )
                        }
                    )
                }

            }
        }
    } else {
        LoginScreen()
    }
}