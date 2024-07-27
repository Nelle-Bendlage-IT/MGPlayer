package common.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.mgtv.shared_core.core.ViewState
import com.mgtvapi.api.model.File
import com.mgtvapi.api.model.WatchResponse
import common.components.DropdownMenu
import common.components.MGCircularProgressIndicator
import common.components.MGTopAppBar

@Composable
fun EpisodeScreen(
    getClipFiles: () -> Unit,
    clipDataState: State<ViewState<WatchResponse>>,
    onBackPressed: () -> Unit,
    onPlayPressed: (String) -> Unit,
    onChangeQuality: (File) -> Unit,
) {

    LaunchedEffect(Unit) {
        getClipFiles()
    }


    Scaffold(topBar = {
        MGTopAppBar(onBackPressed = onBackPressed, showBackButton = true)
    }) {
        when (clipDataState.value) {
            is ViewState.Error, ViewState.Loading, ViewState.Empty -> MGCircularProgressIndicator()
            is ViewState.Success -> {
                val clipData =
                    (clipDataState.value as ViewState.Success<WatchResponse>).data
                val clipFiles = clipData.stream.media.files
                val clipFile = clipFiles.first()

                LazyColumn(modifier = Modifier.padding(it)) {
                    item {
                        Box(Modifier.height(250.dp)) {
                            AsyncImage(
                                model = clipData.clip.image,
                                contentDescription = clipData.clip.title,
                                modifier = Modifier.fillMaxWidth().height(250.dp),
                                contentScale = ContentScale.Crop,
                            )
                            IconButton(onClick = { onPlayPressed(clipData.clip.id) }, content = {
                                Icon(
                                    imageVector = Icons.Filled.PlayCircle,
                                    contentDescription = "play-button",
                                    modifier = Modifier.size(50.dp),
                                )
                            }, modifier = Modifier.align(Alignment.Center))
                        }
                        if (clipFiles.size > 1)
                            DropdownMenu(
                                clipFiles,
                                onChanged = { chosenClip -> onChangeQuality(chosenClip) },
                            )


                        Spacer(Modifier.height(10.dp))
                        Text(
                            clipData.clip.projectTitle,
                            style = MaterialTheme.typography.headlineSmall,
                            modifier = Modifier.padding(8.dp)
                        )
                        Spacer(Modifier.height(5.dp))
                        Text(
                            clipData.clip.title,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(8.dp)
                        )
                        Spacer(Modifier.height(2.5.dp))
                        Text(clipData.clip.description, modifier = Modifier.padding(8.dp))
                    }
                }
            }
        }
    }
}


@Composable
private fun PlayerScreen(
    child: @Composable() () -> Unit
) {
    Box(Modifier.background(Color.Black)) {
        child()
    }
}
