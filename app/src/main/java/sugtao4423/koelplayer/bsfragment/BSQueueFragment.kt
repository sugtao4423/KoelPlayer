package sugtao4423.koelplayer.bsfragment

import android.os.Bundle
import android.support.v4.media.MediaMetadataCompat
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.bottom_sheet_queue.*
import sugtao4423.koelplayer.R
import sugtao4423.koelplayer.adapter.QueueAdapter
import sugtao4423.koelplayer.playmusic.MusicService

class BSQueueFragment : Fragment(R.layout.bottom_sheet_queue), BSFragmentInterface {

    private var musicService: MusicService? = null
    private lateinit var queueAdapter: QueueAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        queueAdapter = QueueAdapter()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        songQueue.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireContext())
            adapter = queueAdapter
        }
    }

    private fun queueChanged() {
        musicService?.let {
            queueAdapter.clear()
            queueAdapter.addAll(it.queueSongs())
        }
    }

    override fun onMusicServiceConnected(musicService: MusicService) {
        this.musicService = musicService
        queueAdapter.musicService = musicService
        queueChanged()
        musicService.queueSongChangedListener = { queueChanged() }
    }

    override fun onMusicServiceDisconnected() {
        queueAdapter.musicService = null
        musicService = null
    }

    override fun updateMetadata(metadata: MediaMetadataCompat) {
    }

    override fun onDestroy() {
        super.onDestroy()
        musicService?.queueSongChangedListener = null
    }

}