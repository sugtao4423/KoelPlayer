package sugtao4423.koelplayer.music.service

import android.app.PendingIntent
import android.content.Intent
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.DefaultMediaNotificationProvider
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaSession
import sugtao4423.koelplayer.R
import sugtao4423.koelplayer.music.player.MusicPlayer
import sugtao4423.koelplayer.ui.activity.MainActivity

class MusicService : MediaLibraryService() {

    @OptIn(UnstableApi::class)
    private val notificationProvider by lazy {
        DefaultMediaNotificationProvider.Builder(applicationContext).apply {
            setChannelId("${packageName}.NOW_PLAYING")
            setChannelName(R.string.notification_channel_name)
            setNotificationId(1145141919)
        }.build()
    }

    private var mediaLibrarySession: MediaLibrarySession? = null

    @OptIn(UnstableApi::class)
    override fun onCreate() {
        super.onCreate()
        setMediaNotificationProvider(notificationProvider)
        val player = initExoPlayer()
        mediaLibrarySession = initMediaSession(player)
    }

    @OptIn(UnstableApi::class)
    private fun initExoPlayer(): MusicPlayer {
        val attr = AudioAttributes.Builder().run {
            setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            setUsage(C.USAGE_MEDIA)
            build()
        }

        val exoPlayer = ExoPlayer.Builder(this).run {
            setAudioAttributes(attr, true)
            setHandleAudioBecomingNoisy(true)
            setMaxSeekToPreviousPositionMs(3000L)
            build()
        }
        return MusicPlayer(exoPlayer)
    }

    private fun initMediaSession(player: MusicPlayer): MediaLibrarySession {
        val appIntent = Intent(this, MainActivity::class.java).apply {
            action = Intent.ACTION_MAIN
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val sessionActivityPendingIntent = PendingIntent.getActivity(
            this, 0, appIntent, PendingIntent.FLAG_UPDATE_CURRENT
        )

        return MediaLibrarySession.Builder(
            this, player, object : MediaLibrarySession.Callback {}).let {
            it.setId("${packageName}.music.service.MusicService")
            it.setSessionActivity(sessionActivityPendingIntent)
            it.build()
        }
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaLibrarySession? =
        mediaLibrarySession

    override fun onDestroy() {
        mediaLibrarySession?.run {
            player.release()
            release()
            mediaLibrarySession = null
        }
        super.onDestroy()
    }

}
