package sugtao4423.koelplayer

import android.content.Intent
import android.media.AudioManager
import android.os.Bundle
import androidx.fragment.app.commit
import sugtao4423.koelplayer.fragment.AlbumFragment
import sugtao4423.koelplayer.playmusic.MusicService

class MainActivity : BaseBottomNowPlayingActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val koelToken = Prefs(this).koelToken
        if (koelToken.isEmpty()) {
            startActivity(Intent(this, ServerSettingsActivity::class.java))
            finish()
            return
        }

        volumeControlStream = AudioManager.STREAM_MUSIC

        startService(Intent(this, MusicService::class.java))

        supportFragmentManager.commit {
            add(R.id.fragmentContainer, AlbumFragment())
        }
    }

}