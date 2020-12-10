package sugtao4423.koelplayer.download

import android.app.*
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import sugtao4423.koel4j.Koel4j
import sugtao4423.koel4j.KoelEndpoints
import sugtao4423.koel4j.dataclass.Song
import sugtao4423.koelplayer.MainActivity
import sugtao4423.koelplayer.Prefs
import sugtao4423.koelplayer.R
import java.io.File
import java.util.*

class KoelDLService : Service() {

    companion object {
        const val INTENT_KEY_SONGS = "songs"
        const val NOTIFICATION_ID = 1919810
        const val NOTIFICATION_CHANNEL_ID = "download"
        const val NOTIFICATION_TITLE_PROGRESS = "%d /%d  %s"
    }

    private val notificationManager by lazy {
        NotificationManagerCompat.from(applicationContext)
    }
    private lateinit var notification: Notification

    private val koelServer by lazy {
        Prefs(applicationContext).koelServer
    }
    private val koelToken by lazy {
        Prefs(applicationContext).koelToken
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent == null) {
            return super.onStartCommand(intent, flags, startId)
        }

        val songs = let {
            val objects = intent.getSerializableExtra(INTENT_KEY_SONGS) as Array<*>
            Arrays.copyOf(objects, objects.size, Array<Song>::class.java)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID, NOTIFICATION_CHANNEL_ID, NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }

        CoroutineScope(Dispatchers.Main).launch {
            notification = notificationBuilder(songs.size, 0).build()
            startForeground(NOTIFICATION_ID, notification)

            withContext(Dispatchers.IO) {
                songs.forEachIndexed { index, song ->
                    saveSong(index, songs.size, song)
                }
            }

            notification = notificationBuilder(-1, -1).let {
                it.setContentTitle(getString(R.string.download_done))
                it.setSmallIcon(android.R.drawable.stat_sys_download_done)
                it.setAutoCancel(true)
                it.build()
            }
            notificationManager.notify(NOTIFICATION_ID + 1, notification)

            stopSelf()
        }

        return START_NOT_STICKY
    }

    private fun saveSong(index: Int, maxSize: Int, song: Song) {
        notification = notificationBuilder(maxSize, index + 1, song.title).build()
        notificationManager.notify(NOTIFICATION_ID, notification)

        val url = koelServer + KoelEndpoints.musicFile(koelToken, song.id)
        val inputStream = let {
            val request = Request.Builder().let {
                it.addHeader("User-Agent", Koel4j.USER_AGENT)
                it.url(url)
                it.build()
            }
            OkHttpClient().newCall(request).execute().body!!.byteStream()
        }
        val downloadPath = KoelDLUtil(this).getSongFilePath(song)
        File(downloadPath).outputStream().use {
            inputStream.copyTo(it)
        }
    }

    private fun notificationBuilder(progressMax: Int, progress: Int, songTitle: String = ""): NotificationCompat.Builder {
        val appIntent = Intent(this, MainActivity::class.java).apply {
            action = Intent.ACTION_MAIN
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val pendingIntent = PendingIntent.getActivity(applicationContext, NOTIFICATION_ID, appIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        return NotificationCompat.Builder(applicationContext, NOTIFICATION_CHANNEL_ID).run {
            setContentTitle(NOTIFICATION_TITLE_PROGRESS.format(progress, progressMax, songTitle))
            if (progressMax >= 0 || progress >= 0) {
                setProgress(progressMax, progress, false)
            }
            setSmallIcon(android.R.drawable.stat_sys_download)
            setAutoCancel(false)
            setContentIntent(pendingIntent)
            setWhen(System.currentTimeMillis())
        }
    }

}