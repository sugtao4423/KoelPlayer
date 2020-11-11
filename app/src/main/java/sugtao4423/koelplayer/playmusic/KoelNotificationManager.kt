package sugtao4423.koelplayer.playmusic

import android.app.PendingIntent
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import com.bumptech.glide.Glide
import com.google.android.exoplayer2.DefaultControlDispatcher
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import kotlinx.coroutines.*
import sugtao4423.koelplayer.R

class KoelNotificationManager(
    private val context: Context,
    sessionToken: MediaSessionCompat.Token,
    notificationListener: PlayerNotificationManager.NotificationListener
) {

    companion object {
        const val NOW_PLAYING_CHANNEL_ID = "sugtao4423.koelplayer.NOW_PLAYING"
        const val NOW_PLAYING_NOTIFICATION_ID = 1145141919

        const val NOTIFICATION_LARGE_ICON_SIZE = 144
    }

    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private val notificationManager: PlayerNotificationManager

    init {
        val mediaController = MediaControllerCompat(context, sessionToken)

        notificationManager = PlayerNotificationManager.createWithNotificationChannel(
            context,
            NOW_PLAYING_CHANNEL_ID,
            R.string.notification_channel,
            R.string.notification_channel_description,
            NOW_PLAYING_NOTIFICATION_ID,
            DescriptionAdapter(mediaController),
            notificationListener
        ).apply {
            setUseStopAction(true)
            setMediaSessionToken(sessionToken)
            setSmallIcon(R.drawable.ic_launcher_foreground)
            setControlDispatcher(DefaultControlDispatcher(0, 0))
        }
    }

    fun setPlayer(player: Player?) {
        notificationManager.setPlayer(player)
    }

    private inner class DescriptionAdapter(private val controller: MediaControllerCompat) : PlayerNotificationManager.MediaDescriptionAdapter {

        var currentIconUri: Uri? = null
        var currentBitmap: Bitmap? = null

        override fun createCurrentContentIntent(player: Player): PendingIntent? = controller.sessionActivity
        override fun getCurrentContentText(player: Player) = controller.metadata.description.subtitle.toString()
        override fun getCurrentContentTitle(player: Player) = controller.metadata.description.title.toString()

        override fun getCurrentLargeIcon(player: Player, callback: PlayerNotificationManager.BitmapCallback): Bitmap? {
            val iconUri = controller.metadata.description.iconUri
            return if (currentIconUri != iconUri || currentBitmap == null) {
                currentIconUri = iconUri
                serviceScope.launch {
                    currentBitmap = iconUri?.let {
                        resolveUriAsBitmap(it)
                    }
                    currentBitmap?.let { callback.onBitmap(it) }
                }
                null
            } else {
                currentBitmap
            }
        }

        private suspend fun resolveUriAsBitmap(uri: Uri): Bitmap? {
            return withContext(Dispatchers.IO) {
                Glide.with(context).asBitmap().load(uri).submit(NOTIFICATION_LARGE_ICON_SIZE, NOTIFICATION_LARGE_ICON_SIZE).get()
            }
        }
    }

}