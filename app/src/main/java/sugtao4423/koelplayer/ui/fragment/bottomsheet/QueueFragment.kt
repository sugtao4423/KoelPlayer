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
    private var enableScrollToTop = true

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
                queueAdapter.submitList(it)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            bottomSheetViewModel.currentMediaItem.collect {
                if (it != null && queueAdapter.itemCount > 0 && enableScrollToTop) {
                    val playingPosition = bottomSheetViewModel.playingPosition()
                    if (playingPosition >= 0) {
                        binding.songQueue.smoothScrollToPosition(playingPosition)
                    }
                }
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
        private var dragStartPosition = -1
        private var dragEndPosition = -1

        override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
            super.onSelectedChanged(viewHolder, actionState)
            if (actionState != ItemTouchHelper.ACTION_STATE_IDLE) {
                return
            }

            enableScrollToTop = true
            if (dragStartPosition >= 0 && dragEndPosition >= 0) {
                bottomSheetViewModel.moveSong(dragStartPosition, dragEndPosition)
                dragStartPosition = -1
                dragEndPosition = -1
            }
        }

        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            val from = viewHolder.bindingAdapterPosition
            val to = target.bindingAdapterPosition

            queueAdapter.move(from, to)
            if (dragStartPosition == -1) {
                dragStartPosition = from
            }
            dragEndPosition = to

            return true
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            val position = viewHolder.bindingAdapterPosition
            bottomSheetViewModel.removeSong(position)
        }
    }

}
