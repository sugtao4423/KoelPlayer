package sugtao4423.koelplayer.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import sugtao4423.koel4j.dataclass.Song
import sugtao4423.koelplayer.GlideUtil
import sugtao4423.koelplayer.R
import sugtao4423.koelplayer.playmusic.MusicService

abstract class BaseMusicAdapter(private val viewType: Int) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val VIEW_TYPE_ALBUM = 1
        const val VIEW_TYPE_QUEUE = 2
    }

    var musicService: MusicService? = null
    var isCompilation = false

    protected var songs = ArrayList<Song>()

    override fun getItemViewType(position: Int): Int = viewType

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_ALBUM) {
            AlbumMusicViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_album_song, parent, false))
        } else {
            QueueMusicViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_queue_song, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val song = songs[position]

        val hour = song.length.toInt() / 60 / 60
        val min = song.length.toInt() / 60 % 60
        val sec = (song.length.toInt() % 60).toString().padStart(2, '0')
        var length = if (hour == 0) {
            "$min:$sec"
        } else {
            val m = min.toString().padStart(2, '0')
            "$hour:$m:$sec"
        }

        if (isCompilation) {
            length = song.artist.name + "・" + length
        }

        if (viewType == VIEW_TYPE_ALBUM) {
            holder as AlbumMusicViewHolder
            holder.position.text = song.track.toString()
            holder.title.text = song.title
            holder.duration.text = length
        } else {
            holder as QueueMusicViewHolder
            GlideUtil.load(holder.itemView, song.album.cover, holder.cover)
            holder.title.text = song.title
            holder.duration.text = length
        }
    }

    override fun getItemCount(): Int {
        return songs.size
    }

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

    fun songPosition(song: Song): Int {
        return songs.indexOf(song)
    }

    inner class AlbumMusicViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val position: TextView = itemView.findViewById(R.id.albumSongPosition)
        val title: TextView = itemView.findViewById(R.id.albumSongTitle)
        val duration: TextView = itemView.findViewById(R.id.albumSongDuration)
    }

    inner class QueueMusicViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cover: ImageView = itemView.findViewById(R.id.queueSongCover)
        val title: TextView = itemView.findViewById(R.id.queueSongTitle)
        val duration: TextView = itemView.findViewById(R.id.queueSongDuration)
    }

}