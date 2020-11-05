package sugtao4423.koel4j

import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import sugtao4423.koel4j.dataclass.AllMusicData
import sugtao4423.koel4j.dataclass.Playlist
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

    fun auth(email: String, password: String): String? {
        val endpoint = KoelEndpoints.authentication
        val body = mapOf(
            "email" to email,
            "password" to password
        )
        val json = postKoelApi(endpoint, body)
        return json?.getString("token")
    }

    fun allMusicData(): AllMusicData? {
        val endpoint = KoelEndpoints.applicationData
        val json = getKoelApi(endpoint) ?: return null
        val jsonPlaylists = json.getJSONArray("playlists")
        val playlists = ArrayList<Playlist>()
        for (i in 0 until jsonPlaylists.length()) {
            val obj = jsonPlaylists.getJSONObject(i)
            val id = obj.getInt("id")
            val name = obj.getString("name")
            val playlist = getPlaylistData(id, name) ?: return null
            playlists.add(playlist)
        }
        return ParseAllMusicData(json, playlists).parse()
    }

    private fun getPlaylistData(id: Int, name: String): Playlist? {
        val endpoint = KoelEndpoints.playlistData(id)
        val json = getKoelApi(endpoint, true) ?: return null
        val songIds = json.getJSONArray("array")
        val songs = ArrayList<String>()
        for (i in 0 until songIds.length()) {
            val songId = songIds.getString(i)
            songs.add(songId)
        }
        return Playlist(id, name, songs)
    }

    private fun getKoelApi(endpoint: String, isResultArray: Boolean = false): JSONObject? {
        return accessKoelApi("GET", endpoint, mapOf(), isResultArray)
    }

    private fun postKoelApi(
        endpoint: String,
        body: Map<String, String> = mapOf(),
        isResultArray: Boolean = false
    ): JSONObject? {
        return accessKoelApi("POST", endpoint, body, isResultArray)
    }

    private fun accessKoelApi(
        method: String,
        endpoint: String,
        body: Map<String, String>,
        isResultArray: Boolean
    ): JSONObject? {
        val headers = mapOf(
            "Content-Type" to "application/json",
            "Accept" to "application/json",
            "Authorization" to "Bearer $token",
            "User-Agent" to USER_AGENT
        )
        val request = Request.Builder().let {
            headers.map { header ->
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
        return try {
            val response = OkHttpClient().newCall(request).execute()
            if (response.isSuccessful) {
                response.body?.let {
                    return if (isResultArray) {
                        val array = JSONArray(it.string()).toString()
                        JSONObject("{\"array\": $array}")
                    } else {
                        JSONObject(it.string())
                    }
                }
            }
            null
        } catch (e: IOException) {
            null
        }
    }

}