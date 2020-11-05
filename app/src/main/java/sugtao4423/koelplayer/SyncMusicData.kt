package sugtao4423.koelplayer

import android.app.ProgressDialog
import android.content.Context
import androidx.appcompat.app.AlertDialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import sugtao4423.koel4j.Koel4j
import sugtao4423.koelplayer.musicdb.MusicDB

class SyncMusicData(private val context: Context) {

    fun sync(completeCallBack: () -> Unit) {
        CoroutineScope(Dispatchers.Main).launch {
            val progressDialog = loadingDialog()
            progressDialog.show()

            val server = Prefs(context).koelServer
            val token = Prefs(context).koelToken
            val allMusicData = withContext(Dispatchers.IO) {
                Koel4j(server, token).allMusicData()
            }
            if (allMusicData == null) {
                errorGetAllMusicData()
                return@launch
            }
            withContext(Dispatchers.IO) {
                val musicDB = MusicDB(context)
                musicDB.resetDatabase()
                musicDB.saveAllMusicData(allMusicData)
                musicDB.close()
            }
            progressDialog.dismiss()
            completeCallBack()
        }
    }

    private fun loadingDialog(): ProgressDialog {
        return ProgressDialog(context).apply {
            setMessage(context.getString(R.string.loading))
            isIndeterminate = false
            setProgressStyle(ProgressDialog.STYLE_SPINNER)
            setCancelable(false)
        }
    }

    private fun errorGetAllMusicData() {
        AlertDialog.Builder(context).apply {
            setMessage(R.string.error_get_all_music_data)
            show()
        }
    }

}