package features.home.components

import androidx.compose.animation.Crossfade
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Tv
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import navigation.HomeRoute

data class TopLevelDestination(
    val route: HomeRoute,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val iconText: String,
)

private val TOP_LEVEL_DESTINATIONS =
    listOf(
        TopLevelDestination(
            HomeRoute.Home,
            Icons.Filled.Home,
            Icons.Outlined.Home,
            "Home",
        ),
        TopLevelDestination(
            HomeRoute.Magazines,
            Icons.Filled.Tv,
            Icons.Outlined.Tv,
            "Tv",
        ),
        TopLevelDestination(
            HomeRoute.Settings,
            Icons.Filled.Settings,
            Icons.Outlined.Settings,
            "Settings",
        ),
    )

@Composable
fun BottomBar(
    navController: NavController,
    route: MutableState<HomeRoute>
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.primary,
    ) {

        TOP_LEVEL_DESTINATIONS.forEach { destination ->
            val isSelected = destination.route == route.value
            NavigationBarItem(
                icon = {
                    Crossfade(
                        targetState = isSelected,
                        label = destination.iconText,
                    ) { selected ->
                        Icon(
                            imageVector = if (selected) destination.selectedIcon else destination.unselectedIcon,
                            contentDescription = destination.iconText,
                        )
                    }
                },
                selected = isSelected,
                onClick = {
                    route.value = destination.route
                    navController.navigate(destination.route.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = false
                        }
                        launchSingleTop = true
                        restoreState = false
                    }
                },
            )
        }
    }
}
