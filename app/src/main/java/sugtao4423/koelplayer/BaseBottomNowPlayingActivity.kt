package sugtao4423.koelplayer

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.android.synthetic.main.bottom_sheet.*
import sugtao4423.koelplayer.bsfragment.BSFragmentInterface
import sugtao4423.koelplayer.bsfragment.BSNowPlayingFragment
import sugtao4423.koelplayer.playmusic.MusicService

abstract class BaseBottomNowPlayingActivity(
    private val layoutResId: Int = R.layout.activity_main,
    private val backgroundAppbarId: Int = R.id.mainAppbar
) : AppCompatActivity() {

    protected var musicService: MusicService? = null
    private lateinit var bottomSheet: BottomSheetBehavior<CoordinatorLayout>

    private lateinit var bottomSheetFragments: List<BSFragmentInterface>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layoutResId)

        bottomSheetFragments = listOf(BSNowPlayingFragment())

        initActionBar()
        initBottomSheet()
        initBottomNav()
    }

    override fun onStart() {
        super.onStart()
        bindService(Intent(this, MusicService::class.java), serviceConnection, BIND_AUTO_CREATE)
    }

    override fun onStop() {
        super.onStop()
        unbindService(serviceConnection)
    }

    override fun onDestroy() {
        super.onDestroy()
        musicService?.removeMediaControllerCallback(mediaControllerCallback)
    }

    private fun initActionBar() {
        nowPlayingToolbar.setNavigationOnClickListener { bottomSheet.toggleState() }
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

        val bgAppbar = findViewById<View>(backgroundAppbarId)
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

    private fun showFragment(fragment: BSFragmentInterface) {
        supportFragmentManager.commit {
            bottomSheetFragments.forEach {
                it as Fragment
                if (fragment == it) show(it) else hide(it)
            }
        }
    }

    private fun initBottomNav() {
        supportFragmentManager.commit {
            bottomSheetFragments.forEach {
                add(R.id.bottomSheetContainer, it as Fragment)
            }
        }

        bottomSheetBottomNav.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.bottomSheetQueueButton -> null
                R.id.bottomSheetNowPlayingButton -> showFragment(bottomSheetFragments[0])
            }
            true
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

    private val mediaControllerCallback = object : MediaControllerCompat.Callback() {
        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            metadata?.let {
                updateMetadata(it)
                bottomSheetFragments.forEach { fragment ->
                    fragment.updateMetadata(it)
                }
            }
        }
    }

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            musicService = (service as MusicService.MusicServiceBinder).musicService
            musicService!!.setMediaControllerCallback(mediaControllerCallback)
            musicService!!.playingMetadata()?.let {
                updateMetadata(it)
                bottomSheetFragments.forEach { fragment ->
                    fragment.updateMetadata(it)
                }
            }

            onMusicServiceConnected(musicService!!)
            bottomSheetFragments.forEach { fragment ->
                fragment.onMusicServiceConnected(musicService!!)
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            musicService = null
            onMusicServiceDisconnected()
            bottomSheetFragments.forEach { fragment ->
                fragment.onMusicServiceDisconnected()
            }
        }
    }

    open fun onMusicServiceConnected(musicService: MusicService) {}

    open fun onMusicServiceDisconnected() {}

    private fun updateMetadata(metadata: MediaMetadataCompat) {
        Glide.with(this).let {
            if (metadata.description.iconUri == null || metadata.description.iconUri.toString().endsWith("unknown-album.png")) {
                it.load(R.drawable.unknown_album)
            } else {
                it.load(metadata.description.iconUri.toString())
            }
        }.into(bottomNowPlayingCover)

        bottomNowPlayingTitle.text = metadata.description.title
        bottomNowPlayingArtist.text = metadata.description.subtitle
    }

}