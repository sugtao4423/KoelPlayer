package sugtao4423.koel4j

import org.json.JSONArray
import org.json.JSONObject
import sugtao4423.koel4j.dataclass.Album
import sugtao4423.koel4j.dataclass.Artist
import sugtao4423.koel4j.dataclass.Song
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object KoelParser {

    private const val DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"

    private val VARIOUS_ARTISTS = Artist("various-artists", "Various Artists", null)

    private val UNKNOWN_ARTIST = Artist("unknown-artist", "Unknown Artist", null)

    private val UNKNOWN_ALBUM = Album(
        "unknown-album", UNKNOWN_ARTIST, "Unknown Album", null, Date(0), false
    )

    private fun JSONObject.nullString(key: String): String? {
        return if (isNull(key)) null else getString(key)
    }

    fun artists(json: JSONArray): List<Artist> {
        val result = ArrayList<Artist>()
        result.add(VARIOUS_ARTISTS)
        result.add(UNKNOWN_ARTIST)

        for (i in 0 until json.length()) {
            val obj = json.getJSONObject(i)
            val id = obj.getString("id")
            val name = obj.getString("name")
            val image = obj.nullString("image")
            val artist = Artist(id, name, image)
            result.add(artist)
        }
        return result
    }

    fun albums(json: JSONArray, artists: List<Artist>): List<Album> {
        val result = ArrayList<Album>()
        result.add(UNKNOWN_ALBUM)

        val sdf = SimpleDateFormat(DATE_FORMAT, Locale.getDefault())
        for (i in 0 until json.length()) {
            val obj = json.getJSONObject(i)
            val id = obj.getString("id")
            val artistId = obj.getString("artist_id")
            val artistName = obj.getString("artist_name")
            val name = obj.getString("name")
            val cover = obj.nullString("cover")
            val createdAt = sdf.parse(obj.getString("created_at"))!!
            val isCompilation = artistName == VARIOUS_ARTISTS.name

            val artist = if (isCompilation) {
                VARIOUS_ARTISTS
            } else {
                artists.find { it.id == artistId } ?: UNKNOWN_ARTIST
            }
            val album = Album(id, artist, name, cover, createdAt, isCompilation)
            result.add(album)
        }
        return result
    }

    fun songs(json: JSONArray, artists: List<Artist>, albums: List<Album>): List<Song> {
        val result = ArrayList<Song>()
        val sdf = SimpleDateFormat(DATE_FORMAT, Locale.getDefault())
        for (i in 0 until json.length()) {
            val obj = json.getJSONObject(i)
            val id = obj.getString("id")
            val albumId = obj.getString("album_id")
            val artistId = obj.getString("artist_id")
            val title = obj.getString("title")
            val length = obj.getDouble("length")
            val track = obj.getInt("track")
            val disc = obj.getInt("disc")
            val createdAt = sdf.parse(obj.getString("created_at"))!!

            val album = albums.find { it.id == albumId } ?: UNKNOWN_ALBUM
            val artist = artists.find { it.id == artistId } ?: UNKNOWN_ARTIST
            val song = Song(id, album, artist, title, length, track, disc, createdAt)
            result.add(song)
        }
        return result
    }

}
