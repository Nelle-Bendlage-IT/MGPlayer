package features.magazineOverview

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import coil3.compose.AsyncImage
import com.mgtvapi.api.model.Magazine
import com.mgtvapi.domain.ResultState
import common.components.MGCircularProgressIndicator
import common.screens.MagazineScreen

@Composable
fun MagazineOverview(
    fetchMagazines: () -> Unit,
    magazines: ResultState<List<Magazine>>
) {
    val navigator = LocalNavigator.currentOrThrow

    LaunchedEffect(Unit) {
        fetchMagazines()
    }


    when (magazines) {
        is ResultState.Loading, is ResultState.Empty -> MGCircularProgressIndicator()
        is ResultState.Error -> Text(magazines.message, style = MaterialTheme.typography.bodyLarge)
        is ResultState.Success -> {
            LazyVerticalGrid(
                contentPadding = PaddingValues(5.dp),
                columns = GridCells.Adaptive(minSize = 128.dp)
            ) {
                items(magazines.data.reversed()) {
                    val magazine = it.getSelf()
                    Box(
                        Modifier.padding(3.dp).clip(
                            RoundedCornerShape(16.dp)
                        ).aspectRatio(16f / 9f).clickable {
                            navigator.push(
                                MagazineScreen(
                                    magazine = magazine
                                )
                            )
                        }
                    ) {
                        AsyncImage(
                            contentDescription = magazine.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize(),
                            model = magazine.artwork.logoSquare
                        )
                    }
                }
            }
        }
    }
}

