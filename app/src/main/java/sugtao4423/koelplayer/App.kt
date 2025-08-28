package sugtao4423.koelplayer

import android.app.Application
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import sugtao4423.koel4j.dataclass.Playlist

class App : Application() {

    companion object {
        private const val PREF_KEY_KOEL_SERVER = "koelServer"
        private const val PREF_KEY_KOEL_TOKEN = "koelToken"
    }

    private val pref: SharedPreferences by lazy {
        PreferenceManager.getDefaultSharedPreferences(applicationContext)
    }

    var koelServer: String
        get() = pref.getString(PREF_KEY_KOEL_SERVER, "") ?: ""
        set(value) = pref.edit { putString(PREF_KEY_KOEL_SERVER, value) }

    var koelToken: String
        get() = pref.getString(PREF_KEY_KOEL_TOKEN, "") ?: ""
        set(value) = pref.edit { putString(PREF_KEY_KOEL_TOKEN, value) }

    var reloadedAllMusicData = false

    private fun getPlaylistSortOrderKey(playlist: Playlist): String {
        return "playlistOrder_${playlist.id}"
    }

    fun getPlaylistSortOrder(playlist: Playlist): Int {
        return pref.getInt(getPlaylistSortOrderKey(playlist), 0)
    }

    fun setPlaylistSortOrder(playlist: Playlist, order: Int) {
        pref.edit { putInt(getPlaylistSortOrderKey(playlist), order) }
    }

    fun clearPlaylistOrderSettings() {
        val server = koelServer
        val token = koelToken
        pref.edit { clear() }
        koelServer = server
        koelToken = token
    }

}
