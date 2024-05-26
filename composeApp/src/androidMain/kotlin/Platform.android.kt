import android.annotation.SuppressLint
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Build
import android.view.WindowManager
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.session.MediaSession
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import androidx.media3.ui.PlayerView.SHOW_BUFFERING_ALWAYS
import com.mgtv.MainActivity
import java.util.UUID

class AndroidPlatform : Platform {
    override val name: String = "Android"
}

actual fun getPlatform(): Platform = AndroidPlatform()

sealed class ZoomState {
    data object ZoomedIn : ZoomState()
    data object ZoomedOut : ZoomState()
}


@RequiresApi(Build.VERSION_CODES.O)
@SuppressLint("RememberReturnType")
@androidx.annotation.OptIn(UnstableApi::class)
@Composable
actual fun VideoPlayer(
    modifier: Modifier,
    url: String,
    cookie: Map<Any?, *>?,
    playbackTitle: String,
    playbackArtwork: String,
) {
    val ctx = LocalContext.current.applicationContext
    val mediaItem = MediaItem.Builder().setMediaId(url).setUri(url).setMediaMetadata(
        MediaMetadata.Builder().setArtist("MassengeschmackTV").setTitle(playbackTitle)
            .setArtworkUri(Uri.parse(playbackArtwork)).build(),
    ).build()

    val exoPlayer = remember {
        initPlayer(
            ctx, cookie, mediaItem
        )
    }

    var scale by remember { mutableStateOf<ZoomState>(ZoomState.ZoomedOut) }
    var playerView by remember { mutableStateOf<PlayerView?>(null) }

    val state = rememberTransformableState { zoomChange, _, _ ->
        if (zoomChange > 1.004f) {
            scale = ZoomState.ZoomedIn
        } else if (zoomChange < 0.996f) {
            scale = ZoomState.ZoomedOut
        }
    }
    val activity = LocalContext.current.getActivity() ?: return

    LaunchedEffect(scale) {
        playerView?.let {
            if (scale == ZoomState.ZoomedIn) {
                it.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
            } else {
                it.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
            }
        }
    }

    DisposableEffect(Unit) {
        val windowInsetsController =
            WindowCompat.getInsetsController(activity.window, activity.window.decorView)
        windowInsetsController.apply {
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            hide(WindowInsetsCompat.Type.systemBars())
        }
        activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE

        onDispose {
            exoPlayer.stop()
            exoPlayer.release()
            windowInsetsController.apply {
                systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_DEFAULT
                show(WindowInsetsCompat.Type.systemBars())
            }

            activity.window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    Box(
        modifier = Modifier.background(Color.Black).transformable(state = state)
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                PlayerView(context).apply {
                    setExtraAdGroupMarkers()
                    useController = true
                    player = exoPlayer
                    playerView = this
                    setShowBuffering(SHOW_BUFFERING_ALWAYS)
                    setShowNextButton(false)
                    setFullscreenButtonClickListener { isFullScreen ->
                        with(context) {
                            if (isFullScreen) {
                                setScreenOrientation(orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
                            } else {
                                setScreenOrientation(orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
                            }
                        }
                    }
                }
            },
            update = {},
            onRelease = {},
        )
    }

}

fun Context.getActivity(): MainActivity? = when (this) {
    is MainActivity -> this
    is ContextWrapper -> baseContext.getActivity()
    else -> null
}


@androidx.annotation.OptIn(UnstableApi::class)
private fun initPlayer(
    appContext: Context, cookie: Map<Any?, *>?, mediaItem: MediaItem
): ExoPlayer {
    val factory = DefaultHttpDataSource.Factory().setDefaultRequestProperties(
        mapOf(
            Pair(
                "cookie",
                cookie!!["cookie"]!!.toString(),
            ),
        ),
    )
    val player = ExoPlayer.Builder(appContext).build().apply {
        setMediaSource(
            ProgressiveMediaSource.Factory(factory).createMediaSource(mediaItem),
        )
        setAudioAttributes(AudioAttributes.DEFAULT, true)
        setHandleAudioBecomingNoisy(true)
        videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT
        playWhenReady = true
        prepare()
    }
    return player
}


fun Context.setScreenOrientation(orientation: Int) {
    val activity = this.getActivity() ?: return
    activity.requestedOrientation = orientation
    if (orientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
        hideSystemUi()
    } else {
        showSystemUi()
    }
}

fun Context.hideSystemUi() {
    val activity = this.getActivity() ?: return
    val windowInsetsController =
        WindowCompat.getInsetsController(activity.window, activity.window.decorView)
    windowInsetsController.apply {
        systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        hide(WindowInsetsCompat.Type.systemBars())
    }
    activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
}

fun Context.showSystemUi() {
    val activity = this.getActivity() ?: return
    val windowInsetsController =
        WindowCompat.getInsetsController(activity.window, activity.window.decorView)
    windowInsetsController.apply {
        systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_DEFAULT
        show(WindowInsetsCompat.Type.systemBars())
    }

    activity.window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
}
