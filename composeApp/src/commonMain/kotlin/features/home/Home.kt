package features.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.mgtvapi.api.model.Clip
import com.mgtvapi.domain.ResultState
import common.components.ClipCard
import common.components.MGCircularProgressIndicator
import common.screens.EpisodeScreen

private const val PAGINATION_THRESHOLD = 12

@Composable
fun Home(
    mainFeedClipsState: ResultState<List<Clip>>,
    mainFeedClipsPaginationState: ResultState<Boolean>,
    getMainFeedClipsPagination: (offset: Int) -> Unit,
    getMainFeedClips: (offset: Int, count: Int) -> Unit,
    isInitial: Boolean,
) {
    val lazyColumnListState = rememberLazyListState()
    var lastCalledIndex by remember { mutableStateOf<Int?>(null) }
    val navigator = LocalNavigator.currentOrThrow

    LaunchedEffect(lazyColumnListState.layoutInfo.visibleItemsInfo.lastOrNull()?.index) {
        val lastIndex = lazyColumnListState.layoutInfo.visibleItemsInfo.lastOrNull()?.index
        if (lastIndex != lastCalledIndex) {
            if (!isInitial && lazyColumnListState.layoutInfo.totalItemsCount - PAGINATION_THRESHOLD < lastIndex!!) {
                getMainFeedClipsPagination(lazyColumnListState.layoutInfo.totalItemsCount)
                lastCalledIndex = lastIndex
            }
        }
    }

    LaunchedEffect(Unit) {
        if (isInitial) {
            getMainFeedClips(0, 20)
        }
    }

    when (mainFeedClipsState) {
        is ResultState.Loading, is ResultState.Empty -> {
            MGCircularProgressIndicator()
        }

        is ResultState.Success -> {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                state = lazyColumnListState,
            ) {
                items(mainFeedClipsState.data, key = { it.id }) { clip ->
                    ClipCard(
                        clip = clip,
                        onClick = {
                            navigator.push(EpisodeScreen(clip = clip))
                        },
                    )
                }
                item(key = "paginationState") {
                    when (mainFeedClipsPaginationState) {
                        is ResultState.Loading -> {
                            MGCircularProgressIndicator()
                        }

                        is ResultState.Error -> {
                            Text(mainFeedClipsPaginationState.message)
                        }

                        else -> {}
                    }
                }
            }
        }

        is ResultState.Error -> {
            Text("ERROR")
        }
    }
}
