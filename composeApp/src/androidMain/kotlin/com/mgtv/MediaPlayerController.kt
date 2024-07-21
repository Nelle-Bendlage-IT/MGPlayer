package com.mgtv


import android.content.ComponentName
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import com.mgtv.player.playback.PlayBackService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext
import kotlin.time.Duration.Companion.seconds

class MediaPlayerController(
    context: Context,
) :
    ViewModel() {
    private var appContext = context.applicationContext
    private var _duration: Long = 0
    private var _currentPosition: Long = 0
    private var _mediaController: MediaController? = null
    private var _isPlaying: Boolean = false
    var listenableMediaController: ListenableFuture<MediaController>? = null
        private set

    init {
        initialize()
    }

    private fun initialize() {
        val sessionToken =
            SessionToken(appContext, ComponentName(appContext, PlayBackService::class.java))
        listenableMediaController =
            MediaController.Builder(appContext, sessionToken).buildAsync()
        listenableMediaController = MediaController.Builder(appContext, sessionToken).buildAsync()
        listenableMediaController!!.addListener(
            {
                listenableMediaController!!.get()?.let {
                    _mediaController = it
                    _isPlaying = true
                }
            },
            MoreExecutors.directExecutor()
        )

        listenableMediaController!!.addListener({
            val player = listenableMediaController!!.get()
            _duration = player.duration
            viewModelScope.launch(Dispatchers.Main) {
                while (true) {
                    _currentPosition = player.currentPosition
                    if (_duration < 0)
                        _duration = player.duration
                    delay(5.seconds)
                }
            }
        }, MoreExecutors.directExecutor())
    }


    fun addListener(onPlaySendProgress: (progress: Int) -> Unit) {
        listenableMediaController?.addListener({
            viewModelScope.launch(Dispatchers.Main) {
                while (true) {
                    if (_isPlaying && _currentPosition > 0) {
                        val progress = 100.0 * _currentPosition / _duration
                        onPlaySendProgress(progress.toInt())
                    }
                    delay(10.seconds)
                }
            }

        }, MoreExecutors.directExecutor())
    }


    fun stop() {
        _mediaController?.let { controller ->
            controller.stop()
            controller.release()
        }
    }

}