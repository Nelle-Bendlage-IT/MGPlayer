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

package com.mgplayer.tv.presentation.screens.videoPlayer

import android.content.Context
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ClosedCaption
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaItem.RequestMetadata
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.ui.PlayerView
import com.mgplayer.tv.presentation.screens.videoPlayer.components.VideoPlayerControlsIcon
import com.mgplayer.tv.presentation.screens.videoPlayer.components.VideoPlayerMainFrame
import com.mgplayer.tv.presentation.screens.videoPlayer.components.VideoPlayerOverlay
import com.mgplayer.tv.presentation.screens.videoPlayer.components.VideoPlayerPulse
import com.mgplayer.tv.presentation.screens.videoPlayer.components.VideoPlayerPulse.Type.BACK
import com.mgplayer.tv.presentation.screens.videoPlayer.components.VideoPlayerPulse.Type.FORWARD
import com.mgplayer.tv.presentation.screens.videoPlayer.components.VideoPlayerPulseState
import com.mgplayer.tv.presentation.screens.videoPlayer.components.VideoPlayerSeeker
import com.mgplayer.tv.presentation.screens.videoPlayer.components.VideoPlayerState
import com.mgplayer.tv.presentation.screens.videoPlayer.components.rememberVideoPlayerPulseState
import com.mgplayer.tv.presentation.screens.videoPlayer.components.rememberVideoPlayerState
import com.mgplayer.tv.data.util.StringConstants
import com.mgplayer.tv.presentation.screens.videoPlayer.components.VideoPlayerMediaTitle
import com.mgplayer.tv.presentation.screens.videoPlayer.components.VideoPlayerMediaTitleType
import com.mgplayer.tv.presentation.utils.handleDPadKeyEvents
import com.mgtvapi.api.model.Clip
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

object VideoPlayerScreen {
    const val MovieIdBundleKey = "movieId"
}

/**
 * [Work in progress] A composable screen for playing a video.
 *
 * @param onBackPressed The callback to invoke when the user presses the back button.
 */
@Composable
fun VideoPlayerScreen(
    onBackPressed: () -> Unit,
    videoUrl: String,
    clip: Clip
) {

    VideoPlayerScreenContent(
        onBackPressed = onBackPressed,
        videoUrl = videoUrl,
        clip = clip
    )
}


@androidx.annotation.OptIn(UnstableApi::class)
@Composable
fun VideoPlayerScreenContent(
    clip: Clip,
    videoUrl: String,
    onBackPressed: () -> Unit
) {
    val context = LocalContext.current
    val videoPlayerState = rememberVideoPlayerState(hideSeconds = 4)

    // TODO: Move to ViewModel for better reuse
    val exoPlayer = rememberExoPlayer(context)
    LaunchedEffect(exoPlayer, clip) {
        exoPlayer.setMediaItem(
            MediaItem.Builder()
                .setMediaId(clip.id)
                .setUri(videoUrl)
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(clip.title)
                        .setSubtitle(clip.projectTitle)
                        .setArtworkUri(Uri.parse(clip.artworkUrl)).build(),
                )
                .setRequestMetadata(RequestMetadata.Builder().build())
                .build()
        )
        exoPlayer.prepare()
    }

    var contentCurrentPosition by remember { mutableLongStateOf(0L) }
    var isPlaying: Boolean by remember { mutableStateOf(exoPlayer.isPlaying) }
    // TODO: Update in a more thoughtful manner
    LaunchedEffect(Unit) {
        while (true) {
            delay(300)
            contentCurrentPosition = exoPlayer.currentPosition
            isPlaying = exoPlayer.isPlaying
        }
    }

    BackHandler(onBack = onBackPressed)

    val pulseState = rememberVideoPlayerPulseState()

    Box(
        Modifier
            .dPadEvents(
                exoPlayer,
                videoPlayerState,
                pulseState
            )
            .focusable()
    ) {
        AndroidView(
            factory = {
                PlayerView(context).apply { useController = false }
            },
            update = { it.player = exoPlayer },
            onRelease = { exoPlayer.release() }
        )

        val focusRequester = remember { FocusRequester() }
        VideoPlayerOverlay(
            modifier = Modifier.align(Alignment.BottomCenter),
            focusRequester = focusRequester,
            state = videoPlayerState,
            isPlaying = isPlaying,
            centerButton = { VideoPlayerPulse(pulseState) },
            subtitles = { /* TODO Implement subtitles */ },
            controls = {
                VideoPlayerControls(
                    clip,
                    isPlaying,
                    contentCurrentPosition,
                    exoPlayer,
                    videoPlayerState,
                    focusRequester
                )
            }
        )
    }
}

