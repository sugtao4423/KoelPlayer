package sugtao4423.koel4j

import org.json.JSONObject
import sugtao4423.koel4j.dataclass.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class ParseAllMusicData(private val json: JSONObject, private val playlists: List<Playlist>) {

    fun parse(): AllMusicData {
        val albums = parseAlbums()
        val artists = parseArtists()
        val songs = parseSongs()
        return AllMusicData(albums, artists, songs, playlists)
    }

    private fun parseAlbums(): List<Album> {
        val result = ArrayList<Album>()
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val objects = json.getJSONArray("albums")
        for (i in 0 until objects.length()) {
            val obj = objects.getJSONObject(i)
            val id = obj.getInt("id")
            val artistId = obj.getInt("artist_id")
            val name = obj.getString("name")
            val cover = obj.getString("cover")
            val createdAt = sdf.parse(obj.getString("created_at"))!!
            val isCompilation = obj.getBoolean("is_compilation")
            val album = Album(id, artistId, name, cover, createdAt, isCompilation)
            result.add(album)
        }
        return result
    }

    private fun parseArtists(): List<Artist> {
        val result = ArrayList<Artist>()
        val objects = json.getJSONArray("artists")
        for (i in 0 until objects.length()) {
            val obj = objects.getJSONObject(i)
            val id = obj.getInt("id")
            val name = obj.getString("name")
            val image = obj.optString("image")
            val artist = Artist(id, name, image)
            result.add(artist)
        }
        return result
    }

    private fun parseSongs(): List<Song> {
        val result = ArrayList<Song>()
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val objects = json.getJSONArray("songs")
        for (i in 0 until objects.length()) {
            val obj = objects.getJSONObject(i)
            val id = obj.getString("id")
            val albumId = obj.getInt("album_id")
            val artistId = obj.getInt("artist_id")
            val title = obj.getString("title")
            val length = obj.getDouble("length")
            val track = obj.getInt("track")
            val disc = obj.getInt("disc")
            val createdAt = sdf.parse(obj.getString("created_at"))!!
            val song = Song(id, albumId, artistId, title, length, track, disc, createdAt)
            result.add(song)
        }
        return result
    }

}