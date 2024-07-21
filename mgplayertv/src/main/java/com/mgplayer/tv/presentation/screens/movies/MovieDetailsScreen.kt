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


package com.mgplayer.tv.presentation.screens.movies

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.tv.material3.MaterialTheme
import com.mgplayer.tv.R
import com.mgplayer.tv.presentation.common.ErrorScreen
import com.mgplayer.tv.presentation.common.Loading
import com.mgplayer.tv.presentation.screens.dashboard.rememberChildPadding
import com.mgtv.shared_core.core.ViewState
import com.mgtvapi.api.model.Clip
import com.mgtvapi.viewModel.CommonViewModel
import kotlinx.datetime.LocalDate
import org.koin.compose.koinInject

object MovieDetailsScreen {
    const val MovieIdBundleKey = "movieId"
}

@Composable
fun ClipDetailsScreen(
    goToMoviePlayer: () -> Unit,
    onBackPressed: () -> Unit,
    refreshScreenWithNewMovie: (Clip) -> Unit,
    commonViewModel: CommonViewModel = koinInject<CommonViewModel>()
) {
    val clipData by commonViewModel.clipData.collectAsStateWithLifecycle()

    when (val s = clipData) {
        is ViewState.Loading, ViewState.Empty -> {
            Loading()
        }

        is ViewState.Error -> {
            ErrorScreen(modifier = Modifier.fillMaxSize())
        }

        is ViewState.Success -> {
            Details(
                clip = s.data.clip,
                goToMoviePlayer = goToMoviePlayer,
                onBackPressed = onBackPressed,
                refreshScreenWithNewMovie = refreshScreenWithNewMovie,
                modifier = Modifier
                    .fillMaxSize()
                    .animateContentSize()
            )
        }
    }
}

@Composable
private fun Details(
    clip: Clip,
    goToMoviePlayer: () -> Unit,
    onBackPressed: () -> Unit,
    refreshScreenWithNewMovie: (Clip) -> Unit,
    modifier: Modifier = Modifier,
) {
    val childPadding = rememberChildPadding()

    BackHandler(onBack = onBackPressed)
    LazyColumn(
        contentPadding = PaddingValues(bottom = 135.dp),
        modifier = modifier,
    ) {
        item {
            MovieDetails(
                clipDetails = clip,
                goToMoviePlayer = goToMoviePlayer
            )
        }

        item {
            CastAndCrewList(
                castAndCrew = clip.participants
            )
        }

//        item {
//            MoviesRow(
//                title = StringConstants
//                    .Composable
//                    .movieDetailsScreenSimilarTo(clip.projectTitle),
//                titleStyle = MaterialTheme.typography.titleMedium,
//                movieList = clip.similarMovies,
//                onMovieSelected = refreshScreenWithNewMovie
//            )
//        }

        item {
            Box(
                modifier = Modifier
                    .padding(horizontal = childPadding.start)
                    .padding(BottomDividerPadding)
                    .fillMaxWidth()
                    .height(1.dp)
                    .alpha(0.15f)
                    .background(MaterialTheme.colorScheme.onSurface)
            )
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = childPadding.start),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val itemModifier = Modifier.width(192.dp)

                TitleValueText(
                    modifier = itemModifier,
                    title = stringResource(R.string.status),
                    value = LocalDate.parse(clip.time.toString()).toString()
                )
            }
        }
    }
}

private val BottomDividerPadding = PaddingValues(vertical = 48.dp)
