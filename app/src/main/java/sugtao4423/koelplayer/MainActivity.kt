package sugtao4423.koelplayer

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import kotlinx.android.synthetic.main.activity_main.*
import sugtao4423.koel4j.dataclass.AllMusicData
import sugtao4423.koelplayer.adapter.AlbumAdapter
import sugtao4423.koelplayer.musicdb.MusicDB

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
    }

    private fun loadMusicData() {
        allMusicData = MusicDB(this).getAllMusicData()
    }

    private fun setAdapter() {
        val adapter = AlbumAdapter(this, allMusicData.albums)
        val layoutManager = GridLayoutManager(this, 3, GridLayoutManager.VERTICAL, false)
        albumGrid.layoutManager = layoutManager
        albumGrid.setHasFixedSize(true)
        albumGrid.adapter = adapter
    }
}