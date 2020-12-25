package sugtao4423.koelplayer.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_album.*
import sugtao4423.koel4j.dataclass.AllMusicData
import sugtao4423.koelplayer.R
import sugtao4423.koelplayer.adapter.AlbumAdapter
import sugtao4423.koelplayer.playmusic.MusicService
import java.util.*

class AlbumFragment(private val allMusicData: AllMusicData) : Fragment(R.layout.fragment_album) {

    var musicService: MusicService? = null
        set(value) {
            field = value
            albumAdapter.musicService = value
        }

    private val albumAdapter = AlbumAdapter()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        albumAdapter.albums = allMusicData.albums
        albumGrid.adapter = albumAdapter
    }

    fun filter(filterText: String) {
        albumAdapter.albums = if (filterText.isEmpty()) {
            allMusicData.albums
        } else {
            val searchText = filterText.toLowerCase(Locale.ROOT)
            allMusicData.albums.filter { album ->
                album.name.toLowerCase(Locale.ROOT).contains(searchText) ||
                        allMusicData.songs.filter { it.album.id == album.id }.any {
                            it.title.toLowerCase(Locale.ROOT).contains(searchText) ||
                                    it.artist.name.toLowerCase(Locale.ROOT).contains(searchText)
                        }
            }
        }
        albumAdapter.notifyDataSetChanged()
    }

}