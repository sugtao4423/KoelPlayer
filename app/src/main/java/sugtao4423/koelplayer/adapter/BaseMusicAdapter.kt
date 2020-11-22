package sugtao4423.koelplayer.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import sugtao4423.koel4j.dataclass.Song
import sugtao4423.koelplayer.R
import sugtao4423.koelplayer.playmusic.MusicService

abstract class BaseMusicAdapter : RecyclerView.Adapter<BaseMusicAdapter.MusicViewHolder>() {

    var musicService: MusicService? = null
    var isCompilation = false

    protected var songs = ArrayList<Song>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MusicViewHolder {
        return MusicViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_album_song, parent, false))
    }

    override fun onBindViewHolder(holder: MusicViewHolder, position: Int) {
        val song = songs[position]
        holder.position.text = song.track.toString()
        holder.title.text = song.title

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
        holder.duration.text = length
    }

    override fun getItemCount(): Int {
        return songs.size
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

    inner class MusicViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val position: TextView = itemView.findViewById(R.id.albumSongPosition)
        val title: TextView = itemView.findViewById(R.id.albumSongTitle)
        val duration: TextView = itemView.findViewById(R.id.albumSongDuration)
    }

}