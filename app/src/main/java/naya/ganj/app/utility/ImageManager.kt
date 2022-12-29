package naya.ganj.app.utility

import android.content.Context
import android.widget.ImageView
import com.bumptech.glide.Glide
import naya.ganj.app.Nayaganj
import naya.ganj.app.R
import naya.ganj.app.retrofit.URLConstant

class ImageManager {
    companion object {
        fun onLoadingImage(
            app: Nayaganj,
            context: Context,
            imageName: String,
            imageView: ImageView
        ) {
            if (URLConstant.BaseImageUrl == "") {
                Glide.with(context)
                    .load(app.user.getUserDetails()?.configObj?.productImgUrl.plus(imageName))
                    .error(R.drawable.default_image).into(imageView)
            } else {
                Glide.with(context).load(URLConstant.BaseImageUrl + imageName)
                    .error(R.drawable.default_image).into(imageView)
            }
        }
    }
}