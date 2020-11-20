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
import android.view.View
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.bumptech.glide.Glide
import com.google.android.exoplayer2.Player
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.android.synthetic.main.bottom_sheet_now_playing.*
import sugtao4423.koelplayer.playmusic.MusicService
import sugtao4423.koelplayer.view.SquareImageButton

abstract class BaseBottomNowPlayingActivity(
    private val layoutResId: Int = R.layout.activity_main,
    private val backgroundAppbarId: Int = R.id.mainAppbar
) : AppCompatActivity() {

    protected var musicService: MusicService? = null
    private lateinit var bottomSheet: BottomSheetBehavior<CoordinatorLayout>
    private lateinit var watchCurrentTimeHandler: Handler
    private lateinit var watchCurrentTimeRunnable: Runnable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layoutResId)

        initActionBar()
        initBottomSheet()
        initControlButtons()
        initMusicTimes()
    }

    override fun onStart() {
        super.onStart()
        bindService(Intent(this, MusicService::class.java), serviceConnection, BIND_AUTO_CREATE)
        watchCurrentTimeRunnable.run()
    }

    override fun onStop() {
        super.onStop()
        watchCurrentTimeHandler.removeCallbacks(watchCurrentTimeRunnable)
        unbindService(serviceConnection)
    }

    override fun onDestroy() {
        super.onDestroy()
        musicService?.removeMediaControllerCallback(mediaControllerCallback)
        musicService?.removePlayerEventListener(playerEventListener)
    }

    private fun initActionBar() {
        setSupportActionBar(nowPlayingToolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
    }

    override fun onSupportNavigateUp(): Boolean {
        bottomSheet.toggleState()
        return super.onSupportNavigateUp()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initBottomSheet() {
        bottomSheet = BottomSheetBehavior.from(bottomSheetLayout)
        if (bottomSheet.state == BottomSheetBehavior.STATE_EXPANDED) {
            nowPlayingSheetCollapsed.alpha = 0f
            nowPlayingSheetExpanded.alpha = 1f
        } else if (bottomSheet.state == BottomSheetBehavior.STATE_COLLAPSED) {
            nowPlayingSheetCollapsed.alpha = 1f
            nowPlayingSheetExpanded.alpha = 0f
        }

        val bgAppbar = findViewById<AppBarLayout>(backgroundAppbarId)
        bottomSheet.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                val inverseOffset = 1 - slideOffset
                nowPlayingSheetCollapsed.alpha = inverseOffset
                nowPlayingSheetExpanded.alpha = slideOffset
                bgAppbar.alpha = inverseOffset
            }

            override fun onStateChanged(bottomSheet: View, newState: Int) {
                bgAppbar.visibility = if (newState == BottomSheetBehavior.STATE_EXPANDED) View.GONE else View.VISIBLE
            }
        })
        nowPlayingSheetExpanded.setOnTouchListener { _, _ -> true }
        nowPlayingToolbar.setOnClickListener {
            bottomSheet.toggleState()
        }
    }

    override fun onBackPressed() {
        if (bottomSheet.state == BottomSheetBehavior.STATE_EXPANDED) {
            bottomSheet.toggleState()
        } else {
            super.onBackPressed()
        }
    }

    private fun BottomSheetBehavior<CoordinatorLayout>.toggleState() {
        if (state == BottomSheetBehavior.STATE_COLLAPSED) {
            state = BottomSheetBehavior.STATE_EXPANDED
        } else if (state == BottomSheetBehavior.STATE_EXPANDED) {
            state = BottomSheetBehavior.STATE_COLLAPSED
        }
    }

    private fun initControlButtons() {
        findViewById<SquareImageButton>(R.id.nowPlayingShuffleButton).setOnClickListener(controlButtonsListener)
        findViewById<SquareImageButton>(R.id.nowPlayingPrevButton).setOnClickListener(controlButtonsListener)
        findViewById<SquareImageButton>(R.id.nowPlayingPlayButton).setOnClickListener(controlButtonsListener)
        findViewById<SquareImageButton>(R.id.nowPlayingNextButton).setOnClickListener(controlButtonsListener)
        findViewById<SquareImageButton>(R.id.nowPlayingRepeatButton).setOnClickListener(controlButtonsListener)
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

    @SuppressLint("SetTextI18n")
    private fun updateMetadata(metadata: MediaMetadataCompat) {
        Glide.with(this).let {
            if (metadata.description.iconUri == null || metadata.description.iconUri.toString().endsWith("unknown-album.png")) {
                it.load(R.drawable.unknown_album)
            } else {
                it.load(metadata.description.iconUri.toString())
            }
        }.also {
            it.into(nowPlayingCover)
            it.into(bottomNowPlayingCover)
        }

        metadata.description.title.let {
            bottomNowPlayingTitle.text = it
            nowPlayingTitle.text = it
        }
        metadata.description.subtitle.let {
            bottomNowPlayingArtist.text = it
            nowPlayingArtist.text = it
        }

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

}