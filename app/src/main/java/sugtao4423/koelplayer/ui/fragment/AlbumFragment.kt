package sugtao4423.koelplayer.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import sugtao4423.koelplayer.databinding.FragmentAlbumBinding
import sugtao4423.koelplayer.ui.adapter.AlbumAdapter
import sugtao4423.koelplayer.viewmodel.BottomSheetViewModel
import sugtao4423.koelplayer.viewmodel.MainViewModel

class AlbumFragment(bottomSheetViewModel: BottomSheetViewModel) : Fragment() {

    private var _binding: FragmentAlbumBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MainViewModel by activityViewModels()

    private val albumAdapter = AlbumAdapter(bottomSheetViewModel)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAlbumBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.albumGrid.adapter = albumAdapter

        viewModel.albums.observe(viewLifecycleOwner) {
            albumAdapter.albums = it ?: emptyList()
            binding.albumProgressBar.visibility = View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
