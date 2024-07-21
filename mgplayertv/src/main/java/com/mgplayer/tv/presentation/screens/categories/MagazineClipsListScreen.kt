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

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.mgplayer.tv.presentation.common.ErrorScreen
import com.mgplayer.tv.presentation.common.Loading
import com.mgplayer.tv.presentation.common.MovieCard
import com.mgplayer.tv.presentation.common.PosterImage
import com.mgplayer.tv.presentation.screens.dashboard.rememberChildPadding
import com.mgplayer.tv.presentation.theme.JetStreamBottomListPadding
import com.mgplayer.tv.presentation.utils.focusOnInitialVisibility
import com.mgtv.shared_core.core.ViewState
import com.mgtvapi.api.model.Clip
import com.mgtvapi.viewModel.MagazineOverviewViewModel
import org.koin.compose.koinInject

object CategoryMovieListScreen {
    const val CategoryIdBundleKey = "magazineName"
}

@Composable
fun MagazineClipsListScreen(
    onBackPressed: () -> Unit,
    onClipSelected: (Clip) -> Unit,
    magazineName: String,
    magazineOverviewViewModel: MagazineOverviewViewModel = koinInject<MagazineOverviewViewModel>()
) {
    LaunchedEffect(Unit) {
        magazineOverviewViewModel.fetchMagazines()
    }
    val clips by magazineOverviewViewModel.magazineEpisodes.collectAsStateWithLifecycle()

    when (val s = clips) {
        is ViewState.Loading, ViewState.Empty -> {
            Loading()
        }

        is ViewState.Error -> {
            ErrorScreen(modifier = Modifier.fillMaxSize())
        }

        is ViewState.Success -> {
            CategoryDetails(
                magazineName = magazineName,
                onBackPressed = onBackPressed,
                onClipSelected = onClipSelected,
                clips = s.data
            )
        }
    }
}

@Composable
private fun CategoryDetails(
    magazineName: String,
    clips: List<Clip>,
    onBackPressed: () -> Unit,
    onClipSelected: (Clip) -> Unit,
    modifier: Modifier = Modifier
) {
    val childPadding = rememberChildPadding()
    val isFirstItemVisible = remember { mutableStateOf(false) }

    BackHandler(onBack = onBackPressed)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier,
    ) {
        Text(
            text = magazineName,
            style = MaterialTheme.typography.displaySmall.copy(
                fontWeight = FontWeight.SemiBold
            ),
            modifier = Modifier.padding(
                vertical = childPadding.top.times(3.5f)
            )
        )
        LazyVerticalGrid(
            columns = GridCells.Fixed(6),
            contentPadding = PaddingValues(bottom = JetStreamBottomListPadding)
        ) {
            itemsIndexed(
                clips,
                key = { _, clip ->
                    clip.id
                }
            ) { index, clip ->
                MovieCard(
                    onClick = { onClipSelected(clip) },
                    modifier = Modifier
                        .aspectRatio(1 / 1.5f)
                        .padding(8.dp)
                        .then(
                            if (index == 0)
                                Modifier.focusOnInitialVisibility(isFirstItemVisible)
                            else Modifier
                        ),
                ) {
                    PosterImage(
                        posterURI = clip.artworkUrl,
                        name = clip.episodeTitle,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}
