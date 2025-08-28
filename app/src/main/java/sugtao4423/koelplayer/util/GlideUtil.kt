package sugtao4423.koelplayer.util

import android.content.Context
import android.net.Uri
import android.view.View
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import sugtao4423.koelplayer.R

object GlideUtil {

    fun load(
        context: Context, url: String?, targetView: ImageView, enableRoundedCorners: Boolean = false
    ) {
        Glide.with(context.applicationContext).load(url ?: R.drawable.unknown_album)
            .placeholder(R.drawable.unknown_album).let {
                if (enableRoundedCorners) it.transform(RoundedCorners(8)) else it
            }.into(targetView)
    }

    fun load(context: Context, uri: Uri?, targetView: ImageView) {
        load(context, uri.toString(), targetView)
    }

    fun load(view: View, url: String?, targetView: ImageView) {
        load(view.context, url, targetView)
    }

    fun load(fragment: Fragment, uri: Uri?, targetView: ImageView) {
        load(fragment.requireContext(), uri, targetView)
    }

}
