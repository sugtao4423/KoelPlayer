package sugtao4423.koelplayer

import android.content.Intent
import android.media.AudioManager
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import sugtao4423.koelplayer.databinding.ActivityMainBinding
import sugtao4423.koelplayer.databinding.BottomSheetBinding
import sugtao4423.koelplayer.fragment.AlbumFragment
import sugtao4423.koelplayer.fragment.PlaylistFragment
import sugtao4423.koelplayer.playmusic.MusicService
import sugtao4423.koelplayer.viewmodel.MainViewModel

class MainActivity : BaseBottomNowPlayingActivity() {

    private val binding: ActivityMainBinding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    override val bsBinding: BottomSheetBinding by lazy { binding.mainBottomSheet }

    private val mainViewModel: MainViewModel by viewModels()

    private val albumFragment: AlbumFragment by lazy { AlbumFragment(bottomSheetViewModel) }
    private val playlistFragment: PlaylistFragment by lazy { PlaylistFragment(bottomSheetViewModel) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setSupportActionBar(binding.mainToolbar)
        initViews(binding.mainAppbar)

        val koelToken = (applicationContext as App).koelToken
        if (koelToken.isEmpty()) {
            startActivity(Intent(this, ServerSettingsActivity::class.java))
            finish()
            return
        }

        volumeControlStream = AudioManager.STREAM_MUSIC
        startService(Intent(this, MusicService::class.java))

        binding.mainViewPager.adapter = MainTabAdapter(supportFragmentManager)
        binding.mainTabLayout.setupWithViewPager(binding.mainViewPager)
    }

    override fun onResume() {
        super.onResume()
        val app = applicationContext as App
        if (app.reloadedAllMusicData) {
            app.reloadedAllMusicData = false
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    inner class MainTabAdapter(fm: FragmentManager) :
        FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

        override fun getItem(position: Int): Fragment {
            return when (position) {
                0 -> albumFragment
                else -> playlistFragment
            }
        }

        override fun getPageTitle(position: Int): CharSequence {
            return when (position) {
                0 -> getString(R.string.album)
                else -> getString(R.string.playlist)
            }
        }

        override fun getCount(): Int = 2

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if (menu == null) {
            return true
        }
        menuInflater.inflate(R.menu.main_menu, menu)
        val searchView = menu.findItem(R.id.menuSearch).actionView as SearchView
        setupSearchView(searchView)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.menuSettings) {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
        return true
    }

    private fun setupSearchView(searchView: SearchView) {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                mainViewModel.filter(newText ?: "")
                return true
            }
        })
    }

}
