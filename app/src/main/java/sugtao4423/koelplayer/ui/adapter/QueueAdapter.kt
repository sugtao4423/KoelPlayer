package sugtao4423.koelplayer.ui.adapter

import androidx.recyclerview.widget.RecyclerView
import sugtao4423.koelplayer.ui.adapter.base.MusicAdapter
import sugtao4423.koelplayer.viewmodel.base.MusicServiceViewModel

class QueueAdapter(private val viewModel: MusicServiceViewModel) :
    MusicAdapter(VIEW_TYPE_QUEUE, true, viewModel) {

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        holder.itemView.setOnClickListener {
            viewModel.changeSong(holder.layoutPosition)
        }
    }

    fun move(from: Int, to: Int) {
        val newList = currentList.toMutableList()
        newList.add(to, newList.removeAt(from))
        submitList(newList)
    }

}
