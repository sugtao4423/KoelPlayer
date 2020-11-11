package sugtao4423.koelplayer.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import sugtao4423.koel4j.dataclass.Song
import sugtao4423.koelplayer.R
import sugtao4423.koelplayer.playmusic.MusicService

class AlbumMusicAdapter(private val songs: List<Song>, private val isCompilation: Boolean) :
    RecyclerView.Adapter<AlbumMusicAdapter.AlbumMusicViewHolder>() {

    var musicService: MusicService? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlbumMusicViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return AlbumMusicViewHolder(inflater.inflate(R.layout.item_album_song, parent, false))
    }

    override fun onBindViewHolder(holder: AlbumMusicViewHolder, position: Int) {
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

        holder.itemView.setOnClickListener {
            musicService?.playSong(song)
        }
    }

    override fun getItemCount(): Int {
        return songs.size
    }

    class AlbumMusicViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val position: TextView = itemView.findViewById(R.id.albumSongPosition)
        val title: TextView = itemView.findViewById(R.id.albumSongTitle)
        val duration: TextView = itemView.findViewById(R.id.albumSongDuration)
    }

}