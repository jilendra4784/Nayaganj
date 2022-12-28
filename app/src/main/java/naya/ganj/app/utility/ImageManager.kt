package naya.ganj.app.utility

import android.content.Context
import android.widget.ImageView
import com.bumptech.glide.Glide
import naya.ganj.app.R

class ImageManager {
    companion object {
        fun onLoadingImage( context: Context,  imgURL:String,  imageView: ImageView){
            Glide.with(context).load(imgURL).error(R.drawable.default_image).into(imageView)
        }
    }
}