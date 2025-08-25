package sugtao4423.koelplayer.download

import android.Manifest
import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import androidx.annotation.OptIn
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadManager
import androidx.media3.exoplayer.offline.DownloadService
import androidx.media3.exoplayer.scheduler.PlatformScheduler
import androidx.media3.exoplayer.scheduler.Scheduler
import sugtao4423.koelplayer.R
import sugtao4423.koelplayer.ui.activity.MainActivity

@OptIn(UnstableApi::class)
class MusicDownloadService : DownloadService(
    FOREGROUND_NOTIFICATION_ID,
    DEFAULT_FOREGROUND_NOTIFICATION_UPDATE_INTERVAL,
    DOWNLOAD_NOTIFICATION_CHANNEL_ID,
    R.string.download_notification_channel_name,
    R.string.download_notification_channel_name,
) {

    companion object {
        private const val JOB_ID = 364
        private const val FOREGROUND_NOTIFICATION_ID = 1919810
        private const val DOWNLOAD_NOTIFICATION_CHANNEL_ID = "download_channel"
    }

    override fun getDownloadManager(): DownloadManager = DownloadUtil.getDownloadManager(this)

    @RequiresPermission(Manifest.permission.RECEIVE_BOOT_COMPLETED)
    override fun getScheduler(): Scheduler? = PlatformScheduler(this, JOB_ID)

    override fun getForegroundNotification(
        downloads: List<Download>, notMetRequirements: Int
    ): Notification {
        val leftCountText = getString(R.string.param_download_left, downloads.size)

        val downloadingSong = downloads.find { it.state == Download.STATE_DOWNLOADING }
        val downloadingSongTitle = downloadingSong?.request?.data?.decodeToString()

        val appIntent = Intent(this, MainActivity::class.java).apply {
            action = Intent.ACTION_MAIN
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            FOREGROUND_NOTIFICATION_ID,
            appIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, DOWNLOAD_NOTIFICATION_CHANNEL_ID).let {
            it.setContentTitle(leftCountText)
            it.setContentText(downloadingSongTitle ?: "")
            it.setProgress(0, 0, true)
            it.setSmallIcon(android.R.drawable.stat_sys_download)
            it.setContentIntent(pendingIntent)
            it.setOngoing(true)
            it.setWhen(System.currentTimeMillis())
        }.build()
    }

}
