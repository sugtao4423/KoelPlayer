package sugtao4423.koelplayer

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import com.google.android.material.bottomsheet.BottomSheetBehavior
import sugtao4423.koelplayer.bsfragment.BSFragmentInterface
import sugtao4423.koelplayer.bsfragment.BSNowPlayingFragment
import sugtao4423.koelplayer.bsfragment.BSQueueFragment
import sugtao4423.koelplayer.databinding.BottomSheetBinding
import sugtao4423.koelplayer.playmusic.MusicService

abstract class BaseBottomNowPlayingActivity : AppCompatActivity() {

    protected abstract val bsBinding: BottomSheetBinding

    protected var musicService: MusicService? = null

    private val bottomSheet: BottomSheetBehavior<CoordinatorLayout> by lazy {
        BottomSheetBehavior.from(bsBinding.root)
    }

    private val bottomSheetFragments: List<BSFragmentInterface> by lazy {
        listOf(BSNowPlayingFragment(), BSQueueFragment())
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

    protected fun initViews(backgroundAppbar: View) {
        initActionBar()
        initBottomSheet(backgroundAppbar)
        initBottomNav()
    }

    private fun initActionBar() {
        bsBinding.nowPlayingToolbar.setNavigationOnClickListener { bottomSheet.toggleState() }
    }

    override fun onSupportNavigateUp(): Boolean {
        bottomSheet.toggleState()
        return super.onSupportNavigateUp()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initBottomSheet(backgroundAppbar: View) {
        if (bottomSheet.state == BottomSheetBehavior.STATE_EXPANDED) {
            bsBinding.nowPlayingSheetCollapsed.alpha = 0f
            bsBinding.nowPlayingSheetExpanded.alpha = 1f
        } else if (bottomSheet.state == BottomSheetBehavior.STATE_COLLAPSED) {
            bsBinding.nowPlayingSheetCollapsed.alpha = 1f
            bsBinding.nowPlayingSheetExpanded.alpha = 0f
        }

        bottomSheet.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                val inverseOffset = 1 - slideOffset
                bsBinding.nowPlayingSheetCollapsed.alpha = inverseOffset
                bsBinding.nowPlayingSheetExpanded.alpha = slideOffset
                backgroundAppbar.alpha = inverseOffset
            }

            override fun onStateChanged(bottomSheet: View, newState: Int) {
                backgroundAppbar.visibility =
                    if (newState == BottomSheetBehavior.STATE_EXPANDED) View.GONE else View.VISIBLE
            }
        })
        bsBinding.nowPlayingSheetExpanded.setOnTouchListener { _, _ -> true }
        bsBinding.nowPlayingToolbar.setOnClickListener {
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

        bsBinding.bottomSheetBottomNav.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.bottomSheetNowPlayingButton -> showFragment(bottomSheetFragments[0])
                R.id.bottomSheetQueueButton -> showFragment(bottomSheetFragments[1])
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
        private var beforeSongUri = "".toUri()

        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            if (beforeSongUri == (metadata?.description?.mediaUri ?: "".toUri())) {
                return
            }
            metadata?.let {
                updateMetadata(it)
                bottomSheetFragments.forEach { fragment ->
                    fragment.updateMetadata(it)
                }
            }
            beforeSongUri = (metadata?.description?.mediaUri ?: "".toUri())
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
        GlideUtil.load(this, metadata.description.iconUri, bsBinding.bottomNowPlayingCover)
        bsBinding.bottomNowPlayingTitle.text = metadata.description.title
        bsBinding.bottomNowPlayingArtist.text = metadata.description.subtitle
    }

}
