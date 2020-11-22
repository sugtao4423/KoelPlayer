package sugtao4423.koelplayer.adapter

import androidx.recyclerview.widget.RecyclerView
import sugtao4423.koel4j.dataclass.Song

class AlbumMusicAdapter(songs: List<Song>, isCompilation: Boolean) :
    BaseMusicAdapter(VIEW_TYPE_ALBUM) {

    init {
        addAll(songs)
        this.isCompilation = isCompilation
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        holder.itemView.setOnClickListener {
            musicService?.playSongs(songs, position)
        }
    }

}