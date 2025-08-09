package sugtao4423.koelplayer.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import sugtao4423.koel4j.dataclass.AllMusicData
import sugtao4423.koelplayer.adapter.PlaylistAdapter
import sugtao4423.koelplayer.databinding.FragmentPlaylistBinding
import sugtao4423.koelplayer.playmusic.MusicService
import java.util.Locale

class PlaylistFragment : Fragment() {

    private var _binding: FragmentPlaylistBinding? = null
    private val binding get() = _binding!!

    var musicService: MusicService? = null
        set(value) {
            field = value
            playlistAdapter.musicService = value
        }

    private val playlistAdapter = PlaylistAdapter()
    private var hideLoading = false

    var allMusicData: AllMusicData? = null
        set(value) {
            field = value
            value ?: return

            hideLoading = true
            binding.playlistProgressBar.visibility = View.GONE
            playlistAdapter.songs = value.songs
            playlistAdapter.playlists = value.playlists
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPlaylistBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.playlistView.adapter = playlistAdapter
        if (hideLoading) {
            binding.playlistProgressBar.visibility = View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun filter(filterText: String) {
        allMusicData ?: return

        playlistAdapter.playlists = if (filterText.isEmpty()) {
            allMusicData!!.playlists
        } else {
            val searchText = filterText.lowercase(Locale.ROOT)
            allMusicData!!.playlists.filter { playlist ->
                playlist.name.lowercase(Locale.ROOT)
                    .contains(searchText) || playlist.songs.map { songId -> allMusicData!!.songs.find { it.id == songId }!! }
                    .any {
                        it.title.lowercase(Locale.ROOT)
                            .contains(searchText) || it.artist.name.lowercase(Locale.ROOT)
                            .contains(searchText) || it.album.name.lowercase(Locale.ROOT)
                            .contains(searchText)
                    }
            }
        }
        playlistAdapter.notifyDataSetChanged()
    }

}
