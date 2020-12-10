package sugtao4423.koelplayer

import android.content.Intent
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import kotlinx.android.synthetic.main.activity_main.*
import sugtao4423.koel4j.dataclass.AllMusicData
import sugtao4423.koel4j.dataclass.Song
import sugtao4423.koelplayer.download.KoelDLService
import sugtao4423.koelplayer.fragment.AlbumFragment
import sugtao4423.koelplayer.fragment.PlaylistFragment
import sugtao4423.koelplayer.musicdb.MusicDB
import sugtao4423.koelplayer.playmusic.MusicService

class MainActivity : BaseBottomNowPlayingActivity() {

    private var allMusicData: AllMusicData? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setSupportActionBar(mainToolbar)

        val koelToken = Prefs(this).koelToken
        if (koelToken.isEmpty()) {
            startActivity(Intent(this, ServerSettingsActivity::class.java))
            finish()
            return
        }

        volumeControlStream = AudioManager.STREAM_MUSIC

        startService(Intent(this, MusicService::class.java))

        MusicDB(this).let {
            allMusicData = it.getAllMusicData()
            it.close()
        }

        mainViewPager.adapter = MainTabAdapter(supportFragmentManager)
        mainTabLayout.setupWithViewPager(mainViewPager)
    }

    inner class MainTabAdapter(fm: FragmentManager) :
        FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

        override fun getItem(position: Int): Fragment {
            return when (position) {
                0 -> AlbumFragment(allMusicData!!)
                else -> PlaylistFragment(allMusicData!!)
            }
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return when (position) {
                0 -> getString(R.string.album)
                else -> getString(R.string.playlist)
            }
        }

        override fun getCount(): Int = 2

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menu?.add("とりあえずDL")
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        saveSongs(allMusicData!!.songs.toTypedArray())
        return true
    }

    private fun saveSongs(songs: Array<Song>) {
        val intent = Intent(this, KoelDLService::class.java)
        intent.putExtra(KoelDLService.INTENT_KEY_SONGS, songs)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

}