package sugtao4423.koelplayer.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import sugtao4423.koel4j.dataclass.Song
import sugtao4423.koelplayer.GlideUtil
import sugtao4423.koelplayer.R
import sugtao4423.koelplayer.databinding.ItemAlbumSongBinding
import sugtao4423.koelplayer.databinding.ItemPlaylistSongBinding
import sugtao4423.koelplayer.databinding.ItemQueueSongBinding
import sugtao4423.koelplayer.secToTimeFormat
import sugtao4423.koelplayer.viewmodel.MusicServiceViewModel

abstract class BaseMusicAdapter(
    private val viewType: Int,
    private val isCompilation: Boolean,
    private val viewModel: MusicServiceViewModel
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val VIEW_TYPE_ALBUM = 1
        const val VIEW_TYPE_PLAYLIST = 2
        const val VIEW_TYPE_QUEUE = 3
    }

    private lateinit var context: Context

    protected var songs = ArrayList<Song>()

    override fun getItemViewType(position: Int): Int = viewType

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        context = parent.context!!
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_ALBUM -> AlbumMusicViewHolder(
                ItemAlbumSongBinding.inflate(inflater, parent, false)
            )

            VIEW_TYPE_PLAYLIST -> PlaylistMusicViewHolder(
                ItemPlaylistSongBinding.inflate(inflater, parent, false)
            )

            else -> QueueMusicViewHolder(
                ItemQueueSongBinding.inflate(inflater, parent, false)
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val song = songs[position]

        val length = song.length.toInt().secToTimeFormat(song.length >= 3600).let {
            if (isCompilation) song.artist.name + "・" + it else it
        }

        when (viewType) {
            VIEW_TYPE_ALBUM -> {
                holder as AlbumMusicViewHolder
                holder.binding.albumSongPosition.text = song.track.toString()
                holder.binding.albumSongTitle.text = song.title
                holder.binding.albumSongDuration.text = length
                holder.binding.albumSongMore.setOnClickListener { clickMoreButton(it, position) }
            }

            VIEW_TYPE_PLAYLIST -> {
                holder as PlaylistMusicViewHolder
                GlideUtil.load(holder.itemView, song.album.cover, holder.binding.playlistSongCover)
                holder.binding.playlistSongTitle.text = song.title
                holder.binding.playlistSongDuration.text = length
                holder.binding.playlistSongMore.setOnClickListener { clickMoreButton(it, position) }
            }

            VIEW_TYPE_QUEUE -> {
                holder as QueueMusicViewHolder
                GlideUtil.load(holder.itemView, song.album.cover, holder.binding.queueSongCover)
                holder.binding.queueSongTitle.text = song.title
                holder.binding.queueSongDuration.text = length
            }
        }
    }

    override fun getItemCount(): Int = songs.size

    fun clear() {
        val size = songs.size
        songs.clear()
        notifyItemRangeRemoved(0, size)
    }

    fun add(song: Song) {
        songs.add(song)
        notifyItemInserted(songs.lastIndex)
    }

    fun addAll(songs: List<Song>) {
        val lastItemIndex = this.songs.lastIndex
        this.songs.addAll(songs)
        notifyItemRangeInserted(lastItemIndex + 1, songs.size)
    }

    inner class AlbumMusicViewHolder(val binding: ItemAlbumSongBinding) :
        RecyclerView.ViewHolder(binding.root)

    inner class PlaylistMusicViewHolder(val binding: ItemPlaylistSongBinding) :
        RecyclerView.ViewHolder(binding.root)

    inner class QueueMusicViewHolder(val binding: ItemQueueSongBinding) :
        RecyclerView.ViewHolder(binding.root)

    private fun clickMoreButton(anchor: View, position: Int) {
        PopupMenu(context, anchor).apply {
            menuInflater.inflate(R.menu.song_more_menu, menu)
            setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.songMorePlayNext -> viewModel.let {
                        it.addQueueNext(listOf(songs[position]))
                        Toast.makeText(
                            context.applicationContext, R.string.play_next_song, Toast.LENGTH_SHORT
                        ).show()
                    }

                    R.id.songMoreAddQueue -> viewModel.let {
                        it.addQueueLast(listOf(songs[position]))
                        Toast.makeText(
                            context.applicationContext, R.string.add_queue_song, Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                true
            }
            show()
        }
    }

}
