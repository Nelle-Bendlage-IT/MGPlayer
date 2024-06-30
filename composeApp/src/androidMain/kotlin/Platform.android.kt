import android.annotation.SuppressLint
import android.app.PictureInPictureParams
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import android.graphics.Rect
import android.net.Uri
import android.os.Build
import android.util.Rational
import android.view.WindowManager
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.mgtv.MainActivity
import kotlinx.coroutines.delay
import player.ControllerVisibility
import player.SimpleController
import player.rememberMediaState
import kotlin.time.Duration.Companion.seconds

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
    onVideoIsPlayingTask: (progress: Int) -> Unit
) {
    val context = LocalContext.current
    val mediaItem = MediaItem.Builder().setMediaId(url).setUri(url).setMediaMetadata(
        MediaMetadata.Builder().setArtist("MassengeschmackTV").setTitle(playbackTitle)
            .setArtworkUri(Uri.parse(playbackArtwork)).build(),
    ).build()
    val activity = LocalContext.current.getActivity() ?: return
    val lifecycleOwner = rememberUpdatedState(LocalLifecycleOwner.current)
    var playerView by remember { mutableStateOf<PlayerView?>(null) }
    val mediaState = rememberMediaState(initPlayer(context, cookie, mediaItem))

    var scale by remember { mutableStateOf<ZoomState>(ZoomState.ZoomedOut) }
    val state = rememberTransformableState { zoomChange, _, _ ->
        if (zoomChange > 1.004f) {
            scale = ZoomState.ZoomedIn
        } else if (zoomChange < 0.996f) {
            scale = ZoomState.ZoomedOut
        }
    }

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
            windowInsetsController.apply {
                systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_DEFAULT
                show(WindowInsetsCompat.Type.systemBars())
            }

            activity.window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    DisposableEffect(Unit) {
        setShouldEnterPipMode(activity, true)

        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                    mediaState.controllerAutoShow = false
                    mediaState.isControllerShowing = false
                    if (!activity.isInPictureInPictureMode) {
                        mediaState.player!!.pause()
                    }

                }

                Lifecycle.Event.ON_RESUME -> {
                    mediaState.controllerAutoShow = true
                }

                else -> {}
            }
        }
        val lifecycle = lifecycleOwner.value.lifecycle
        lifecycle.addObserver(observer)

        onDispose {
            setShouldEnterPipMode(activity, false)
            mediaState.player!!.clearMediaItems()
            lifecycle.removeObserver(observer)
            mediaState.playerState?.dispose()
        }
    }


    LaunchedEffect(Unit) {
        while (true) {
            val currentPosition = mediaState.player!!.currentPosition
            val duration = mediaState.player!!.duration

            delay(30.seconds)
            if (mediaState.player!!.isPlaying && duration > 0) {
                val progress = 100.0 * currentPosition / duration
                onVideoIsPlayingTask(progress.toInt())
            }

        }
    }

    Box(
        modifier = Modifier
            .background(Color.Black)
            .transformable(state = state)
    ) {
        AndroidView(
            modifier = Modifier
                .fillMaxSize(),
            factory = { context ->
                PlayerView(context).apply {

                    useController = false
                    playerView = this
                    setShowSubtitleButton(true)
                    setShowBuffering(PlayerView.SHOW_BUFFERING_WHEN_PLAYING)

                    player = mediaState.player!!
                    videoSurfaceView!!.addOnLayoutChangeListener { v, left, top, right, bottom,
                                                                   oldLeft, oldTop, oldRight, oldBottom ->


                        if (!activity.isInPictureInPictureMode && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && (left != oldLeft
                                    || right != oldRight
                                    || top != oldTop
                                    || bottom != oldBottom
                                    )
                        ) {

                            val visibleRect = Rect()
                            v.getGlobalVisibleRect(visibleRect)

                            activity.setPictureInPictureParams(
                                PictureInPictureParams.Builder()
                                    .setAutoEnterEnabled(true)
                                    .setSourceRectHint(visibleRect)
                                    .setAspectRatio(
                                        Rational(
                                            visibleRect.width(),
                                            visibleRect.height()
                                        )
                                            .coerceAtMost(Rational(239, 100))
                                            .coerceAtLeast(Rational(100, 239))
                                    )
                                    .build()
                            )

                        }

                    }
                    activity.addOnPictureInPictureModeChangedListener {
                        // pause video when user closed picture in picture mode
                        if (!it.isInPictureInPictureMode && lifecycleOwner.value.lifecycle.currentState == Lifecycle.State.CREATED) {
                            mediaState.player!!.pause()
                        }
                    }
                    mediaState.player!!.play()
                }
            },
        )
        Box(
            modifier = Modifier
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) {
                    if (mediaState.player != null) {
                        mediaState.controllerVisibility =
                            when (mediaState.controllerVisibility) {
                                ControllerVisibility.Visible -> ControllerVisibility.Invisible
                                ControllerVisibility.PartiallyVisible -> ControllerVisibility.Visible
                                ControllerVisibility.Invisible -> ControllerVisibility.Visible
                            }
                    }
                }
        ) {
            SimpleController(
                mediaState,
                modifier = Modifier.fillMaxSize(),
                onBackClick = {},
                onSettingsClick = {},
            )
        }
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
