package sugtao4423.koelplayer.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import sugtao4423.koel4j.dataclass.Playlist
import sugtao4423.koel4j.dataclass.Song
import sugtao4423.koelplayer.GlideUtil
import sugtao4423.koelplayer.PlaylistDetailActivity
import sugtao4423.koelplayer.R
import sugtao4423.koelplayer.view.SquareImageView

class PlaylistAdapter(
    private val context: Context,
    private val playlists: List<Playlist>,
    private val songs: List<Song>
) :
    RecyclerView.Adapter<PlaylistAdapter.PlaylistViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return PlaylistViewHolder(inflater.inflate(R.layout.item_playlist, parent, false))
    }

    override fun onBindViewHolder(holder: PlaylistViewHolder, position: Int) {
        val playlist = playlists[position]
        val firstSongCover = songs.find {
            it.id == playlist.songs[0]
        }!!.album.cover
        GlideUtil.load(context, firstSongCover, holder.cover)
        holder.title.text = playlist.name
        holder.itemView.setOnClickListener(playlistClickListener(playlist))
    }

    private fun playlistClickListener(playlist: Playlist): View.OnClickListener {
        return View.OnClickListener {
            val intent = Intent(context, PlaylistDetailActivity::class.java)
            intent.putExtra(PlaylistDetailActivity.KEY_INTENT_PLAYLIST_DATA, playlist)
            context.startActivity(intent)
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