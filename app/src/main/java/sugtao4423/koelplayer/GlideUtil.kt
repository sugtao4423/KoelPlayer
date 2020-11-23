package sugtao4423.koelplayer

import android.content.Context
import android.net.Uri
import android.view.View
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide

class GlideUtil {

    companion object {
        fun load(context: Context, url: String?, targetView: ImageView) {
            if (url == null || url.endsWith("unknown-album.png")) {
                Glide.with(context).load(R.drawable.unknown_album).placeholder(R.drawable.unknown_album).into(targetView)
            } else {
                Glide.with(context).load(url).placeholder(R.drawable.unknown_album).into(targetView)
            }
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

}