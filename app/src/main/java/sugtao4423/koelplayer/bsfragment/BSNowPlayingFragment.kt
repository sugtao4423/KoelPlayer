package sugtao4423.koelplayer.bsfragment

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.support.v4.media.MediaMetadataCompat
import android.view.View
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.android.exoplayer2.Player
import kotlinx.android.synthetic.main.bottom_sheet_now_playing.*
import sugtao4423.koelplayer.R
import sugtao4423.koelplayer.playmusic.MusicService
import sugtao4423.koelplayer.view.SquareImageButton

class BSNowPlayingFragment : Fragment(R.layout.bottom_sheet_now_playing), BSFragmentInterface {

    private var musicService: MusicService? = null
    private lateinit var watchCurrentTimeHandler: Handler
    private lateinit var watchCurrentTimeRunnable: Runnable

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
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

    override fun onDestroy() {
        super.onDestroy()
        musicService?.removePlayerEventListener(playerEventListener)
    }

    override fun onMusicServiceConnected(musicService: MusicService) {
        this.musicService = musicService
        musicService.setPlayerEventListener(playerEventListener)
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
        Glide.with(this).let {
            if (metadata.description.iconUri == null || metadata.description.iconUri.toString().endsWith("unknown-album.png")) {
                it.load(R.drawable.unknown_album)
            } else {
                it.load(metadata.description.iconUri.toString())
            }
        }.into(nowPlayingCover)

        nowPlayingTitle.text = metadata.description.title
        nowPlayingArtist.text = metadata.description.subtitle

        if (musicService == null) {
            nowPlayingTotalTime.text = "00:00"
            nowPlayingCurrentTime.text = "00:00"
        } else {
            val duration = musicService!!.duration()
            val currentPosition = musicService!!.currentPosition()
            nowPlayingTotalTime.text = duration.toTimeFormat()
            nowPlayingSeek.max = (duration / 1000).toInt()
            nowPlayingCurrentTime.text = currentPosition.toTimeFormat()
            nowPlayingSeek.progress = (currentPosition / 1000).toInt()
        }
    }

    private fun initControlButtons() {
        requireView().findViewById<SquareImageButton>(R.id.nowPlayingShuffleButton).setOnClickListener(controlButtonsListener)
        requireView().findViewById<SquareImageButton>(R.id.nowPlayingPrevButton).setOnClickListener(controlButtonsListener)
        requireView().findViewById<SquareImageButton>(R.id.nowPlayingPlayButton).setOnClickListener(controlButtonsListener)
        requireView().findViewById<SquareImageButton>(R.id.nowPlayingNextButton).setOnClickListener(controlButtonsListener)
        requireView().findViewById<SquareImageButton>(R.id.nowPlayingRepeatButton).setOnClickListener(controlButtonsListener)
    }

    private fun initMusicTimes() {
        nowPlayingSeek.setOnSeekBarChangeListener(seekBarListener)
        watchCurrentTimeHandler = Handler(Looper.getMainLooper())
        watchCurrentTimeRunnable = Runnable {
            musicService?.let {
                val currentPosition = it.currentPosition()
                nowPlayingCurrentTime.text = currentPosition.toTimeFormat()
                nowPlayingSeek.progress = (currentPosition / 1000).toInt()
                nowPlayingSeek.secondaryProgress = (it.bufferedPosition() / 1000).toInt()
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

    private val playerEventListener = object : Player.EventListener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            val res = if (isPlaying) R.drawable.ic_playing_pause else R.drawable.ic_playing_play
            nowPlayingPlayButton.setImageResource(res)
        }

        override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
            if (shuffleModeEnabled) {
                nowPlayingShuffleButton.clearColorFilter()
            } else {
                nowPlayingShuffleButton.setColorFilter(Color.GRAY)
            }
        }

        override fun onRepeatModeChanged(repeatMode: Int) {
            when (repeatMode) {
                Player.REPEAT_MODE_ALL -> {
                    nowPlayingRepeatButton.clearColorFilter()
                    nowPlayingRepeatButton.setImageResource(R.drawable.ic_playing_repeat)
                }
                Player.REPEAT_MODE_ONE -> {
                    nowPlayingRepeatButton.clearColorFilter()
                    nowPlayingRepeatButton.setImageResource(R.drawable.ic_playing_repeat_one)
                }
                Player.REPEAT_MODE_OFF -> {
                    nowPlayingRepeatButton.setImageResource(R.drawable.ic_playing_repeat)
                    nowPlayingRepeatButton.setColorFilter(Color.GRAY)
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