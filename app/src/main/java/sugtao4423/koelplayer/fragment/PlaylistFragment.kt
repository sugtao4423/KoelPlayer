package sugtao4423.koelplayer.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_playlist.*
import sugtao4423.koel4j.dataclass.AllMusicData
import sugtao4423.koelplayer.R
import sugtao4423.koelplayer.adapter.PlaylistAdapter
import sugtao4423.koelplayer.playmusic.MusicService

class PlaylistFragment(allMusicData: AllMusicData) : Fragment(R.layout.fragment_playlist) {

    var musicService: MusicService? = null
        set(value) {
            field = value
            playlistAdapter.musicService = value
        }

    private val playlistAdapter = PlaylistAdapter(allMusicData.playlists, allMusicData.songs)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        playlistView.adapter = playlistAdapter
    }

}