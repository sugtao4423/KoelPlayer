package sugtao4423.koelplayer.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import sugtao4423.koel4j.dataclass.Album
import sugtao4423.koelplayer.AlbumDetailActivity
import sugtao4423.koelplayer.R

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
        if (album.cover.endsWith("unknown-album.png")) {
            Glide.with(context).load(R.drawable.unknown_album).into(holder.image)
        } else {
            Glide.with(context).load(album.cover).into(holder.image)
        }
        holder.title.text = album.name
        holder.artist.text = album.artist.name
        holder.itemView.setOnClickListener(albumClickListener(album))
    }

    private fun albumClickListener(album: Album): View.OnClickListener {
        return View.OnClickListener {
            val intent = Intent(context, AlbumDetailActivity::class.java)
            intent.putExtra(AlbumDetailActivity.KEY_INTENT_ALBUM_DATA, album)
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