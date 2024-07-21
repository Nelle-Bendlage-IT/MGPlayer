package com.mgplayer.tv.navigation

import android.annotation.SuppressLint
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.mgplayer.tv.presentation.screens.movies.MovieDetailsScreen
import com.mgplayer.tv.presentation.screens.Screens
import com.mgplayer.tv.presentation.screens.categories.MagazineClipsListScreen
import com.mgplayer.tv.presentation.screens.dashboard.DashboardScreen
import com.mgplayer.tv.presentation.screens.movies.ClipDetailsScreen
import com.mgplayer.tv.presentation.screens.videoPlayer.VideoPlayerScreen
import com.mgplayer.tv.ui.LoginScreen
import com.mgtvapi.api.repository.MGTVApiRepository
import com.mgtvapi.api.repository.TokenManager
import com.mgtvapi.viewModel.CommonViewModel
import com.mgtvapi.viewModel.MagazineOverviewViewModel
import org.koin.compose.koinInject

object Routes {
    const val ROOT = "root_graph"
    const val HOME = "home_graph"
    const val EPISODE = "magazines/{magazineID}/{episodeID}"
    const val MAGAZINE = "magazines/{magazineID}"
    const val PLAYER = "/video/{episodeID}"
    fun createPlayerRoute(episodeID: String) = "magazines/video/$episodeID"
    const val LOGIN = "/login"
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun RootNavigationGraph(navController: NavHostController, onBackPressed: () -> Unit) {
    val tokenManager: TokenManager = koinInject()
    val repo: MGTVApiRepository = koinInject()
    val isLoggedIn by repo.isLoggedIn().collectAsState(tokenManager.email != null)
    val commonViewModel = koinInject<CommonViewModel>()
    val magazineViewModel = koinInject<MagazineOverviewViewModel>()
    var isComingBackFromDifferentScreen by remember { mutableStateOf(false) }

    if (isLoggedIn) {
        NavHost(
            navController = navController,
            startDestination = Screens.Dashboard(),
        ) {


            composable(
                route = Screens.CategoryMovieList(),
                enterTransition = { tabEnterTransition() },
                exitTransition = { tabExitTransition() },
                arguments = listOf(
                    navArgument("magazineName") {
                        type = NavType.StringType
                    }
                )
            ) {
                val magazineName = it.arguments?.getString("magazineName")

                MagazineClipsListScreen(
                    onBackPressed = {
                        if (navController.navigateUp()) {
                            isComingBackFromDifferentScreen = true
                        }
                    },
                    magazineName = "$magazineName",
                    onClipSelected = { clip ->
                        navController.navigate(
                            Screens.MovieDetails.withArgs(clip.id)
                        )
                    }
                )
            }
            composable(
                route = Screens.MovieDetails(),
                arguments = listOf(
                    navArgument(MovieDetailsScreen.MovieIdBundleKey) {
                        type = NavType.StringType
                    }
                )
            ) {
                ClipDetailsScreen(
                    goToMoviePlayer = {
                        navController.navigate(Screens.VideoPlayer())
                    },
                    refreshScreenWithNewMovie = { movie ->
                        navController.navigate(
                            Screens.MovieDetails.withArgs(movie.id)
                        ) {
                            popUpTo(Screens.MovieDetails()) {
                                inclusive = true
                            }
                        }
                    },
                    onBackPressed = {
                        if (navController.navigateUp()) {
                            isComingBackFromDifferentScreen = true
                        }
                    }
                )
            }
            composable(route = Screens.Dashboard()) {
                DashboardScreen(
                    openCategoryMovieList = { categoryId ->
                        navController.navigate(
                            Screens.CategoryMovieList.withArgs(categoryId)
                        )
                    },
                    openMovieDetailsScreen = { movieId ->
                        navController.navigate(
                            Screens.MovieDetails.withArgs(movieId)
                        )
                    },
                    openVideoPlayer = {
                        navController.navigate(Screens.VideoPlayer())
                    },
                    onBackPressed = onBackPressed,
                    isComingBackFromDifferentScreen = isComingBackFromDifferentScreen,
                    resetIsComingBackFromDifferentScreen = {
                        isComingBackFromDifferentScreen = false
                    }
                )
            }
            composable(route = Screens.VideoPlayer()) {
                VideoPlayerScreen(
                    onBackPressed = {
                        if (navController.navigateUp()) {
                            isComingBackFromDifferentScreen = true
                        }
                    }
                )
            }
        }
    } else {
        NavHost(
            navController = navController,
            route = Routes.ROOT,
            startDestination = Routes.LOGIN,
        ) {
            composable(
                Routes.LOGIN,
                enterTransition = { tabEnterTransition() },
                exitTransition = { tabExitTransition() }) {
                LoginScreen(repo = repo)
            }
        }
    }

}

fun tabExitTransition(
    duration: Int = 500,
) = fadeOut(tween(duration / 2, easing = LinearEasing))

fun tabEnterTransition(
    duration: Int = 500,
    delay: Int = duration - 350,
) = fadeIn(tween(duration, duration - delay))

fun NavHostController.navigateSingleTopTo(route: String) =
    this.navigate(route) {
        popUpTo(
            this@navigateSingleTopTo.graph.findStartDestination().id,
        ) {
            saveState = true
            inclusive = true
        }
        launchSingleTop = true
        restoreState = true
    }