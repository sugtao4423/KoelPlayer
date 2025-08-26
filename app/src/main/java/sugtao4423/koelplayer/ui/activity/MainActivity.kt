package sugtao4423.koelplayer.ui.activity

import android.content.Intent
import android.media.AudioManager
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.widget.SearchView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import sugtao4423.koelplayer.App
import sugtao4423.koelplayer.R
import sugtao4423.koelplayer.databinding.ActivityMainBinding
import sugtao4423.koelplayer.databinding.BottomSheetBinding
import sugtao4423.koelplayer.music.service.MusicService
import sugtao4423.koelplayer.ui.activity.base.BottomSheetActivity
import sugtao4423.koelplayer.ui.fragment.AlbumFragment
import sugtao4423.koelplayer.ui.fragment.PlaylistFragment
import sugtao4423.koelplayer.viewmodel.MainViewModel

class MainActivity : BottomSheetActivity() {

    private val binding: ActivityMainBinding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    override val bsBinding: BottomSheetBinding by lazy { binding.mainBottomSheet }

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        optimizeEdgeToEdge()
        initToolbarAlphaListener()
        setContentView(binding.root)
        setSupportActionBar(binding.mainToolbar)
        initViews(binding.mainAppbar)

        val koelToken = (applicationContext as App).koelToken
        if (koelToken.isEmpty()) {
            startActivity(Intent(this, ServerSettingsActivity::class.java))
            finish()
            return
        }

        volumeControlStream = AudioManager.STREAM_MUSIC
        startService(Intent(this, MusicService::class.java))

        binding.mainViewPager.adapter = MainTabAdapter()
        TabLayoutMediator(binding.mainTabLayout, binding.mainViewPager) { tab, position ->
            val textResId = when (position) {
                0 -> R.string.album
                else -> R.string.playlist
            }
            tab.setText(textResId)
        }.attach()
    }

    override fun onResume() {
        super.onResume()
        val app = applicationContext as App
        if (app.reloadedAllMusicData) {
            app.reloadedAllMusicData = false
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    override fun onDestroy() {
        bottomSheetViewModel.releaseController()
        super.onDestroy()
    }

    private fun optimizeEdgeToEdge() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.mainViewPager) { v, insets ->
            val i = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
            v.updatePadding(bottom = i.bottom)
            WindowInsetsCompat.CONSUMED
        }
    }

    private fun initToolbarAlphaListener() {
        binding.mainAppbar.addOnOffsetChangedListener { appBarLayout, verticalOffset ->
            val totalScrollRange = appBarLayout.totalScrollRange
            val alpha = (totalScrollRange + verticalOffset).toFloat() / totalScrollRange.toFloat()
            binding.mainToolbar.alpha = alpha
        }
    }

    inner class MainTabAdapter : FragmentStateAdapter(this) {

        override fun getItemCount(): Int = 2

        override fun createFragment(position: Int): Fragment = when (position) {
            0 -> AlbumFragment(bottomSheetViewModel)
            else -> PlaylistFragment(bottomSheetViewModel)
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if (menu == null) {
            return true
        }
        menuInflater.inflate(R.menu.main_menu, menu)
        val searchView = menu.findItem(R.id.menuSearch).actionView as SearchView
        setupSearchView(searchView)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.menuSettings) {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
        return true
    }

    private fun setupSearchView(searchView: SearchView) {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.filter(newText ?: "")
                return true
            }
        })
    }

}
