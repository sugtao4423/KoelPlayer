package sugtao4423.koelplayer

import android.app.Application
import android.content.SharedPreferences
import androidx.preference.PreferenceManager

class App : Application() {

    companion object {
        private const val PREF_KEY_KOEL_SERVER = "koelServer"
        private const val PREF_KEY_KOEL_TOKEN = "koelToken"
    }

    private lateinit var pref: SharedPreferences

    override fun onCreate() {
        super.onCreate()
        pref = PreferenceManager.getDefaultSharedPreferences(applicationContext)

        _koelServer = pref.getString(PREF_KEY_KOEL_SERVER, "") ?: ""
        _koelToken = pref.getString(PREF_KEY_KOEL_TOKEN, "") ?: ""
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

}