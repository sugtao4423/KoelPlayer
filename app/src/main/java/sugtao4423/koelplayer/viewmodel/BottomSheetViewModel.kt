package sugtao4423.koelplayer.viewmodel

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.material.bottomsheet.BottomSheetBehavior
import sugtao4423.koelplayer.viewmodel.base.MusicServiceViewModel

class BottomSheetViewModel(application: Application) : MusicServiceViewModel(application) {

    private val _bottomSheetState = MutableLiveData(BottomSheetBehavior.STATE_COLLAPSED)
    val bottomSheetState: LiveData<Int> = _bottomSheetState

    private val _currentBottomSheetTag = MutableLiveData<String>()
    val currentBottomSheetTag: LiveData<String> = _currentBottomSheetTag

    companion object {
        const val BOTTOM_SHEET_NOW_PLAYING = "NOW_PLAYING"
        const val BOTTOM_SHEET_QUEUE = "QUEUE"
    }

    fun setBottomSheetState(state: Int) {
        _bottomSheetState.value = state
    }

    fun toggleBottomSheetState() {
        _bottomSheetState.value = when (_bottomSheetState.value) {
            BottomSheetBehavior.STATE_COLLAPSED -> BottomSheetBehavior.STATE_EXPANDED
            BottomSheetBehavior.STATE_EXPANDED -> BottomSheetBehavior.STATE_COLLAPSED
            else -> BottomSheetBehavior.STATE_COLLAPSED
        }
    }

    fun setCurrentBottomSheetTag(tag: String) {
        _currentBottomSheetTag.value = tag
    }

    /**
     * @return true if the back press was handled, false otherwise.
     */
    fun handleBackPressed(): Boolean {
        return if (_bottomSheetState.value == BottomSheetBehavior.STATE_EXPANDED) {
            _bottomSheetState.value = BottomSheetBehavior.STATE_COLLAPSED
            true
        } else {
            false
        }
    }

}
