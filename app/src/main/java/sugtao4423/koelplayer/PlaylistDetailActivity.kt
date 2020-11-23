package sugtao4423.koelplayer

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_playlist_detail.*
import sugtao4423.koel4j.dataclass.Playlist
import sugtao4423.koelplayer.adapter.PlaylistMusicAdapter
import sugtao4423.koelplayer.musicdb.MusicDB
import sugtao4423.koelplayer.playmusic.MusicService

class PlaylistDetailActivity : BaseBottomNowPlayingActivity(
    R.layout.activity_playlist_detail, R.id.playlistDetailAppbar
) {

    companion object {
        const val KEY_INTENT_PLAYLIST_DATA = "playlistData"
    }

    private lateinit var adapter: PlaylistMusicAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setSupportActionBar(playlistDetailToolbar)
        playlistDetailToolbar.setNavigationOnClickListener { finish() }

        val playlist = intent.getSerializableExtra(KEY_INTENT_PLAYLIST_DATA) as Playlist
        supportActionBar!!.title = playlist.name

        val musicDB = MusicDB(this)
        val songs = musicDB.getSongsById(playlist.songs)
        musicDB.close()

        adapter = PlaylistMusicAdapter(songs)
        playlistMusicView.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(this@PlaylistDetailActivity)
            adapter = this@PlaylistDetailActivity.adapter
        }
    }

    override fun onMusicServiceConnected(musicService: MusicService) {
        adapter.musicService = musicService
    }

    override fun onMusicServiceDisconnected() {
        adapter.musicService = null
    }

}