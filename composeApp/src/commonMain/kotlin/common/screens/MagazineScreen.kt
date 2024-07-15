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
import androidx.compose.runtime.DisposableEffect
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
import coil3.compose.AsyncImage
import com.mgtvapi.api.model.Clip
import com.mgtvapi.api.model.Magazine
import com.mgtvapi.domain.ResultState
import com.mgtvapi.viewModel.MagazineOverviewViewModel
import common.components.MGCircularProgressIndicator
import common.components.MGTopAppBar
import common.util.PAGINATION_THRESHOLD
import org.koin.compose.koinInject

@Composable
fun MagazineScreen(
    magazine: Magazine,
    onEpisodeClick: (String, String) -> Unit,
    onBackPressed: () -> Unit,
    onDispose: () -> Unit,
) {
    val lazyColumnListState = rememberLazyListState()
    val magazineOverviewViewModel: MagazineOverviewViewModel =
        koinInject<MagazineOverviewViewModel>()
    val isInitial = magazineOverviewViewModel.isInitial.collectAsState().value
    val magazineEpisodes = magazineOverviewViewModel.magazineEpisodes.collectAsState().value
    var lastCalledIndex by remember { mutableStateOf<Int?>(null) }

    DisposableEffect(Unit) {
        magazineOverviewViewModel.fetchMagazine(
            "${magazine.pid}",
            limit = 20,
            offset = 0,
            magazine = magazine
        )

        onDispose {
            onDispose()
        }
    }


    LaunchedEffect(lazyColumnListState.layoutInfo.visibleItemsInfo.lastOrNull()?.index) {
        val lastIndex = lazyColumnListState.layoutInfo.visibleItemsInfo.lastOrNull()?.index
        if (lastIndex != lastCalledIndex) {
            if (!isInitial && lazyColumnListState.layoutInfo.totalItemsCount - PAGINATION_THRESHOLD < lastIndex!!) {
                magazineOverviewViewModel.fetchMagazine(
                    "${magazine.pid}",
                    limit = 40,
                    offset = lazyColumnListState.layoutInfo.totalItemsCount,
                    magazine = magazine
                )
                lastCalledIndex = lastIndex
            }
        }
    }

    Scaffold(topBar = {
        MGTopAppBar(onBackPressed = onBackPressed, showBackButton = true)
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
                        onClick = {
                            onEpisodeClick(
                                magazineEpisodes.data[clip].magazineId.toString(),
                                magazineEpisodes.data[clip].id
                            )
                        }
                    )
                    if (clip != magazineEpisodes.data.count() - 1) {
                        HorizontalDivider()
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
            .padding(horizontal = 4.dp, vertical = 12.dp) // Padding moved outside
            .fillMaxWidth()
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.height(81.dp)
                        .width(153.dp).clip(RoundedCornerShape(12.dp))
                ) {
                    AsyncImage(
                        contentDescription = clip.description,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                        model = clip.artworkUrl
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    clip.shortDescription?.let { it1 ->
                        Text(
                            text = it1,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            }
        }
    }
}
