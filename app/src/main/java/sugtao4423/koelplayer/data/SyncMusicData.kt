package sugtao4423.koelplayer.data

import android.app.ProgressDialog
import android.content.Context
import androidx.appcompat.app.AlertDialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import sugtao4423.koel4j.Koel4j
import sugtao4423.koel4j.dataclass.Song
import sugtao4423.koelplayer.App
import sugtao4423.koelplayer.R
import sugtao4423.koelplayer.data.database.MusicDB
import sugtao4423.koelplayer.download.MusicDownloader

class SyncMusicData(private val context: Context) {

    fun sync(completeCallBack: (() -> Unit)? = null) {
        CoroutineScope(Dispatchers.Main).launch {
            val progressDialog = loadingDialog()
            progressDialog.show()

            val server = (context.applicationContext as App).koelServer
            val token = (context.applicationContext as App).koelToken
            val allMusicData = withContext(Dispatchers.IO) {
                runCatching {
                    Koel4j(server, token).allMusicData()
                }.getOrNull()
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
        val songIds = songs.map { it.id }
        MusicDownloader(context).deleteUnusedSongFiles(songIds)
    }

}
