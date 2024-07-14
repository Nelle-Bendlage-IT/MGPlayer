import android.annotation.SuppressLint
import android.app.PictureInPictureParams
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Build
import android.view.WindowManager
import androidx.annotation.OptIn
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.media3.common.AudioAttributes
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaItem.RequestMetadata
import androidx.media3.common.MediaMetadata
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.PlayerView
import com.google.common.util.concurrent.MoreExecutors
import com.mgtv.MainActivity
import com.mgtv.MediaPlayerController


class AndroidPlatform : Platform {
    override val name: String = "Android"
}

actual fun getPlatform(): Platform = AndroidPlatform()


@RequiresApi(Build.VERSION_CODES.O)
@SuppressLint("RememberReturnType")
@OptIn(UnstableApi::class)
@Composable
actual fun VideoPlayer(
    modifier: Modifier,
    url: String,
    playbackTitle: String,
    playbackArtwork: String,
    onVideoIsPlayingTask: (progress: Int) -> Unit,
    playbackSubtitle: String,
) {
    val context = LocalContext.current
    val activity = context.getActivity() ?: return
    val mediaPlayerController = remember {
        MediaPlayerController(
            context,
        )
    }

    DisposableEffect(Unit) {
        mediaPlayerController.addListener {
            onVideoIsPlayingTask(it)
        }
        setShouldEnterPipMode(activity, true)
        val windowInsetsController =
            WindowCompat.getInsetsController(activity.window, activity.window.decorView)
        windowInsetsController.apply {
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            hide(WindowInsetsCompat.Type.systemBars())
        }
        activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE

        onDispose {
            setShouldEnterPipMode(activity, false)
            windowInsetsController.apply {
                systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_DEFAULT
                show(WindowInsetsCompat.Type.systemBars())
            }

            activity.window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    Box(
        modifier = Modifier
            .background(Color.Black)
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                PlayerView(context).apply {
                    useController = true
                }
            },
            onRelease = {
                mediaPlayerController.stop()

            },
            update = { playerView ->
                mediaPlayerController.listenableMediaController?.addListener({
                    val player = mediaPlayerController.listenableMediaController?.get()
                    val mediaItem =
                        MediaItem.Builder()
                            .setMediaId(url)
                            .setUri(url)
                            .setMediaMetadata(
                                MediaMetadata.Builder()
                                    .setTitle(playbackTitle)
                                    .setSubtitle(playbackSubtitle)
                                    .setArtworkUri(Uri.parse(playbackArtwork)).build(),
                            )
                            .setRequestMetadata(RequestMetadata.Builder().build())
                            .build()

                    player?.apply {
                        setMediaItem(mediaItem)
                        setAudioAttributes(AudioAttributes.DEFAULT, true)
                        playWhenReady = true
                        prepare()
                    }
                    playerView.setPlayer(player)

                }, MoreExecutors.directExecutor())
            }
        )
    }

}

fun Context.getActivity(): MainActivity? = when (this) {
    is MainActivity -> this
    is ContextWrapper -> baseContext.getActivity()
    else -> null
}


fun setShouldEnterPipMode(context: Context, shouldEnterPipMode: Boolean) {
    context.getActivity()?.let { activity ->
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            activity.setPictureInPictureParams(
                PictureInPictureParams.Builder()
                    .setAutoEnterEnabled(shouldEnterPipMode)
                    .build()
            )
        } else {
            // we don't support pip mode on older devices
        }
    }
}
