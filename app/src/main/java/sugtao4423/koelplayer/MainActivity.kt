package sugtao4423.koelplayer

import android.content.Intent
import android.media.AudioManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import sugtao4423.koel4j.dataclass.AllMusicData
import sugtao4423.koelplayer.adapter.AlbumAdapter
import sugtao4423.koelplayer.musicdb.MusicDB
import sugtao4423.koelplayer.playmusic.MusicService

class MainActivity : AppCompatActivity() {

    private lateinit var allMusicData: AllMusicData

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val koelToken = Prefs(this).koelToken
        if (koelToken.isEmpty()) {
            startActivity(Intent(this, ServerSettingsActivity::class.java))
            finish()
            return
        }

        loadMusicData()
        setAdapter()

        volumeControlStream = AudioManager.STREAM_MUSIC

        startService(Intent(this, MusicService::class.java))
    }

    private fun loadMusicData() {
        val musicDB = MusicDB(this)
        allMusicData = musicDB.getAllMusicData()
        musicDB.close()
    }

    private fun setAdapter() {
        albumGrid.adapter = AlbumAdapter(this, allMusicData.albums)
    }
}