package sugtao4423.koelplayer

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_song_list.*
import sugtao4423.koel4j.dataclass.Album
import sugtao4423.koel4j.dataclass.Playlist
import sugtao4423.koel4j.dataclass.Song
import sugtao4423.koelplayer.adapter.AlbumMusicAdapter
import sugtao4423.koelplayer.adapter.BaseMusicAdapter
import sugtao4423.koelplayer.adapter.PlaylistMusicAdapter
import sugtao4423.koelplayer.download.KoelDLUtil
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

    private val intentType by lazy {
        intent.getIntExtra(KEY_INTENT_TYPE, -1)
    }
    private lateinit var adapter: BaseMusicAdapter
    private lateinit var songs: List<Song>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setSupportActionBar(songListToolbar)
        songListToolbar.setNavigationOnClickListener { finish() }

        val data = when (intentType) {
            INTENT_TYPE_ALBUM -> getAlbumData()
            INTENT_TYPE_PLAYLIST -> getPlaylistData()
            else -> {
                finish()
                return
            }
        }

        val coverUrl = data[DATA_KEY_COVER_URL] as String?
        val title = data[DATA_KEY_TITLE] as String
        val artist = data[DATA_KEY_ARTIST] as String?
        val isCompilation = (data[DATA_KEY_IS_COMPILATION] as Boolean?) ?: false
        songs = (data[DATA_KEY_SONGS] as List<*>).map { it as Song }

        val dlUtil = KoelDLUtil(this)
        val downloadedCount = songs.filter { dlUtil.isDownloaded(it) }.size
        val theseSongsAllDownloaded = songs.size == downloadedCount
        val theseSongsFileSize = dlUtil.getSongFilesSize(songs)

        GlideUtil.load(this, coverUrl, songListCover, true)
        songListTitle.text = title
        supportActionBar!!.title = title
        val songTime = getSongsTime(songs)
        songListArtist.text = if (artist == null) songTime else "$artist・$songTime"
        if (theseSongsAllDownloaded) {
            songListDlSize.text = theseSongsFileSize
        }

        adapter = when (intentType) {
            INTENT_TYPE_ALBUM -> AlbumMusicAdapter(songs, isCompilation)
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

    private fun getAlbumData(): Map<String, Any?> {
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

    private fun getPlaylistData(): Map<String, Any?> {
        val playlist = intent.getSerializableExtra(KEY_INTENT_PLAYLIST_DATA) as Playlist
        val musicDB = MusicDB(this)
        var songs = musicDB.getSongsById(playlist.songs)
        musicDB.close()

        val sortOrder = (applicationContext as App).getPlaylistSortOrder(playlist)
        if (sortOrder == 1) {
            songs = songs.sortedBy { it.track }.sortedBy { it.album.name }
        }

        return mapOf(
            DATA_KEY_COVER_URL to songs.randomOrNull()?.album?.cover,
            DATA_KEY_TITLE to playlist.name,
            DATA_KEY_SONGS to songs,
        )
    }

    private fun getSongsTime(songs: List<Song>): String {
        var second = 0.0
        songs.forEach { second += it.length }
        val sec = second.toInt()
        val h = sec / 60 / 60
        val m = (sec / 60 % 60).toString().padStart(2, '0')
        val s = (sec % 60).toString().padStart(2, '0')
        return if (h == 0) {
            "$m:$s"
        } else {
            "$h:$m:$s"
        }
    }

    fun clickPlayButton(@Suppress("UNUSED_PARAMETER") v: View) {
        musicService?.playSongs(songs)
    }

    fun clickShuffleButton(@Suppress("UNUSED_PARAMETER") v: View) {
        musicService?.shufflePlaySongs(songs)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if (intentType == INTENT_TYPE_PLAYLIST) {
            menu?.add(Menu.NONE, Menu.FIRST, Menu.NONE, R.string.sort)
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (intentType == INTENT_TYPE_PLAYLIST && item.itemId == Menu.FIRST) {
            showSortDialog()
        }
        return true
    }

    private fun showSortDialog() {
        val playlist = (intent.getSerializableExtra(KEY_INTENT_PLAYLIST_DATA) as Playlist)
        val order = (applicationContext as App).getPlaylistSortOrder(playlist)
        AlertDialog.Builder(this).apply {
            setSingleChoiceItems(R.array.sort_items, order) { dialogInterface, which ->
                if (order == which) {
                    dialogInterface.dismiss()
                } else {
                    (applicationContext as App).setPlaylistSortOrder(playlist, which)
                    val intent = Intent(context, SongListActivity::class.java).apply {
                        putExtra(KEY_INTENT_TYPE, INTENT_TYPE_PLAYLIST)
                        putExtra(KEY_INTENT_PLAYLIST_DATA, playlist)
                    }
                    startActivity(intent)
                    finish()
                }
            }
            show()
        }
    }

}