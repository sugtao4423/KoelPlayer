package sugtao4423.koelplayer

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_album_detail.*
import sugtao4423.koel4j.dataclass.Album
import sugtao4423.koelplayer.adapter.AlbumMusicAdapter
import sugtao4423.koelplayer.musicdb.MusicDB
import sugtao4423.koelplayer.playmusic.MusicService

class AlbumDetailActivity : AppCompatActivity() {

    companion object {
        const val KEY_INTENT_ALBUM_DATA = "albumData"
    }

    private lateinit var adapter: AlbumMusicAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_album_detail)
        setSupportActionBar(albumDetailToolbar)
        albumDetailToolbar.setNavigationOnClickListener { finish() }

        val album = intent.getSerializableExtra(KEY_INTENT_ALBUM_DATA) as Album
        val songs = MusicDB(this).getAlbumSongs(album.id)

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

        bindService(Intent(this, MusicService::class.java), serviceConnection, BIND_AUTO_CREATE)
    }

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            adapter.musicService = (service as MusicService.MusicServiceBinder).musicService
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            adapter.musicService = null
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(serviceConnection)
    }

}