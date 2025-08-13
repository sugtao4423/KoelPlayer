package sugtao4423.koelplayer.adapter

import androidx.recyclerview.widget.RecyclerView
import sugtao4423.koel4j.dataclass.Song
import sugtao4423.koelplayer.viewmodel.MusicServiceViewModel

class PlaylistMusicAdapter(songs: List<Song>, private val viewModel: MusicServiceViewModel) :
    BaseMusicAdapter(VIEW_TYPE_PLAYLIST, true, viewModel) {

    init {
        addAll(songs)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        holder.itemView.setOnClickListener {
            viewModel.playSongs(songs, position)
        }
    }

}
