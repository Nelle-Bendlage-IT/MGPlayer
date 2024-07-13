import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.cinterop.CValue
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryPlayback
import platform.AVFAudio.setActive
import platform.AVFoundation.AVFileTypeJPEG
import platform.AVFoundation.AVMetadataCommonIdentifierArtwork
import platform.AVFoundation.AVMetadataCommonIdentifierTitle
import platform.AVFoundation.AVMutableMetadataItem
import platform.AVFoundation.AVPlayer
import platform.AVFoundation.AVPlayerItem
import platform.AVFoundation.AVPlayerItemDidPlayToEndTimeNotification
import platform.AVFoundation.AVPlayerTimeControlStatusPlaying
import platform.AVFoundation.AVURLAsset
import platform.AVFoundation.addPeriodicTimeObserverForInterval
import platform.AVFoundation.asset
import platform.AVFoundation.currentItem
import platform.AVFoundation.pause
import platform.AVFoundation.play
import platform.AVFoundation.replaceCurrentItemWithPlayerItem
import platform.AVFoundation.seekToTime
import platform.AVFoundation.timeControlStatus
import platform.AVKit.AVPlayerViewController
import platform.AVKit.setExternalMetadata
import platform.CoreMedia.CMTime
import platform.CoreMedia.CMTimeGetSeconds
import platform.CoreMedia.CMTimeMakeWithSeconds
import platform.Foundation.NSData
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSOperationQueue
import platform.Foundation.NSString
import platform.Foundation.NSURL
import platform.Foundation.dataWithContentsOfURL
import platform.UIKit.UIView
import platform.darwin.Float64
import platform.darwin.NSEC_PER_SEC

@OptIn(ExperimentalForeignApi::class)
class MediaPlayerController(
    private val playbackArtworkUrl: String,
    private val playbackTitle: String,
) : ViewModel() {
    private val avPlayer: AVPlayer = AVPlayer()
    private val avPlayerViewController: AVPlayerViewController = AVPlayerViewController()
    private lateinit var timeObserver: Any
    private var duration: Float64 = 0.0

    init {
        setUpAudioSession()
        avPlayerViewController.apply {
            player = avPlayer
            showsPlaybackControls = true
            entersFullScreenWhenPlaybackBegins = true
            exitsFullScreenWhenPlaybackEnds = true
            contentOverlayView?.autoresizesSubviews = true
            allowsPictureInPicturePlayback = true
            canStartPictureInPictureAutomaticallyFromInline = true
        }
    }

    val view: UIView
        get() {
            return avPlayerViewController.view
        }

    fun prepare(url: String) {
        val asset =
            AVURLAsset.URLAssetWithURL(
                URL = NSURL(string = url),
                options = null,
            )
        val playerItem = AVPlayerItem(asset = asset)
        viewModelScope.launch(Dispatchers.IO) {
            playerItem.setExternalMetadata(
                setupMetadata(),
            )
        }
        stop()
        avPlayer.replaceCurrentItemWithPlayerItem(playerItem)
        viewModelScope.launch(Dispatchers.IO) {
            duration = CMTimeGetSeconds(avPlayer.currentItem!!.asset.duration())
        }
        avPlayer.play()
    }


    @OptIn(ExperimentalForeignApi::class)
    fun startObserving(onVideoIsPlayingTask: (progress: Int) -> Unit) {
        val interval = CMTimeMakeWithSeconds(30.0, NSEC_PER_SEC.toInt())
        timeObserver = avPlayer.addPeriodicTimeObserverForInterval(
            interval,
            null
        ) { time: CValue<CMTime> ->
            // https://stackoverflow.com/a/55915184/20329236
            if (avPlayer.timeControlStatus == AVPlayerTimeControlStatusPlaying) {
                val seconds = CMTimeGetSeconds(time)
                onVideoIsPlayingTask((seconds / duration * 100).toInt())
            }
        }

        NSNotificationCenter.defaultCenter.addObserverForName(
            name = AVPlayerItemDidPlayToEndTimeNotification,
            `object` = avPlayer.currentItem,
            queue = NSOperationQueue.mainQueue,
            usingBlock = {}
        )
    }

    @Suppress("CAST_NEVER_SUCCEEDS")
    private fun setupMetadata(): List<AVMutableMetadataItem> {
        val title = AVMutableMetadataItem()
        title.setIdentifier(AVMetadataCommonIdentifierTitle)
        // https://kotlinlang.org/docs/native-objc-interop.html#casting-between-mapped-types
        title.setValue(value = playbackTitle as NSString)
        title.setExtendedLanguageTag("und")

        val artwork = AVMutableMetadataItem()
        artwork.setIdentifier(AVMetadataCommonIdentifierArtwork)
        artwork.setExtendedLanguageTag("und")
        artwork.setValue(value = NSData.dataWithContentsOfURL(NSURL.URLWithString(playbackArtworkUrl)!!))
        artwork.setDataType(AVFileTypeJPEG)

        // Doesn't work for now
        /* val artist = AVMutableMetadataItem()
        artist.setIdentifier(AVMetadataIdentifieriTunesMetadataTrackSubTitle)
        artist.setValue(value = "Title" as NSString)
        artist.setExtendedLanguageTag("und")*/

        return listOf(title, artwork)
    }

    private fun setUpAudioSession() {
        try {
            val audioSession = AVAudioSession.sharedInstance()
            audioSession.setCategory(AVAudioSessionCategoryPlayback, null)
            audioSession.setActive(true, null)
        } catch (e: Exception) {
            println("Error setting up audio session: ${e.message}")
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    fun stop() {
        avPlayer.pause()
        avPlayer.currentItem?.seekToTime(CMTimeMakeWithSeconds(0.0, NSEC_PER_SEC.toInt()))
    }
}
