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

class AlbumFragment : Fragment(R.layout.fragment_album) {

    var musicService: MusicService? = null
        set(value) {
            field = value
            albumAdapter.musicService = value
        }

    private val albumAdapter = AlbumAdapter()
    private var hideLoading = false

    var allMusicData: AllMusicData? = null
        set(value) {
            field = value
            value ?: return

            hideLoading = true
            albumProgressBar?.visibility = View.GONE
            albumAdapter.albums = value.albums
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        albumGrid.adapter = albumAdapter
        if (hideLoading) {
            albumProgressBar.visibility = View.GONE
        }
    }

    fun filter(filterText: String) {
        allMusicData ?: return

        albumAdapter.albums = if (filterText.isEmpty()) {
            allMusicData!!.albums
        } else {
            val searchText = filterText.toLowerCase(Locale.ROOT)
            allMusicData!!.albums.filter { album ->
                album.name.toLowerCase(Locale.ROOT).contains(searchText) ||
                        allMusicData!!.songs.filter { it.album.id == album.id }.any {
                            it.title.toLowerCase(Locale.ROOT).contains(searchText) ||
                                    it.artist.name.toLowerCase(Locale.ROOT).contains(searchText)
                        }
            }
        }
        albumAdapter.notifyDataSetChanged()
    }

}