package sugtao4423.koelplayer

import android.app.Application
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import sugtao4423.koel4j.dataclass.Playlist

class App : Application() {

    companion object {
        private const val PREF_KEY_KOEL_SERVER = "koelServer"
        private const val PREF_KEY_KOEL_TOKEN = "koelToken"
    }

    private lateinit var pref: SharedPreferences

    override fun onCreate() {
        super.onCreate()
        pref = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        reloadServerSettings()
    }

    private lateinit var _koelServer: String
    private lateinit var _koelToken: String

    var koelServer: String
        get() = _koelServer
        set(value) {
            pref.edit().putString(PREF_KEY_KOEL_SERVER, value).apply()
            _koelServer = value
        }

    var koelToken: String
        get() = _koelToken
        set(value) {
            pref.edit().putString(PREF_KEY_KOEL_TOKEN, value).apply()
            _koelToken = value
        }

    fun reloadServerSettings() {
        _koelServer = pref.getString(PREF_KEY_KOEL_SERVER, "") ?: ""
        _koelToken = pref.getString(PREF_KEY_KOEL_TOKEN, "") ?: ""
    }

    var reloadedAllMusicData = false

    private fun getPlaylistSortOrderKey(playlist: Playlist): String {
        return "playlistOrder_${playlist.id}"
    }

    fun getPlaylistSortOrder(playlist: Playlist): Int {
        return pref.getInt(getPlaylistSortOrderKey(playlist), 0)
    }

    fun setPlaylistSortOrder(playlist: Playlist, order: Int) {
        pref.edit().putInt(getPlaylistSortOrderKey(playlist), order).apply()
    }

    fun clearPlaylistOrderSettings() {
        val server = koelServer
        val token = koelToken
        pref.edit().clear().apply()
        koelServer = server
        koelToken = token
    }

}