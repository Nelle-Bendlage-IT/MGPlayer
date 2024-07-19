package com.mgplayer.tv.exoplayer


import android.content.Context
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import androidx.media3.common.AudioAttributes
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.ayflix.exoplayer.domain.TLPlayer
import java.lang.ref.WeakReference

@UnstableApi
object PlayerFactory {

    fun create(
        context: Context
    ): TLPlayer {
        val renderersFactory =
            DefaultRenderersFactory(context)

        val exoPlayer = ExoPlayer.Builder(context, renderersFactory)
            .setAudioAttributes(AudioAttributes.DEFAULT, true)
            .setHandleAudioBecomingNoisy(true)
            .build()
        return ExoPlayerImpl(
            WeakReference(context),
            exoPlayer
        ) {
            PlayerView(context).apply {
                hideController()
                player = exoPlayer
                useController = false
                resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                layoutParams = FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
            }

        }
    }
}