package sugtao4423.koelplayer.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import sugtao4423.koel4j.dataclass.Album
import sugtao4423.koel4j.dataclass.Playlist
import sugtao4423.koel4j.dataclass.Song
import sugtao4423.koelplayer.App
import sugtao4423.koelplayer.download.KoelDLUtil
import sugtao4423.koelplayer.musicdb.MusicDB
import sugtao4423.koelplayer.secToTimeFormat

class SongListViewModel(application: Application) : AndroidViewModel(application) {

    data class SongListData(
        val coverUrl: String?,
        val title: String,
        val artist: String?,
        val isCompilation: Boolean,
        val songs: List<Song>,
        val fileSize: String?,
        val allDownloaded: Boolean,
    )

    private val _songListData = MutableLiveData<SongListData?>()
    val songListData: LiveData<SongListData?> = _songListData

    private fun getFileSize(songs: List<Song>): String? {
        val dlUtil = KoelDLUtil(getApplication())
        val allDownloaded = songs.all { dlUtil.isDownloaded(it) }
        return if (allDownloaded) dlUtil.getSongFilesSize(songs) else null
    }

    fun loadAlbumData(album: Album) {
        viewModelScope.launch {
            val data = withContext(Dispatchers.IO) {
                val musicDB = MusicDB(getApplication())
                val songs = musicDB.getAlbumSongs(album.id).sortedBy { it.track }
                musicDB.close()

                val fileSize = getFileSize(songs)

                SongListData(
                    album.cover,
                    album.name,
                    album.artist.name,
                    album.isCompilation,
                    songs,
                    fileSize,
                    fileSize != null,
                )
            }

            _songListData.value = data
        }
    }

    fun loadPlaylistData(playlist: Playlist) {
        viewModelScope.launch {
            val data = withContext(Dispatchers.IO) {
                val musicDB = MusicDB(getApplication())
                var songs = musicDB.getSongsById(playlist.songs)
                musicDB.close()

                val app = getApplication<App>()
                val sortOrder = app.getPlaylistSortOrder(playlist)
                songs = when (sortOrder) {
                    0 -> {
                        val songMap = songs.associateBy { it.id }
                        playlist.songs.map { songMap[it]!! }
                    }

                    1 -> songs.sortedBy { it.track }.sortedBy { it.album.name }
                    else -> throw IllegalArgumentException("Unknown sort order: $sortOrder")
                }

                val fileSize = getFileSize(songs)

                SongListData(
                    songs.randomOrNull()?.album?.cover,
                    playlist.name,
                    null,
                    false,
                    songs,
                    fileSize,
                    fileSize != null,
                )
            }

            _songListData.value = data
        }
    }

    fun getSongTime(): String? {
        return songListData.value?.songs?.sumOf { it.length }?.toInt()?.secToTimeFormat()
    }

    fun getPlaylistSort(playlist: Playlist): Int {
        val app = getApplication<App>()
        return app.getPlaylistSortOrder(playlist)
    }

    fun updatePlaylistSort(playlist: Playlist, sortOrder: Int) {
        val app = getApplication<App>()
        app.setPlaylistSortOrder(playlist, sortOrder)
        loadPlaylistData(playlist)
    }

}
