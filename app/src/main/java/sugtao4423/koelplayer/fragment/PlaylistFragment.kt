package sugtao4423.koelplayer.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_playlist.*
import sugtao4423.koel4j.dataclass.AllMusicData
import sugtao4423.koelplayer.R
import sugtao4423.koelplayer.adapter.PlaylistAdapter
import sugtao4423.koelplayer.playmusic.MusicService
import java.util.*

class PlaylistFragment(private val allMusicData: AllMusicData) : Fragment(R.layout.fragment_playlist) {

    var musicService: MusicService? = null
        set(value) {
            field = value
            playlistAdapter.musicService = value
        }

    private val playlistAdapter = PlaylistAdapter(allMusicData.songs)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        playlistAdapter.playlists = allMusicData.playlists
        playlistView.adapter = playlistAdapter
    }

    fun filter(filterText: String) {
        playlistAdapter.playlists = if (filterText.isEmpty()) {
            allMusicData.playlists
        } else {
            val searchText = filterText.toLowerCase(Locale.ROOT)
            allMusicData.playlists.filter { playlist ->
                playlist.name.toLowerCase(Locale.ROOT).contains(searchText) ||
                        playlist.songs.map { songId -> allMusicData.songs.find { it.id == songId }!! }.any {
                            it.title.toLowerCase(Locale.ROOT).contains(searchText) ||
                                    it.artist.name.toLowerCase(Locale.ROOT).contains(searchText) ||
                                    it.album.name.toLowerCase(Locale.ROOT).contains(searchText)
                        }
            }
        }
        playlistAdapter.notifyDataSetChanged()
    }

}