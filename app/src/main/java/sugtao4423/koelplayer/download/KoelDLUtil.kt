package sugtao4423.koelplayer.download

import android.content.Context
import sugtao4423.koel4j.dataclass.Song
import java.io.File
import java.text.DecimalFormat

class KoelDLUtil(context: Context) {

    private val downloadDirectory: File = context.getExternalFilesDir(null) ?: context.filesDir

    fun getSongFilePath(song: Song): String {
        return "$downloadDirectory/${song.id}"
    }

    fun isDownloaded(song: Song): Boolean {
        return File(getSongFilePath(song)).exists()
    }

    fun getSongFilesSize(songs: List<Song>): String {
        var size = 0L
        songs.forEach {
            size += File(getSongFilePath(it)).length()
        }
        val fileSizeMb = size / 1024f / 1024f
        val fileSizeGb = fileSizeMb / 1024f
        return DecimalFormat("0.00").let {
            if (fileSizeMb > 1024) it.format(fileSizeGb) + "GB" else it.format(fileSizeMb) + "MB"
        }
    }

    fun deleteUnusedMusicFiles(songs: List<Song>) {
        val deleteFiles = ArrayList(downloadDirectory.listFiles()!!.filterNotNull().map { it.absolutePath })
        songs.forEach {
            val songFilePath = getSongFilePath(it)
            deleteFiles.remove(songFilePath)
        }
        deleteFiles.forEach {
            File(it).delete()
        }
    }

}