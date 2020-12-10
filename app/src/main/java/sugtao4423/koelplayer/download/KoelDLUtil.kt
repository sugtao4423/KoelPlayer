package sugtao4423.koelplayer.download

import android.content.Context
import sugtao4423.koel4j.dataclass.Song
import java.io.File

class KoelDLUtil(context: Context) {

    private val downloadDirectory: File = context.getExternalFilesDir(null) ?: context.filesDir

    fun getSongFilePath(song: Song): String {
        return "$downloadDirectory/${song.id}"
    }

    fun isDownloaded(song: Song): Boolean {
        return File(getSongFilePath(song)).exists()
    }

}