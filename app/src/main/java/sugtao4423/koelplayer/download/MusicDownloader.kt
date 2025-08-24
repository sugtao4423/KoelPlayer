package sugtao4423.koelplayer.download

import android.content.Context
import androidx.annotation.OptIn
import androidx.core.net.toUri
import androidx.media3.common.MimeTypes
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadRequest
import androidx.media3.exoplayer.offline.DownloadService
import sugtao4423.koel4j.KoelEndpoints
import sugtao4423.koel4j.dataclass.Song
import sugtao4423.koelplayer.App
import java.io.IOException

@OptIn(UnstableApi::class)
class MusicDownloader(private val context: Context) {

    private val downloadManager = DownloadUtil.getDownloadManager(context)

    fun downloadSong(song: Song) {
        val app = context.applicationContext as App
        val url = app.koelServer + KoelEndpoints.musicFile(app.koelToken, song.id)

        val downloadRequest = DownloadRequest.Builder(song.id, url.toUri()).let {
            it.setMimeType(MimeTypes.AUDIO_UNKNOWN)
            it.setData(song.title.toByteArray())
            it.build()
        }

        DownloadService.sendAddDownload(
            context, MusicDownloadService::class.java, downloadRequest, false
        )
    }

    fun downloadSongs(songs: List<Song>) = songs.forEach { downloadSong(it) }

    fun isDownloaded(songId: String): Boolean = try {
        val download = downloadManager.downloadIndex.getDownload(songId)
        download?.state == Download.STATE_COMPLETED
    } catch (_: IOException) {
        false
    }

    fun getDownloadedFileSize(songId: String): Long = try {
        val download = downloadManager.downloadIndex.getDownload(songId)
        download?.bytesDownloaded ?: 0
    } catch (_: IOException) {
        0
    }

    private fun getAllDownloads(): List<Download> {
        val downloads = mutableListOf<Download>()
        try {
            val cursor = downloadManager.downloadIndex.getDownloads()
            cursor.use {
                while (it.moveToNext()) {
                    downloads.add(it.download)
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return downloads
    }

    fun getAllDownloadedFileSize(): Long = getAllDownloads().sumOf { it.bytesDownloaded }

    fun deleteUnusedSongFiles(newSongIds: List<String>) {
        val downloadedSongIds = getAllDownloads().map { it.request.id }.toSet()
        val newSongIds = newSongIds.toSet()
        val unusedSongIds = downloadedSongIds - newSongIds

        unusedSongIds.forEach {
            DownloadService.sendRemoveDownload(context, MusicDownloadService::class.java, it, false)
        }
    }

    fun deleteAllDownloadedSongFiles() {
        DownloadService.sendRemoveAllDownloads(context, MusicDownloadService::class.java, false)
    }

}
