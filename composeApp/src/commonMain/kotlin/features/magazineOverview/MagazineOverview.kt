package features.magazineOverview

import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.mgtvapi.api.model.Clip
import com.mgtvapi.api.model.Ep
import com.mgtvapi.api.model.ListableClip
import com.mgtvapi.api.model.Magazine
import com.mgtvapi.domain.ResultState
import common.components.ClipCard
import common.components.DropdownMenu
import common.components.MGCircularProgressIndicator
import common.screens.EpisodeScreen
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource

@Composable
fun MagazineOverview(
    fetchMagazine: (magazineID: String, limit: Int) -> Unit,
    magazines: ResultState<List<Magazine>>,
    magazineEpisodes: ResultState<List<Ep>>
) {
    val chosenMagazine = remember { mutableStateOf<ListableClip<Magazine>?>(null) }
    val navigator = LocalNavigator.currentOrThrow



    LaunchedEffect(chosenMagazine.value) {
        if (chosenMagazine.value != null) {
            fetchMagazine("${chosenMagazine.value!!.getSelf().pid}", 20)
        }
    }

    when (magazines) {
        is ResultState.Loading, is ResultState.Empty -> MGCircularProgressIndicator()
        is ResultState.Error -> Text(magazines.message, style = MaterialTheme.typography.bodyLarge)
        is ResultState.Success -> {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                item {
                    if (chosenMagazine.value != null) {
                        val magazine = chosenMagazine.value!!.getSelf()
                        Card(
                            Modifier.clip(
                                RoundedCornerShape(16.dp)
                            ).padding(8.dp).background(Color.Transparent)
                        ) {
                            KamelImage(
                                resource = asyncPainterResource(data = if (magazine.artwork.banner == null) magazine.artwork.logo else magazine.artwork.banner!!.n1x),
                                contentDescription = magazine.title,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop,
                                animationSpec = tween(),
                            )
                        }
                    }
                    DropdownMenu(
                        magazines.data,
                        onChanged = { chosenMagazine.value = it },
                        label = "Magazine"
                    )
                }
                when (magazineEpisodes) {
                    is ResultState.Loading -> item {
                        MGCircularProgressIndicator()
                    }

                    is ResultState.Empty -> item {}

                    is ResultState.Error -> item {
                        Text(magazineEpisodes.message, style = MaterialTheme.typography.bodyLarge)
                    }

                    is ResultState.Success -> items(magazineEpisodes.data) {
                        ClipCard(
                            clip = it,
                            onClick = { navigator.push(EpisodeScreen(clip = it.toClip())) })
                    }
                }
            }
        }
    }
}

fun Ep.toClip(): Clip {
    return Clip(
        id = identifier,
        magazineId = this.pid,
        canAccess = canAccess,
        categoryId = contentType,
        categoryTitle = null,
        hasDownload = true,
        time = date,
        seqNr = enum,
        hideSeqNr = false,
        title = episodeTitle,
        projectTitle = magazineTitle,
        image = artworkUrl,
        description = desc,
        shortDescription = desc,
        duration = duration,
        chapterSections = listOf(),
        participants = listOf(),
        teaserFile = null,
    )
}
