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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.tv.material3.CardScale
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import androidx.tv.material3.WideClassicCard
import com.mgplayer.tv.presentation.common.ErrorScreen
import com.mgplayer.tv.presentation.common.Loading
import com.mgplayer.tv.presentation.common.PosterImage
import com.mgplayer.tv.presentation.screens.dashboard.rememberChildPadding
import com.mgplayer.tv.presentation.utils.focusOnInitialVisibility
import com.mgtv.shared_core.core.ViewState
import com.mgtvapi.api.model.Clip
import com.mgtvapi.viewModel.MagazineOverviewViewModel
import org.koin.compose.koinInject

object MagazineClipsListScreen {
    const val MagazineIdBundleKey = "magazineName"
    const val IsActiveKey = "active"
}

@Composable
fun MagazineClipsListScreen(
    onBackPressed: () -> Unit,
    onClipSelected: (Clip) -> Unit,
    magazineId: String,
    magazineOverviewViewModel: MagazineOverviewViewModel = koinInject<MagazineOverviewViewModel>(),
    isActive: Boolean,
) {
    LaunchedEffect(Unit) {
        magazineOverviewViewModel.chosenMagazine = magazineId
        magazineOverviewViewModel.fetchMagazine(magazineId, 40, 0)
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
                magazineName = s.data.first().projectTitle,
                onBackPressed = onBackPressed,
                onClipSelected = onClipSelected,
                clips = s.data,
                isActive = isActive
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
    modifier: Modifier = Modifier,
    isActive: Boolean
) {
    val childPadding = rememberChildPadding()
    val isFirstItemVisible = remember { mutableStateOf(false) }

    BackHandler(onBack = onBackPressed)
    val lazyListState = rememberLazyListState()
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier,
    ) {
        Text(
            text = magazineName,
            style = MaterialTheme.typography.displaySmall.copy(
                fontWeight = FontWeight.SemiBold
            ),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(
                vertical = childPadding.top.times(3.5f)
            )
        )
        LazyColumn(state = lazyListState) {
            itemsIndexed(
                clips,
                key = { _, clip ->
                    clip.id
                }
            ) { index, clip ->
                WideClassicCard(
                    scale = CardScale.None,
                    onClick = { onClipSelected(clip) },
                    modifier = Modifier
                        .then(
                            if (index == 0)
                                Modifier.focusOnInitialVisibility(isFirstItemVisible)
                            else Modifier
                        )
                        .fillMaxWidth()
                        .padding(8.dp),
                    image = {
                        PosterImage(
                            posterURI = clip.artworkUrl,
                            name = clip.episodeTitle,
                            modifier = Modifier
                                .width(147.dp)
                                .aspectRatio(16f / 9f)
                        )
                    },
                    title = {
                        Text(clip.episodeTitle, modifier = Modifier.padding(4.dp))
                    },
                    subtitle = {
                        Text(
                            if (clip.shortDescription.isNullOrEmpty()) clip.description else clip.shortDescription!!,
                            modifier = Modifier.padding(4.dp)
                        )
                    }

                )


//                MovieCard(
//                    onClick = { onClipSelected(clip) },
//                    modifier = Modifier
//                        .aspectRatio(1 / 1.5f)
//                        .padding(8.dp)
//                        .then(
//                            if (index == 0)
//                                Modifier.focusOnInitialVisibility(isFirstItemVisible)
//                            else Modifier
//                        ),
//                ) {
//                    PosterImage(
//                        posterURI = img,
//                        name = clip.episodeTitle,
//                        modifier = Modifier.fillMaxSize()
//                    )
//                }
            }
        }

    }
}
