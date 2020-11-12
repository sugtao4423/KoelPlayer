package sugtao4423.koelplayer.view

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import androidx.recyclerview.R
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class GridRecyclerView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = R.attr.recyclerViewStyle
) : RecyclerView(context, attrs, defStyleAttr) {

    private val spanCount = 3
    private val spacing = 4

    init {
        setHasFixedSize(true)
        val gridLayoutManager = GridLayoutManager(context, spanCount)
        layoutManager = gridLayoutManager
        addItemDecoration(GridSpacingItemDecoration(spanCount, spacing))
    }

}

class GridSpacingItemDecoration(private val spanCount: Int, private val spacing: Int) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        val position = parent.getChildAdapterPosition(view)
        val column = position % spanCount

        when {
            column == 0 -> outRect.left = spacing
            (column + 1) == spanCount -> {
                outRect.left = spacing / 2
                outRect.right = spacing
            }
            else -> outRect.left = spacing / 2
        }

        if (position < spanCount) {
            outRect.top = spacing
        }
        outRect.bottom = spacing
    }

}