@Composable
fun VideoPlayerControls(
    movieDetails: Clip,
    isPlaying: Boolean,
    contentCurrentPosition: Long,
    exoPlayer: ExoPlayer,
    state: VideoPlayerState,
    focusRequester: FocusRequester
) {
    val onPlayPauseToggle = { shouldPlay: Boolean ->
        if (shouldPlay) {
            exoPlayer.play()
        } else {
            exoPlayer.pause()
        }
    }

    VideoPlayerMainFrame(
        mediaTitle = {
            VideoPlayerMediaTitle(
                title = movieDetails.title,
                secondaryText = movieDetails.releaseDateFormatted(),
                type = VideoPlayerMediaTitleType.DEFAULT
            )
        },
        mediaActions = {
            Row(
                modifier = Modifier.padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                VideoPlayerControlsIcon(
                    icon = Icons.Default.Menu,
                    state = state,
                    isPlaying = isPlaying,
                    contentDescription = StringConstants
                        .Composable
                        .VideoPlayerControlPlaylistButton
                )
                VideoPlayerControlsIcon(
                    modifier = Modifier.padding(start = 12.dp),
                    icon = Icons.Default.ClosedCaption,
                    state = state,
                    isPlaying = isPlaying,
                    contentDescription = StringConstants
                        .Composable
                        .VideoPlayerControlClosedCaptionsButton
                )
                VideoPlayerControlsIcon(
                    modifier = Modifier.padding(start = 12.dp),
                    icon = Icons.Default.Settings,
                    state = state,
                    isPlaying = isPlaying,
                    contentDescription = StringConstants
                        .Composable
                        .VideoPlayerControlSettingsButton
                )
            }
        },
        seeker = {
            VideoPlayerSeeker(
                focusRequester,
                state,
                isPlaying,
                onPlayPauseToggle,
                onSeek = { exoPlayer.seekTo(exoPlayer.duration.times(it).toLong()) },
                contentProgress = contentCurrentPosition.milliseconds,
                contentDuration = exoPlayer.duration.milliseconds
            )
        },
        more = null
    )
}

@androidx.annotation.OptIn(UnstableApi::class)
@Composable
private fun rememberExoPlayer(context: Context) = remember {
    ExoPlayer.Builder(context)
        .setSeekForwardIncrementMs(10)
        .setSeekBackIncrementMs(10)
        .setMediaSourceFactory(
            ProgressiveMediaSource.Factory(DefaultDataSource.Factory(context))
        )
        .setVideoScalingMode(C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING)
        .build()
        .apply {
            playWhenReady = true
            repeatMode = Player.REPEAT_MODE_ONE
        }
}

private fun Modifier.dPadEvents(
    exoPlayer: ExoPlayer,
    videoPlayerState: VideoPlayerState,
    pulseState: VideoPlayerPulseState
): Modifier = this.handleDPadKeyEvents(
    onLeft = {
        if (!videoPlayerState.controlsVisible) {
            exoPlayer.seekBack()
            pulseState.setType(BACK)
        }
    },
    onRight = {
        if (!videoPlayerState.controlsVisible) {
            exoPlayer.seekForward()
            pulseState.setType(FORWARD)
        }
    },
    onUp = { videoPlayerState.showControls() },
    onDown = { videoPlayerState.showControls() },
    onEnter = {
        exoPlayer.pause()
        videoPlayerState.showControls()
    }
)
