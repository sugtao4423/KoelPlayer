package sugtao4423.koelplayer.playmusic

import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import android.os.Binder
import android.os.Bundle
import android.os.IBinder
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import androidx.core.content.ContextCompat
import androidx.media.MediaBrowserServiceCompat
import androidx.media.session.MediaButtonReceiver
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueNavigator
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory
import com.google.android.exoplayer2.util.Util
import sugtao4423.koel4j.KoelEndpoints
import sugtao4423.koel4j.dataclass.Song
import sugtao4423.koelplayer.Prefs

class MusicService : MediaBrowserServiceCompat() {

    private lateinit var koelServer: String
    private lateinit var koelToken: String

    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var notificationManager: KoelNotificationManager
    private lateinit var exoPlayer: ExoPlayer
    private lateinit var metadataItem: MediaMetadataCompat

    private var isForegroundService = false

    override fun onCreate() {
        super.onCreate()
        initPrefs()
        initExoPlayer()
        initMediaSession()
        initNotificationManager()
    }

    private fun initPrefs() {
        koelServer = Prefs(this).koelServer
        koelToken = Prefs(this).koelToken
    }

    private fun initExoPlayer() {
        val attr = AudioAttributes.Builder().run {
            setContentType(C.CONTENT_TYPE_MUSIC)
            setUsage(C.USAGE_MEDIA)
            build()
        }

        val playerEventListener = object : Player.EventListener {
            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                when (playbackState) {
                    Player.STATE_BUFFERING,
                    Player.STATE_READY -> {
                        notificationManager.setPlayer(exoPlayer)
                        if (playbackState == Player.STATE_READY && !playWhenReady) {
                            stopForeground(false)
                        }
                    }
                    else -> {
                        notificationManager.setPlayer(null)
                    }
                }
            }
        }

        exoPlayer = SimpleExoPlayer.Builder(this).build().apply {
            setAudioAttributes(attr, true)
            addListener(playerEventListener)
        }
    }

    private fun initMediaSession() {
        val sessionActivityPendingIntent = packageManager?.getLaunchIntentForPackage(packageName)?.let {
            PendingIntent.getActivity(this, 0, it, 0)
        }
        mediaSession = MediaSessionCompat(this, "MusicService").apply {
            setSessionActivity(sessionActivityPendingIntent)
            isActive = true
        }
        sessionToken = mediaSession.sessionToken

        val timelineQueueNavigator = object : TimelineQueueNavigator(mediaSession) {
            override fun getMediaDescription(player: Player, windowIndex: Int): MediaDescriptionCompat {
                return metadataItem.description
            }
        }

        val mediaSessionConnector = MediaSessionConnector(mediaSession)
        mediaSessionConnector.setPlayer(exoPlayer)
        mediaSessionConnector.setQueueNavigator(timelineQueueNavigator)
    }

    private fun initNotificationManager() {
        val playerNotificationListener = object : PlayerNotificationManager.NotificationListener {
            override fun onNotificationPosted(notificationId: Int, notification: Notification, ongoing: Boolean) {
                if (ongoing && !isForegroundService) {
                    ContextCompat.startForegroundService(applicationContext, Intent(applicationContext, this@MusicService.javaClass))
                    startForeground(notificationId, notification)
                    isForegroundService = true
                }
            }

            override fun onNotificationCancelled(notificationId: Int, dismissedByUser: Boolean) {
                isForegroundService = false
                stopSelf()
            }
        }

        notificationManager = KoelNotificationManager(this, mediaSession.sessionToken, playerNotificationListener)
        notificationManager.setPlayer(exoPlayer)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        MediaButtonReceiver.handleIntent(mediaSession, intent)
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaSession.isActive = false
        mediaSession.release()
        exoPlayer.release()
        stopForeground(true)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return MusicServiceBinder()
    }

    inner class MusicServiceBinder : Binder() {
        val musicService = this@MusicService
    }

    override fun onGetRoot(clientPackageName: String, clientUid: Int, rootHints: Bundle?): BrowserRoot? {
        return BrowserRoot("media_root_id", null)
    }

    override fun onLoadChildren(parentId: String, result: Result<MutableList<MediaBrowserCompat.MediaItem>>) {
        result.sendResult(null)
    }

    private fun Song.toMetadata(): MediaMetadataCompat {
        return MediaMetadataCompat.Builder().let {
            it.putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE, title)
            it.putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_SUBTITLE, artist.name)
            it.putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON_URI, album.cover)
            it.build()
        }
    }

    private fun Song.toMediaSource(): MediaSource {
        val songUrl = koelServer + KoelEndpoints.musicFile(koelToken, id)
        val dataSourceFactory = DefaultHttpDataSourceFactory(Util.getUserAgent(applicationContext, packageName))
        return ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(MediaItem.fromUri(songUrl))
    }

    fun playSong(song: Song) {
        metadataItem = song.toMetadata()
        exoPlayer.setMediaSource(song.toMediaSource())
        exoPlayer.prepare()
        exoPlayer.play()
    }

}