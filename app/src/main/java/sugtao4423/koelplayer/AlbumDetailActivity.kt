package sugtao4423.koelplayer

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_album_detail.*
import sugtao4423.koel4j.dataclass.Album
import sugtao4423.koelplayer.adapter.AlbumMusicAdapter
import sugtao4423.koelplayer.musicdb.MusicDB

class AlbumDetailActivity : AppCompatActivity() {

    companion object {
        const val KEY_INTENT_ALBUM_DATA = "albumData"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_album_detail)
        setSupportActionBar(albumDetailToolbar)
        albumDetailToolbar.setNavigationOnClickListener { finish() }

        val album = intent.getSerializableExtra(KEY_INTENT_ALBUM_DATA) as Album
        val artist = MusicDB(this).getArtist(album.artistId)
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
        albumDetailArtist.text = artist.name

        val layoutManager = LinearLayoutManager(this)
        val adapter = AlbumMusicAdapter(songs)
        albumDetailMusicList.layoutManager = layoutManager
        albumDetailMusicList.adapter = adapter
    }

}