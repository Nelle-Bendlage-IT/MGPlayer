package features.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mgtv.shared_core.core.ViewState
import com.mgtvapi.api.model.Clip
import com.mgtvapi.viewModel.HomeViewModel
import common.components.ClipCard
import common.components.MGCircularProgressIndicator
import org.koin.compose.koinInject


private const val PAGINATION_THRESHOLD = 12

@Composable
fun HomeScreen(
    innerPadding: PaddingValues, onTitleClick: (String, String) -> Unit = { _, _ -> }
) {
    val homeViewModel: HomeViewModel = koinInject()

    HomeContent(
        mainFeedClipsState = homeViewModel.clips.collectAsState().value,
        mainFeedClipsPaginationState = homeViewModel.paginationLoading.collectAsState().value,
        getMainFeedClips = { offset, count ->
            homeViewModel.getMainFeedClips(offset, count)
        },
        getMainFeedClipsPagination = { offset ->
            homeViewModel.getMainFeedClipsPagination(offset)
        },
        isInitial = homeViewModel.isInitial.collectAsState().value,
        innerPadding = innerPadding,
        onTitleClick = onTitleClick
    )
}

@Composable
private fun HomeContent(
    mainFeedClipsState: ViewState<List<Clip>>,
    mainFeedClipsPaginationState: ViewState<Boolean>,
    getMainFeedClipsPagination: (offset: Int) -> Unit,
    getMainFeedClips: (offset: Int, count: Int) -> Unit,
    isInitial: Boolean,
    innerPadding: PaddingValues,
    onTitleClick: (String, String) -> Unit = { _, _ -> },
) {

    val lazyColumnListState = rememberLazyListState()
    var lastCalledIndex by remember { mutableStateOf<Int?>(null) }

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
        is ViewState.Loading, is ViewState.Empty -> {
            MGCircularProgressIndicator()
        }

        is ViewState.Error -> {
            Text("ERROR", style = MaterialTheme.typography.bodyLarge)
        }

        is ViewState.Success -> {
            LazyColumn(
                Modifier.padding(innerPadding),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                state = lazyColumnListState,
            ) {
                items(mainFeedClipsState.data, key = { it.id }) { clip ->
                    ClipCard(
                        clip = clip,
                        onClick = {
                            onTitleClick(clip.magazineId.toString(), clip.id)
                        },
                    )
                }
                item(key = "paginationState") {
                    when (mainFeedClipsPaginationState) {
                        is ViewState.Loading -> {
                            MGCircularProgressIndicator()
                        }

                        is ViewState.Error -> {
                            Text("ERROR", style = MaterialTheme.typography.bodyLarge)
                        }

                        else -> {}
                    }
                }

            }
        }
    }
}