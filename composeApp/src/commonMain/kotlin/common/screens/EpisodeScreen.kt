package common.screens

import VideoPlayer
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.annotation.ExperimentalVoyagerApi
import cafe.adriel.voyager.core.lifecycle.LifecycleEffectOnce
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import coil3.compose.AsyncImage
import com.mgtvapi.Parcelable
import com.mgtvapi.Parcelize
import com.mgtvapi.api.model.Clip
import com.mgtvapi.domain.ResultState
import com.mgtvapi.viewModel.ClipData
import com.mgtvapi.viewModel.CommonViewModel
import common.components.DropdownMenu
import common.components.MGCircularProgressIndicator
import common.components.MGTopAppBar
import getPlatform
import org.koin.compose.koinInject

@Parcelize
data class EpisodeScreen(
    val clip: Clip,
) : Screen, Parcelable {
    @OptIn(ExperimentalVoyagerApi::class)
    @Composable
    override fun Content() {
        val commonViewModel = koinInject<CommonViewModel>()
        val clipDataState = commonViewModel.clipData.collectAsState()
        val navigator = LocalNavigator.currentOrThrow

        LifecycleEffectOnce {
            commonViewModel.getClipFiles(clipId = clip.id)
        }

        Scaffold(topBar = {
            MGTopAppBar(onBackPressed = { navigator.pop() }, showBackButton = true)
        }) {
            when (clipDataState.value) {
                is ResultState.Error, ResultState.Loading, ResultState.Empty -> MGCircularProgressIndicator()
                is ResultState.Success -> {
                    val clipData =
                        (clipDataState.value as ResultState.Success<ClipData>).data
                    val clipFiles = clipData.clipFiles
                    val clipFile = clipFiles.first()
                    val chosenQuality = remember { mutableStateOf(clipFile) }
                    val startPlaying = remember { mutableStateOf(false) }

                    LazyColumn(modifier = Modifier.padding(it)) {
                        item {
                            Box(Modifier.height(250.dp)) {
                                if (!startPlaying.value) {
                                    AsyncImage(
                                        model = clip.image,
                                        contentDescription = clip.title,
                                        modifier = Modifier.fillMaxWidth().height(250.dp),
                                        contentScale = ContentScale.Crop,
                                    )
                                    IconButton(onClick = { startPlaying.value = true }, content = {
                                        Icon(
                                            imageVector = Icons.Filled.PlayCircle,
                                            contentDescription = "play-button",
                                            modifier = Modifier.size(50.dp),
                                        )
                                    }, modifier = Modifier.align(Alignment.Center))
                                } else {
                                    if (getPlatform().name == "Android") {
                                        navigator.push(PlayerScreen {
                                            VideoPlayer(
                                                modifier = Modifier.fillMaxSize(),
                                                url = chosenQuality.value.url.toUrl(),
                                                cookie = mapOf("cookie" to clipData.cookie),
                                                playbackArtwork = clip.image,
                                                playbackTitle = clip.projectTitle,
                                                onVideoIsPlayingTask = { progress ->
                                                    commonViewModel.updateClipProgress(
                                                        clip.id,
                                                        progress
                                                    )
                                                }
                                            )
                                        })
                                    } else {
                                        VideoPlayer(
                                            modifier = Modifier.fillMaxSize(),
                                            url = chosenQuality.value.url.toUrl(),
                                            cookie = mapOf("cookie" to clipData.cookie),
                                            playbackArtwork = clip.image,
                                            playbackTitle = clip.projectTitle,
                                            onVideoIsPlayingTask = { progress ->
                                                commonViewModel.updateClipProgress(
                                                    clip.id,
                                                    progress
                                                )
                                            }
                                        )
                                    }
                                }
                            }
                            if (clipFiles.size > 1)
                                DropdownMenu(
                                    clipFiles,
                                    onChanged = { chosenClip -> chosenQuality.value = chosenClip },
                                )


                            Spacer(Modifier.height(10.dp))
                            Text(
                                clip.projectTitle,
                                style = MaterialTheme.typography.headlineSmall,
                                modifier = Modifier.padding(8.dp)
                            )
                            Spacer(Modifier.height(5.dp))
                            Text(
                                clip.title,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(8.dp)
                            )
                            Spacer(Modifier.height(2.5.dp))
                            Text(clip.description, modifier = Modifier.padding(8.dp))
                        }
                    }
                }
            }
        }
    }
}


fun String.toUrl(): String {
    return "https:$this"
}

@Parcelize
private data class PlayerScreen(
    val child: @Composable() () -> Unit
) : Screen, Parcelable {

    @Composable
    override fun Content() {
        Box(Modifier.background(Color.Black)) {
            child()
        }
    }
}