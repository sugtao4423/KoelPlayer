package sugtao4423.koelplayer.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import sugtao4423.koel4j.dataclass.AllMusicData
import sugtao4423.koelplayer.adapter.AlbumAdapter
import sugtao4423.koelplayer.databinding.FragmentAlbumBinding
import sugtao4423.koelplayer.playmusic.MusicService
import java.util.*

class AlbumFragment : Fragment() {

    private var _binding: FragmentAlbumBinding? = null
    private val binding get() = _binding!!

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
            binding.albumProgressBar.visibility = View.GONE
            albumAdapter.albums = value.albums
        }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentAlbumBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.albumGrid.adapter = albumAdapter
        if (hideLoading) {
            binding.albumProgressBar.visibility = View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun filter(filterText: String) {
        allMusicData ?: return

        albumAdapter.albums = if (filterText.isEmpty()) {
            allMusicData!!.albums
        } else {
            val searchText = filterText.lowercase(Locale.ROOT)
            allMusicData!!.albums.filter { album ->
                album.name.lowercase(Locale.ROOT).contains(searchText) ||
                        allMusicData!!.songs.filter { it.album.id == album.id }.any {
                            it.title.lowercase(Locale.ROOT).contains(searchText) ||
                                    it.artist.name.lowercase(Locale.ROOT).contains(searchText)
                        }
            }
        }
        albumAdapter.notifyDataSetChanged()
    }

}