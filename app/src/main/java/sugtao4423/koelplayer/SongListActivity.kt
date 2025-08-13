package sugtao4423.koelplayer

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import sugtao4423.koel4j.dataclass.Album
import sugtao4423.koel4j.dataclass.Playlist
import sugtao4423.koel4j.dataclass.Song
import sugtao4423.koelplayer.adapter.AlbumMusicAdapter
import sugtao4423.koelplayer.adapter.BaseMusicAdapter
import sugtao4423.koelplayer.adapter.PlaylistMusicAdapter
import sugtao4423.koelplayer.databinding.ActivitySongListBinding
import sugtao4423.koelplayer.databinding.BottomSheetBinding
import sugtao4423.koelplayer.download.KoelDLUtil
import sugtao4423.koelplayer.musicdb.MusicDB

class SongListActivity : BaseBottomNowPlayingActivity() {

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

    private val binding: ActivitySongListBinding by lazy {
        ActivitySongListBinding.inflate(layoutInflater)
    }

    override val bsBinding: BottomSheetBinding by lazy {
        binding.songListBottomSheet
    }

    private val intentType by lazy {
        intent.getIntExtra(KEY_INTENT_TYPE, -1)
    }
    private lateinit var adapter: BaseMusicAdapter
    private lateinit var songs: List<Song>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setSupportActionBar(binding.songListToolbar)
        binding.songListToolbar.setNavigationOnClickListener { finish() }
        initViews(binding.songListToolbar)

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

        GlideUtil.load(this, coverUrl, binding.songListCover, true)
        binding.songListTitle.text = title
        supportActionBar!!.title = title
        val songTime = songs.sumOf { it.length }.toInt().secToTimeFormat()
        binding.songListArtist.text = if (artist == null) songTime else "$artist・$songTime"
        if (theseSongsAllDownloaded) {
            binding.songListDlSize.text = theseSongsFileSize
        }

        adapter = when (intentType) {
            INTENT_TYPE_ALBUM -> AlbumMusicAdapter(songs, isCompilation, bottomSheetViewModel)
            INTENT_TYPE_PLAYLIST -> PlaylistMusicAdapter(songs, bottomSheetViewModel)
            else -> {
                finish()
                return
            }
        }

        binding.songListMusicList.apply {
            layoutManager = LinearLayoutManager(this@SongListActivity)
            adapter = this@SongListActivity.adapter
        }
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
        songs = when (sortOrder) {
            0 -> playlist.songs.map { songId -> songs.find { it.id == songId }!! }
            1 -> songs.sortedBy { it.track }.sortedBy { it.album.name }
            else -> throw IllegalArgumentException("Unknown sort order: $sortOrder")
        }

        return mapOf(
            DATA_KEY_COVER_URL to songs.randomOrNull()?.album?.cover,
            DATA_KEY_TITLE to playlist.name,
            DATA_KEY_SONGS to songs,
        )
    }

    fun clickPlayButton(@Suppress("UNUSED_PARAMETER") v: View) {
        bottomSheetViewModel.playSongs(songs)
    }

    fun clickShuffleButton(@Suppress("UNUSED_PARAMETER") v: View) {
        bottomSheetViewModel.shufflePlaySongs(songs)
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
