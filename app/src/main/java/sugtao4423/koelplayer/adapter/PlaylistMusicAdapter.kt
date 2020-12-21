package sugtao4423.koelplayer.adapter

import androidx.recyclerview.widget.RecyclerView
import sugtao4423.koel4j.dataclass.Song

class PlaylistMusicAdapter(songs: List<Song>) : BaseMusicAdapter(VIEW_TYPE_PLAYLIST) {

    init {
        addAll(songs)
        isCompilation = true
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        holder.itemView.setOnClickListener {
            musicService?.playSongs(songs, position)
        }
    }

}