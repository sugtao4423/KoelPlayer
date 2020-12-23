package sugtao4423.koelplayer.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_album.*
import sugtao4423.koel4j.dataclass.AllMusicData
import sugtao4423.koelplayer.R
import sugtao4423.koelplayer.adapter.AlbumAdapter
import sugtao4423.koelplayer.playmusic.MusicService

class AlbumFragment(allMusicData: AllMusicData) : Fragment(R.layout.fragment_album) {

    var musicService: MusicService? = null
        set(value) {
            field = value
            albumAdapter.musicService = value
        }

    private val albumAdapter = AlbumAdapter(allMusicData.albums)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        albumGrid.adapter = albumAdapter
    }

}