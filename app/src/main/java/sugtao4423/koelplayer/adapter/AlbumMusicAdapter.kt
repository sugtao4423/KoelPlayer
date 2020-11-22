package sugtao4423.koelplayer.adapter

import sugtao4423.koel4j.dataclass.Song

class AlbumMusicAdapter(songs: List<Song>, isCompilation: Boolean) :
    BaseMusicAdapter() {

    init {
        addAll(songs)
        this.isCompilation = isCompilation
    }

    override fun onBindViewHolder(holder: MusicViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        holder.itemView.setOnClickListener {
            musicService?.playSongs(songs, position)
        }
    }

}