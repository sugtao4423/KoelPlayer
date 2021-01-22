package sugtao4423.koel4j

import org.json.JSONObject
import sugtao4423.koel4j.dataclass.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class ParseAllMusicData(private val json: JSONObject, private val playlists: List<Playlist>) {

    companion object {
        private const val koelDateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
    }

    fun parse(): AllMusicData {
        val artists = parseArtists()
        val albums = parseAlbums(artists)
        val songs = parseSongs(artists, albums)
        return AllMusicData(albums, artists, songs, playlists)
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

    private fun parseAlbums(artists: List<Artist>): List<Album> {
        val result = ArrayList<Album>()
        val sdf = SimpleDateFormat(koelDateFormat, Locale.getDefault())
        val objects = json.getJSONArray("albums")
        for (i in 0 until objects.length()) {
            val obj = objects.getJSONObject(i)
            val id = obj.getInt("id")
            val artistId = obj.getInt("artist_id")
            val name = obj.getString("name")
            val cover = obj.getString("cover")
            val createdAt = sdf.parse(obj.getString("created_at"))!!
            val isCompilation = obj.getBoolean("is_compilation")

            val artist = artists.find { it.id == artistId }!!
            val album = Album(id, artist, name, cover, createdAt, isCompilation)
            result.add(album)
        }
        return result
    }

    private fun parseSongs(artists: List<Artist>, albums: List<Album>): List<Song> {
        val result = ArrayList<Song>()
        val sdf = SimpleDateFormat(koelDateFormat, Locale.getDefault())
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

            val album = albums.find { it.id == albumId }!!
            val artist = artists.find { it.id == artistId }!!
            val song = Song(id, album, artist, title, length, track, disc, createdAt)
            result.add(song)
        }
        return result
    }

}