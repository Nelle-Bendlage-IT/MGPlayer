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
import kotlinx.coroutines.withContext
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryPlayback
import platform.AVFAudio.setActive
import platform.AVFoundation.AVPlayer
import platform.AVFoundation.AVPlayerItem
import platform.AVFoundation.AVURLAsset
import platform.AVFoundation.play
import platform.AVKit.AVPlayerViewController
import platform.Foundation.NSURL
import platform.UIKit.UIDevice
import platform.UIKit.UIView

class IOSPlatform : Platform {
    override val name: String =
        UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
}

actual fun getPlatform(): Platform = IOSPlatform()


@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun VideoPlayer(modifier: Modifier, url: String, cookie: Map<Any?, *>?) {
    var avPlayer by remember { mutableStateOf<AVPlayer?>(null) }
    var avPlayerViewController by remember { mutableStateOf<AVPlayerViewController?>(null) }
    var uiView by remember { mutableStateOf<UIView?>(null) }
    val audioSession = AVAudioSession.sharedInstance()
    val asset = AVURLAsset.URLAssetWithURL(
        URL = NSURL(string = url),
        // https://stackoverflow.com/a/78026972
        options = mapOf("AVURLAssetHTTPHeaderFieldsKey" to cookie)
    )
    val playerItem = AVPlayerItem(asset = asset)

    LaunchedEffect(Unit) {
        avPlayer = withContext(Dispatchers.Main) {
            AVPlayer(playerItem = playerItem)
        }

        avPlayerViewController = withContext(Dispatchers.Main) {
            AVPlayerViewController().apply {
                player = avPlayer
                showsPlaybackControls = true
                entersFullScreenWhenPlaybackBegins = true
                exitsFullScreenWhenPlaybackEnds = true
                contentOverlayView?.autoresizesSubviews = true
            }
        }
        uiView = withContext(Dispatchers.Main) {
            avPlayerViewController?.view
        }
    }

    if (avPlayer != null && uiView != null) {
        UIKitView(
            modifier = modifier,
            factory = {
                uiView!!
            },
            interactive = true,
            background = Color.Gray,
            update = {
                avPlayer?.play()
                avPlayerViewController?.player!!.play()
                audioSession.setCategory(AVAudioSessionCategoryPlayback, null)
                audioSession.setActive(true, null)
            },
        )
    }
}

