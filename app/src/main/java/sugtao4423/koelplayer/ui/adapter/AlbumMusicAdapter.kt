package sugtao4423.koelplayer.ui.adapter

import androidx.recyclerview.widget.RecyclerView
import sugtao4423.koel4j.dataclass.Song
import sugtao4423.koelplayer.ui.adapter.base.MusicAdapter
import sugtao4423.koelplayer.viewmodel.base.MusicServiceViewModel

class AlbumMusicAdapter(
    songs: List<Song>, isCompilation: Boolean, private val viewModel: MusicServiceViewModel
) : MusicAdapter(VIEW_TYPE_ALBUM, isCompilation, viewModel) {

    init {
        submitList(songs)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        holder.itemView.setOnClickListener {
            viewModel.playSongs(currentList, position)
        }
    }

}
