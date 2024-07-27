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
import coil3.compose.AsyncImage
import com.mgtv.shared_core.core.ViewState
import com.mgtvapi.api.model.Magazine
import common.components.MGCircularProgressIndicator

@Composable
fun MagazineOverview(
    onItemClick: (String, Magazine) -> Unit,
    innerPadding: PaddingValues,
    fetchMagazines: () -> Unit,
    magazines: ViewState<List<Magazine>>
) {

    LaunchedEffect(Unit) {
        fetchMagazines()
    }
    MagazineOverviewContent(
        magazines = magazines,
        onItemClick = onItemClick,
        innerPadding = innerPadding
    )

}

@Composable
private fun MagazineOverviewContent(
    magazines: ViewState<List<Magazine>>,
    onItemClick: (String, Magazine) -> Unit,
    innerPadding: PaddingValues,
) {
    when (magazines) {
        is ViewState.Loading, is ViewState.Empty -> MGCircularProgressIndicator()
        is ViewState.Error -> Text(magazines.message, style = MaterialTheme.typography.bodyLarge)
        is ViewState.Success -> {
            LazyVerticalGrid(
                modifier = Modifier.padding(innerPadding),
                contentPadding = PaddingValues(5.dp),
                columns = GridCells.Adaptive(minSize = 128.dp)
            ) {
                items(magazines.data.reversed()) {
                    val magazine = it.getSelf()
                    Box(
                        Modifier.padding(3.dp).clip(
                            RoundedCornerShape(16.dp)
                        ).aspectRatio(16f / 9f).clickable {
                            onItemClick(magazine.pid.toString(), magazine)
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