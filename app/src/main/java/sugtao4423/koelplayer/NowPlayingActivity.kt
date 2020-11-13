package sugtao4423.koelplayer

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.view.MenuItem
import android.view.View
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.exoplayer2.Player
import kotlinx.android.synthetic.main.activity_now_playing.*
import sugtao4423.koelplayer.playmusic.MusicService
import sugtao4423.koelplayer.view.SquareImageButton

class NowPlayingActivity : AppCompatActivity() {

    private var musicService: MusicService? = null
    private lateinit var watchCurrentTimeHandler: Handler
    private lateinit var watchCurrentTimeRunnable: Runnable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_now_playing)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        bindService(Intent(this, MusicService::class.java), serviceConnection, BIND_AUTO_CREATE)

        initControlButtons()
        initMusicTimes()
    }

    override fun onStart() {
        super.onStart()
        watchCurrentTimeRunnable.run()
    }

    private fun initControlButtons() {
        findViewById<SquareImageButton>(R.id.nowPlayingShuffleButton).setOnClickListener(controlButtonsListener)
        findViewById<SquareImageButton>(R.id.nowPlayingPrevButton).setOnClickListener(controlButtonsListener)
        findViewById<SquareImageButton>(R.id.nowPlayingPlayButton).setOnClickListener(controlButtonsListener)
        findViewById<SquareImageButton>(R.id.nowPlayingNextButton).setOnClickListener(controlButtonsListener)
        findViewById<SquareImageButton>(R.id.nowPlayingRepeatButton).setOnClickListener(controlButtonsListener)
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

    private val mediaControllerCallback = object : MediaControllerCompat.Callback() {
        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            metadata?.let {
                updateMetadata(it)
            }
        }
    }

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            musicService = (service as MusicService.MusicServiceBinder).musicService
            musicService!!.setMediaControllerCallback(mediaControllerCallback)
            musicService!!.setPlayerEventListener(playerEventListener)
            musicService!!.playingMetadata()?.let {
                updateMetadata(it)
            }

            playerEventListener.onIsPlayingChanged(musicService!!.isPlaying())
            playerEventListener.onShuffleModeEnabledChanged(musicService!!.isShuffle())
            val repeatMode = when {
                musicService!!.isRepeat() -> Player.REPEAT_MODE_ALL
                musicService!!.isRepeatOne() -> Player.REPEAT_MODE_ONE
                else -> Player.REPEAT_MODE_OFF
            }
            playerEventListener.onRepeatModeChanged(repeatMode)
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            musicService = null
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateMetadata(metadata: MediaMetadataCompat) {
        if (metadata.description.iconUri == null || metadata.description.iconUri.toString().endsWith("unknown-album.png")) {
            Glide.with(this).load(R.drawable.unknown_album).into(nowPlayingCover)
        } else {
            Glide.with(this).load(metadata.description.iconUri.toString()).into(nowPlayingCover)
        }

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

    override fun onStop() {
        super.onStop()
        watchCurrentTimeHandler.removeCallbacks(watchCurrentTimeRunnable)
    }

    override fun onDestroy() {
        super.onDestroy()
        musicService?.removeMediaControllerCallback(mediaControllerCallback)
        musicService?.removePlayerEventListener(playerEventListener)
        unbindService(serviceConnection)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

}