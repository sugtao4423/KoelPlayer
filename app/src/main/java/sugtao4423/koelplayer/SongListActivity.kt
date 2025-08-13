package sugtao4423.koelplayer

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import sugtao4423.koel4j.dataclass.Album
import sugtao4423.koel4j.dataclass.Playlist
import sugtao4423.koelplayer.adapter.AlbumMusicAdapter
import sugtao4423.koelplayer.adapter.PlaylistMusicAdapter
import sugtao4423.koelplayer.databinding.ActivitySongListBinding
import sugtao4423.koelplayer.databinding.BottomSheetBinding
import sugtao4423.koelplayer.viewmodel.SongListViewModel

class SongListActivity : BaseBottomNowPlayingActivity() {

    companion object {
        const val KEY_INTENT_TYPE = "songsType"
        const val INTENT_TYPE_ALBUM = 0
        const val INTENT_TYPE_PLAYLIST = 1

        const val KEY_INTENT_ALBUM_DATA = "albumData"
        const val KEY_INTENT_PLAYLIST_DATA = "playlistData"
    }

    private val binding: ActivitySongListBinding by lazy {
        ActivitySongListBinding.inflate(layoutInflater)
    }

    override val bsBinding: BottomSheetBinding by lazy {
        binding.songListBottomSheet
    }

    private val viewModel: SongListViewModel by viewModels()

    private val intentType by lazy {
        intent.getIntExtra(KEY_INTENT_TYPE, -1)
    }

    private var playlist: Playlist? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setSupportActionBar(binding.songListToolbar)
        binding.songListToolbar.setNavigationOnClickListener { finish() }
        initViews(binding.songListToolbar)

        binding.songListMusicList.layoutManager = LinearLayoutManager(this)
        initObservers()

        when (intentType) {
            INTENT_TYPE_ALBUM -> {
                val album = intent.getSerializableExtra(KEY_INTENT_ALBUM_DATA) as Album
                viewModel.loadAlbumData(album)
            }

            INTENT_TYPE_PLAYLIST -> {
                playlist = intent.getSerializableExtra(KEY_INTENT_PLAYLIST_DATA) as Playlist
                viewModel.loadPlaylistData(playlist!!)
            }

            else -> throw IllegalArgumentException("Unknown intent type: $intentType")
        }
    }

    private fun initObservers() {
        viewModel.songListData.observe(this) {
            if (it == null) return@observe
            updateUI(it)
            initAdapter(it)
        }
    }

    private fun updateUI(data: SongListViewModel.SongListData) {
        GlideUtil.load(this, data.coverUrl, binding.songListCover, true)
        binding.songListTitle.text = data.title
        supportActionBar?.title = data.title

        val songTime = viewModel.getSongTime() ?: ""
        binding.songListArtist.text = if (data.artist == null) {
            songTime
        } else {
            "${data.artist}・$songTime"
        }

        if (data.allDownloaded && data.fileSize != null) {
            binding.songListDlSize.text = data.fileSize
            binding.songListDlSize.visibility = View.VISIBLE
        } else {
            binding.songListDlSize.visibility = View.GONE
        }
    }

    private fun initAdapter(data: SongListViewModel.SongListData) {
        val adapter = when (intentType) {
            INTENT_TYPE_ALBUM -> AlbumMusicAdapter(
                data.songs, data.isCompilation, bottomSheetViewModel
            )

            INTENT_TYPE_PLAYLIST -> PlaylistMusicAdapter(
                data.songs, bottomSheetViewModel
            )

            else -> return
        }
        binding.songListMusicList.adapter = adapter
    }

    fun clickPlayButton(@Suppress("UNUSED_PARAMETER") v: View) {
        viewModel.songListData.value?.let {
            bottomSheetViewModel.playSongs(it.songs)
        }
    }

    fun clickShuffleButton(@Suppress("UNUSED_PARAMETER") v: View) {
        viewModel.songListData.value?.let {
            bottomSheetViewModel.shufflePlaySongs(it.songs)
        }
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
        playlist?.let {
            val order = viewModel.getPlaylistSort(it)
            AlertDialog.Builder(this).apply {
                setSingleChoiceItems(R.array.sort_items, order) { dialogInterface, which ->
                    if (order != which) {
                        viewModel.updatePlaylistSort(it, which)
                    }
                    dialogInterface.dismiss()
                }
                show()
            }
        }
    }

}
