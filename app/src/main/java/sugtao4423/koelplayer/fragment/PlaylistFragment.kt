package sugtao4423.koelplayer.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import sugtao4423.koelplayer.adapter.PlaylistAdapter
import sugtao4423.koelplayer.databinding.FragmentPlaylistBinding
import sugtao4423.koelplayer.viewmodel.BottomSheetViewModel
import sugtao4423.koelplayer.viewmodel.MainViewModel

class PlaylistFragment(bottomSheetViewModel: BottomSheetViewModel) : Fragment() {

    private var _binding: FragmentPlaylistBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MainViewModel by activityViewModels()

    private val playlistAdapter = PlaylistAdapter(bottomSheetViewModel)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPlaylistBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.playlistView.adapter = playlistAdapter

        viewModel.playlists.observe(viewLifecycleOwner) {
            playlistAdapter.playlists = it ?: emptyList()
            binding.playlistProgressBar.visibility = View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
