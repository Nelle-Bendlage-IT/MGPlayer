import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier


@Composable
expect fun VideoPlayer(
    modifier: Modifier,
    url: String,
    playbackTitle: String,
    playbackArtwork: String,
    onVideoIsPlayingTask: (progress: Int) -> Unit,
    playbackSubtitle: String,
)

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
