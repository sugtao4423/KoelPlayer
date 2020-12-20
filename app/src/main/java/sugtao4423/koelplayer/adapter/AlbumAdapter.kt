package sugtao4423.koelplayer.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import sugtao4423.koel4j.dataclass.Album
import sugtao4423.koelplayer.GlideUtil
import sugtao4423.koelplayer.R
import sugtao4423.koelplayer.SongListActivity

class AlbumAdapter(
    private val context: Context,
    private val albums: List<Album>,
) :
    RecyclerView.Adapter<AlbumAdapter.AlbumViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlbumViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return AlbumViewHolder(inflater.inflate(R.layout.item_album, parent, false))
    }

    override fun onBindViewHolder(holder: AlbumViewHolder, position: Int) {
        val album = albums[position]
        GlideUtil.load(context, album.cover, holder.image)
        holder.title.text = album.name
        holder.artist.text = album.artist.name
        holder.itemView.setOnClickListener(albumClickListener(album))
    }

    private fun albumClickListener(album: Album): View.OnClickListener {
        return View.OnClickListener {
            val intent = Intent(context, SongListActivity::class.java).apply {
                putExtra(SongListActivity.KEY_INTENT_TYPE, SongListActivity.INTENT_TYPE_ALBUM)
                putExtra(SongListActivity.KEY_INTENT_ALBUM_DATA, album)
            }
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return albums.size
    }

    class AlbumViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image: ImageView = itemView.findViewById(R.id.albumCover)
        val title: TextView = itemView.findViewById(R.id.albumTitle)
        val artist: TextView = itemView.findViewById(R.id.albumArtist)
    }

}