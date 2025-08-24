package sugtao4423.koelplayer.ui.activity

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import sugtao4423.koelplayer.App
import sugtao4423.koelplayer.R
import sugtao4423.koelplayer.data.SyncMusicData
import sugtao4423.koelplayer.data.database.MusicDB
import sugtao4423.koelplayer.download.MusicDownloader
import sugtao4423.koelplayer.util.bytesToHumanReadable

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

        private val notificationPermissionRequest =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                if (isGranted) return@registerForActivityResult
                AlertDialog.Builder(requireContext()).apply {
                    setMessage(R.string.preferences_deny_notification_permission)
                    setPositiveButton(R.string.ok, null)
                }.show()
            }

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.preferences, rootKey)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                notificationPermissionRequest.launch(Manifest.permission.POST_NOTIFICATIONS)
            }

            findPreference<Preference>("reAuth")?.setOnPreferenceClickListener {
                reAuth()
                true
            }

            findPreference<Preference>("syncMusicData")?.setOnPreferenceClickListener {
                syncMusicData()
                true
            }

            findPreference<Preference>("allDownload")?.setOnPreferenceClickListener {
                downloadAllMusic()
                true
            }

            findPreference<Preference>("removeAllDownloaded")?.setOnPreferenceClickListener {
                removeAllDownloadedMusic()
                true
            }

            findPreference<Preference>("showDownloadedInfo")?.let {
                it.summary = getDownloadedInfoMessage()
                it.setOnPreferenceClickListener { _ ->
                    it.summary = getDownloadedInfoMessage()
                    true
                }
            }

        }

        private fun reAuth() {
            val intent = Intent(requireContext(), ServerSettingsActivity::class.java).apply {
                putExtra(ServerSettingsActivity.INTENT_KEY_IS_RE_AUTH, true)
            }
            startActivity(intent)
        }

        private fun syncMusicData() {
            AlertDialog.Builder(requireContext()).apply {
                setTitle(R.string.preferences_sync_music_data)
                setMessage(R.string.preferences_sync_music_data_description)
                setNegativeButton(R.string.cancel, null)
            }.setPositiveButton(R.string.ok) { _, _ ->
                SyncMusicData(requireContext()).sync()
                (requireContext().applicationContext as App).reloadedAllMusicData = true
            }.show()
        }

        private fun downloadAllMusic() {
            AlertDialog.Builder(requireContext()).apply {
                setTitle(R.string.preferences_all_download)
                setMessage(R.string.preferences_all_download_description)
                setNegativeButton(R.string.cancel, null)
            }.setPositiveButton(R.string.ok) { _, _ ->
                val allSongs = MusicDB(requireContext()).let {
                    val songs = it.getAllMusicData().songs
                    it.close()
                    songs
                }
                MusicDownloader(requireContext()).downloadSongs(allSongs)
            }.show()
        }

        private fun removeAllDownloadedMusic() {
            AlertDialog.Builder(requireContext()).apply {
                setTitle(R.string.preferences_remove_all_downloaded)
                setMessage(R.string.preferences_remove_all_downloaded_description)
                setNegativeButton(R.string.cancel, null)
            }.setPositiveButton(R.string.ok) { _, _ ->
                MusicDownloader(requireContext()).deleteAllDownloadedSongFiles()
            }.show()
        }

        private fun getDownloadedInfoMessage(): String {
            val allSongs = MusicDB(requireContext()).let {
                val songs = it.getAllMusicData().songs
                it.close()
                songs
            }

            val downloader = MusicDownloader(requireContext())
            val downloadedSongCount = allSongs.filter { downloader.isDownloaded(it.id) }.size
            val allDownloadedFileSize = downloader.getAllDownloadedFileSize().bytesToHumanReadable()

            return getString(
                R.string.preferences_downloaded_info_message,
                downloadedSongCount,
                allSongs.size,
                allDownloadedFileSize
            )
        }

    }

}
