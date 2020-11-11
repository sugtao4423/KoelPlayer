package sugtao4423.koelplayer

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_server_settings.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import sugtao4423.koel4j.Koel4j

class ServerSettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_server_settings)
        fab.setOnClickListener {
            saveKoelToken()
        }
    }

    private fun saveKoelToken() {
        val host = serverHost.text.toString().let {
            if (it.endsWith("/")) {
                it.removeSuffix("/")
            } else {
                it
            }
        }
        val email = serverEmail.text.toString()
        val password = serverPassword.text.toString()

        CoroutineScope(Dispatchers.Main).launch {
            val token = withContext(Dispatchers.IO) {
                Koel4j(host).auth(email, password)
            }
            if (token == null) {
                errorGetToken()
                return@launch
            }
            Prefs(this@ServerSettingsActivity).koelServer = host
            Prefs(this@ServerSettingsActivity).koelToken = token
            SyncMusicData(this@ServerSettingsActivity).sync {
                startActivity(Intent(this@ServerSettingsActivity, MainActivity::class.java))
                finish()
            }
        }
    }

    private fun errorGetToken() {
        AlertDialog.Builder(this).apply {
            setMessage(R.string.error_get_token)
            show()
        }
    }

    fun hideKeyboard(v: View) {
        val inputManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputManager.hideSoftInputFromWindow(v.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
    }

}