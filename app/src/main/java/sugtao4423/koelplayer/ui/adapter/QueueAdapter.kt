package sugtao4423.koelplayer.ui.adapter

import androidx.recyclerview.widget.RecyclerView
import sugtao4423.koelplayer.ui.adapter.base.BaseMusicAdapter
import sugtao4423.koelplayer.viewmodel.base.MusicServiceViewModel

class QueueAdapter(private val viewModel: MusicServiceViewModel) :
    BaseMusicAdapter(VIEW_TYPE_QUEUE, true, viewModel) {

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        holder.itemView.setOnClickListener {
            viewModel.changeSong(holder.layoutPosition)
        }
    }

    fun move(from: Int, to: Int) {
        songs.add(to, songs.removeAt(from))
        notifyItemMoved(from, to)
    }

    fun remove(position: Int) {
        songs.removeAt(position)
        notifyItemRemoved(position)
    }

}
