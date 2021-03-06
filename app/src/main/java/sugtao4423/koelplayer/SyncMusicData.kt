package sugtao4423.koelplayer

import android.app.ProgressDialog
import android.content.Context
import androidx.appcompat.app.AlertDialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import sugtao4423.koel4j.Koel4j
import sugtao4423.koel4j.dataclass.Song
import sugtao4423.koelplayer.download.KoelDLUtil
import sugtao4423.koelplayer.musicdb.MusicDB

class SyncMusicData(private val context: Context) {

    fun sync(completeCallBack: (() -> Unit)? = null) {
        CoroutineScope(Dispatchers.Main).launch {
            val progressDialog = loadingDialog()
            progressDialog.show()

            val server = (context.applicationContext as App).koelServer
            val token = (context.applicationContext as App).koelToken
            val allMusicData = withContext(Dispatchers.IO) {
                Koel4j(server, token).allMusicData()
            }
            (context.applicationContext as App).clearPlaylistOrderSettings()
            if (allMusicData == null) {
                progressDialog.dismiss()
                errorGetAllMusicData()
                return@launch
            }
            withContext(Dispatchers.IO) {
                val musicDB = MusicDB(context)
                musicDB.resetDatabase()
                musicDB.saveAllMusicData(allMusicData)
                musicDB.close()
                syncDownloadedMusicFiles(allMusicData.songs)
            }
            progressDialog.dismiss()
            completeCallBack?.invoke()
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

    private fun syncDownloadedMusicFiles(songs: List<Song>) {
        KoelDLUtil(context).deleteUnusedMusicFiles(songs)
    }

}