package sugtao4423.koelplayer.bsfragment

import android.support.v4.media.MediaMetadataCompat
import sugtao4423.koelplayer.playmusic.MusicService

interface BSFragmentInterface {
    fun onMusicServiceConnected(musicService: MusicService)
    fun onMusicServiceDisconnected()
    fun updateMetadata(metadata: MediaMetadataCompat)
}