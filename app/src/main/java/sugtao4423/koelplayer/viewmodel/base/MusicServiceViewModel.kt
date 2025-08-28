package sugtao4423.koelplayer.viewmodel.base

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import sugtao4423.koel4j.dataclass.Song
import sugtao4423.koelplayer.music.repository.MusicRepository

open class MusicServiceViewModel(application: Application) : AndroidViewModel(application) {

    private val musicRepository = MusicRepository.getInstance(application)

    val currentMediaItem = musicRepository.currentMediaItem
    val isPlaying = musicRepository.isPlaying
    val queueSongs = musicRepository.queueSongs
    val isShuffleEnabled = musicRepository.isShuffleEnabled
    val repeatMode = musicRepository.repeatMode

    init {
        musicRepository.initialize()
    }

    fun releaseController() {
        musicRepository.release()
    }

    fun playingPosition() = musicRepository.playingPosition()
    fun duration() = musicRepository.duration()
    fun currentPosition() = musicRepository.currentPosition()
    fun bufferedPosition() = musicRepository.bufferedPosition()

    fun prev() = musicRepository.prev()
    fun next() = musicRepository.next()
    fun seekTo(position: Long) = musicRepository.seekTo(position)

    fun togglePlay() = musicRepository.togglePlay()
    fun toggleShuffle() = musicRepository.toggleShuffle()
    fun toggleRepeat() = musicRepository.toggleRepeat()

    fun playSongs(songs: List<Song>, position: Int = 0) = musicRepository.playSongs(songs, position)
    fun shufflePlaySongs(songs: List<Song>) = musicRepository.shufflePlaySongs(songs)

    fun addQueueNext(songs: List<Song>) = musicRepository.addQueueNext(songs)
    fun addQueueLast(songs: List<Song>) = musicRepository.addQueueLast(songs)

    fun changeSong(position: Int) = musicRepository.changeSong(position)
    fun moveSong(from: Int, to: Int) = musicRepository.moveSong(from, to)
    fun removeSong(position: Int) = musicRepository.removeSong(position)

}
