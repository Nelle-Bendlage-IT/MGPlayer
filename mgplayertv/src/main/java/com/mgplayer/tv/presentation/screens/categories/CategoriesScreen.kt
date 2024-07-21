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

package com.mgplayer.tv.presentation.screens.categories

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mgplayer.tv.presentation.common.ErrorScreen
import com.mgplayer.tv.presentation.common.Loading
import com.mgplayer.tv.presentation.common.MovieCard
import com.mgplayer.tv.presentation.common.PosterImage
import com.mgplayer.tv.presentation.screens.dashboard.rememberChildPadding
import com.mgtv.shared_core.core.ViewState
import com.mgtvapi.api.model.Magazine
import com.mgtvapi.viewModel.MagazineOverviewViewModel
import org.koin.compose.koinInject

@Composable
fun MagazineOverviewScreen(
    gridColumns: Int = 4,
    onCategoryClick: (categoryId: String) -> Unit,
    onScroll: (isTopBarVisible: Boolean) -> Unit,
    magazineOverviewViewModel: MagazineOverviewViewModel =
        koinInject<MagazineOverviewViewModel>()
) {

    val uiState by magazineOverviewViewModel.magazines.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) {
        magazineOverviewViewModel.fetchMagazines()
    }
    when (val s = uiState) {
        is ViewState.Loading, ViewState.Empty -> {
            Loading()
        }

        is ViewState.Error -> {
            ErrorScreen(Modifier.fillMaxSize())
        }

        is ViewState.Success -> {
            Catalog(
                gridColumns = gridColumns,
                movieCategories = s.data,
                onCategoryClick = onCategoryClick,
                onScroll = onScroll,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun Catalog(
    movieCategories: List<Magazine>,
    modifier: Modifier = Modifier,
    gridColumns: Int = 4,
    onCategoryClick: (categoryId: String) -> Unit,
    onScroll: (isTopBarVisible: Boolean) -> Unit,
) {
    val childPadding = rememberChildPadding()
    val lazyGridState = rememberLazyGridState()
    val shouldShowTopBar by remember {
        derivedStateOf {
            lazyGridState.firstVisibleItemIndex == 0 &&
                    lazyGridState.firstVisibleItemScrollOffset < 100
        }
    }
    LaunchedEffect(shouldShowTopBar) {
        onScroll(shouldShowTopBar)
    }

    AnimatedContent(
        targetState = movieCategories,
        modifier = Modifier
            .padding(horizontal = childPadding.start)
            .padding(top = childPadding.top),
        label = "",
    ) { it ->
        LazyVerticalGrid(
            state = lazyGridState,
            modifier = modifier,
            columns = GridCells.Fixed(gridColumns),
        ) {
            itemsIndexed(it) { index, movieCategory ->
                var isFocused by remember { mutableStateOf(false) }
                MovieCard(
                    onClick = {
                        onCategoryClick(movieCategory.pid.toString())
                    },
                    modifier = Modifier
                        .padding(8.dp)
                        .aspectRatio(16 / 9f)
                        .onFocusChanged {
                            isFocused = it.isFocused || it.hasFocus
                        }
                        .focusProperties {
                            if (index % gridColumns == 0) {
                                left = FocusRequester.Cancel
                            }
                        }
                ) {
                    val itemAlpha by animateFloatAsState(
                        targetValue = if (isFocused) .6f else 0.2f,
                        label = ""
                    )

                    Box(contentAlignment = Alignment.Center) {
                        PosterImage(
                            posterURI = movieCategory.artwork.logo,
                            modifier = Modifier
                                .fillMaxSize()
                                .alpha(itemAlpha),
                            name = movieCategory.magazineName
                        )
                    }
                }
            }
        }
    }
}
