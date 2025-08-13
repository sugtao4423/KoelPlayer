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
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import sugtao4423.koelplayer.GlideUtil
import sugtao4423.koelplayer.R
import sugtao4423.koelplayer.databinding.BottomSheetNowPlayingBinding
import sugtao4423.koelplayer.millisToTimeFormat
import sugtao4423.koelplayer.playmusic.MusicRepository
import sugtao4423.koelplayer.viewmodel.BottomSheetViewModel

class BSNowPlayingFragment : Fragment() {

    private var _binding: BottomSheetNowPlayingBinding? = null
    private val binding get() = _binding!!

    private val bottomSheetViewModel: BottomSheetViewModel by activityViewModels()

    private lateinit var watchCurrentTimeHandler: Handler
    private lateinit var watchCurrentTimeRunnable: Runnable

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        _binding = BottomSheetNowPlayingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initTextViewMarquee()
        initControlButtons()
        initMusicTimes()
        initObservers()
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

    @SuppressLint("SetTextI18n")
    private fun updateMetadata(metadata: MediaMetadataCompat) {
        GlideUtil.load(this, metadata.description.iconUri, binding.nowPlayingCover)
        binding.nowPlayingTitle.text = metadata.description.title
        binding.nowPlayingArtist.text = metadata.description.subtitle

        val duration = bottomSheetViewModel.duration()
        val currentPosition = bottomSheetViewModel.currentPosition()
        binding.nowPlayingTotalTime.text = duration.millisToTimeFormat()
        binding.nowPlayingSeek.max = (duration / 1000).toInt()
        binding.nowPlayingCurrentTime.text = currentPosition.millisToTimeFormat()
        binding.nowPlayingSeek.progress = (currentPosition / 1000).toInt()
    }

    private fun initTextViewMarquee() {
        binding.nowPlayingTitle.isSelected = true
        binding.nowPlayingArtist.isSelected = true
    }

    private fun initControlButtons() {
        binding.nowPlayingShuffleButton.setOnClickListener(controlButtonsListener)
        binding.nowPlayingPrevButton.setOnClickListener(controlButtonsListener)
        binding.nowPlayingPlayButton.setOnClickListener(controlButtonsListener)
        binding.nowPlayingNextButton.setOnClickListener(controlButtonsListener)
        binding.nowPlayingRepeatButton.setOnClickListener(controlButtonsListener)
    }

    private fun initMusicTimes() {
        binding.nowPlayingSeek.setOnSeekBarChangeListener(seekBarListener)
        watchCurrentTimeHandler = Handler(Looper.getMainLooper())
        watchCurrentTimeRunnable = Runnable {
            bottomSheetViewModel.let {
                val duration = it.duration()
                val currentPosition = it.currentPosition()
                binding.nowPlayingTotalTime.text = it.duration().millisToTimeFormat()
                binding.nowPlayingSeek.max = (duration / 1000).toInt()
                binding.nowPlayingCurrentTime.text = currentPosition.millisToTimeFormat()
                binding.nowPlayingSeek.progress = (currentPosition / 1000).toInt()
                binding.nowPlayingSeek.secondaryProgress = (it.bufferedPosition() / 1000).toInt()
            }
            watchCurrentTimeHandler.postDelayed(watchCurrentTimeRunnable, 500)
        }
    }

    private val controlButtonsListener = View.OnClickListener {
        val model = bottomSheetViewModel
        when (it.id) {
            R.id.nowPlayingShuffleButton -> model.toggleShuffle()
            R.id.nowPlayingPrevButton -> model.prev()
            R.id.nowPlayingPlayButton -> model.togglePlay()
            R.id.nowPlayingNextButton -> model.next()
            R.id.nowPlayingRepeatButton -> model.toggleRepeat()
        }
    }

    private val seekBarListener = object : SeekBar.OnSeekBarChangeListener {
        private var touching = false

        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            if (touching) {
                bottomSheetViewModel.seekTo((progress * 1000).toLong())
            }
        }

        override fun onStartTrackingTouch(seekBar: SeekBar?) {
            touching = true
        }

        override fun onStopTrackingTouch(seekBar: SeekBar?) {
            touching = false
        }
    }

    private fun initObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            bottomSheetViewModel.currentMetadata.collect {
                if (it != null) updateMetadata(it)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            bottomSheetViewModel.isPlaying.collect {
                val res = if (it) R.drawable.ic_playing_pause else R.drawable.ic_playing_play
                binding.nowPlayingPlayButton.setImageResource(res)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            bottomSheetViewModel.isShuffleEnabled.collect {
                if (it) {
                    binding.nowPlayingShuffleButton.clearColorFilter()
                } else {
                    binding.nowPlayingShuffleButton.setColorFilter(Color.GRAY)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            bottomSheetViewModel.repeatMode.collect {
                when (it) {
                    MusicRepository.RepeatMode.ALL -> {
                        binding.nowPlayingRepeatButton.clearColorFilter()
                        binding.nowPlayingRepeatButton.setImageResource(R.drawable.ic_playing_repeat)
                    }

                    MusicRepository.RepeatMode.ONE -> {
                        binding.nowPlayingRepeatButton.clearColorFilter()
                        binding.nowPlayingRepeatButton.setImageResource(R.drawable.ic_playing_repeat_one)
                    }

                    MusicRepository.RepeatMode.OFF -> {
                        binding.nowPlayingRepeatButton.setImageResource(R.drawable.ic_playing_repeat)
                        binding.nowPlayingRepeatButton.setColorFilter(Color.GRAY)
                    }
                }
            }
        }
    }

}
