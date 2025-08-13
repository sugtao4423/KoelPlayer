package sugtao4423.koelplayer.ui.adapter

import androidx.recyclerview.widget.RecyclerView
import sugtao4423.koel4j.dataclass.Song
import sugtao4423.koelplayer.ui.adapter.base.BaseMusicAdapter
import sugtao4423.koelplayer.viewmodel.base.MusicServiceViewModel

class AlbumMusicAdapter(
    songs: List<Song>, isCompilation: Boolean, private val viewModel: MusicServiceViewModel
) : BaseMusicAdapter(VIEW_TYPE_ALBUM, isCompilation, viewModel) {

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
