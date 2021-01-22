package sugtao4423.koelplayer

import android.content.Intent
import android.media.AudioManager
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import sugtao4423.koelplayer.fragment.AlbumFragment
import sugtao4423.koelplayer.fragment.PlaylistFragment
import sugtao4423.koelplayer.musicdb.MusicDB
import sugtao4423.koelplayer.playmusic.MusicService

class MainActivity : BaseBottomNowPlayingActivity() {

    private lateinit var albumFragment: AlbumFragment
    private lateinit var playlistFragment: PlaylistFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setSupportActionBar(mainToolbar)

        val koelToken = (applicationContext as App).koelToken
        if (koelToken.isEmpty()) {
            startActivity(Intent(this, ServerSettingsActivity::class.java))
            finish()
            return
        }

        volumeControlStream = AudioManager.STREAM_MUSIC

        startService(Intent(this, MusicService::class.java))

        albumFragment = AlbumFragment()
        playlistFragment = PlaylistFragment()

        mainViewPager.adapter = MainTabAdapter(supportFragmentManager)
        mainTabLayout.setupWithViewPager(mainViewPager)

        CoroutineScope(Dispatchers.Main).launch {
            val allMusicData = withContext(Dispatchers.IO) {
                val musicDB = MusicDB(this@MainActivity)
                val allMusicData = musicDB.getAllMusicData()
                musicDB.close()
                allMusicData
            }
            albumFragment.allMusicData = allMusicData
            playlistFragment.allMusicData = allMusicData
        }
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

    override fun onMusicServiceConnected(musicService: MusicService) {
        albumFragment.musicService = musicService
        playlistFragment.musicService = musicService
    }

    override fun onMusicServiceDisconnected() {
        albumFragment.musicService = null
        playlistFragment.musicService = null
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
                if (newText == null) {
                    return true
                }
                albumFragment.filter(newText)
                playlistFragment.filter(newText)
                return true
            }
        })
    }

}