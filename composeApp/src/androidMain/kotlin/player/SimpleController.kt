package player

import android.util.Log
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.systemGestureExclusion
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

/**
 * A simple controller, which consists of a play/pause button and a time bar.
 */
@Composable
fun SimpleController(
    mediaState: MediaState,
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
) {
    val title = mediaState.playerState?.mediaMetadata?.title
    val subtitle = mediaState.playerState?.mediaMetadata?.subtitle

    Crossfade(targetState = mediaState.isControllerShowing, modifier, label = "") { isShowing ->
        if (isShowing) {
            val controllerState = rememberControllerState(mediaState)
            var scrubbing by remember { mutableStateOf(false) }
            var scrubPosition by remember { mutableLongStateOf(0L) }
            val hideWhenTimeout = !mediaState.shouldShowControllerIndefinitely && !scrubbing
            var hideEffectReset by remember { mutableIntStateOf(0) }

            LaunchedEffect(hideWhenTimeout, hideEffectReset) {
                if (hideWhenTimeout) {
                    // hide after 3s
                    delay(3000)
                    mediaState.isControllerShowing = false
                }
            }
            Surface(
                color = Color(0x98000000),
                contentColor = Color.White,
                modifier = Modifier
                    .fillMaxSize()
            ) {
                Box {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.TopStart)
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBackIos, contentDescription = null)
                        }
                        Column(

                        ) {
                            if (!title.isNullOrBlank()) {
                                Text(
                                    text = title.toString(),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White
                                )
                            }

                            if (!subtitle.isNullOrBlank()) {
                                Text(
                                    text = subtitle.toString(),
                                    style = MaterialTheme.typography.titleSmall,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    IconButton(
                        onClick = onSettingsClick,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = null)
                    }

                    Row(
                        modifier = Modifier
                            .align(Alignment.Center),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { /*TODO*/ }, modifier = Modifier.size(52.dp)) {
                            Icon(Icons.Default.FastRewind, contentDescription = null)
                        }

                        Crossfade(targetState = controllerState.showPause, label = "PlayButton") {
                            IconButton(
                                modifier = Modifier.size(80.dp),
                                onClick = {
                                    hideEffectReset++
                                    controllerState.playOrPause()
                                }
                            ) {
                                Icon(
                                    modifier = Modifier
                                        .size(60.dp)
                                        .padding(4.dp),
                                    imageVector = if (it) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = null
                                )
                            }
                        }

                        IconButton(onClick = {
                            mediaState.playerState?.seekForwardIncrement
                        }, modifier = Modifier.size(52.dp)) {
                            Icon(Icons.Default.FastForward, contentDescription = null)
                        }
                    }


                    LaunchedEffect(Unit) {
                        while (true) {
                            delay(200)
                            controllerState.triggerPositionUpdate()
                        }
                    }



                    Column(
                        Modifier.align(Alignment.BottomCenter)
                    ) {

                        TimeBar(
                            controllerState.durationMs,
                            controllerState.positionMs,
                            controllerState.bufferedPositionMs,
                            modifier = Modifier
                                .systemGestureExclusion()
                                .fillMaxWidth()
                                .height(28.dp),
                            contentPadding = PaddingValues(12.dp),
                            scrubberCenterAsAnchor = true,
                            onScrubStart = { scrubbing = true },
                            onScrubMove = { scrubPosition = it },
                            onScrubStop = { positionMs ->
                                scrubbing = false
                                controllerState.seekTo(positionMs)
                            }
                        )

                        Row(
                            modifier = Modifier
                                .padding(horizontal = 12.dp)
                                .padding(top = 8.dp, bottom = 16.dp)
                        ) {
                            Text(
                                text =
                                if (scrubbing) {
                                    scrubPosition.toTimeString()
                                } else {
                                    controllerState.positionMs.toTimeString()
                                },
                                color = Color.White,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color.LightGray
                                ),
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Text(
                                text = controllerState.durationMs.toTimeString(),
                                color = Color.White,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color.LightGray
                                ),
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Convert a time in milliseconds to a string in the format of "mm:ss" or "hh:mm:ss".
 */
private fun Long.toTimeString(): String {
    if (this < 0) {
        return "00:00"
    }

    val hours = this / 3600000
    val minutes = (this % 3600000) / 60000
    val seconds = (this % 60000) / 1000
    return if (hours > 0) {
        "%02d:%02d:%02d".format(hours, minutes, seconds)
    } else {
        "00:%02d:%02d".format(minutes, seconds)
    }
}

