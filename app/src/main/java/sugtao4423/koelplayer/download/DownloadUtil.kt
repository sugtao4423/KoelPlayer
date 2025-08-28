package sugtao4423.koelplayer.download

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.NoOpCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.exoplayer.offline.DownloadManager
import sugtao4423.koel4j.Koel4j
import java.io.File
import java.util.concurrent.Executors

@OptIn(UnstableApi::class)
object DownloadUtil {

    private const val DOWNLOAD_CONTENT_DIRECTORY = "downloads"

    private var databaseProvider: StandaloneDatabaseProvider? = null
    private var downloadCache: SimpleCache? = null
    private var downloadManager: DownloadManager? = null

    fun getDatabaseProvider(context: Context): StandaloneDatabaseProvider {
        if (databaseProvider == null) {
            synchronized(this) {
                databaseProvider = StandaloneDatabaseProvider(context.applicationContext)
            }
        }
        return databaseProvider!!
    }

    fun getDownloadCache(context: Context): SimpleCache {
        if (downloadCache == null) {
            synchronized(this) {
                downloadCache = createDownloadCache(context.applicationContext)
            }
        }
        return downloadCache!!
    }

    fun getDownloadManager(context: Context): DownloadManager {
        if (downloadManager == null) {
            synchronized(this) {
                downloadManager = createDownloadManager(context.applicationContext)
            }
        }
        return downloadManager!!
    }

    private fun createDownloadCache(context: Context): SimpleCache {
        val downloadDirectory = context.getExternalFilesDir(null) ?: context.filesDir
        val downloadContentDirectory = File(downloadDirectory, DOWNLOAD_CONTENT_DIRECTORY)
        return SimpleCache(
            downloadContentDirectory, NoOpCacheEvictor(), getDatabaseProvider(context)
        )
    }

    private fun createDownloadManager(context: Context): DownloadManager {
        val dataSourceFactory = DefaultHttpDataSource.Factory().let {
            it.setUserAgent(Koel4j.USER_AGENT)
            it.setAllowCrossProtocolRedirects(true)
        }

        val downloadExecutor = Executors.newFixedThreadPool(3)

        return DownloadManager(
            context,
            getDatabaseProvider(context),
            getDownloadCache(context),
            dataSourceFactory,
            downloadExecutor
        ).apply {
            maxParallelDownloads = 3
        }
    }

}
