package com.mgplayer.tv.navigation

import android.annotation.SuppressLint
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.mgplayer.tv.features.home.HomeScreen
import com.mgplayer.tv.features.home.HomeSidebarViewModel
import com.mgplayer.tv.ui.LoginScreen
import com.mgtvapi.api.repository.MGTVApiRepository
import com.mgtvapi.api.repository.TokenManager
import com.mgtvapi.viewModel.CommonViewModel
import com.mgtvapi.viewModel.HomeViewModel
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
fun RootNavigationGraph(navController: NavHostController) {
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
            composable(Routes.HOME, enterTransition = { tabEnterTransition() },
                exitTransition = { tabExitTransition() }) {
                Scaffold {
                    HomeScreen(HomeSidebarViewModel(), { _, _ -> })
                }

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