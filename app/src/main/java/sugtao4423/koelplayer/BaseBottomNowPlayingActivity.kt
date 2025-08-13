package sugtao4423.koelplayer

import android.annotation.SuppressLint
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.commit
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.coroutines.launch
import sugtao4423.koelplayer.bsfragment.BSNowPlayingFragment
import sugtao4423.koelplayer.bsfragment.BSQueueFragment
import sugtao4423.koelplayer.databinding.BottomSheetBinding
import sugtao4423.koelplayer.viewmodel.BottomSheetViewModel

abstract class BaseBottomNowPlayingActivity : AppCompatActivity() {

    protected abstract val bsBinding: BottomSheetBinding

    protected val bottomSheetViewModel: BottomSheetViewModel by viewModels()

    protected fun initViews(backgroundAppbar: View) {
        initActionBar()
        val bottomSheet = initBottomSheet(backgroundAppbar)
        initBottomNav()
        initObservers(bottomSheet)
    }

    private fun initActionBar() {
        bsBinding.nowPlayingToolbar.setNavigationOnClickListener {
            bottomSheetViewModel.toggleBottomSheetState()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        bottomSheetViewModel.toggleBottomSheetState()
        return super.onSupportNavigateUp()
    }

    private fun initBottomSheet(backgroundAppbar: View): BottomSheetBehavior<CoordinatorLayout> {
        val bottomSheet = BottomSheetBehavior.from(bsBinding.root)

        if (bottomSheet.state == BottomSheetBehavior.STATE_EXPANDED) {
            bsBinding.nowPlayingSheetCollapsed.alpha = 0f
            bsBinding.nowPlayingSheetExpanded.alpha = 1f
        } else if (bottomSheet.state == BottomSheetBehavior.STATE_COLLAPSED) {
            bsBinding.nowPlayingSheetCollapsed.alpha = 1f
            bsBinding.nowPlayingSheetExpanded.alpha = 0f
        }

        bottomSheet.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                val inverseOffset = 1 - slideOffset
                bsBinding.nowPlayingSheetCollapsed.alpha = inverseOffset
                bsBinding.nowPlayingSheetExpanded.alpha = slideOffset
                backgroundAppbar.alpha = inverseOffset
            }

            override fun onStateChanged(bottomSheet: View, newState: Int) {
                bottomSheetViewModel.setBottomSheetState(newState)
                backgroundAppbar.visibility =
                    if (newState == BottomSheetBehavior.STATE_EXPANDED) View.GONE else View.VISIBLE
            }
        })

        @SuppressLint("ClickableViewAccessibility") bsBinding.nowPlayingSheetExpanded.setOnTouchListener { _, _ -> true }
        bsBinding.nowPlayingToolbar.setOnClickListener {
            bottomSheetViewModel.toggleBottomSheetState()
        }

        return bottomSheet
    }

    private fun initBottomNav() {
        supportFragmentManager.commit {
            add(
                R.id.bottomSheetContainer,
                BSNowPlayingFragment(),
                BottomSheetViewModel.BOTTOM_SHEET_NOW_PLAYING
            )
            add(
                R.id.bottomSheetContainer,
                BSQueueFragment(),
                BottomSheetViewModel.BOTTOM_SHEET_QUEUE
            )
        }

        bsBinding.bottomSheetBottomNav.setOnItemSelectedListener {
            val selectedFragmentTag = when (it.itemId) {
                R.id.bottomSheetNowPlayingButton -> BottomSheetViewModel.BOTTOM_SHEET_NOW_PLAYING
                R.id.bottomSheetQueueButton -> BottomSheetViewModel.BOTTOM_SHEET_QUEUE
                else -> throw IllegalArgumentException("Unknown item selected: ${it.itemId}")
            }
            bottomSheetViewModel.setCurrentBottomSheetTag(selectedFragmentTag)
            true
        }
    }

    private fun initObservers(bottomSheet: BottomSheetBehavior<CoordinatorLayout>) {
        lifecycleScope.launch {
            bottomSheetViewModel.currentMetadata.collect { metadata ->
                metadata?.let {
                    GlideUtil.load(
                        applicationContext, it.description.iconUri, bsBinding.bottomNowPlayingCover
                    )
                    bsBinding.bottomNowPlayingTitle.text = it.description.title
                    bsBinding.bottomNowPlayingArtist.text = it.description.subtitle
                }
            }
        }

        bottomSheetViewModel.bottomSheetState.observe(this, { state ->
            if (bottomSheet.state != state) {
                bottomSheet.state = state
            }
        })

        bottomSheetViewModel.currentBottomSheetTag.observe(this, { tag ->
            supportFragmentManager.fragments.filter {
                listOf(
                    BottomSheetViewModel.BOTTOM_SHEET_NOW_PLAYING,
                    BottomSheetViewModel.BOTTOM_SHEET_QUEUE
                ).contains(it.tag)
            }.forEach {
                supportFragmentManager.commit {
                    if (it.tag == tag) show(it) else hide(it)
                }
            }
        })
    }

    override fun onBackPressed() {
        if (!bottomSheetViewModel.handleBackPressed()) {
            super.onBackPressed()
        }
    }

}
