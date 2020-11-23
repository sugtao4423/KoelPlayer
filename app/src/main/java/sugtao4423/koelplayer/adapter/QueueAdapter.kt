package sugtao4423.koelplayer.adapter

import androidx.recyclerview.widget.RecyclerView

class QueueAdapter : BaseMusicAdapter(VIEW_TYPE_QUEUE) {

    init {
        isCompilation = true
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        holder.itemView.setOnClickListener {
            musicService?.changeSong(holder.layoutPosition)
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