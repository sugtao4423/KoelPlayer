package sugtao4423.koelplayer.bsfragment

import android.os.Bundle
import android.support.v4.media.MediaMetadataCompat
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.bottom_sheet_queue.*
import sugtao4423.koelplayer.R
import sugtao4423.koelplayer.adapter.QueueAdapter
import sugtao4423.koelplayer.playmusic.MusicService

class BSQueueFragment : Fragment(R.layout.bottom_sheet_queue), BSFragmentInterface {

    private var musicService: MusicService? = null
    private lateinit var queueAdapter: QueueAdapter

    private var isScrollTop = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        queueAdapter = QueueAdapter()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        songQueue.apply {
            setHasFixedSize(true)
            layoutManager = QueueLinearLayoutManager()
            adapter = queueAdapter
        }
        ItemTouchHelper(moveSwipeCallback).attachToRecyclerView(songQueue)
    }

    private val onQueueChangedListener = object : MusicService.OnQueueChangedListener {
        override fun onChanged() {
            musicService?.let {
                queueAdapter.clear()
                queueAdapter.addAll(it.queueSongs())
            }
        }
    }

    override fun onMusicServiceConnected(musicService: MusicService) {
        this.musicService = musicService
        queueAdapter.musicService = musicService
        onQueueChangedListener.onChanged()
        musicService.addOnQueueChangedListener(onQueueChangedListener)
    }

    override fun onMusicServiceDisconnected() {
        queueAdapter.musicService = null
        musicService = null
    }

    override fun updateMetadata(metadata: MediaMetadataCompat) {
        if (queueAdapter.itemCount > 0 && isScrollTop) {
            val playingPosition = musicService!!.playingPosition()
            if (playingPosition >= 0) {
                songQueue.smoothScrollToPosition(playingPosition)
            }
        }
        isScrollTop = true
    }

    override fun onDestroy() {
        super.onDestroy()
        musicService?.removeOnQueueChangedListener(onQueueChangedListener)
    }

    inner class QueueLinearLayoutManager : LinearLayoutManager(context) {
        override fun smoothScrollToPosition(recyclerView: RecyclerView?, state: RecyclerView.State?, position: Int) {
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
        override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
            isScrollTop = false
            val fromPosition = viewHolder.adapterPosition
            val toPosition = target.adapterPosition
            queueAdapter.move(fromPosition, toPosition)
            musicService?.moveSong(fromPosition, toPosition)
            return true
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            isScrollTop = false
            val position = viewHolder.adapterPosition
            queueAdapter.remove(position)
            musicService?.removeSong(position)
        }
    }

}