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

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.focus.FocusState
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.mgplayer.tv.R
import com.mgplayer.tv.presentation.common.ImmersiveListMoviesRow
import com.mgplayer.tv.presentation.common.ItemDirection
import com.mgplayer.tv.presentation.common.PosterImage
import com.mgplayer.tv.presentation.screens.dashboard.rememberChildPadding
import com.mgplayer.tv.presentation.utils.bringIntoViewIfChildrenAreFocused
import com.mgtvapi.api.model.Clip
import com.mgtvapi.api.model.ProgressClip

@Composable
fun RecentlyWatchedList(
    clipList: List<ProgressClip>,
    modifier: Modifier = Modifier,
    gradientColor: Color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
    onClipClick: (clip: Clip) -> Unit
) {
    var isListFocused by remember { mutableStateOf(false) }
    var selectedMovie by remember(clipList) { mutableStateOf(clipList.first().clip) }

    val sectionTitle = if (isListFocused) {
        null
    } else {
        stringResource(R.string.last_seen)
    }

    ImmersiveList(
        selectedClip = selectedMovie,
        isListFocused = isListFocused,
        gradientColor = gradientColor,
        clipList = clipList.toClipList(),
        sectionTitle = sectionTitle,
        onClipClick = onClipClick,
        onClipFocused = {
            selectedMovie = it
        },
        onFocusChanged = {
            isListFocused = it.hasFocus
        },
        modifier = modifier.bringIntoViewIfChildrenAreFocused(
            PaddingValues(bottom = 116.dp)
        )
    )
}

@Composable
fun ImmersiveList(
    selectedClip: Clip,
    isListFocused: Boolean,
    gradientColor: Color,
    clipList: List<Clip>,
    sectionTitle: String?,
    onFocusChanged: (FocusState) -> Unit,
    onClipFocused: (Clip) -> Unit,
    onClipClick: (Clip) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        contentAlignment = Alignment.BottomStart,
        modifier = modifier
    ) {
        Background(
            clip = selectedClip,
            visible = isListFocused,
            modifier = modifier
                .height(432.dp)
                .gradientOverlay(gradientColor)
        )
        Column {
            if (isListFocused) {
                ClipDescription(
                    clip = selectedClip,
                    modifier = Modifier.padding(
                        start = rememberChildPadding().start,
                        bottom = 40.dp
                    )
                )
            }

            ImmersiveListMoviesRow(
                clipList = clipList,
                itemDirection = ItemDirection.Horizontal,
                title = sectionTitle,
                showItemTitle = !isListFocused,
                onClipSelected = onClipClick,
                onClipFocused = onClipFocused,
                modifier = Modifier.onFocusChanged(onFocusChanged).height(150.dp)
            )
        }
    }
}
fun List<ProgressClip>.toClipList(): List<Clip> {
    return this.map { it.clip }
}


@Composable
private fun Background(
    clip: Clip,
    visible: Boolean,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
        modifier = modifier
    ) {
        Crossfade(
            targetState = clip,
            label = "posterUriCrossfade",

            ) {
            PosterImage(
                name = it.projectTitle,
                posterURI = it.artworkUrl,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
private fun ClipDescription(
    clip: Clip,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(text = clip.projectTitle, style = MaterialTheme.typography.displaySmall)
        Text(
            modifier = Modifier.fillMaxWidth(0.5f),
            text = if(clip.shortDescription != null) clip.shortDescription!! else clip.description,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
            fontWeight = FontWeight.Light
        )
    }
}

private fun Modifier.gradientOverlay(gradientColor: Color): Modifier =
    drawWithCache {
        val horizontalGradient = Brush.horizontalGradient(
            colors = listOf(
                gradientColor,
                Color.Transparent
            ),
            startX = size.width.times(0.2f),
            endX = size.width.times(0.7f)
        )
        val verticalGradient = Brush.verticalGradient(
            colors = listOf(
                Color.Transparent,
                gradientColor
            ),
            endY = size.width.times(0.3f)
        )
        val linearGradient = Brush.linearGradient(
            colors = listOf(
                gradientColor,
                Color.Transparent
            ),
            start = Offset(
                size.width.times(0.2f),
                size.height.times(0.5f)
            ),
            end = Offset(
                size.width.times(0.9f),
                0f
            )
        )

        onDrawWithContent {
            drawContent()
            drawRect(horizontalGradient)
            drawRect(verticalGradient)
            drawRect(linearGradient)
        }
    }
