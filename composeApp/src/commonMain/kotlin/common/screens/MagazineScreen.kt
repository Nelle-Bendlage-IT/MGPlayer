package common.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.annotation.ExperimentalVoyagerApi
import cafe.adriel.voyager.core.lifecycle.LifecycleEffectOnce
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import coil3.compose.AsyncImage
import com.mgtvapi.Parcelable
import com.mgtvapi.Parcelize
import com.mgtvapi.api.model.Clip
import com.mgtvapi.api.model.Magazine
import com.mgtvapi.domain.ResultState
import com.mgtvapi.viewModel.MagazineOverviewViewModel
import common.components.MGCircularProgressIndicator
import common.components.MGTopAppBar
import common.util.PAGINATION_THRESHOLD
import org.koin.compose.koinInject

@Parcelize
data class MagazineScreen(
    val magazine: Magazine,
) : Screen, Parcelable {
    @OptIn(ExperimentalVoyagerApi::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val lazyColumnListState = rememberLazyListState()
        val magazineOverviewViewModel: MagazineOverviewViewModel =
            koinInject<MagazineOverviewViewModel>()
        val isInitial = magazineOverviewViewModel.isInitial.collectAsState().value
        val magazineEpisodes = magazineOverviewViewModel.magazineEpisodes.collectAsState().value
        var lastCalledIndex by remember { mutableStateOf<Int?>(null) }

        LifecycleEffectOnce {
            magazineOverviewViewModel.fetchMagazine("${magazine.pid}", 20, 0)
        }

        LaunchedEffect(lazyColumnListState.layoutInfo.visibleItemsInfo.lastOrNull()?.index) {
            val lastIndex = lazyColumnListState.layoutInfo.visibleItemsInfo.lastOrNull()?.index
            if (lastIndex != lastCalledIndex) {
                if (!isInitial && lazyColumnListState.layoutInfo.totalItemsCount - PAGINATION_THRESHOLD < lastIndex!!) {
                    magazineOverviewViewModel.fetchMagazine(
                        "${magazine.pid}",
                        40,
                        lazyColumnListState.layoutInfo.totalItemsCount
                    )
                    lastCalledIndex = lastIndex
                }
            }
        }

        Scaffold(topBar = {
            MGTopAppBar(onBackPressed = { navigator.pop() }, showBackButton = true)
        }) {
            LazyColumn(Modifier.padding(it), state = lazyColumnListState) {
                item {
                    Box(
                        Modifier
                            .height(150.dp).fillMaxWidth()
                            .padding(8.dp)
                            .clip(RoundedCornerShape(16.dp))
                    ) {
                        AsyncImage(
                            contentDescription = magazine.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize(),
                            model = if (magazine.artwork.banner == null) magazine.artwork.logo else magazine.artwork.banner!!.n1x
                        )
                    }
                }
                when (magazineEpisodes) {
                    is ResultState.Empty, ResultState.Loading -> item {
                        MGCircularProgressIndicator()
                    }

                    is ResultState.Error -> item {
                        Text(magazineEpisodes.message, style = MaterialTheme.typography.bodyLarge)
                    }

                    is ResultState.Success -> items(magazineEpisodes.data.count()) { clip ->
                        EpisodeListItem(
                            clip = magazineEpisodes.data[clip],
                            onClick = { navigator.push(EpisodeScreen(magazineEpisodes.data[clip])) })
                        if (clip != magazineEpisodes.data.count() - 1) {
                            HorizontalDivider()
                        }
                    }
                }

            }
        }
    }
}

@Composable
private fun EpisodeListItem(
    clip: Clip,
    onClick: () -> Unit = {},
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(
                onClick = onClick
            )
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .fillMaxWidth()
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.height(83.dp)
                        .width(153.dp).clip(RoundedCornerShape(10.dp))
                ) {
                    AsyncImage(
                        contentDescription = clip.description,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize(),
                        model = clip.artworkUrl
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    clip.shortDescription?.let { it1 ->
                        Text(
                            text = it1,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            }
        }
    }
}
