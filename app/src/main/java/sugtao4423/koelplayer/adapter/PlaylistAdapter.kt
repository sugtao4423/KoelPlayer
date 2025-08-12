package sugtao4423.koelplayer.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import sugtao4423.koel4j.dataclass.Playlist
import sugtao4423.koelplayer.GlideUtil
import sugtao4423.koelplayer.R
import sugtao4423.koelplayer.SongListActivity
import sugtao4423.koelplayer.musicdb.MusicDB
import sugtao4423.koelplayer.view.SquareImageView
import sugtao4423.koelplayer.viewmodel.MusicServiceViewModel

class PlaylistAdapter(private val viewModel: MusicServiceViewModel) :
    RecyclerView.Adapter<PlaylistAdapter.PlaylistViewHolder>() {

    var playlists = listOf<Pair<Playlist, String?>>()
        set(value) {
            notifyItemRangeRemoved(0, field.size)
            field = value
            notifyItemRangeInserted(0, value.size)
        }

    private lateinit var context: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistViewHolder {
        context = parent.context
        val inflater = LayoutInflater.from(parent.context)
        return PlaylistViewHolder(inflater.inflate(R.layout.item_playlist, parent, false))
    }

    override fun onBindViewHolder(holder: PlaylistViewHolder, position: Int) {
        val (playlist, coverUrl) = playlists[position]
        GlideUtil.load(context, coverUrl, holder.cover)
        holder.title.text = playlist.name
        holder.itemView.setOnClickListener(playlistClickListener(playlist))
        holder.itemView.setOnLongClickListener(playlistLongClickListener(playlist))
    }

    private fun playlistClickListener(playlist: Playlist): View.OnClickListener {
        return View.OnClickListener {
            val intent = Intent(context, SongListActivity::class.java).apply {
                putExtra(SongListActivity.KEY_INTENT_TYPE, SongListActivity.INTENT_TYPE_PLAYLIST)
                putExtra(SongListActivity.KEY_INTENT_PLAYLIST_DATA, playlist)
            }
            context.startActivity(intent)
        }
    }

    private fun playlistLongClickListener(playlist: Playlist): View.OnLongClickListener {
        return View.OnLongClickListener { view ->
            PopupMenu(context, view).apply {
                menuInflater.inflate(R.menu.song_more_menu, menu)
                setOnMenuItemClickListener { menuItem ->
                    val musicDB = MusicDB(context)
                    val songs = musicDB.getSongsById(playlist.songs)
                    musicDB.close()
                    when (menuItem.itemId) {
                        R.id.songMorePlayNext -> viewModel.let {
                            it.addQueueNext(songs)
                            Toast.makeText(
                                context.applicationContext,
                                R.string.play_next_playlist,
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        R.id.songMoreAddQueue -> viewModel.let {
                            it.addQueueLast(songs)
                            Toast.makeText(
                                context.applicationContext,
                                R.string.add_queue_playlist,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                    true
                }
                show()
            }
            true
        }
    }

    override fun getItemCount(): Int {
        return playlists.size
    }

    class PlaylistViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cover: SquareImageView = itemView.findViewById(R.id.playlistCover)
        val title: TextView = itemView.findViewById(R.id.playlistTitle)
    }

}
