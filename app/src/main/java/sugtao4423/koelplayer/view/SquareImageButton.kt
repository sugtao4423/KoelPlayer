package sugtao4423.koelplayer.view

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.R
import androidx.appcompat.widget.AppCompatImageButton

class SquareImageButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = R.attr.imageButtonStyle
) : AppCompatImageButton(context, attrs, defStyleAttr) {

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec)
    }

}