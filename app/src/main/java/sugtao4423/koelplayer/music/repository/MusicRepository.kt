package sugtao4423.koelplayer.music.repository

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.net.Uri
import androidx.annotation.OptIn
import androidx.core.net.toUri
import androidx.media3.common.C
import androidx.media3.common.FlagSet
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.Timeline
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import sugtao4423.koel4j.KoelEndpoints
import sugtao4423.koel4j.dataclass.Song
import sugtao4423.koelplayer.App
import sugtao4423.koelplayer.data.database.MusicDB
import sugtao4423.koelplayer.music.service.MusicService

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

    private var controller: MediaController? = null
    private var controllerFuture: ListenableFuture<MediaController>? = null

    private val _currentMediaItem = MutableStateFlow<MediaItem?>(null)
    val currentMediaItem: StateFlow<MediaItem?> = _currentMediaItem

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying

    private val songMap: MutableMap<String, Song> = mutableMapOf()
    private val _queueSongs = MutableStateFlow<List<Song>>(emptyList())
    val queueSongs: StateFlow<List<Song>> = _queueSongs

    private val _isShuffleEnabled = MutableStateFlow(false)
    val isShuffleEnabled: StateFlow<Boolean> = _isShuffleEnabled

    private val _repeatMode = MutableStateFlow(RepeatMode.OFF)
    val repeatMode: StateFlow<RepeatMode> = _repeatMode

    enum class RepeatMode { OFF, ALL, ONE }

    private val controllerListener = object : Player.Listener {
        @OptIn(UnstableApi::class)
        private val handlingFlags = FlagSet.Builder().addAll(
            Player.EVENT_MEDIA_ITEM_TRANSITION,
            Player.EVENT_TIMELINE_CHANGED,
            Player.EVENT_SHUFFLE_MODE_ENABLED_CHANGED,
            Player.EVENT_IS_PLAYING_CHANGED,
            Player.EVENT_REPEAT_MODE_CHANGED,
        ).build()

        @OptIn(UnstableApi::class)
        fun initStates(player: Player) = onEvents(player, Player.Events(handlingFlags))

        override fun onEvents(player: Player, events: Player.Events) {
            if (events.contains(Player.EVENT_MEDIA_ITEM_TRANSITION)) {
                _currentMediaItem.value = player.currentMediaItem
            }
            if (events.contains(Player.EVENT_TIMELINE_CHANGED) || events.contains(Player.EVENT_SHUFFLE_MODE_ENABLED_CHANGED)) {
                val mediaIds = player.currentMediaItems.map { it.mediaId }
                _queueSongs.value = songIdsToSongList(mediaIds)
            }
            if (events.contains(Player.EVENT_IS_PLAYING_CHANGED)) {
                _isPlaying.value = player.isPlaying
            }
            if (events.contains(Player.EVENT_SHUFFLE_MODE_ENABLED_CHANGED)) {
                _isShuffleEnabled.value = player.shuffleModeEnabled
            }
            if (events.contains(Player.EVENT_REPEAT_MODE_CHANGED)) {
                _repeatMode.value = when (player.repeatMode) {
                    Player.REPEAT_MODE_OFF -> RepeatMode.OFF
                    Player.REPEAT_MODE_ALL -> RepeatMode.ALL
                    Player.REPEAT_MODE_ONE -> RepeatMode.ONE
                    else -> throw IllegalArgumentException("Unknown repeat mode: ${player.repeatMode}")
                }
            }
        }
    }

    fun initialize() {
        if (controller != null) {
            return
        }

        val sessionToken = SessionToken(context, ComponentName(context, MusicService::class.java))
        controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        controllerFuture!!.addListener({
            controller = controllerFuture!!.get()
            controller!!.let {
                controllerListener.initStates(it)
                it.addListener(controllerListener)
            }
        }, MoreExecutors.directExecutor())
    }

    fun release() {
        controller?.let {
            it.removeListener(controllerListener)
            it.release()
        }
        controllerFuture?.let {
            MediaController.releaseFuture(it)
        }
        controller = null
        controllerFuture = null
        INSTANCE = null
    }

    fun playingPosition(): Int = controller?.let {
        it.mediaItemsIndices.indexOf(it.currentMediaItemIndex)
    } ?: -1

    fun duration(): Long = controller?.duration ?: 0
    fun currentPosition(): Long = controller?.currentPosition ?: 0
    fun bufferedPosition(): Long = controller?.bufferedPosition ?: 0

    fun prev() = controller?.seekToPrevious()
    fun next() = controller?.seekToNext()
    fun seekTo(position: Long) = controller?.seekTo(position)

    fun togglePlay() = controller?.run { if (isPlaying()) pause() else play() }
    fun toggleShuffle() = controller?.run { shuffleModeEnabled = !shuffleModeEnabled }
    fun toggleRepeat() = controller?.repeatMode = when (controller?.repeatMode) {
        Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
        Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
        Player.REPEAT_MODE_ONE -> Player.REPEAT_MODE_OFF
        else -> throw IllegalArgumentException("Unknown repeat mode: ${controller?.repeatMode}")
    }

    fun playSongs(songs: List<Song>, position: Int) = controller?.run {
        shuffleModeEnabled = false
        setMediaItems(songs.toMediaItems(), position, 0)
        prepare()
        play()
    }

    fun shufflePlaySongs(songs: List<Song>) = controller?.run {
        shuffleModeEnabled = true
        setMediaItems(songs.toMediaItems(), true)
        prepare()
        play()
    }

    fun addQueueNext(songs: List<Song>) = controller?.run {
        addMediaItems(currentMediaItemIndex + 1, songs.toMediaItems())
    }

    fun addQueueLast(songs: List<Song>) = controller?.run {
        addMediaItems(mediaItemCount, songs.toMediaItems())
    }

    fun changeSong(position: Int) = controller?.seekTo(position, 0)
    fun moveSong(from: Int, to: Int) = controller?.moveMediaItem(from, to)
    fun removeSong(position: Int) = controller?.removeMediaItem(position)

    private val Player.mediaItemsIndices: List<Int>
        get() {
            val indices = mutableListOf<Int>()
            var index = currentTimeline.getFirstWindowIndex(shuffleModeEnabled)
            if (index == C.INDEX_UNSET) {
                return emptyList()
            }

            repeat(currentTimeline.windowCount) {
                indices.add(index)
                index = currentTimeline.getNextWindowIndex(
                    index, Player.REPEAT_MODE_OFF, shuffleModeEnabled
                )
            }

            return indices
        }

    private val Player.currentMediaItems: List<MediaItem>
        get() = if (shuffleModeEnabled) {
            mediaItemsIndices.map { getMediaItemAt(it) }
        } else {
            List(currentTimeline.windowCount) { i ->
                currentTimeline.getWindow(i, Timeline.Window()).mediaItem
            }
        }

    private fun List<Song>.toMediaItems(): List<MediaItem> {
        val app = context as App
        fun Song.toUri(): Uri =
            (app.koelServer + KoelEndpoints.musicFile(app.koelToken, this.id)).toUri()

        fun Song.toMetadata(): MediaMetadata = MediaMetadata.Builder().let {
            it.setMediaType(MediaMetadata.MEDIA_TYPE_MUSIC)

            it.setTitle(this.title)
            it.setArtist(this.artist.name)
            it.setAlbumTitle(this.album.name)
            it.setAlbumArtist(this.album.artist.name)
            it.setArtworkUri(this.album.cover?.toUri())

            it.setDurationMs(this.length.toLong() * 1000)
            it.setDiscNumber(this.disc)
            it.setTrackNumber(this.track)
        }.build()

        return this.map { song ->
            MediaItem.Builder().let {
                it.setMediaId(song.id)
                it.setUri(song.toUri())
                it.setMediaMetadata(song.toMetadata())
            }.build()
        }
    }

    private fun songIdsToSongList(songIds: List<String>): List<Song> {
        val notFoundIds = songIds.filter { !songMap.containsKey(it) }
        if (notFoundIds.isEmpty()) {
            return songIds.map { songMap[it]!! }
        }

        val songs = MusicDB(context).let {
            val result = it.getSongsById(notFoundIds)
            it.close()
            result
        }
        songMap.putAll(songs.associateBy { it.id })

        return songIds.map { songMap[it]!! }
    }

}
