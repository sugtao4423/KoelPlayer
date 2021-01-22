package sugtao4423.koelplayer.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import sugtao4423.koel4j.dataclass.Album
import sugtao4423.koelplayer.GlideUtil
import sugtao4423.koelplayer.R
import sugtao4423.koelplayer.SongListActivity
import sugtao4423.koelplayer.musicdb.MusicDB
import sugtao4423.koelplayer.playmusic.MusicService

class AlbumAdapter : RecyclerView.Adapter<AlbumAdapter.AlbumViewHolder>() {

    var musicService: MusicService? = null

    var albums = listOf<Album>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    private lateinit var context: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlbumViewHolder {
        context = parent.context
        val inflater = LayoutInflater.from(parent.context)
        return AlbumViewHolder(inflater.inflate(R.layout.item_album, parent, false))
    }

    override fun onBindViewHolder(holder: AlbumViewHolder, position: Int) {
        val album = albums[position]
        GlideUtil.load(context, album.cover, holder.image)
        holder.title.text = album.name
        holder.artist.text = album.artist.name
        holder.itemView.setOnClickListener(albumClickListener(album))
        holder.itemView.setOnLongClickListener(albumLongClickListener(album))
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

    private fun albumLongClickListener(album: Album): View.OnLongClickListener {
        return View.OnLongClickListener { view ->
            PopupMenu(context, view).apply {
                menuInflater.inflate(R.menu.song_more_menu, menu)
                setOnMenuItemClickListener { menuItem ->
                    val musicDB = MusicDB(context)
                    val songs = musicDB.getAlbumSongs(album.id)
                    musicDB.close()
                    when (menuItem.itemId) {
                        R.id.songMorePlayNext -> musicService?.let {
                            it.addQueueNext(songs)
                            Toast.makeText(context.applicationContext, R.string.play_next_album, Toast.LENGTH_SHORT).show()
                        }
                        R.id.songMoreAddQueue -> musicService?.let {
                            it.addQueueLast(songs)
                            Toast.makeText(context.applicationContext, R.string.add_queue_album, Toast.LENGTH_SHORT).show()
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
        return albums.size
    }

    class AlbumViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image: ImageView = itemView.findViewById(R.id.albumCover)
        val title: TextView = itemView.findViewById(R.id.albumTitle)
        val artist: TextView = itemView.findViewById(R.id.albumArtist)
    }

}