package common.screens

import Parcelable
import Parcelize
import VideoPlayer
import androidx.compose.animation.core.tween
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
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.mgtvapi.api.model.Clip
import com.mgtvapi.api.model.ClipFile
import com.mgtvapi.domain.ResultState
import com.mgtvapi.viewModel.ClipData
import com.mgtvapi.viewModel.CommonViewModel
import common.components.MGCircularProgressIndicator
import common.components.MGTopAppBar
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Parcelize
data class EpisodeScreen(
    val clip: Clip,
) : Screen, Parcelable {
    @Composable
    override fun Content() {
        val commonViewModel = koinInject<CommonViewModel>()
        val scope = rememberCoroutineScope()
        val clipDataState = commonViewModel.clipData.collectAsState()
        val navigator = LocalNavigator.currentOrThrow

        LaunchedEffect(Unit) {
            scope.launch(Dispatchers.IO) {
                commonViewModel.getClipFiles(clipId = clip.id)
            }
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
                    val startPlaying = remember { mutableStateOf(false) }
                    val chosenQuality = remember { mutableStateOf(clipFile) }
                    LazyColumn(modifier = Modifier.padding(it)) {
                        item {
                            Box(Modifier.height(250.dp)) {
                                if (!startPlaying.value) {
                                    KamelImage(
                                        resource = asyncPainterResource(data = clip.image),
                                        contentDescription = clip.title,
                                        modifier = Modifier.fillMaxWidth().height(250.dp),
                                        contentScale = ContentScale.Crop,
                                        animationSpec = tween(),
                                    )
                                    IconButton(onClick = { startPlaying.value = true }, content = {
                                        Icon(
                                            imageVector = Icons.Filled.PlayCircle,
                                            contentDescription = "play-button",
                                            modifier = Modifier.size(50.dp),
                                        )
                                    }, modifier = Modifier.align(Alignment.Center))
                                } else {
                                    VideoPlayer(
                                        modifier = Modifier.fillMaxSize(),
                                        url = chosenQuality.value.url.toUrl(),
                                        cookie = mapOf("cookie" to clipData.cookie),
                                        playbackArtwork = clip.image,
                                        playbackTitle = clip.projectTitle,
                                    )
                                }
                            }
                            DropdownMenu(
                                clipFiles,
                                onChanged = { chosenClip -> chosenQuality.value = chosenClip },
                            )
                            Spacer(Modifier.height(10.dp))
                            Text(clip.projectTitle, style = MaterialTheme.typography.headlineSmall)
                            Spacer(Modifier.height(5.dp))
                            Text(clip.title, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(2.5.dp))
                            Text(clip.description)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DropdownMenu(
    options: List<ClipFile>,
    onChanged: (clipFile: ClipFile) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    var text by remember { mutableStateOf(options[0].desc) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
    ) {
        TextField(
            modifier = Modifier.menuAnchor(),
            value = text,
            onValueChange = {},
            readOnly = true,
            singleLine = true,
            label = { Text("Quality") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.textFieldColors(),
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Text(
                            option.desc,
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    },
                    onClick = {
                        onChanged(option)
                        text = option.desc
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                )
            }
        }
    }
}

fun String.toUrl(): String {
    return "https:$this"
}