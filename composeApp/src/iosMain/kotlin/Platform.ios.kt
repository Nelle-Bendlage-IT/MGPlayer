import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.interop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

class IOSPlatform : Platform {
    override val name: String = "Apple"
}

actual fun getPlatform(): Platform = IOSPlatform()


@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun VideoPlayer(
    modifier: Modifier,
    url: String,
    playbackTitle: String,
    playbackArtwork: String,
    onVideoIsPlayingTask: (progress: Int) -> Unit,
    playbackSubtitle: String,
) {
    var mediaPlayerController by remember { mutableStateOf<MediaPlayerController?>(null) }
    var currentUrl by remember { mutableStateOf(url) }
    LaunchedEffect(Unit) {
        mediaPlayerController =
            withContext(Dispatchers.Main) {
                MediaPlayerController(
                    playbackTitle = playbackTitle,
                    playbackArtworkUrl = playbackArtwork,
                )
            }
        mediaPlayerController!!.prepare(url)
        mediaPlayerController!!.startObserving { onVideoIsPlayingTask(it) }
    }

    LaunchedEffect(url) {
        if (currentUrl != url) {
            currentUrl = url
            mediaPlayerController!!.prepare(url)
        }
    }

    if (mediaPlayerController != null && mediaPlayerController?.view != null) {
        UIKitView(
            modifier = modifier.fillMaxSize(),
            factory = {
                mediaPlayerController!!.view
            },
            interactive = true,
            background = Color.Gray,
            update = {},
            onRelease = {
                mediaPlayerController!!.stop()
            },
        )
    }
}
