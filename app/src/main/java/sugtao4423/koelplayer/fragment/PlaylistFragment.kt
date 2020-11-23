package sugtao4423.koelplayer.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_playlist.*
import sugtao4423.koel4j.dataclass.AllMusicData
import sugtao4423.koelplayer.R
import sugtao4423.koelplayer.adapter.PlaylistAdapter

class PlaylistFragment(private val allMusicData: AllMusicData) :
    Fragment(R.layout.fragment_playlist) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        playlistView.adapter = PlaylistAdapter(requireContext(), allMusicData.playlists, allMusicData.songs)
    }

}