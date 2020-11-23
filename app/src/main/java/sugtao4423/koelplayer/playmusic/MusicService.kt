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
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import androidx.core.content.ContextCompat
import androidx.media.MediaBrowserServiceCompat
import androidx.media.session.MediaButtonReceiver
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueNavigator
import com.google.android.exoplayer2.source.ShuffleOrder
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import sugtao4423.koel4j.KoelEndpoints
import sugtao4423.koel4j.dataclass.Song
import sugtao4423.koelplayer.MainActivity
import sugtao4423.koelplayer.Prefs

class MusicService : MediaBrowserServiceCompat() {

    private lateinit var koelServer: String
    private lateinit var koelToken: String

    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var notificationManager: KoelNotificationManager
    private lateinit var exoPlayer: ExoPlayer
    private var metadataItems: ArrayList<MediaMetadataCompat> = arrayListOf()
    private var songQueue: ArrayList<Song> = arrayListOf()

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
        val appIntent = Intent(this, MainActivity::class.java).apply {
            action = Intent.ACTION_MAIN
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val sessionActivityPendingIntent = PendingIntent.getActivity(this, 0, appIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        mediaSession = MediaSessionCompat(this, "MusicService").apply {
            setSessionActivity(sessionActivityPendingIntent)
            isActive = true
        }
        sessionToken = mediaSession.sessionToken

        val timelineQueueNavigator = object : TimelineQueueNavigator(mediaSession) {
            override fun getMediaDescription(player: Player, windowIndex: Int): MediaDescriptionCompat {
                return metadataItems[windowIndex].description
            }
        }

        MediaSessionConnector(mediaSession).apply {
            setPlayer(exoPlayer)
            setQueueNavigator(timelineQueueNavigator)
            setMediaMetadataProvider {
                if (metadataItems.size > it.currentWindowIndex) {
                    metadataItems[it.currentWindowIndex]
                } else {
                    MediaMetadataCompat.Builder().build()
                }
            }
        }
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
                stopForeground(true)
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

    private fun List<Song>.toMetadata(): List<MediaMetadataCompat> {
        return List(this.size) { index ->
            MediaMetadataCompat.Builder().let {
                it.putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE, this[index].title)
                it.putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_SUBTITLE, this[index].artist.name)
                it.putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON_URI, this[index].album.cover)
                it.putLong(MediaMetadataCompat.METADATA_KEY_DURATION, (this[index].length * 1000).toLong())

                it.putString(MediaMetadataCompat.METADATA_KEY_TITLE, this[index].title)
                it.putString(MediaMetadataCompat.METADATA_KEY_ARTIST, this[index].artist.name)
                it.putString(MediaMetadataCompat.METADATA_KEY_ALBUM, this[index].album.name)
                it.putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ARTIST, this[index].album.artist.name)
                it.build()
            }
        }
    }

    private fun List<Song>.toMediaItem(): List<MediaItem> {
        return List(this.size) { index ->
            val songUrl = koelServer + KoelEndpoints.musicFile(koelToken, this[index].id)
            MediaItem.fromUri(songUrl)
        }
    }

    fun changeSong(playPos: Int) {
        val windowIndex = if (isShuffle()) {
            val song = queueSongs()[playPos]
            songQueue.indexOf(song)
        } else {
            playPos
        }
        exoPlayer.seekTo(windowIndex, 0)
    }

    fun playSongs(songs: List<Song>, playPos: Int) {
        metadataItems.clear()
        metadataItems.addAll(songs.toMetadata())
        exoPlayer.setMediaItems(songs.toMediaItem())
        songQueue.clear()
        songQueue.addAll(songs)
        queueSongChangedListener?.invoke()
        exoPlayer.seekTo(playPos, 0)
        exoPlayer.prepare()
        exoPlayer.play()
    }

    fun playingMetadata(): MediaMetadataCompat? = mediaSession.controller.metadata

    fun isPlaying(): Boolean = exoPlayer.isPlaying
    fun playingSong(): Song = songQueue[exoPlayer.currentWindowIndex]
    fun togglePlay() = if (exoPlayer.isPlaying) exoPlayer.pause() else exoPlayer.play()
    fun prev() = if (exoPlayer.currentPosition < 3000) exoPlayer.previous() else exoPlayer.seekTo(0)
    fun next() = exoPlayer.next()

    fun bufferedPosition(): Long = exoPlayer.bufferedPosition
    fun currentPosition(): Long = exoPlayer.currentPosition
    fun duration(): Long = exoPlayer.duration
    fun seekTo(position: Long) = run { exoPlayer.seekTo(position) }

    fun isShuffle(): Boolean = exoPlayer.shuffleModeEnabled
    fun toggleShuffle() {
        exoPlayer.shuffleModeEnabled = !exoPlayer.shuffleModeEnabled
        if (!isShuffle()) {
            exoPlayer.setShuffleOrder(ShuffleOrder.DefaultShuffleOrder(songQueue.size))
        }
        queueSongChangedListener?.invoke()
    }

    fun isRepeat(): Boolean = exoPlayer.repeatMode == Player.REPEAT_MODE_ALL
    fun isRepeatOne(): Boolean = exoPlayer.repeatMode == Player.REPEAT_MODE_ONE
    fun repeat() = run { exoPlayer.repeatMode = Player.REPEAT_MODE_ALL }
    fun repeatOne() = run { exoPlayer.repeatMode = Player.REPEAT_MODE_ONE }
    fun repeatOff() = run { exoPlayer.repeatMode = Player.REPEAT_MODE_OFF }

    fun setMediaControllerCallback(callback: MediaControllerCompat.Callback) {
        mediaSession.controller.registerCallback(callback)
    }

    fun addPlayerEventListener(listener: Player.EventListener) {
        exoPlayer.addListener(listener)
    }

    fun removeMediaControllerCallback(callback: MediaControllerCompat.Callback) {
        mediaSession.controller.unregisterCallback(callback)
    }

    fun removePlayerEventListener(listener: Player.EventListener) {
        exoPlayer.removeListener(listener)
    }

    var queueSongChangedListener: (() -> Unit)? = null
    fun queueSongs(): ArrayList<Song> {
        if (!isShuffle()) {
            return songQueue
        }

        val result = arrayListOf<Song>()
        var index = exoPlayer.currentTimeline.getFirstWindowIndex(true)
        while (true) {
            result.add(songQueue[index])
            index = exoPlayer.currentTimeline.getNextWindowIndex(index, Player.REPEAT_MODE_OFF, true)
            if (index == C.INDEX_UNSET) {
                break
            }
        }
        return result
    }

    fun moveSong(from: Int, to: Int) {
        metadataItems.add(to, metadataItems.removeAt(from))
        songQueue.add(to, songQueue.removeAt(from))
        exoPlayer.moveMediaItem(from, to)
    }

    fun removeSong(position: Int) {
        metadataItems.removeAt(position)
        songQueue.removeAt(position)
        exoPlayer.removeMediaItem(position)
    }

}