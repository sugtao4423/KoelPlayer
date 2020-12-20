package sugtao4423.koelplayer

import android.content.Context
import android.net.Uri
import android.view.View
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions

class GlideUtil {

    companion object {
        fun load(context: Context, url: String?, targetView: ImageView, enableRoundedCorners: Boolean = false) {
            var requestBuilder = if (url == null || url.endsWith("unknown-album.png")) {
                Glide.with(context).load(R.drawable.unknown_album).placeholder(R.drawable.unknown_album)
            } else {
                Glide.with(context).load(url).placeholder(R.drawable.unknown_album)
            }
            if (enableRoundedCorners) {
                requestBuilder = requestBuilder.apply(RequestOptions.bitmapTransform(RoundedCorners(8)))
            }
            requestBuilder.into(targetView)
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