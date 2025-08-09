package sugtao4423.koelplayer.bsfragment

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.support.v4.media.MediaMetadataCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import com.google.android.exoplayer2.Player
import sugtao4423.koelplayer.GlideUtil
import sugtao4423.koelplayer.R
import sugtao4423.koelplayer.databinding.BottomSheetNowPlayingBinding
import sugtao4423.koelplayer.playmusic.MusicService
import sugtao4423.koelplayer.view.SquareImageButton

class BSNowPlayingFragment : Fragment(), BSFragmentInterface {

    private var _binding: BottomSheetNowPlayingBinding? = null
    private val binding get() = _binding!!

    private var musicService: MusicService? = null
    private lateinit var watchCurrentTimeHandler: Handler
    private lateinit var watchCurrentTimeRunnable: Runnable

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = BottomSheetNowPlayingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initTextViewMarquee()
        initControlButtons()
        initMusicTimes()
    }

    override fun onStart() {
        super.onStart()
        watchCurrentTimeRunnable.run()
    }

    override fun onStop() {
        super.onStop()
        watchCurrentTimeHandler.removeCallbacks(watchCurrentTimeRunnable)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onDestroy() {
        super.onDestroy()
        musicService?.removePlayerEventListener(playerEventListener)
    }

    override fun onMusicServiceConnected(musicService: MusicService) {
        this.musicService = musicService
        musicService.addPlayerEventListener(playerEventListener)
        playerEventListener.onIsPlayingChanged(musicService.isPlaying())
        playerEventListener.onShuffleModeEnabledChanged(musicService.isShuffle())
        val repeatMode = when {
            musicService.isRepeat() -> Player.REPEAT_MODE_ALL
            musicService.isRepeatOne() -> Player.REPEAT_MODE_ONE
            else -> Player.REPEAT_MODE_OFF
        }
        playerEventListener.onRepeatModeChanged(repeatMode)
    }

    override fun onMusicServiceDisconnected() {
        this.musicService = null
    }

    @SuppressLint("SetTextI18n")
    override fun updateMetadata(metadata: MediaMetadataCompat) {
        GlideUtil.load(this, metadata.description.iconUri, binding.nowPlayingCover)
        binding.nowPlayingTitle.text = metadata.description.title
        binding.nowPlayingArtist.text = metadata.description.subtitle

        if (musicService == null) {
            binding.nowPlayingTotalTime.text = "00:00"
            binding.nowPlayingCurrentTime.text = "00:00"
        } else {
            val duration = musicService!!.duration()
            val currentPosition = musicService!!.currentPosition()
            binding.nowPlayingTotalTime.text = duration.toTimeFormat()
            binding.nowPlayingSeek.max = (duration / 1000).toInt()
            binding.nowPlayingCurrentTime.text = currentPosition.toTimeFormat()
            binding.nowPlayingSeek.progress = (currentPosition / 1000).toInt()
        }
    }

    private fun initTextViewMarquee() {
        binding.nowPlayingTitle.isSelected = true
        binding.nowPlayingArtist.isSelected = true
    }

    private fun initControlButtons() {
        requireView().findViewById<SquareImageButton>(R.id.nowPlayingShuffleButton).setOnClickListener(controlButtonsListener)
        requireView().findViewById<SquareImageButton>(R.id.nowPlayingPrevButton).setOnClickListener(controlButtonsListener)
        requireView().findViewById<SquareImageButton>(R.id.nowPlayingPlayButton).setOnClickListener(controlButtonsListener)
        requireView().findViewById<SquareImageButton>(R.id.nowPlayingNextButton).setOnClickListener(controlButtonsListener)
        requireView().findViewById<SquareImageButton>(R.id.nowPlayingRepeatButton).setOnClickListener(controlButtonsListener)
    }

    private fun initMusicTimes() {
        binding.nowPlayingSeek.setOnSeekBarChangeListener(seekBarListener)
        watchCurrentTimeHandler = Handler(Looper.getMainLooper())
        watchCurrentTimeRunnable = Runnable {
            musicService?.let {
                val duration = it.duration()
                val currentPosition = it.currentPosition()
                binding.nowPlayingTotalTime.text = it.duration().toTimeFormat()
                binding.nowPlayingSeek.max = (duration / 1000).toInt()
                binding.nowPlayingCurrentTime.text = currentPosition.toTimeFormat()
                binding.nowPlayingSeek.progress = (currentPosition / 1000).toInt()
                binding.nowPlayingSeek.secondaryProgress = (it.bufferedPosition() / 1000).toInt()
            }
            watchCurrentTimeHandler.postDelayed(watchCurrentTimeRunnable, 500)
        }
    }

    private val controlButtonsListener = View.OnClickListener {
        if (it == null || musicService == null) {
            return@OnClickListener
        }
        when (it.id) {
            R.id.nowPlayingShuffleButton -> musicService!!.toggleShuffle()
            R.id.nowPlayingPrevButton -> musicService!!.prev()
            R.id.nowPlayingPlayButton -> musicService!!.togglePlay()
            R.id.nowPlayingNextButton -> musicService!!.next()
            R.id.nowPlayingRepeatButton -> {
                when {
                    musicService!!.isRepeat() -> musicService!!.repeatOne()
                    musicService!!.isRepeatOne() -> musicService!!.repeatOff()
                    else -> musicService!!.repeat()
                }
            }
        }
    }

    private val seekBarListener = object : SeekBar.OnSeekBarChangeListener {
        private var touching = false

        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            if (touching) {
                musicService?.seekTo((progress * 1000).toLong())
            }
        }

        override fun onStartTrackingTouch(seekBar: SeekBar?) {
            touching = true
        }

        override fun onStopTrackingTouch(seekBar: SeekBar?) {
            touching = false
        }
    }

    private val playerEventListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            val res = if (isPlaying) R.drawable.ic_playing_pause else R.drawable.ic_playing_play
            binding.nowPlayingPlayButton.setImageResource(res)
        }

        override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
            if (shuffleModeEnabled) {
                binding.nowPlayingShuffleButton.clearColorFilter()
            } else {
                binding.nowPlayingShuffleButton.setColorFilter(Color.GRAY)
            }
        }

        override fun onRepeatModeChanged(repeatMode: Int) {
            when (repeatMode) {
                Player.REPEAT_MODE_ALL -> {
                    binding.nowPlayingRepeatButton.clearColorFilter()
                    binding.nowPlayingRepeatButton.setImageResource(R.drawable.ic_playing_repeat)
                }
                Player.REPEAT_MODE_ONE -> {
                    binding.nowPlayingRepeatButton.clearColorFilter()
                    binding.nowPlayingRepeatButton.setImageResource(R.drawable.ic_playing_repeat_one)
                }
                Player.REPEAT_MODE_OFF -> {
                    binding.nowPlayingRepeatButton.setImageResource(R.drawable.ic_playing_repeat)
                    binding.nowPlayingRepeatButton.setColorFilter(Color.GRAY)
                }
            }
        }
    }

    private fun Long.toTimeFormat(): String {
        val second = this / 1000
        val hour = second / 60 / 60
        val min = (second / 60 % 60).toString().padStart(2, '0')
        val sec = (second % 60).toString().padStart(2, '0')
        return if (hour == 0L) {
            "$min:$sec"
        } else {
            "$hour:$min:$sec"
        }
    }

}