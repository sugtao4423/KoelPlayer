package sugtao4423.koelplayer

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import sugtao4423.koelplayer.download.KoelDLService
import sugtao4423.koelplayer.musicdb.MusicDB

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportFragmentManager.commit {
            replace(android.R.id.content, SettingsFragment())
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    class SettingsFragment : PreferenceFragmentCompat() {

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.preferences, rootKey)

            findPreference<Preference>("allDownload")?.setOnPreferenceClickListener {
                downloadAllMusic()
                true
            }

        }

        private fun downloadAllMusic() {
            AlertDialog.Builder(requireContext()).apply {
                setTitle(R.string.preferences_all_download)
                setMessage(R.string.preferences_all_download_description)
                setNegativeButton(R.string.cancel, null)
            }.setPositiveButton(R.string.ok) { _, _ ->
                val musicDB = MusicDB(requireContext())
                val allSongs = musicDB.getAllMusicData().songs.toTypedArray()
                musicDB.close()

                val intent = Intent(requireContext(), KoelDLService::class.java)
                intent.putExtra(KoelDLService.INTENT_KEY_SONGS, allSongs)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    requireContext().startForegroundService(intent)
                } else {
                    requireContext().startService(intent)
                }
            }.show()
        }
    }

}