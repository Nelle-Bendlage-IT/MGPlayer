package common.components

import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mgtvapi.api.model.PlayableClip
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource

@Composable
fun ClipCard(
    clip: PlayableClip,
    onClick: () -> Unit,
) {
    Card(
        Modifier.fillMaxWidth().padding(start = 8.dp, end = 8.dp),
        shape = RoundedCornerShape(8.dp),
        elevation =
        CardDefaults.cardElevation(
            defaultElevation = 10.dp,
        ),
    ) {
        KamelImage(
            resource = asyncPainterResource(data = clip.artworkUrl),
            contentDescription = clip.episodeTitle,
            modifier = Modifier.height(175.dp).fillMaxWidth().clickable { onClick() },
            contentScale = ContentScale.Crop,
            animationSpec = tween(),
        )
        Text(
            clip.episodeTitle,
            modifier = Modifier.padding(8.dp),
            style =
            MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            ),
        )
        Text(
            clip.summary,
            modifier = Modifier.padding(8.dp),
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}
