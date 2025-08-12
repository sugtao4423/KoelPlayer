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
import sugtao4423.koel4j.dataclass.AllMusicData
import sugtao4423.koel4j.dataclass.Playlist
import sugtao4423.koelplayer.musicdb.MusicDB
import java.util.Locale

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private var allMusicData: AllMusicData? = null

    private val _albums = MutableLiveData<List<Album>?>(null)
    val albums: LiveData<List<Album>?> = _albums

    private val _playlists = MutableLiveData<List<Pair<Playlist, String?>>?>(null)
    val playlists: LiveData<List<Pair<Playlist, String?>>?> = _playlists

    init {
        loadMusicData()
    }

    private fun List<Playlist>.toPlaylistWithCover(): List<Pair<Playlist, String?>> {
        return map { playlist ->
            val coverUrl = allMusicData?.songs?.find { it.id == playlist.songs[0] }?.album?.cover
            Pair(playlist, coverUrl)
        }
    }

    private fun loadMusicData() {
        viewModelScope.launch {
            val allMusicData = withContext(Dispatchers.IO) {
                runCatching {
                    val musicDB = MusicDB(getApplication())
                    val data = musicDB.getAllMusicData()
                    musicDB.close()
                    data
                }.getOrNull()
            }
            if (allMusicData == null) return@launch
            this@MainViewModel.allMusicData = allMusicData
            _albums.value = allMusicData.albums
            _playlists.value = allMusicData.playlists.toPlaylistWithCover()
        }
    }

    fun filter(query: String) {
        if (query.isEmpty()) {
            _albums.value = allMusicData?.albums
            _playlists.value = allMusicData?.playlists?.toPlaylistWithCover()
            return
        }

        fun String.has(): Boolean = this.lowercase(Locale.ROOT).contains(query)

        _albums.value = allMusicData?.albums?.filter { album ->
            album.name.has() || allMusicData!!.songs.filter { it.album.id == album.id }.any {
                it.title.has() || it.title.has() || it.artist.name.has()
            }
        }

        _playlists.value = allMusicData?.playlists?.filter { playlist ->
            playlist.name.has() || playlist.songs.map { songId ->
                allMusicData!!.songs.find { it.id == songId }!!
            }.any { it.title.has() || it.artist.name.has() || it.album.name.has() }
        }?.toPlaylistWithCover()
    }

}
