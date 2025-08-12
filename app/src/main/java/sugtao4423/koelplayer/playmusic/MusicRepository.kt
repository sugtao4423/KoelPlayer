package sugtao4423.koelplayer.playmusic

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import com.google.android.exoplayer2.Player
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import sugtao4423.koel4j.dataclass.Song

class MusicRepository private constructor(private val context: Context) {

    companion object {
        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var INSTANCE: MusicRepository? = null

        fun getInstance(context: Context): MusicRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: MusicRepository(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    private var musicService: MusicService? = null
    private var isServiceBound = false

    private val _currentMetadata = MutableStateFlow<MediaMetadataCompat?>(null)
    val currentMetadata: StateFlow<MediaMetadataCompat?> = _currentMetadata

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying

    private val _queueSongs = MutableStateFlow<List<Song>>(emptyList())
    val queueSongs: StateFlow<List<Song>> = _queueSongs

    private val _isShuffleEnabled = MutableStateFlow(false)
    val isShuffleEnabled: StateFlow<Boolean> = _isShuffleEnabled

    private val _repeatMode = MutableStateFlow(RepeatMode.OFF)
    val repeatMode: StateFlow<RepeatMode> = _repeatMode

    enum class RepeatMode { OFF, ALL, ONE }

    private val mediaControllerCallback = object : MediaControllerCompat.Callback() {
        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            _currentMetadata.value = metadata
        }
    }

    private val playerEventListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _isPlaying.value = isPlaying
        }

        override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
            _isShuffleEnabled.value = shuffleModeEnabled
        }

        override fun onRepeatModeChanged(repeatMode: Int) {
            _repeatMode.value = when (repeatMode) {
                Player.REPEAT_MODE_OFF -> RepeatMode.OFF
                Player.REPEAT_MODE_ALL -> RepeatMode.ALL
                Player.REPEAT_MODE_ONE -> RepeatMode.ONE
                else -> throw IllegalArgumentException("Unknown repeat mode: $repeatMode")
            }
        }
    }

    private val queueChangedListener = object : MusicService.OnQueueChangedListener {
        override fun onChanged() {
            musicService?.let {
                _queueSongs.value = it.queueSongs()
            }
        }
    }

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            musicService = (service as MusicService.MusicServiceBinder).musicService
            musicService!!.apply {
                setMediaControllerCallback(mediaControllerCallback)
                addOnQueueChangedListener(queueChangedListener)
                addPlayerEventListener(playerEventListener)

                playingMetadata()?.let { _currentMetadata.value = it }
                playerEventListener.let {
                    it.onIsPlayingChanged(isPlaying())
                    it.onShuffleModeEnabledChanged(isShuffle())
                    it.onRepeatModeChanged(repeatMode())
                }
                _queueSongs.value = queueSongs()
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            musicService?.apply {
                removePlayerEventListener(playerEventListener)
                removeOnQueueChangedListener(queueChangedListener)
                removeMediaControllerCallback(mediaControllerCallback)
            }
            musicService = null
        }
    }

    fun bindService() {
        if (isServiceBound) return

        val intent = Intent(context, MusicService::class.java)
        context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        isServiceBound = true
    }

    fun unbindService() {
        if (!isServiceBound) return

        musicService?.removeMediaControllerCallback(mediaControllerCallback)
        context.unbindService(serviceConnection)
        isServiceBound = false
    }

    fun playSongs(songs: List<Song>, playPos: Int = 0) {
        musicService?.playSongs(songs, playPos)
    }

    fun shufflePlaySongs(songs: List<Song>) {
        musicService?.shufflePlaySongs(songs)
    }

    fun togglePlay() {
        musicService?.togglePlay()
    }

    fun next() {
        musicService?.next()
    }

    fun prev() {
        musicService?.prev()
    }

    fun seekTo(position: Long) {
        musicService?.seekTo(position)
    }

    fun toggleShuffle() {
        musicService?.toggleShuffle()
    }

    fun toggleRepeat() {
        musicService?.let {
            when {
                it.isRepeat() -> it.repeatOne()
                it.isRepeatOne() -> it.repeatOff()
                else -> it.repeat()
            }
        }
    }

    fun addQueueNext(songs: List<Song>) {
        musicService?.addQueueNext(songs)
    }

    fun addQueueLast(songs: List<Song>) {
        musicService?.addQueueLast(songs)
    }

    fun changeSong(position: Int) {
        musicService?.changeSong(position)
    }

    fun moveSong(from: Int, to: Int) {
        musicService?.moveSong(from, to)
    }

    fun removeSong(position: Int) {
        musicService?.removeSong(position)
    }

    fun currentPosition(): Long {
        return musicService?.currentPosition() ?: 0L
    }

    fun duration(): Long {
        return musicService?.duration() ?: 0L
    }

    fun bufferedPosition(): Long {
        return musicService?.bufferedPosition() ?: 0L
    }

}
