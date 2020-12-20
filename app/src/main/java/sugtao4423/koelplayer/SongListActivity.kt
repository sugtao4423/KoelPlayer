package sugtao4423.koelplayer

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_song_list.*
import sugtao4423.koel4j.dataclass.Album
import sugtao4423.koel4j.dataclass.Playlist
import sugtao4423.koel4j.dataclass.Song
import sugtao4423.koelplayer.adapter.AlbumMusicAdapter
import sugtao4423.koelplayer.adapter.BaseMusicAdapter
import sugtao4423.koelplayer.adapter.PlaylistMusicAdapter
import sugtao4423.koelplayer.musicdb.MusicDB
import sugtao4423.koelplayer.playmusic.MusicService

class SongListActivity : BaseBottomNowPlayingActivity(
    R.layout.activity_song_list, R.id.songListToolbar
) {

    companion object {
        const val KEY_INTENT_TYPE = "songsType"
        const val INTENT_TYPE_ALBUM = 0
        const val INTENT_TYPE_PLAYLIST = 1

        const val KEY_INTENT_ALBUM_DATA = "albumData"
        const val KEY_INTENT_PLAYLIST_DATA = "playlistData"

        private const val DATA_KEY_COVER_URL = "cover"
        private const val DATA_KEY_TITLE = "title"
        private const val DATA_KEY_ARTIST = "artist"
        private const val DATA_KEY_IS_COMPILATION = "compilation"
        private const val DATA_KEY_SONGS = "songs"
    }

    private lateinit var adapter: BaseMusicAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setSupportActionBar(songListToolbar)
        songListToolbar.setNavigationOnClickListener { finish() }

        val type = intent.getIntExtra(KEY_INTENT_TYPE, -1)
        val data = when (type) {
            INTENT_TYPE_ALBUM -> getAlbumData()
            INTENT_TYPE_PLAYLIST -> getPlaylistData()
            else -> {
                finish()
                return
            }
        }

        val coverUrl = data[DATA_KEY_COVER_URL] as String
        val title = data[DATA_KEY_TITLE] as String
        val artist = data[DATA_KEY_ARTIST] as String?
        val isCompilation = data[DATA_KEY_IS_COMPILATION] as Boolean?
        val songs = (data[DATA_KEY_SONGS] as List<*>).map { it as Song }

        GlideUtil.load(this, coverUrl, songListCover)
        songListTitle.text = title
        supportActionBar!!.title = title
        artist?.let {
            songListArtist.text = it
        }

        adapter = when (type) {
            INTENT_TYPE_ALBUM -> AlbumMusicAdapter(songs, isCompilation!!)
            INTENT_TYPE_PLAYLIST -> PlaylistMusicAdapter(songs)
            else -> {
                finish()
                return
            }
        }

        songListMusicList.apply {
            layoutManager = LinearLayoutManager(this@SongListActivity)
            adapter = this@SongListActivity.adapter
        }
    }

    override fun onMusicServiceConnected(musicService: MusicService) {
        adapter.musicService = musicService
    }

    override fun onMusicServiceDisconnected() {
        adapter.musicService = null
    }

    private fun getAlbumData(): Map<String, Any> {
        val album = intent.getSerializableExtra(KEY_INTENT_ALBUM_DATA) as Album
        val musicDB = MusicDB(this)
        val songs = musicDB.getAlbumSongs(album.id)
        musicDB.close()
        return mapOf(
            DATA_KEY_COVER_URL to album.cover,
            DATA_KEY_TITLE to album.name,
            DATA_KEY_ARTIST to album.artist.name,
            DATA_KEY_IS_COMPILATION to album.isCompilation,
            DATA_KEY_SONGS to songs,
        )
    }

    private fun getPlaylistData(): Map<String, Any> {
        val playlist = intent.getSerializableExtra(KEY_INTENT_PLAYLIST_DATA) as Playlist
        val musicDB = MusicDB(this)
        val songs = musicDB.getSongsById(playlist.songs)
        musicDB.close()
        return mapOf(
            DATA_KEY_COVER_URL to songs.random().album.cover,
            DATA_KEY_TITLE to playlist.name,
            DATA_KEY_SONGS to songs,
        )
    }
}