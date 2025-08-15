package sugtao4423.koelplayer.ui.fragment.bottomsheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import sugtao4423.koelplayer.databinding.BottomSheetQueueBinding
import sugtao4423.koelplayer.ui.adapter.QueueAdapter
import sugtao4423.koelplayer.viewmodel.BottomSheetViewModel

class QueueFragment : Fragment() {

    private var _binding: BottomSheetQueueBinding? = null
    private val binding get() = _binding!!

    private val bottomSheetViewModel: BottomSheetViewModel by activityViewModels()

    private val queueAdapter: QueueAdapter by lazy { QueueAdapter(bottomSheetViewModel) }
    private var isScrollTop = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        _binding = BottomSheetQueueBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.songQueue.apply {
            setHasFixedSize(true)
            layoutManager = QueueLinearLayoutManager()
            adapter = queueAdapter
        }
        ItemTouchHelper(moveSwipeCallback).attachToRecyclerView(binding.songQueue)

        initObservers()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun initObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            bottomSheetViewModel.queueSongs.collect {
                queueAdapter.clear()
                queueAdapter.addAll(it)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            bottomSheetViewModel.currentMediaItem.collect {
                if (it == null) return@collect
                if (queueAdapter.itemCount > 0 && isScrollTop) {
                    val playingPosition = bottomSheetViewModel.playingPosition()
                    if (playingPosition >= 0) {
                        binding.songQueue.smoothScrollToPosition(playingPosition)
                    }
                }
                isScrollTop = true
            }
        }
    }

    inner class QueueLinearLayoutManager : LinearLayoutManager(context) {
        override fun smoothScrollToPosition(
            recyclerView: RecyclerView?, state: RecyclerView.State?, position: Int
        ) {
            val linearSmoothScroller = object : LinearSmoothScroller(recyclerView?.context) {
                override fun getVerticalSnapPreference(): Int {
                    return SNAP_TO_START
                }
            }
            linearSmoothScroller.targetPosition = position
            startSmoothScroll(linearSmoothScroller)
        }
    }

    private val moveSwipeCallback = object : ItemTouchHelper.SimpleCallback(
        ItemTouchHelper.UP or ItemTouchHelper.DOWN, ItemTouchHelper.LEFT
    ) {
        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            isScrollTop = false
            val fromPosition = viewHolder.adapterPosition
            val toPosition = target.adapterPosition
            queueAdapter.move(fromPosition, toPosition)
            bottomSheetViewModel.moveSong(fromPosition, toPosition)
            return true
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            isScrollTop = false
            val position = viewHolder.adapterPosition
            queueAdapter.remove(position)
            bottomSheetViewModel.removeSong(position)
        }
    }

}
