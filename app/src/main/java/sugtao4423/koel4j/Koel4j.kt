package sugtao4423.koel4j

import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import sugtao4423.koel4j.dataclass.Album
import sugtao4423.koel4j.dataclass.AllMusicData
import sugtao4423.koel4j.dataclass.Artist
import sugtao4423.koel4j.dataclass.Playlist
import sugtao4423.koel4j.dataclass.Song
import java.io.IOException

class Koel4j(private var host: String, private val token: String = "") {

    init {
        if (host.endsWith("/")) {
            host = host.removeSuffix("/")
        }
    }

    companion object {
        const val USER_AGENT = "Android KoelPlayer"
    }

    @Throws(IOException::class, JSONException::class)
    fun auth(email: String, password: String): String {
        val endpoint = KoelEndpoints.AUTHENTICATION
        val body = mapOf(
            "email" to email,
            "password" to password,
        )
        val json = postKoelApi(endpoint, body)
        return json.getString("token")
    }

    @Throws(IOException::class, JSONException::class)
    fun allMusicData(): AllMusicData {
        val artists = getArtists()
        val albums = getAlbums(artists)
        val songs = getSongs(artists, albums)
        val playlists = getPlaylists()

        return AllMusicData(albums, artists, songs, playlists)
    }

    @Throws(IOException::class, JSONException::class)
    fun getArtists(): List<Artist> {
        val endpoint = KoelEndpoints.ARTISTS
        val json = getPaginatedData(endpoint)
        return KoelParser.artists(json)
    }

    @Throws(IOException::class, JSONException::class)
    fun getAlbums(artists: List<Artist>): List<Album> {
        val endpoint = KoelEndpoints.ALBUMS
        val json = getPaginatedData(endpoint)
        return KoelParser.albums(json, artists)
    }

    @Throws(IOException::class, JSONException::class)
    fun getSongs(artists: List<Artist>, albums: List<Album>): List<Song> {
        val endpoint = KoelEndpoints.SONGS
        val json = getPaginatedData(endpoint)
        return KoelParser.songs(json, artists, albums)
    }

    @Throws(IOException::class, JSONException::class)
    fun getPlaylists(): List<Playlist> {
        val endpoint = KoelEndpoints.PLAYLISTS
        val json = getKoelApi(endpoint, true)
        val playlistArray = json.getJSONArray("array")

        val result = ArrayList<Playlist>()
        for (i in 0 until playlistArray.length()) {
            val obj = playlistArray.getJSONObject(i)
            val id = obj.getString("id")
            val name = obj.getString("name")
            val playlist = getPlaylistData(id, name)
            result.add(playlist)
        }
        return result
    }

    @Throws(IOException::class, JSONException::class)
    private fun getPlaylistData(id: String, name: String): Playlist {
        val endpoint = KoelEndpoints.playlistData(id)
        val json = getKoelApi(endpoint, true)
        val songArray = json.getJSONArray("array")
        val songs = ArrayList<String>()
        for (i in 0 until songArray.length()) {
            val song = songArray.getJSONObject(i)
            val songId = song.getString("id")
            songs.add(songId)
        }
        return Playlist(id, name, songs)
    }

    @Throws(IOException::class, JSONException::class)
    private fun getPaginatedData(endpoint: String): JSONArray {
        val result = JSONArray()

        var page: Int? = 1
        while (page != null) {
            val json = getKoelApi("$endpoint?page=$page")
            val data = json.getJSONArray("data")
            for (i in 0 until data.length()) {
                result.put(data.getJSONObject(i))
            }

            val links = json.getJSONObject("links")
            val hasNext = !links.isNull("next")
            page = if (hasNext) page + 1 else null
        }

        return result
    }

    @Throws(IOException::class, JSONException::class)
    private fun getKoelApi(endpoint: String, isResultArray: Boolean = false): JSONObject {
        return accessKoelApi("GET", endpoint, mapOf(), isResultArray)
    }

    @Throws(IOException::class, JSONException::class)
    private fun postKoelApi(
        endpoint: String, body: Map<String, String> = mapOf(), isResultArray: Boolean = false
    ): JSONObject {
        return accessKoelApi("POST", endpoint, body, isResultArray)
    }

    @Throws(IOException::class, JSONException::class)
    private fun accessKoelApi(
        method: String, endpoint: String, body: Map<String, String>, isResultArray: Boolean
    ): JSONObject {
        val headers = mapOf(
            "Content-Type" to "application/json",
            "Accept" to "application/json",
            "Authorization" to "Bearer $token",
            "User-Agent" to USER_AGENT
        )
        val request = Request.Builder().let {
            headers.forEach { header ->
                it.addHeader(header.key, header.value)
            }
            it.url(host + endpoint)
            when (method) {
                "GET" -> it.get()
                "POST" -> {
                    val json = JSONObject(body).toString()
                    val requestBody =
                        json.toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
                    it.post(requestBody)
                }
            }
            it.build()
        }

        val response = OkHttpClient().newCall(request).execute()
        if (!response.isSuccessful) {
            throw IOException("Unexpected code ${response.code}: ${response.message}")
        }

        return response.body.string().let {
            if (isResultArray) {
                val array = JSONArray(it).toString()
                JSONObject("{\"array\": $array}")
            } else {
                JSONObject(it)
            }
        }
    }

}
