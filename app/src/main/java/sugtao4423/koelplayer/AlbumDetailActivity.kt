package sugtao4423.koelplayer

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_album_detail.*
import sugtao4423.koel4j.dataclass.Album
import sugtao4423.koelplayer.adapter.AlbumMusicAdapter
import sugtao4423.koelplayer.musicdb.MusicDB
import sugtao4423.koelplayer.playmusic.MusicService

class AlbumDetailActivity : BaseBottomNowPlayingActivity(
    R.layout.activity_album_detail, R.id.albumDetailToolbar
) {

    companion object {
        const val KEY_INTENT_ALBUM_DATA = "albumData"
    }

    private lateinit var adapter: AlbumMusicAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setSupportActionBar(albumDetailToolbar)
        albumDetailToolbar.setNavigationOnClickListener { finish() }

        val album = intent.getSerializableExtra(KEY_INTENT_ALBUM_DATA) as Album
        val musicDB = MusicDB(this)
        val songs = musicDB.getAlbumSongs(album.id)
        musicDB.close()

        if (album.cover.endsWith("unknown-album.png")) {
            Glide.with(this).load(R.drawable.unknown_album).into(albumDetailCover)
        } else {
            Glide.with(this).load(album.cover).into(albumDetailCover)
        }
        album.name.let {
            albumDetailTitle.text = it
            supportActionBar!!.title = it
        }
        albumDetailArtist.text = album.artist.name

        val layoutManager = LinearLayoutManager(this)
        adapter = AlbumMusicAdapter(songs, album.isCompilation)
        albumDetailMusicList.layoutManager = layoutManager
        albumDetailMusicList.adapter = adapter
    }

    override fun onMusicServiceConnected(musicService: MusicService) {
        adapter.musicService = musicService
    }

    override fun onMusicServiceDisconnected() {
        adapter.musicService = null
    }

}