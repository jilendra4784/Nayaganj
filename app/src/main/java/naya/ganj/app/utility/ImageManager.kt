package naya.ganj.app.utility

import android.content.Context
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.facebook.shimmer.Shimmer
import com.facebook.shimmer.ShimmerDrawable
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
            val shimmer = Shimmer.AlphaHighlightBuilder()// The attributes for a ShimmerDrawable is set by this builder
                .setDuration(1800) // how long the shimmering animation takes to do one full sweep
                .setBaseAlpha(0.7f) //the alpha of the underlying children
                .setHighlightAlpha(0.6f) // the shimmer alpha amount
                .setDirection(Shimmer.Direction.LEFT_TO_RIGHT)
                .setAutoStart(true)
                .build()

// This is the placeholder for the imageView
            val shimmerDrawable = ShimmerDrawable().apply {
                setShimmer(shimmer)
            }


            if (URLConstant.BaseImageUrl == "") {
                Glide.with(context)
                    .load(app.user.getUserDetails()?.configObj?.productImgUrl.plus(imageName))
                    .placeholder(shimmerDrawable)
                    .error(R.drawable.default_image).diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(imageView)


            } else {
                Glide.with(context).load(URLConstant.BaseImageUrl + imageName)
                    .placeholder(shimmerDrawable)
                    .error(R.drawable.default_image)
                    .diskCacheStrategy(DiskCacheStrategy.ALL).into(imageView)
            }
        }
    }
}