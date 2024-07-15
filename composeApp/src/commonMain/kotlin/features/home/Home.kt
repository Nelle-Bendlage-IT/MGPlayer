package features.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import com.mgtvapi.api.model.Clip
import com.mgtvapi.domain.ResultState
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
    mainFeedClipsState: ResultState<List<Clip>>,
    mainFeedClipsPaginationState: ResultState<Boolean>,
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

    Surface(
        modifier = Modifier
            .fillMaxSize().padding(innerPadding),
        color = MaterialTheme.colorScheme.background,
    ) {
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
                                onTitleClick(clip.magazineId.toString(), clip.id)
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
                Text("ERROR", style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}