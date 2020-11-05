package sugtao4423.koelplayer

import android.content.Context
import androidx.preference.PreferenceManager

class Prefs(context: Context) {

    companion object {
        private const val KEY_KOEL_SERVER = "koelServer"
        private const val KEY_KOEL_TOKEN = "koelToken"
    }

    private val pref = PreferenceManager.getDefaultSharedPreferences(context)

    var koelServer: String
        get() = pref.getString(KEY_KOEL_SERVER, "")!!
        set(value) = pref.edit().putString(KEY_KOEL_SERVER, value).apply()

    var koelToken: String
        get() = pref.getString(KEY_KOEL_TOKEN, "")!!
        set(value) = pref.edit().putString(KEY_KOEL_TOKEN, value).apply()

}