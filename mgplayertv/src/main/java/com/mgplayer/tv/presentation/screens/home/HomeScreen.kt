/*
 * Copyright 2023 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mgplayer.tv.presentation.screens.home

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mgplayer.tv.data.util.StringConstants
import com.mgplayer.tv.presentation.common.ClipsRow
import com.mgplayer.tv.presentation.common.ErrorScreen
import com.mgplayer.tv.presentation.common.Loading
import com.mgplayer.tv.presentation.screens.dashboard.rememberChildPadding
import com.mgtv.shared_core.core.ViewState
import com.mgtvapi.api.model.Clip
import com.mgtvapi.api.model.ProgressClip
import com.mgtvapi.viewModel.HomeViewModel
import org.koin.compose.koinInject


@Composable
fun HomeScreen(
    onClipClick: (clip: Clip) -> Unit,
    goToVideoPlayer: (clip: Clip) -> Unit,
    onScroll: (isTopBarVisible: Boolean) -> Unit,
    isTopBarVisible: Boolean,
    homeViewModel: HomeViewModel = koinInject<HomeViewModel>(),
) {
    LaunchedEffect(Unit) {
        homeViewModel.getMainFeedClips(0, 40)
        homeViewModel.getRecentlyWatched()
    }

    val homeScreenClips by homeViewModel.homeScreenClips.collectAsStateWithLifecycle()

    when (val s = homeScreenClips.mainFeedClips) {
        is ViewState.Success -> {
            val recentlyWatchedClips = homeScreenClips.recentlyWatchedClips
            if (recentlyWatchedClips is ViewState.Success)
                Catalog(
                    featuredClips = s.data.subList(0, 5),
                    trendingClips = s.data,
                    top10Clips = recentlyWatchedClips.data,
                    onClipClick = onClipClick,
                    onScroll = onScroll,
                    goToVideoPlayer = goToVideoPlayer,
                    isTopBarVisible = isTopBarVisible,
                    modifier = Modifier.fillMaxSize(),
                )
        }

        is ViewState.Loading, is ViewState.Empty -> Loading()
        is ViewState.Error -> ErrorScreen(modifier = Modifier.fillMaxSize())
    }
}

@Composable
private fun Catalog(
    featuredClips: List<Clip>,
    trendingClips: List<Clip>,
    top10Clips: List<ProgressClip>,
    onClipClick: (movie: Clip) -> Unit,
    onScroll: (isTopBarVisible: Boolean) -> Unit,
    goToVideoPlayer: (movie: Clip) -> Unit,
    modifier: Modifier = Modifier,
    isTopBarVisible: Boolean = true,
) {

    val lazyListState = rememberLazyListState()
    val childPadding = rememberChildPadding()
    var immersiveListHasFocus by remember { mutableStateOf(false) }

    val shouldShowTopBar by remember {
        derivedStateOf {
            lazyListState.firstVisibleItemIndex == 0 &&
                    lazyListState.firstVisibleItemScrollOffset < 300
        }
    }

    LaunchedEffect(shouldShowTopBar) {
        onScroll(shouldShowTopBar)
    }
    LaunchedEffect(isTopBarVisible) {
        if (isTopBarVisible) lazyListState.animateScrollToItem(0)
    }

    LazyColumn(
        state = lazyListState,
        contentPadding = PaddingValues(bottom = 108.dp),
        // Setting overscan margin to bottom to ensure the last row's visibility
        modifier = modifier,
    ) {

        item(contentType = "FeaturedMoviesCarousel") {
            FeaturedClipsCarousel(
                movies = featuredClips,
                padding = childPadding,
                goToVideoPlayer = goToVideoPlayer,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(324.dp)
                /*
                 Setting height for the FeaturedMovieCarousel to keep it rendered with same height,
                 regardless of the top bar's visibility
                 */
            )
        }
        item(contentType = "RecentlyWatchedList") {
            RecentlyWatchedList(
                clipList = top10Clips,
                onClipClick = onClipClick,
                modifier = Modifier.onFocusChanged {
                    immersiveListHasFocus = it.hasFocus
                },
            )
        }
        item(contentType = "MoviesRow") {
            ClipsRow(
                modifier = Modifier
                    .padding(top = 16.dp)
                    .height(250.dp),
                movieList = trendingClips,
                title = StringConstants.Composable.HomeScreenTrendingTitle,
                onMovieSelected = onClipClick,
                useVerticalImage = true
            )
        }
    }
}
