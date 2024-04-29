package features.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
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


@Composable
fun Home(
    uiState:
    State<ResultState<List<Clip>>>,
    paginationState: State<ResultState<Boolean>>,
    getMainFeedClipsPagination: (offset: Int) -> Unit,
    getMainFeedClips: (offset: Int, count: Int) -> Unit,
    isInitial: State<Boolean>,
) {
    val lazyColumnListState = rememberLazyListState()
    var lastCalledIndex by remember { mutableStateOf<Int?>(null) }
    val navigator = LocalNavigator.currentOrThrow
    LaunchedEffect(lazyColumnListState.layoutInfo.visibleItemsInfo.lastOrNull()?.index) {
        val lastIndex = lazyColumnListState.layoutInfo.visibleItemsInfo.lastOrNull()?.index
        if (lastIndex != lastCalledIndex) {
            if (!isInitial.value && lazyColumnListState.layoutInfo.totalItemsCount - 12 < lastIndex!!) {
                getMainFeedClipsPagination(lazyColumnListState.layoutInfo.totalItemsCount)
                lastCalledIndex = lastIndex
            }
        }
    }

    LaunchedEffect(Unit) {
        if (isInitial.value) {
            getMainFeedClips(0, 20)
        }
    }

    when (uiState.value) {
        is ResultState.Loading, is ResultState.Empty -> {
            MGCircularProgressIndicator()
        }

        is ResultState.Success<List<Clip>> -> {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                state = lazyColumnListState,
            ) {
                items(
                    (uiState.value as ResultState.Success<List<Clip>>).data,
                    key = { it.id }) {
                    ClipCard(
                        clip = it,
                        onClick = {
                            navigator.push(
                                EpisodeScreen(
                                    clip = it,
                                )
                            )
                        })
                }

                item(key = "paginationState") {
                    when (paginationState.value) {
                        is ResultState.Loading -> {
                            MGCircularProgressIndicator()
                        }

                        is ResultState.Success, is ResultState.Empty -> {}
                        is ResultState.Error -> {
                            Text((paginationState.value as ResultState.Error).message)
                        }
                    }
                }
            }
        }

        is ResultState.Error -> {
            Text("ERROR")
        }

    }

}

