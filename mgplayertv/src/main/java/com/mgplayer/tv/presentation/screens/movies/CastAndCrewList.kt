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

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Border
import androidx.tv.material3.CardDefaults
import androidx.tv.material3.ClassicCard
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.mgplayer.tv.R
import com.mgplayer.tv.presentation.common.PosterImage
import com.mgplayer.tv.presentation.screens.dashboard.rememberChildPadding
import com.mgplayer.tv.presentation.theme.JetStreamBorderWidth
import com.mgplayer.tv.presentation.theme.JetStreamCardShape
import com.mgtvapi.api.model.Participant

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun CastAndCrewList(castAndCrew: List<Participant>) {
    val childPadding = rememberChildPadding()

    Column(
        modifier = Modifier.padding(top = childPadding.top),
    ) {
        Text(
            text = stringResource(R.string.cast_and_crew),
            style = MaterialTheme.typography.titleMedium.copy(
                fontSize = 18.sp
            ),
            modifier = Modifier.padding(start = childPadding.start)
        )
        // ToDo: specify the pivot offset
        LazyRow(
            modifier = Modifier
                .padding(top = 16.dp)
                .focusRestorer(),
            contentPadding = PaddingValues(start = childPadding.start)
        ) {
            items(castAndCrew, key = { it.id }) {
                CastAndCrewItem(it, modifier = Modifier.width(144.dp))
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable

private fun CastAndCrewItem(
    participant: Participant,
    modifier: Modifier = Modifier,
) {
    ClassicCard(
        modifier = modifier
            .padding(end = 20.dp, bottom = 16.dp)
            .aspectRatio(1 / 1.8f),
        shape = CardDefaults.shape(shape = JetStreamCardShape),
        scale = CardDefaults.scale(focusedScale = 1f),
        border = CardDefaults.border(
            focusedBorder = Border(
                border = BorderStroke(
                    width = JetStreamBorderWidth,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                shape = JetStreamCardShape
            )
        ),
        title = {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp)
                    .padding(horizontal = 12.dp),
                text = participant.name,
                maxLines = 1,
                style = MaterialTheme.typography.labelMedium,
                overflow = TextOverflow.Ellipsis
            )
        },
        image = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.725f)
            ) {
                PosterImage(
                    posterURI = participant.getParticipantPicture(),
                    name = participant.name,
                    modifier = Modifier.fillMaxSize()
                )
            }
        },
        onClick = {}
    )
}
