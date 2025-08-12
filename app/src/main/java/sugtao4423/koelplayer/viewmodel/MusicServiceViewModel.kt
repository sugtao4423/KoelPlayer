package sugtao4423.koelplayer.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import sugtao4423.koel4j.dataclass.Song
import sugtao4423.koelplayer.playmusic.MusicRepository

open class MusicServiceViewModel(application: Application) : AndroidViewModel(application) {

    private val musicRepository = MusicRepository.getInstance(application)

    val currentMetadata = musicRepository.currentMetadata
    val isPlaying = musicRepository.isPlaying
    val isShuffleEnabled = musicRepository.isShuffleEnabled
    val repeatMode = musicRepository.repeatMode

    init {
        musicRepository.bindService()
    }

    override fun onCleared() {
        musicRepository.unbindService()
        super.onCleared()
    }

    fun duration() = musicRepository.duration()
    fun currentPosition() = musicRepository.currentPosition()
    fun bufferedPosition() = musicRepository.bufferedPosition()

    fun toggleShuffle() = musicRepository.toggleShuffle()
    fun prev() = musicRepository.prev()
    fun next() = musicRepository.next()
    fun togglePlay() = musicRepository.togglePlay()
    fun toggleRepeat() = musicRepository.toggleRepeat()
    fun seekTo(position: Long) = musicRepository.seekTo(position)

    fun playSongs(songs: List<Song>) = musicRepository.playSongs(songs)
    fun shufflePlaySongs(songs: List<Song>) = musicRepository.shufflePlaySongs(songs)

    fun addQueueNext(songs: List<Song>) = musicRepository.addQueueNext(songs)
    fun addQueueLast(songs: List<Song>) = musicRepository.addQueueLast(songs)

}
